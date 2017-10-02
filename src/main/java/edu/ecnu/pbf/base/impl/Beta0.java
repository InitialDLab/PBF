package edu.ecnu.pbf.base.impl;

import java.nio.charset.Charset;
import java.util.BitSet;

import edu.ecnu.pbf.base.PersistentBloomFilter;
import edu.ecnu.pbf.util.MurmurHash3;

/**
 * This is a basic implementation of Bloom filter with timestamp of each element.
 * 
 * @author guo
 *
 */
public class Beta0 implements PersistentBloomFilter
{
	private BitSet bs;
	private int bitNum;
	private int hashNum;

	public Beta0()
	{
		this.bitNum = 1000000;
		this.hashNum = 4;
		bs = new BitSet(bitNum);
	}

	/**
	 * Create a Bloom filter with the provided parameters.
	 * 
	 * @param bitNum
	 *            the capacity of the Bloom filter
	 * @param hashNum
	 *            the numbers of hash function used in Bloom filter
	 */
	public Beta0(int bitNum, int hashNum)
	{
		this.bitNum = bitNum;
		this.hashNum = hashNum;
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
	 * Insert a String element into the Bloom filter with timestamp.
	 * 
	 * @param element
	 * @param timestamp
	 */
	public boolean insertString(String element, long timestamp)
	{
		boolean result = true;
//		byte[] byteElement = (element + "|" + String.valueOf(timestamp))
//				.getBytes(Charset.forName("UTF-8"));
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));

		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, (int)(timestamp * this.hashNum + i));
			int index = getIndex(hash);
			bs.set(index);
		}
		return result;
	}

	/**
	 * Query weather a given String element exists in the Bloom filter with
	 * timestamp
	 * 
	 * @param element
	 * @param timestamp
	 * @return boolean true if the element exists in the given time
	 */
	public boolean queryString(String element, long timestamp)
	{
//		byte[] byteElement = (element + "|" + String.valueOf(timestamp))
//				.getBytes(Charset.forName("UTF-8"));
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));
		boolean isExist = true;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, (int)(timestamp * this.hashNum + i));
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
			for (long i = startTime; i <= endTime; i++)
			{
				if (queryString(element, i))
				{
					isExist = true;
					break;
				}	
			}
		}
		
		return isExist;
	}
	
	/**
	 * Insert (element, timestamp) pair using the element's bytes to this Persistent Bloom filter.
	 * @param element
	 * @param timestamp
	 * @return boolean
	 */
	public boolean insert(byte[] element, long timestamp)
	{
		boolean result = true;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, (int)(timestamp * this.hashNum + i));
			int index = getIndex(hash);
			bs.set(index);
		}
		return result;
	}
	
	/**
	 * Query whether an element appears at a time point using its bytes.
	 * @param element
	 * @param timestamp
	 * @return boolean
	 */
	public boolean query(byte[] element, long timestamp)
	{
		boolean result = true;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, (int)(timestamp * this.hashNum + i));
			int index = getIndex(hash);
			if (false == bs.get(index))
			{
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Query whether an element exists in a temporal range using its bytes.
	 * @param element
	 * @param startTime
	 * @param endTime
	 * @return boolean
	 */
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
			for (long i = startTime; i <= endTime; i++)
			{
				if (query(element, i))
				{
					result = true;
					break;
				}	
			}
		}
		return result;
	}

	private int getIndex(int hash)
	{
		int index = hash % bitNum;	
		return index >= 0 ? index : -index;
	}

	public static void main(String[] args)
	{
		byte[] element = "guo".getBytes();
		Beta0 b = new Beta0(100000, 6);
		b.insert(element, 100);
		System.out.println(b.query(element, 100));
		System.out.println(b.query(element, 0, 99));
		System.out.println(b.query(element, 101, 200));
		System.out.println(b.query(element, 0, 100));
		System.out.println(b.query(element, 100, 200));
		System.out.println(b.query(element, 0, 200));
		System.out.println(b.query(element, 200, 215));
	}
}
