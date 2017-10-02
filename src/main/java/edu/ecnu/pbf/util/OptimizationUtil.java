package edu.ecnu.pbf.util;

public class OptimizationUtil
{
	/**
	 * According to the parameters m and n (the bits number and distinct 
	 * element number of a Bloom filter), calculate the optimized k 
	 * (the number of hash functions used in Bloom filters).
	 * @return
	 */
	public static int getOptimizedK(long m, long n, int kMax)
	{
		int result = 0;
		result = (int)((double)m / n * Math.log(2) + 1);
		if (kMax > 0 && result > kMax)
		{
			result = kMax;
		}
		return result;
	}
	
	public static void main(String[] args)
	{
		System.out.println(getOptimizedK(100000, 12500, 12));
	}
}
