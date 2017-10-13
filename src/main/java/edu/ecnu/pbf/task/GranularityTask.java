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
import edu.ecnu.pbf.data.DatasetG;
import edu.ecnu.pbf.data.QuerySet;
import edu.ecnu.pbf.data.QuerySetG;
import edu.ecnu.pbf.data.TimepointSet;
import edu.ecnu.pbf.util.OptimizationUtil;
import edu.ecnu.pbf.util.PbfUtil;
import edu.ecnu.pbf.util.RandomGenerator;

public class GranularityTask
{
	private int pbfType; // beta0, beta1-opt, beta2-opt
	private int m;
	private int queryLength;
	private DatasetG dataset;
	private QuerySetG querySet;
	private int falseNum;
	private int queryNum;
	private double fpRate;
	private double accuracy;
	
	private int sumBits;

	public GranularityTask(int pbfType, int m, int queryLength, DatasetG dataset, QuerySetG querySet, double accuracy)
	{
		this.pbfType = pbfType;
		this.m = m;
		this.dataset = dataset;
		this.querySet = querySet;
		this.accuracy = accuracy;
		this.queryLength = queryLength;
		this.queryNum = 0;
		this.falseNum = 0;
		this.sumBits = 0;
	}
	
	public double getFPRate()
	{
		return fpRate;
	}
	
	public int getSumBits()
	{
		return sumBits;
	}

	public void start()
	{
		System.out.println("========new========");

		// get metadata
		long N = dataset.size();
		long n = dataset.getOriginalData().size();
		long n_quote = dataset.getDataset().size();
		
		//
		// System.out.println("N=" + N + ";n=" + n + ";n'=" + n_quote);

		// generate pbf
		PersistentBloomFilter pbf = null;
		int levelNum = dataset.getLevelNum();
		
		System.out.println("number of levels: " + levelNum);
		
		if (0 == pbfType)
		{
			int k = OptimizationUtil.getOptimizedK(this.m, n, CommonConstants.K_MAX);
			pbf = new Beta0(m, k);
//			System.out.println("k=" + k);
		}
		else if (1 == pbfType)
		{
			// beta-1-opt
			int[] mm = PbfUtil.getOptimizedM(this.m, dataset.getD(),
					querySet.getQueryFrequency(), queryLength, accuracy);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, dataset.getD(), CommonConstants.K_MAX);
			pbf = new Beta1(mm, k, levelNum, 4);

			for (int i = 0; i < mm.length; i++)
			{
				sumBits += mm[i];
			}
			System.out.println("bits: " + sumBits);
		}
		else if (2 == pbfType)
		{
			// beta-2-opt
			// get the number of distinct elements for each level
			int topLevel = levelNum - 1;
			ArrayList<HashSet<String>> levelSet = dataset.getLevelSet();
			int[] d = new int[levelNum];
			for (int i = 0; i < levelNum; i++)
			{
				d[i] = levelSet.get(i).size();
			}

			int[] mm = PbfUtil.getOptimizedM(this.m, d,
					querySet.getQueryFrequencyForBeta2(), queryLength, accuracy);
			int[] k = PbfUtil.getOptimizedKForBeta1(mm, d, CommonConstants.K_MAX);

			pbf = new Beta2(mm, k, topLevel);
			// System.out.println(time);

			for (int i = 0; i < mm.length; i++)
			{
				sumBits += mm[i];
			}
			System.out.println("bits: " + sumBits);
			
		}
		
		if (Math.abs(sumBits - m) / m > 0.1)
		{
			System.out.println("error! sumBits: " + sumBits + ", m: " + m);
			return;
		}

		// insert temporal elements into pbf
		HashMap<String, TimepointSet> ds = dataset.getDataset();
		int numOfInsertion = 0;
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
		
//		System.out.println("number of insertions: " + numOfInsertion);

