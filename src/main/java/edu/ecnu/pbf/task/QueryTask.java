package edu.ecnu.pbf.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.base.impl.Beta0;
import edu.ecnu.pbf.base.impl.Beta1;
import edu.ecnu.pbf.base.impl.Beta2;
import edu.ecnu.pbf.data.Dataset;
import edu.ecnu.pbf.data.QuerySet;
import edu.ecnu.pbf.data.TimepointSet;
import edu.ecnu.pbf.util.OptimizationUtil;
import edu.ecnu.pbf.util.PbfUtil;

public class QueryTask
{
	private int pbfType; // beta0, beta1, beta2
	private int m;
	private int queryLength;
	private Dataset dataset;
	private QuerySet querySet;
	private int falseNum;
	private int queryNum;
	private double fpRate;

	public QueryTask(int pbfType, int m, int queryLength, Dataset dataset, QuerySet querySet)
	{
		this.pbfType = pbfType;
		this.m = m;
		this.queryLength = queryLength;
		this.dataset = dataset;
		this.querySet = querySet;
		this.queryNum = 0;
		this.falseNum = 0;
	}

	public void start()
	{
		System.out.println("========new========");

		// get metadata
		long N = dataset.size();
		long n = dataset.getOriginalData().size();
		long n_quote = dataset.getDataset().size();

		System.out.println("N=" + N + ";n=" + n + ";n'=" + n_quote);

		// generate pbf
		PersistentBloomFilter pbf = null;
		if (0 == pbfType)
		{
			int k = OptimizationUtil.getOptimizedK(this.m, n, CommonConstants.K_MAX);
			pbf = new Beta0(m, k);
			System.out.println("k=" + k);
		}
		else if (1 == pbfType)
		{
			// int levelNum = Beta1.getLevelNum(dataset.getMaxTimestamp());
			// int bitNum = 200000;
			// System.out.println("the number of levels: " + levelNum);
			// pbf = new Beta1(bitNum, levelNum, 4);
			int levelNum = dataset.getLevelNum();
			int[] mm = PbfUtil.getOptimizedMForBeta1(this.m, dataset.getD(),
					querySet.getQueryFrequency(), queryLength);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, dataset.getD(), CommonConstants.K_MAX);
			pbf = new Beta1(mm, k, levelNum, 4);
		}
		else if (2 == pbfType)
		{
			int topLevel = Beta2.getTopLevel(dataset.getMaxTimestamp());
			int levelNum = topLevel + 1;
			int bitNum = this.m / levelNum + 1; // the number of bits for each
												// level
			// get the number of distinct elements for each level
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

		// insert temporal elements into pbf
		HashMap<String, TimepointSet> ds = dataset.getDataset();
		int numOfInsertion = 0;
		long s = System.nanoTime();
		for (Map.Entry<String, TimepointSet> entry : ds.entrySet())
		{
			TimepointSet timepointSet = entry.getValue();
			byte[] elementBytes = timepointSet.getElement();
			ArrayList<Long> timeSet = timepointSet.getTimeSet();

			for (int i = 0; i < timeSet.size(); i++)
			{
				pbf.insert(elementBytes, timeSet.get(i));
				numOfInsertion++;
			}	
		}
		long e = System.nanoTime();
		System.out.println("number of insertion: " + numOfInsertion);
		System.out.println("total of insertion time: " + (e - s) / 1000 + "us");
		System.out.println("insertion time of per item: " + (e - s) / numOfInsertion + "ns");

		// query
		ArrayList<Long> starttimeSet = querySet.getStarttimeSet();
		// String str = RandomGenerator.getRandomString(20);
		String str = "guojinweishihaoren!!";
		byte[] elementBytes = str.getBytes();
		long start = System.nanoTime();
		for (int i = 0; i < starttimeSet.size(); i++)
		{
			this.queryNum++;
			long starttime = starttimeSet.get(i);
			// the start point in query set is 1, so we has to modify it
			if (pbf.query(elementBytes, starttime - 1, starttime + queryLength - 2))
			{
				this.falseNum++;
			}
		}
		long end = System.nanoTime();

		System.out.println("execution time: " + (end - start) / 1000);
		System.out.println("the nubmer of queries: " + this.queryNum);
		this.fpRate = (double) falseNum / this.queryNum;
		System.out.println("fp number: " + this.falseNum);
		System.out.println("fp rate: " + this.fpRate);
	}

	public static void main(String[] args) throws Exception
	{
		int g = 4;
		int queryLength = 1024;
		int pbfType = 1;

		String dataFileName = "d:/dataset/nw_dat";
		Dataset dataset = new Dataset(g);
		dataset.loadFromFile(dataFileName);

		String queryFileName = "d:/dataset/wc_qry";
		QuerySet querySet = new QuerySet(dataset.getLevelNum(), g, queryLength);
		querySet.loadQueryFromFile(queryFileName);

//		QueryTask qtask = new QueryTask(pbfType, 20000000, queryLength, dataset, querySet);
//		qtask.start();

		for (int j = 0; j < 10; j++)
		{
			for (int i = 0; i < 1; i++)
			{
				QueryTask qtask = new QueryTask(pbfType, 20000000, queryLength, dataset, querySet);
				qtask.start();
			}
		}

		Thread.sleep(10000);
	}
}
