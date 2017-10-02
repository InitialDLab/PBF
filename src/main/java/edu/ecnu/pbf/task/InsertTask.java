package edu.ecnu.pbf.task;

import java.util.ArrayList;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.base.impl.Beta0;
import edu.ecnu.pbf.base.impl.Beta1;
import edu.ecnu.pbf.base.impl.Beta2;
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
public class InsertTask
{
	private int pbfType; // beta0, beta1, beta2
	private int m;
	private int falseNum;
	private int queryNum;
	private int maxT;
	private double fpRate;
	private double result;

	public InsertTask(int pbfType, int m, int maxT)
	{
		this.pbfType = pbfType;
		this.m = m;
		this.queryNum = 0;
		this.falseNum = 0;
		this.maxT = maxT;
		this.result = 0;
	}

	public void start()
	{
		// System.out.println("========new========");

		// generate pbf
		PersistentBloomFilter pbf = null;
		int levelNum = PbfUtil.getLevelNum(maxT);
		// System.out.println("the number of level: " + levelNum);
		if (0 == pbfType)
		{
			pbf = new Beta0(m, CommonConstants.K_MAX);
		}
		else if (1 == pbfType)
		{
			int bitNum = 100000;
			// System.out.println("the number of levels: " + levelNum);
			pbf = new Beta1(bitNum, levelNum, 4);
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
			
		}
		else if (4 == pbfType)
		{
			
		}
		else
		{
			return;
		}

		// insert temporal elements into pbf
		ArrayList<byte[]> dataset = new ArrayList<byte[]>();
		for (int i = 0; i < maxT; i++)
		{
			dataset.add(RandomGenerator.getRandomString(20).getBytes());
		}

		int insertNum = 80000;
		long s = System.nanoTime();
		for (int i = 0; i < insertNum; i++)
		{
			int index = i % maxT;
			pbf.insert(dataset.get(index), index);
		}
		long e = System.nanoTime();
		result = (double) (e - s) / insertNum / 1000;
		// System.out.println("insert time: " + ((double) e - s) / insertNum);

	}

	public double getResult()
	{
		return this.result;
	}

	public static void main(String[] args) throws Exception
	{
		int g = 4;
		int maxT = 86400; // 14400; //86400;//57600; //28800; //14400;
		int[] maxTArray = { 7200, 14400, 28800, 57600, 86400 };

		System.out.println("===========beta-0, beta-1, beta-2===========");
		for (int k = 0; k < maxTArray.length; k++)
		{
			System.out.print(maxTArray[k]);
			//============beta-0==============//
			ArrayList<Double> resultArray = new ArrayList<Double>();
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(0, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			double result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-1==============//
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(1, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-2==============//
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(2, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}

		System.out.println();

		System.out.println("===========beta-0, beta-1-online, beta-2-online===========");

		for (int k = 0; k < maxTArray.length; k++)
		{
			System.out.print(maxTArray[k]);
			//============beta-0==============//
			ArrayList<Double> resultArray = new ArrayList<Double>();
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(0, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			double result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-1-online==============//
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(1, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			resultArray = new ArrayList<Double>();

			Thread.sleep(1000);

			//============beta-2-online==============//
			for (int j = 0; j < 18; j++)
			{

				InsertTask qtask = new InsertTask(2, 20000000, maxTArray[k]);
				qtask.start();
				resultArray.add(qtask.getResult());
			}

			result = ResultUtil.handleResult(resultArray, 3, 4);

			System.out.print("  " + ResultUtil.handleDouble(result, 4));

			System.out.println();

			Thread.sleep(1000);
		}

		System.out.println();

	}
}
