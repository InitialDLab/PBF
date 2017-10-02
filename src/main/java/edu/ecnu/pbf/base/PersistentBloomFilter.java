package edu.ecnu.pbf.base;

/**
 * A variant of Bloom filter. Persistent Bloom filter (PBF) can support temporal
 * sets queries. We use Set[startTime, endTime] to denote the set of elements
 * which arrived within [startTime, endTime]. We can use PBF to check whether an
 * element exists in Set[startTime, endTime]. False positives are possible, but
 * false negatives are not.
 * 
 * @author guo
 *
 */
public interface PersistentBloomFilter
{
	/**
	 * Insert an element with a timestamp into this persistent bloom filter.
	 * 
	 * @param element
	 * @param timestamp
	 * @return
	 */
	public boolean insertString(String element, long timestamp);

	/**
	 * Query whether an element appears at the given time point.
	 * 
	 * @param element
	 * @param timestamp
	 * @return
	 */
	public boolean queryString(String element, long timestamp);

	/**
	 * Query whether an element with a timestamp is a member of the range set
	 * Set[startTime, endTime].
	 * 
	 * @param element
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public boolean queryString(String element, long startTime, long endTime);
	
	/**
	 * Insert (element, timestamp) pair using the element's bytes to this Persistent Bloom filter.
	 * @param element
	 * @param timestamp
	 * @return boolean
	 */
	public boolean insert(byte[] element, long timestamp);
	
	/**
	 * Query whether an element appears at a time point using its bytes.
	 * @param element
	 * @param timestamp
	 * @return boolean
	 */
	public boolean query(byte[] element, long timestamp);
	
	/**
	 * Query whether an element exists in a temporal range using its bytes.
	 * @param element
	 * @param startTime
	 * @param endTime
	 * @return boolean
	 */
	public boolean query(byte[] element, long startTime, long endTime);
}
