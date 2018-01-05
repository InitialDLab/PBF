package edu.ecnu.pbf.task;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.base.impl.Beta0;
import edu.ecnu.pbf.base.impl.Beta1;
import edu.ecnu.pbf.base.impl.Beta2;
import edu.ecnu.pbf.data.Dataset2;
import edu.ecnu.pbf.data.QuerySet;
import edu.ecnu.pbf.data.TemporalElement;
import edu.ecnu.pbf.util.MathUtil;
import edu.ecnu.pbf.util.PbfUtil;
import edu.ecnu.pbf.util.RandomGenerator;
import edu.ecnu.pbf.util.ResultUtil;

/**
 * This class is to evaluate the insertion performance of beta-0, beta-1,
 * beta-2, beta-1-online, beta-2-online
 * number: type
 * 0: beta-0
 * 1: beta-1
 * 2: beta-2
 * 3: beta-1-online
 * 4: beta-2-online
 * 
 * @author Jinwei
 *
 */
public class QueryTask10152
{
	private int pbfType; // beta0, beta1, beta2, beta-1-online, beta-2-online
	private int m;
	private int falseNum;
	private int queryNum;
	private int insertNum;
	private int maxT;
	private int queryLength;
	private double fpRate;
	private double result;
	private int readRatio;  // from 0 to 100
	private double rps; // requests per seconds
	
	private Dataset2 dataset;
	private QuerySet querySet;

	public QueryTask10152(int pbfType, int m, int queryLength, int readRatio, Dataset2 dataset, QuerySet querySet)
	{
		this.pbfType = pbfType;
		this.m = m;
		this.queryLength = queryLength;
		this.readRatio = readRatio;
		this.dataset = dataset;
		this.querySet = querySet;
		this.queryNum = 0;
		this.insertNum = 0;
		this.falseNum = 0;
		//this.maxT = maxT;  // temporarily useless
		this.result = 0;
		this.rps = 0;
	}

