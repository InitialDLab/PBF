package edu.ecnu.pbf.base;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.BitSet;

import edu.ecnu.pbf.util.MurmurHash3;

/**
 * This is a basic implementation of Bloom filter.
 * 
 * @author guo
 *
 */
public class BasicBloomFilter
{
	private BitSet bs;
	private int bitNum;
	private int hashNum;

	public BasicBloomFilter()
	{
		this.bitNum = 1000000;
		this.hashNum = 4;
		bs = new BitSet(this.bitNum);
	}

	/**
	 * Create a Bloom filter with the provided parameters.
	 * 
	 * @param bitNum
	 *            the capacity of the Bloom filter
	 * @param hashNum
	 *            the number of hash function used in Bloom filter
	 */
	public BasicBloomFilter(int bitNum, int hashNum)
	{
		this.bitNum = bitNum;
		this.hashNum = hashNum;
		bs = new BitSet(this.bitNum);
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
	 * Insert an element into the Bloom filter.
	 * 
	 * @param element
	 */
	public void insert(byte[] element)
	{
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, i);
			int index = getIndex(hash);
			bs.set(index);
		}
	}

	/**
	 * Insert a String element into the Bloom filter.
	 * 
	 * @param element
	 */
	public void insertString(String element)
	{
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));

		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, i);
			int index = getIndex(hash);
			bs.set(index);
		}

	}

	/**
	 * Query whether an element exists in this Bloom filter.
	 * 
	 * @param element
	 * @return boolean true if the element exists in this bloom filter
	 */
	public boolean query(byte[] element)
	{
		boolean isExist = true;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(element, 0,
					element.length, i);
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
	 * Query whether a {@code String} element exists in this Bloom filter.
	 * 
	 * @param element
	 * @return boolean true if the element exists in this bloom filter
	 */
	public boolean queryString(String element)
	{
		byte[] byteElement = element.getBytes(Charset.forName("UTF-8"));
		boolean isExist = true;
		//int hash = 0;
		for (int i = 0; i < hashNum; i++)
		{
			int hash = MurmurHash3.murmurhash3_x86_32(byteElement, 0,
					byteElement.length, i);
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
	 * Get the probability of false positive.
	 * 
	 * @return BigDecimal
	 */
	public BigDecimal getPFP(int insertNum)
	{
		return BigDecimal.ONE;
	}

	private int getIndex(int hash)
	{
		int index = hash % bitNum;	
		return index >= 0 ? index : -index;
	}
	
	public static void main(String[] args)
	{
		BasicBloomFilter bbf = new BasicBloomFilter(100000, 5);
		bbf.insert("guo".getBytes());
		System.out.println(bbf.query("guo".getBytes()));
		System.out.println(bbf.query("g".getBytes()));
	}

}
