package edu.ecnu.pbf.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.util.BinaryUtil2;
import edu.ecnu.pbf.util.RandomGenerator;

/**
 * for granularity
 * @author jinwei
 *
 */
public class QuerySetG
{
	private ArrayList<TMTQuery> querySet;
	private ArrayList<Long> starttimeSet;
	private int levelNum;
	private int[] queryFrequency;  // for beta1, queryFrequency[0] is the number of queries for b0.
	private int[] queryFrequencyForBeta2;
	private int queryLength;
	private int size;
	private int gLevel;
	
	private int granularity;

	public QuerySetG()
	{
		this.querySet = new ArrayList<TMTQuery>();
		this.starttimeSet = new ArrayList<Long>();
		this.size = 0;
	}

	public QuerySetG(int levelNum, int g, int queryLength, int granularity)
	{
		this.querySet = new ArrayList<TMTQuery>();
		this.starttimeSet = new ArrayList<Long>();
		this.levelNum = levelNum;
		this.gLevel = 1;
		int tempG = g;
		while ((tempG = tempG / 2) > 0)
		{
			this.gLevel++;
		}
		// for beta1
		this.queryFrequency = new int[1 << (levelNum - gLevel + 1)];
		for (int i = 0; i < queryFrequency.length; i++)
		{
			this.queryFrequency[i] = 0;
		}
		// for beta2
		this.queryFrequencyForBeta2 = new int[levelNum];
		for (int i = 0; i < levelNum; i++)
		{
			this.queryFrequencyForBeta2[i] = 0;
		}
		this.queryLength = queryLength;
		this.size = 0;
		
		this.granularity = granularity;
	}

	/**
	 * Load the query data from the file.
	 * 
	 * @param fileName
	 */
	public void loadQueryFromFile(String fileName)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String tempStr = null;
			while ((tempStr = reader.readLine()) != null)
			{
				StringTokenizer tokenizer = new StringTokenizer(tempStr);
				String elementStr = null;
				String startTimestampStr = null;
				String endTimestampStr = null;
				long startTimestamp = -1;
				long endTimestamp = -1;
				if (null == (startTimestampStr = tokenizer.nextToken()))
				{
					continue;
				}
				else if (null == (endTimestampStr = tokenizer.nextToken()))
				{
					continue;
				}
				else
				{
					startTimestamp = Long.parseLong(startTimestampStr) * granularity;
					endTimestamp = Long.parseLong(endTimestampStr) * granularity;
				}

				elementStr = RandomGenerator.getRandomString(10);
				TMTQuery tmtQuery = new TMTQuery(elementStr, startTimestamp, endTimestamp);
				querySet.add(tmtQuery);
				starttimeSet.add(startTimestamp);
				// for beta1
				ArrayList<Integer> indexes = getBinaryDecompositionForBeta1((int) startTimestamp,
						(int) (startTimestamp + this.queryLength - 1));
				for (int i = 0; i < indexes.size(); i++)
				{
					if (queryFrequency.length - 1 < indexes.get(i))
					{
						System.out.println(indexes);
						System.out.println(queryFrequency.length);
					}
					queryFrequency[indexes.get(i)]++;
				}
				//queryFrequency[0]++;  // 0713 test

				// for beta2
				ArrayList<Integer> levels = getBinaryDecompositionForBeta2((int) startTimestamp,
						(int) (startTimestamp + this.queryLength - 1));
				for (int i = 0; i < levels.size(); i++)
				{
					queryFrequencyForBeta2[(levels.get(i) - 1)]++;
				}
				
				size++;
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Get the total number of queries.
	 * 
	 * @return
	 */
	public int size()
	{
		return size;
	}

	public ArrayList<TMTQuery> getQuerySet()
	{
		return querySet;
	}

	public ArrayList<Long> getStarttimeSet()
	{
		return starttimeSet;
	}

	public int[] getQueryFrequency()
	{
		return queryFrequency;
	}
	
	public int[] getQueryFrequencyForBeta2()
	{
		return queryFrequencyForBeta2;
	}

	/**
	 * Get the indexes of Basic BFs which need to be inserted.
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<Integer> getBinaryDecompositionForBeta1(int start, int end)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = start; i <= end;)
		{
			int level = BinaryUtil2.getBinaryLevel(i, end); // level is from 1
															// to L+1
			int index = (1 << (this.levelNum - level)) + i / CommonConstants.g[level - 1];
			while (index >= this.queryFrequency.length)
			{
				index = index / 2;
			}
			result.add(index);
			i = i + (1 << (level - 1));
		}
		return result;
	}
	
	/**
	 * The leaf level is level 1.
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<Integer> getBinaryDecompositionForBeta2(int start, int end)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = start; i <= end;)
		{
			int level = BinaryUtil2.getBinaryLevel(i, end);
			result.add(new Integer(level));
			i = i + (1 << (level - 1));
		}
		return result;
	}

	public static void main(String[] args)
	{
		String fileName = "d:/dataset/wc_qry";
		QuerySetG querySet = new QuerySetG();
		querySet.loadQueryFromFile(fileName);
		System.out.println(querySet.size());
		System.out.println(querySet.getQuerySet().size());
	}
}
