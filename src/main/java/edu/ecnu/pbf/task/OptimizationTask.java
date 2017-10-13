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
public class OptimizationTask
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
	private double rps; // requests per seconds
	
	private Dataset2 dataset;
	private QuerySet querySet;

	public OptimizationTask(int pbfType, int m, int queryLength, Dataset2 dataset, QuerySet querySet)
	{
		this.pbfType = pbfType;
		this.m = m;
		this.queryLength = queryLength;
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
		double time = 0;
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
			// beta-1-online
			long start = System.nanoTime();
			int[] mm = PbfUtil.getOptimizedMForBeta1(this.m, dataset.getD(),
					querySet.getQueryFrequency(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, dataset.getD(), CommonConstants.K_MAX);
			long end = System.nanoTime();
			pbf = new Beta1(mm, k, levelNum, 4);
			time = (double)(end - start)/1000;
//			System.out.println(time);
			
//			int sum = 0;
//			for (int i = 0; i < mm.length; i++)
//			{
//				sum += mm[i];
//			}
//			System.out.println("bits: " + sum);
		}
		else if (4 == pbfType)
		{
			// beta-2-online
			// get the number of distinct elements for each level
			int topLevel = levelNum - 1;
			ArrayList<HashSet<String>> levelSet = dataset.getLevelSet();
			int[] d = new int[levelNum];
			for (int i = 0; i < levelNum; i++)
			{
				d[i] = levelSet.get(i).size();
			}

			long start = System.nanoTime();
			int[] mm = PbfUtil.getOptimizedMForBeta2(this.m, d,
					querySet.getQueryFrequencyForBeta2(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, d, CommonConstants.K_MAX);

			long end = System.nanoTime();
			pbf = new Beta2(mm, k, topLevel);
			time = (double)(end - start)/1000;
//			System.out.println(time);
			
//			int sum = 0;
//			for (int i = 0; i < mm.length; i++)
//			{
//				sum += mm[i];
//			}
//			System.out.println("bits: " + sum);
		}
		else
		{
			return;
		}

		result = time;

	}

	public double getResult()
	{
		return this.result;
	}

	public static void main(String[] args) throws Exception
	{
		int g = 8;
		int maxT = 86400; // 14400; //86400;//57600; //28800; //14400;
		int m = 50000000;
		int[] maxTArray = { 7200, 14400, 28800, 57600, 86400 };
		int queryLength = 128;
		int[] qLength = {16, 32, 64, 128, 256, 512, 1024};
		int[] readRatio = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
		//int[] readRatio = {0, 10, 80, 90, 100};
		//int[] readRatio = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
		Thread.sleep(10000);
		
		String dataFileName = "d:/dataset/1011/edgar";
		Dataset2 dataset = new Dataset2(g);
		dataset.loadFromFile(dataFileName);

		String queryFileName = "d:/dataset/1011/edgar-query-86400";
		QuerySet querySet = new QuerySet(dataset.getLevelNum(), g, queryLength);
		querySet.loadQueryFromFile(queryFileName);

		System.out.println("===========beta-1-online, beta-2-online===========");
		
//		OptimizationTask otask = null;
//		otask = new OptimizationTask(3, m, queryLength, dataset, querySet);
//		otask.start();
//		otask = new OptimizationTask(4, m, queryLength, dataset, querySet);
//		otask.start();
		
		
		
		for (int k = 0; k < 10; k++)
		{
			System.out.print(queryLength);
			
			ArrayList<Double> resultArray = null;
			double result = 0;

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-1-online==============//

			for (int j = 0; j < 20; j++)
			{
				OptimizationTask otask = new OptimizationTask(3, m, queryLength, dataset, querySet);
				otask.start();
				resultArray.add(otask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 4, 7);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-2-online==============//
//			for (int j = 0; j < 20; j++)
//			{
//
//				OptimizationTask otask = new OptimizationTask(4, m, queryLength, dataset, querySet);
//				otask.start();
//				resultArray.add(otask.getResult());
//			}
//
//			result = ResultUtil.handleResult(resultArray, 4, 7);
//
//			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}
		
		
		
		System.out.println();

	}
}