	public void start()
	{
		// System.out.println("========new========");

		// generate pbf
		PersistentBloomFilter pbf = null;
		int levelNum = dataset.getLevelNum();//PbfUtil.getLevelNum(maxT);
		// System.out.println("the number of level: " + levelNum);
		if (0 == pbfType)
		{
			pbf = new Beta0(m, CommonConstants.K_MAX);
		}
		else if (1 == pbfType)
		{
			// int bitNum = 100000;
			// System.out.println("the number of levels: " + levelNum);
			int bitNum = m / ((int)Math.pow(2, levelNum - 1) - 1) + 1;
			pbf = new Beta1(bitNum, levelNum, 4); // level - 1
		}
		else if (2 == pbfType)
		{
			int topLevel = levelNum - 1;
			int bitNum = this.m / levelNum + 1; // the number of bits for each
												// level
			// get the number of distinct elements for each level
			pbf = new Beta2(bitNum, CommonConstants.K_MAX, topLevel);
		}
		else if (3 == pbfType)
		{
			int[] mm = PbfUtil.getOptimizedMForBeta1(this.m, dataset.getD(),
					querySet.getQueryFrequency(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, dataset.getD(), CommonConstants.K_MAX);
			pbf = new Beta1(mm, k, levelNum, 4);
			
			int sumBits = 0;
			
//			for (int i = 0; i < mm.length; i++)
//			{
//				sumBits += mm[i];
//			}
//			System.out.println("beta-2-opt, bits: " + sumBits);
		}
		else if (4 == pbfType)
		{
			// get the number of distinct elements for each level
			int topLevel = levelNum - 1;
			ArrayList<HashSet<String>> levelSet = dataset.getLevelSet();
			int[] d = new int[levelNum];
			for (int i = 0; i < levelNum; i++)
			{
				d[i] = levelSet.get(i).size();
			}

			int[] mm = PbfUtil.getOptimizedMForBeta2(this.m, d,
					querySet.getQueryFrequencyForBeta2(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, d, CommonConstants.K_MAX);

			pbf = new Beta2(mm, k, topLevel);
			
//			int sumBits = 0;
//			for (int i = 0; i < mm.length; i++)
//			{
//				sumBits += mm[i];
//			}
//			System.out.println("beta-2-opt, bits: " + sumBits);
		}
		else
		{
			return;
		}
		
		int totalNum = 300000; // total number of operations
		
		ArrayList<TemporalElement> ds = dataset.getDataset();
		for (int j = 0; j < ds.size(); j++)
		{
			TemporalElement e = ds.get(insertNum % ds.size());
			pbf.insert(e.getElement(), e.getTimepoint());
		}
		
		ArrayList<Long> starttimeSet = querySet.getStarttimeSet();
		ArrayList<byte[]> elementArray = new ArrayList<byte[]>();
		String str = "guojinweishihaoren!!";
		byte[] elementBytes = str.getBytes();
		long start = System.nanoTime();
		for (int i = 0; i < totalNum; i++)
		{
			long starttime = starttimeSet.get(i % starttimeSet.size());
			// the start point in query set is 1, so we has to modify it
			if (pbf.query(elementBytes, starttime, starttime + queryLength - 1))
			{
				this.falseNum++;
				System.out.println("start: " + starttime + ", end: " + (starttime + queryLength - 1));
			}
			this.queryNum++;
		}
		long end = System.nanoTime();
		
		double queryTime = (double) (end - start) / 1000 / totalNum;
		
		result = queryTime;
		
		if (falseNum > 10)
		{
			System.out.println("false positive num! " + falseNum);
		}

	}

	public double getResult()
	{
		return this.result;
	}

	public static void main(String[] args) throws Exception
	{
		int g = 4;
		int maxT = 86400; // 14400; //86400;//57600; //28800; //14400;
		int m = 50000000;
		int[] maxTArray = { 7200, 14400, 28800, 57600, 86400 };
		int queryLength = 128;
		int[] queryL = {16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
		int[] readRatio = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
		//int[] readRatio = {0, 10, 80, 90, 100};
		//int[] readRatio = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
		Thread.sleep(10000);
		
		String dataFileName = "d:/dataset/nw_dat";
		Dataset2 dataset = new Dataset2(g);
		dataset.loadFromFile(dataFileName);

		System.out.println("===========beta-0, beta-1, beta-2===========");
		
		for (int k = 0; k < queryL.length; k++)
		{
			String queryFileName = "d:/dataset/1013/wc-qry-100000-new";
			QuerySet querySet = new QuerySet(dataset.getLevelNum(), g, queryL[k]);
			querySet.loadQueryFromFile(queryFileName);
			
			System.out.print(queryL[k]);
			ArrayList<Double> resultArray = null;
			double result = 0;
			//============beta-0==============//
//			resultArray = new ArrayList<Double>();
//			for (int j = 0; j < 9; j++)
//			{
//
//				QueryTask10152 qtask = new QueryTask10152(0, m, queryL[k], 100, dataset, querySet);
//				qtask.start();
//				resultArray.add(qtask.getResult());
//			}
//
//			result = ResultUtil.handleResult(resultArray, 2, 3);
//
//			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-1==============//
			for (int j = 0; j < 9; j++)
			{

				QueryTask10152 qtask = new QueryTask10152(1, m, queryL[k], 100, dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 2, 3);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-2==============//
			for (int j = 0; j < 9; j++)
			{

				QueryTask10152 qtask = new QueryTask10152(2, m, queryL[k], 100, dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 2, 3);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}
		
		
		// pbf-online
		
		System.out.println("===========beta-1-online, beta-2-online============");
		for (int k = 0; k < queryL.length; k++)
		{
			String queryFileName = "d:/dataset/1013/wc-qry-100000-new";
			QuerySet querySet = new QuerySet(dataset.getLevelNum(), g, queryL[k]);
			querySet.loadQueryFromFile(queryFileName);
			
			System.out.print(queryL[k]);
			ArrayList<Double> resultArray = null;
			double result = 0;
			//============beta-1==============//
			resultArray = new ArrayList<Double>();
			for (int j = 0; j < 7; j++)
			{

				QueryTask10152 qtask = new QueryTask10152(3, m, queryL[k], 100, dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}
			result = ResultUtil.handleResult(resultArray, 1, 2);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

//
//			Thread.sleep(1000);

			//============beta-2==============//
			resultArray = new ArrayList<Double>();
			for (int j = 0; j < 7; j++)
			{

				QueryTask10152 qtask = new QueryTask10152(4, m, queryL[k], 100, dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 1, 2);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}
		
		

		System.out.println();

	}
}
