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
public class MixTask
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

	public MixTask(int pbfType, int m, int queryLength, int readRatio, Dataset2 dataset, QuerySet querySet)
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

			int[] mm = PbfUtil.getOptimizedMForBeta1(this.m, d,
					querySet.getQueryFrequencyForBeta2(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, d, CommonConstants.K_MAX);

			pbf = new Beta2(mm, k, topLevel);
		}
		else
		{
			return;
		}
		
		int totalNum = 1000000; // total number of operations
		
		if (readRatio < 0)
		{
			ArrayList<TemporalElement> ds = dataset.getDataset();
			long start = System.nanoTime();
			for (int i = 0; i < totalNum; i++)
			{
				TemporalElement e = ds.get(i % ds.size());
				pbf.insert(e.getElement(), e.getTimepoint());
				this.insertNum++;
			}
			long end = System.nanoTime();
			rps = (double)(insertNum + queryNum) * 1000 * 1000 * 1000 / (end - start);
		}
		else if (readRatio > 100)
		{
			ArrayList<Long> starttimeSet = querySet.getStarttimeSet();
			String str = "guojinweishihaoren!!";
			byte[] elementBytes = str.getBytes();
			long start = System.nanoTime();
			for (int i = 0; i < totalNum; i++)
			{
				long starttime = starttimeSet.get(i % starttimeSet.size());
				// the start point in query set is 1, so we has to modify it
				if (pbf.query(elementBytes, starttime - 1, starttime + queryLength - 2))
				{
					this.falseNum++;
				}
				this.queryNum++;
			}
			long end = System.nanoTime();
			rps = (double)(insertNum + queryNum) * 1000 * 1000 * 1000 / (end - start);
		}
		else
		{
			int i = 0;
			int read = readRatio;
			int write = 100 - readRatio;
//			if (0 == read)
//			{
//				read = 1;
//			}
//			if (0 == write)
//			{
//				write = 1;
//			}
			int divisor = MathUtil.gcd(read, write);
			read = read / divisor;
			write = write / divisor;
			
			ArrayList<TemporalElement> ds = dataset.getDataset();
			ArrayList<Long> starttimeSet = querySet.getStarttimeSet();
			
			// the next loop for warm-up
			for (int j = 0; j < ds.size(); j++)
			{
				TemporalElement e = ds.get(insertNum % ds.size());
				pbf.insert(e.getElement(), e.getTimepoint());
			}
			
			String str = "guojinweishihaoren!!";
			byte[] elementBytes = str.getBytes();
			
			long start = System.nanoTime();
			while(i < totalNum)
			{
				for (int j = 0; j < write; j++)
				{
					TemporalElement e = ds.get(insertNum % ds.size());
					pbf.insert(e.getElement(), e.getTimepoint());
					insertNum++;
					i++;	
				}
				
				for (int j = 0; j < read; j++)
				{
					long starttime = starttimeSet.get(queryNum % starttimeSet.size());
					// the start point in query set is 1, so we has to modify it
					if (pbf.query(elementBytes, starttime - 1, starttime + queryLength - 2))
					{
						this.falseNum++;
					}
					this.queryNum++;
					i++;
				}
			}
			long end = System.nanoTime();
			
			rps = (double)(insertNum + queryNum) * 1000 * 1000 * 1000 / (end - start);
		}

		result = rps;

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
		int[] readRatio = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
		//int[] readRatio = {0, 10, 80, 90, 100};
		//int[] readRatio = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
		Thread.sleep(10000);
		
		String dataFileName = "d:/dataset/nw_dat";
		Dataset2 dataset = new Dataset2(g);
		dataset.loadFromFile(dataFileName);

		String queryFileName = "d:/dataset/wc_qry";
		QuerySet querySet = new QuerySet(dataset.getLevelNum(), g, queryLength);
		querySet.loadQueryFromFile(queryFileName);

		System.out.println("===========beta-0, beta-1, beta-2===========");
		/*
		for (int k = 0; k < readRatio.length; k++)
		{
			System.out.print(readRatio[k]);
			//============beta-0==============//
			ArrayList<Double> resultArray = new ArrayList<Double>();
			for (int j = 0; j < 9; j++)
			{

				MixTask qtask = new MixTask(0, m, queryLength, readRatio[k], dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			double result = ResultUtil.handleResult(resultArray, 2, 3);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-1==============//
			for (int j = 0; j < 9; j++)
			{

				MixTask qtask = new MixTask(1, m, queryLength, readRatio[k], dataset, querySet);
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

				MixTask qtask = new MixTask(2, m, queryLength, readRatio[k], dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 2, 3);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}
		*/
		
		// pbf-online
		
		System.out.println("===========beta-1-online, beta-2-online============");
		for (int k = 8; k < readRatio.length; k++)
		{
			System.out.print(readRatio[k]);

			//============beta-1==============//
			ArrayList<Double> resultArray = new ArrayList<Double>();
			for (int j = 0; j < 9; j++)
			{

				MixTask qtask = new MixTask(3, m, queryLength, readRatio[k], dataset, querySet);
				qtask.start();
				resultArray.add(qtask.getResult());
			}
			double result = ResultUtil.handleResult(resultArray, 2, 3);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();
//
//			Thread.sleep(1000);

			//============beta-2==============//
//			for (int j = 0; j < 9; j++)
//			{
//
//				MixTask qtask = new MixTask(4, m, queryLength, readRatio[k], dataset, querySet);
//				qtask.start();
//				resultArray.add(qtask.getResult());
//			}
//
//			double result = ResultUtil.handleResult(resultArray, 2, 3);
//
//			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}
		
		

		System.out.println();

	}
}
