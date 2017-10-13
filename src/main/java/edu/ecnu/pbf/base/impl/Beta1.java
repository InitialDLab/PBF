package edu.ecnu.pbf.base.impl;

import java.util.ArrayList;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.base.BasicBloomFilter;
import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.util.BinaryUtil2;

public class Beta1 implements PersistentBloomFilter
{
	private ArrayList<BasicBloomFilter> bfArray;
	private Beta0 b0;  // single bloom filter

	private int totalBitNum;
	private int levelNum; // L+1
	private int g;
	private int gLevel;
	private int maxT;

	public Beta1(int bitNum, int levelNum, int g)
	{
		b0 = new Beta0(bitNum, CommonConstants.K_MAX);
		totalBitNum = bitNum;

		this.gLevel = 1;
		int tempG = g;
		while ((tempG = tempG / 2) > 0)
		{
			this.gLevel++;
		}
		int bitNumTmp = bitNum;
		this.bfArray = new ArrayList<BasicBloomFilter>();
		int j = 2;
		for (int i = 0; i < (1 << (levelNum - this.gLevel + 1)) - 1; i++)
		{
			this.bfArray.add(new BasicBloomFilter(bitNumTmp, CommonConstants.K_MAX));
			totalBitNum += bitNumTmp;
//			if ((i + 2) % j == 0)
//			{
//				j = j << 1;
//				bitNumTmp = bitNumTmp > 100000 ? bitNumTmp/2:bitNumTmp;
//			}
		}

		this.levelNum = levelNum;
		this.g = g;
		this.maxT = CommonConstants.g[levelNum - 1] - 1;
	}
	
	public Beta1(int[] m, int[] k, int levelNum, int g)
	{
		this.gLevel = 1;
		int tempG = g;
		while ((tempG = tempG / 2) > 0)
		{
			this.gLevel++;
		}
		
		b0 = new Beta0(m[0], k[0]);
		totalBitNum = m[0];
		
		this.bfArray = new ArrayList<BasicBloomFilter>();
		for (int i = 1; i < m.length; i++)
		{
			if (m[i] == 0)
			{
				bfArray.add(new BasicBloomFilter(1, 1));
			}
			else
			{
				bfArray.add(new BasicBloomFilter(m[i], k[i]));
				totalBitNum += m[i];
			}
		}
		
		this.levelNum = levelNum;
		this.g = g;
		this.maxT = CommonConstants.g[levelNum - 1] - 1;
		
//		System.out.println("total number of bits: " + this.totalBitNum);
//		System.out.println("total number of bf: " + this.bfArray.size());
	}

	public boolean insertString(String element, long timestamp)
	{
		int index = 1;
		int start = 0;
		int end = this.maxT;
		for (int i = 0; i < levelNum - gLevel + 1; i++)
		{
			bfArray.get(index - 1).insertString(element);
			int mid = (start + end) / 2;
			if (mid < timestamp)
			{
				start = mid + 1;
				index = index * 2 + 1;
			}
			else
			{
				end = mid;
				index = index * 2;
			}
		}

		b0.insertString(element, timestamp);
		return true;
	}

	public boolean queryString(String element, long timestamp)
	{
		// TODO
		return false;
	}

	public boolean queryString(String element, long startTime, long endTime)
	{
		boolean result = false;
		ArrayList<Integer> indexArray = getBinaryDecomposition((int) startTime, (int) endTime);
		for (int i = 0; i < indexArray.size(); i++)
		{
			int index = indexArray.get(i);
			if (bfArray.get(index - 1).queryString(element))
			{
				if (i == 0 && index > bfArray.size() / 2 && !b0.queryString(element, startTime,
						startTime + this.g - startTime / this.g - 1))
				{
					continue;
				}
				else if ((i == indexArray.size() - 1)
						&& index > bfArray.size() / 2
						&& !b0.queryString(element, endTime - endTime / this.g, endTime))
				{
					continue;
				}
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean insert(byte[] element, long timestamp)
	{
		boolean result = true;
		int index = 1;
		int start = 0;
		int end = this.maxT;
		for (int i = 0; i < levelNum - gLevel + 1; i++)
		{
			BasicBloomFilter bf = bfArray.get(index - 1);
			if (null != bf)
			{
				bf.insert(element);	
			}
			int mid = (start + end) / 2;
			if (mid < timestamp)
			{
				start = mid + 1;
				index = index * 2 + 1;
			}
			else
			{
				end = mid;
				index = index * 2;
			}
		}

		b0.insert(element, timestamp);
		return result;
	}
	
	public boolean query(byte[] element, long timestamp)
	{
		// TODO
		return false;
	}
	
	public boolean query(byte[] element, long startTime, long endTime)
	{
		boolean result = false;
		ArrayList<Integer> indexArray = getBinaryDecomposition((int) startTime, (int) endTime);
		for (int i = 0; i < indexArray.size(); i++)
		{
			int index = indexArray.get(i);
			BasicBloomFilter bf = bfArray.get(index - 1);
			if (null == bf)
			{
				// continue;
				result = true;
				break;
			}
			else if (bf.query(element))
			{
				if (i == 0 && index > bfArray.size() / 2 && !b0.query(element, startTime,
						startTime + this.g - startTime % this.g - 1))
				{
					continue;
				}
				else if ((i == indexArray.size() - 1)
						&& index > bfArray.size() / 2
						&& !b0.query(element, endTime - endTime % this.g, endTime))
				{
					continue;
				}
				result = true;
				break;
			}
		}
		return result;
	}
	
	public ArrayList<Integer> getBinaryDecomposition(int start, int end)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = start; i <= end;)
		{
			int level = BinaryUtil2.getBinaryLevel(i, end); // level is from 1
															// to L+1
			int index = (1 << (this.levelNum - level)) + i / CommonConstants.g[level - 1];
			while (index > bfArray.size())
			{
				index = index / 2;
			}
			result.add(index);
			i = i + (1 << (level - 1));
		}
		return result;
	}

	//////// Basic function////////
	public ArrayList<BasicBloomFilter> getBfArray()
	{
		return bfArray;
	}

	public Beta0 getB0()
	{
		return b0;
	}

	public int getLevelNum()
	{
		return levelNum;
	}
	
	/**
	 * Get the total number of levels required according to the maxT.
	 * @param maxT start from 0
	 * @return
	 */
	public static int getLevelNum(int maxT)
	{
		int result = 0;
		for (int i = 0; i < CommonConstants.g.length; i++)
		{
			if ((maxT + 1) <= CommonConstants.g[i])
			{
				result = i + 1;
				break;
			}
		}
		return result;
	}

	public int getG()
	{
		return g;
	}

	public int getMaxT()
	{
		return maxT;
	}
	
	public int getTotalBitNum()
	{
		return this.totalBitNum;
	}

	public static void main(String[] args)
	{
		byte[] element = "guo".getBytes();
		Beta1 b1 = new Beta1(100000, 4, 1);
		b1.insert(element, 4);

		System.out.println(b1.query(element, 2, 5));
		System.out.println(b1.query(element, 2, 3));
		System.out.println(b1.query("jin".getBytes(), 1, 6));
		System.out.println(getLevelNum(3));
		
		System.out.println("============");
		ArrayList<Integer> list = b1.getBinaryDecomposition(1, 4);
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println(list.get(i));
		}
	}

}
