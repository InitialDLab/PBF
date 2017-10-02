package edu.ecnu.pbf.base.impl;

import java.util.ArrayList;

import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.element.DualTuple;
import edu.ecnu.pbf.util.BinaryUtil2;

/**
 * The leaf level is level 0, the top level is level L, where L=logT.
 * 
 * @author Jinwei
 *
 */
public class Beta2 implements PersistentBloomFilter
{
	public static final int[] g = { 1 << 0, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6, 1 << 7,
			1 << 8, 1 << 9, 1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17,
			1 << 18, 1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, 1 << 26,
			1 << 27, 1 << 28, 1 << 29, 1 << 30 };

	private int bitNum;
	private int topLevel; // L
	private int[] hashNum;
	private int totalBitNum;

	private TemporalRangeBloomFilterV2[] rangeBloomFilter;

	/**
	 * 
	 * @param bitNum  the number of bits for each level
	 * @param topLevel
	 * @param hashNum
	 */
	public Beta2(int bitNum, int hashNum, int topLevel)
	{
		this.bitNum = bitNum;
		this.topLevel = topLevel;
		this.hashNum = new int[topLevel + 1];
		for (int i = 0; i <= topLevel; i++)
		{
			this.hashNum[i] = hashNum;
		}

		this.rangeBloomFilter = new TemporalRangeBloomFilterV2[topLevel + 1];
		for (int i = 0; i <= topLevel; i++)
		{
			rangeBloomFilter[i] = new TemporalRangeBloomFilterV2(this.bitNum, this.hashNum[i], i);
		}
	}
	
	public Beta2(int[] bitNum, int[] hashNum, int topLevel)
	{
		this.totalBitNum = 0;
		this.topLevel = topLevel;
		this.hashNum = hashNum;
		
		this.rangeBloomFilter = new TemporalRangeBloomFilterV2[topLevel + 1];
		for (int i = 0; i <= topLevel; i++)
		{
			if (bitNum[i] == 0)
			{
				rangeBloomFilter[i] = null;
			}
			else
			{
				this.totalBitNum += bitNum[i];
				rangeBloomFilter[i] = new TemporalRangeBloomFilterV2(bitNum[i], hashNum[i], i);
//				System.out.println("level " + i + ": " + bitNum[i]);
			}
		}
//		System.out.println("total bit num: " + totalBitNum);
	}

	/**
	 * According to T, get the topLevel.
	 * 
	 * @param maxT
	 * @return
	 */
	public static int getTopLevel(int maxT)
	{
		int result = 0;
		for (int i = 0; i < g.length; i++)
		{
			if ((maxT + 1) <= g[i])
			{
				result = i;
				break;
			}
		}
		return result;
	}

	public boolean insertString(String element, long timestamp)
	{
		for (int i = 0; i <= topLevel; i++)
		{
			rangeBloomFilter[i].insertString(element, timestamp);
		}

		return true;
	}

	public boolean queryString(String element, long timestamp)
	{
		boolean result = false;
		if (rangeBloomFilter[0].queryString(element, timestamp))
		{
			result = true;
		}
		return result;
	}

	public boolean queryString(String element, long startTime, long endTime)
	{
		boolean result = false;
		long startTimeTemp = startTime;
		long endTimeTemp = endTime;

		ArrayList<DualTuple> dyadicResult = BinaryUtil2.getBinaryDecomposition((int) startTimeTemp,
				(int) endTimeTemp);
		
		for (int i = 0; i < dyadicResult.size(); i++)
		{
			int key = dyadicResult.get(i).getFirst();
			int level = dyadicResult.get(i).getSecond();
			if (rangeBloomFilter[level - 1].queryString(element, key))
			{
				result = true;
				break;
			}
		}
		return result;
	}
	
	public boolean insert(byte[] element, long timestamp)
	{
		boolean result = true;
		for (int i = 0; i <= topLevel; i++)
		{
			if (null != rangeBloomFilter[i])
			{
				rangeBloomFilter[i].insert(element, timestamp);				
			}
		}
		return result;
	}
	
	public boolean query(byte[] element, long timestamp)
	{
		boolean result = false;
		if (rangeBloomFilter[0].query(element, timestamp))
		{
			result = true;
		}
		return result;
	}
	
	public boolean query(byte[] element, long startTime, long endTime)
	{
		boolean result = false;
		long startTimeTemp = startTime;
		long endTimeTemp = endTime;

		ArrayList<DualTuple> dyadicResult = BinaryUtil2.getBinaryDecomposition((int) startTimeTemp,
				(int) endTimeTemp);
		for (int i = 0; i < dyadicResult.size(); i++)
		{
			int key = dyadicResult.get(i).getFirst();
			int level = dyadicResult.get(i).getSecond();
			if (null == rangeBloomFilter[level - 1])
			{
				continue;
			}
			else if (rangeBloomFilter[level - 1].query(element, key))
			{
				result = true;
				break;
			}
		}
		return result;
	}
	
	public int getTotalBitNum()
	{
		return this.totalBitNum;
	}

	public static void main(String[] args)
	{
		Beta2 b = new Beta2(100000, 3, 10);
		b.insert("guo".getBytes(), 200);
		System.out.println(b.query("guo".getBytes(), 200));
		System.out.println(b.query("guo".getBytes(), 100, 300));
		System.out.println(b.query("guo".getBytes(), 201, 300));
		System.out.println(b.query("guo".getBytes(), 100, 199));
		System.out.println(b.query("jin".getBytes(), 1, 1000));
	}
}