		// query
		ArrayList<Long> starttimeSet = querySet.getStarttimeSet();
		ArrayList<String> elements = new ArrayList<String>();
		for (int i = 0; i < 1000; i++)
		{
			String str = RandomGenerator.getRandomString(20);
			elements.add(str);
		}
//		String str = "guojinweishihaoren!!";
//		byte[] elementBytes = str.getBytes();
		for (int i = 0; i < starttimeSet.size(); i++)
		{
			this.queryNum++;
			long starttime = starttimeSet.get(i);
			byte[] elementByte = elements.get(i % elements.size()).getBytes();
			// the start point in query set is 1, so we has to modify it
			// only for wc_qry
			if (pbf.query(elementByte, starttime, starttime + queryLength - 1))
			{
				this.falseNum++;
			}
		}

//		System.out.println("the nubmer of queries: " + this.queryNum);
		this.fpRate = (double) falseNum / this.queryNum;
//		System.out.println("fp number: " + this.falseNum);
//		System.out.println("fp rate: " + this.fpRate);
	}
	
	public static void main(String[] args)
	{
		// java -jar pbf.jar args[0] args[1] args[2] args[3] args[4] args[5]
		// java -jar pbf.jar pbfType m queryLength dataFileName queryFileName accuracy
		// 1 50000000 128 d:/dataset/1013/network d:/dataset/1013/wc-qry-86400-new 1E-10
		int pbfType = Integer.parseInt(args[0]);
		int m = Integer.parseInt(args[1]);
		int queryLength = Integer.parseInt(args[2]);
		int g = 4;
		int granularity = 1000;
		queryLength = queryLength * granularity;
		
		int mStart = 2000000;
		int mEnd = 60000000;
		
		String dataFileName = args[3];
		DatasetG dataset = new DatasetG(g, granularity);
		dataset.loadFromFile(dataFileName);

		String queryFileName = args[4];
		QuerySetG queryset = new QuerySetG(dataset.getLevelNum(), g, queryLength, granularity);
		queryset.loadQueryFromFile(queryFileName);
		
		double accuracy = Double.parseDouble(args[5]);
		
//		GranularityTask fpTask = new GranularityTask(2, 50000000, 128, dataset, queryset, accuracy);
//		fpTask.start();
		
//		System.out.println("========pbf-1==========");
//		for (int i = 0; i < 16; i++)
//		{
//			int mMiddle = (mStart + mEnd) / 2;
//			GranularityTask fpTask = new GranularityTask(1, mMiddle, queryLength, dataset, queryset, accuracy);
//			fpTask.start();
//			
//			double fpRate = fpTask.getFPRate();
//			System.out.println("false positive rate: " + fpRate);
//			
//			if (fpRate > 0.048 && fpRate < 0.052)
//			{
//				System.out.println("sum of bits: " + fpTask.getSumBits() + ", m: " + mMiddle);
//				System.out.println("false positive rate: " + fpRate);
//				break;
//			}
//			else if (fpRate > 0.05)
//			{
//				mStart = mMiddle + 1;
//			}
//			else if (fpRate < 0.05)
//			{
//				mEnd = mMiddle - 1;
//			}
//			else
//			{
//				System.out.println("error!");
//			}
//		}
		
		System.out.println("========pbf-2==========");
		mStart = 2000000;
		mEnd = 60000000;
		for (int i = 0; i < 16; i++)
		{
			int mMiddle = (mStart + mEnd) / 2;
			GranularityTask fpTask = new GranularityTask(2, mMiddle, queryLength, dataset, queryset, accuracy);
			fpTask.start();
			
			double fpRate = fpTask.getFPRate();
			System.out.println("false positive rate: " + fpRate);
			
			if (fpRate > 0.048 && fpRate < 0.052)
			{
				System.out.println("sum of bits: " + fpTask.getSumBits() + ", m: " + mMiddle);
				System.out.println("false positive rate: " + fpRate);
				break;
			}
			else if (fpRate > 0.05)
			{
				mStart = mMiddle + 1;
			}
			else if (fpRate < 0.05)
			{
				mEnd = mMiddle - 1;
			}
			else
			{
				System.out.println("error!");
			}
		}
			
	}
}
