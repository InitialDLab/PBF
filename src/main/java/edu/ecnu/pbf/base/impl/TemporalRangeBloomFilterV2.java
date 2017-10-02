package edu.ecnu.pbf.base.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;

import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.util.BinaryUtil;
import edu.ecnu.pbf.util.MurmurHash3;

/**
 * Optimize the query of TemporalRangeBloomFilter.
 * 
 * @author Jinwei
 *
 */
public class TemporalRangeBloomFilterV2 implements PersistentBloomFilter
{
	private BitSet bs;
	private int bitNum;
	private int hashNum;
	private int g;
	private int level;
	private int queryNum;

	/**
	 * Create a temporal range Bloom filter with the provided parameters.
	 * 
	 * @param bitNum
	 *            the capacity of the Bloom filter
	 * @param hashNum
	 *            the numbers of hash function used in Bloom filter
	 * @param g
	 *            the size of temporal range used in the Bloom filter
	 * 
	 */
	public TemporalRangeBloomFilterV2(int bitNum, int hashNum, int level)
	{
		this.bitNum = bitNum;
		this.hashNum = hashNum;
		this.level = level;
		this.g = 1 << (this.level);
		this.queryNum = 0;
		bs = new BitSet(bitNum);
	}

	/**
	 * Get the member variable used to store bits.
	 * 
	 * @return BitSet
	 */
	public BitSet getBitSet()
	{
		return this.bs;
	}
	
	/**
	 * Set the bit set used for Bloom filter.
	 * 
	 * @param bs
	 */
	public void setBitSet(BitSet bs)
	{
		this.bs = bs;
	}

	/**
	 * Get the number of bits in this Bloom filter.
	 * 
	 * @return int
	 */
	public int getSize()
	{
		return this.bitNum;
	}

	/**
	 * Get the number of hash function in this Bloom filter.
	 * 
	 * @return int
	 */
	public int getHashNum()
	{
		return this.hashNum;
	}

	/**
	 * Get the size of time range of this Bloom filter
	 * 
	 * @return int
	 */
	public int getG()
	{
		return this.g;
	}

	/**
	 * Get the query number of this Bloom filter
	 * 
	 * @return int
	 */
	public int getQueryNum()
	{
		return this.queryNum;
	}

	/**
	 * Insert a String element into the Bloom filter in accordance with the
	 * timestamp. This element timestamp is mapped to a time range.
	 * 
	 * @param element
	 * @param timestamp
	 */
	public boolean insertString(String element, long timestamp)
	{
		boolean result = true;
		
		long rangeNum = getRange(timestamp);
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));
//		byte[] byteElement = (element + "|" + String.valueOf(rangeNum))
//				.getBytes(Charset.forName("UTF-8"));

		for (int i = 0; i < hashNum; i++)
		{
//			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
//					byteElement.length, i);
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, (int)rangeNum * this.hashNum + i);
			int index = getIndex(hash);
			bs.set(index);
		}
		
		return result;
	}

	/**
	 * Query weather a given String element exists in the Bloom filter in
	 * accordance with the timestamp of element
	 * 
	 * @param element
	 * @param timestamp
	 * @return boolean true if the element exists in the given time
	 */
	public boolean queryString(String element, long timestamp)
	{
		long rangeNum = getRange(timestamp);
		this.queryNum++;
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));
//		byte[] byteElement = (element + "|" + String.valueOf(rangeNum))
//				.getBytes(Charset.forName("UTF-8"));
		boolean isExist = true;
		for (int i = 0; i < hashNum; i++)
		{
//			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
//					byteElement.length, i);
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, (int)rangeNum * this.hashNum + i);
			int index = getIndex(hash);
			if (false == bs.get(index))
			{
				isExist = false;
				break;
			}
		}

		return isExist;
	}

	/**
	 * Query weather a given String element exists in the given time range
	 * 
	 * @param element
	 * @param timestamp
	 * @return boolean true if the element exists in the given time range
	 */
	public boolean queryString(String element, long startTime, long endTime)
	{
		boolean isExist = false;

		if (startTime > endTime || startTime < 0 || endTime < 0)
		{
			isExist = false;
		}
		else
		{
			ArrayList<Long> startKeyArray = BinaryUtil.getStartKey(startTime,
					endTime, this.level);
			for (long startKey : startKeyArray)
			{
				if (queryString(element, startKey))
				{
					isExist = true;
					break;
				}
			}
		}

		return isExist;
	}
	
	public boolean insert(byte[] element, long timestamp)
	{
		boolean result = true;
		long rangeNum = getRange(timestamp);

		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, (int)rangeNum * this.hashNum + i);
			int index = getIndex(hash);
			bs.set(index);
		}
		return result;
	}
	
	public boolean query(byte[] element, long timestamp)
	{
		boolean result = true;
		long rangeNum = getRange(timestamp);
		this.queryNum++;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, (int)rangeNum * this.hashNum + i);
			int index = getIndex(hash);
			if (false == bs.get(index))
			{
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean query(byte[] element, long startTime, long endTime)
	{
		boolean result = false;
		if (startTime > endTime || endTime < 0)
		{
			result = false;
		}
		else
		{
			startTime = startTime < 0 ? 0:startTime;
			ArrayList<Long> startKeyArray = BinaryUtil.getStartKey(startTime,
					endTime, this.level);
			for (long startKey : startKeyArray)
			{
				if (query(element, startKey))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Query a given String element with the given time range, and return the
	 * ranges which may contain this element.
	 * 
	 * @param element
	 * @param startTime
	 * @param endTime
	 * @return ArrayList
	 */
	public ArrayList<Long> queryStringWithInfo(String element, long startTime,
			long endTime)
	{
		ArrayList<Long> array = new ArrayList<Long>();

		if (startTime > endTime || startTime < 0 || endTime < 0)
		{
		}
		else
		{
			ArrayList<Long> startKeyArray = BinaryUtil.getStartKey(startTime,
					endTime, this.level);
			for (long startKey : startKeyArray)
			{
				if (queryString(element, startKey))
				{
					array.add(startKey);
				}
			}
		}
		return array;
	}

	private int getIndex(int hash)
	{
		int index = hash % bitNum;
		return index >= 0 ? index : -index;
	}

	private long getRange(long timestamp)
	{
		return timestamp / g;
	}

	public static void main(String[] args)
	{
		TemporalRangeBloomFilterV2 trbf = new TemporalRangeBloomFilterV2(100000, 3, 1);
		System.out.println(trbf.getG());
		System.out.println(trbf.getRange(2));
	}
}
