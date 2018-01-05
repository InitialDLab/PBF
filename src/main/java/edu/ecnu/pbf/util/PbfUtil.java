package edu.ecnu.pbf.util;

import java.util.BitSet;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.base.impl.TemporalRangeBloomFilterV2;

public class PbfUtil
{
	public static final double INVARIANT = Math.log(2) * Math.log(2);

	public static int[] getOptimizedMForBeta1(int m, int[] d, int[] f, int queryLength)
	{
		int[] optimizedM = null;
		if (d.length != f.length)
		{
			// TODO
		}
		else
		{
			double absoluteAccuracy = 0;
//			if (queryLength <= 16)
//			{
//				absoluteAccuracy = 1E-43;
//			}
//			else if (queryLength <= 32)
//			{
//				absoluteAccuracy = 1E-36;
//			}
//			else if (queryLength <= 64)
//			{
//				absoluteAccuracy = 1E-32;
//			}
//			else
//			{
//				absoluteAccuracy = 1E-30;
//			}
			absoluteAccuracy = 1E-93; // 93 for beta1, 54 for beta2
			optimizedM = new int[d.length];
			LamdaFunction lamdaFunc = new LamdaFunction(m, d, f);
			BisectionSolver solver = new BisectionSolver(absoluteAccuracy);
			double lamda = solver.solve(40000, lamdaFunc, -1, 0);
//			System.out.println("lamda: " + lamda);
//			System.out.println(lamda);
			for (int i = 0; i < optimizedM.length; i++)
			{
				if (f[i] == 0 || d[i] == 0)
				{
					optimizedM[i] = 0;
				}
				else
				{
					optimizedM[i] = (int) ((double) d[i] / INVARIANT
							* Math.log(1.0D - (double) f[i] * PbfUtil.INVARIANT / lamda / d[i]));
				}
			}
		}
		return optimizedM;
	}
	
	public static int[] getOptimizedM(int m, int[] d, int[] f, int queryLength, double acc)
	{
		int[] optimizedM = null;
		if (d.length != f.length)
		{
			// TODO
		}
		else
		{
			double absoluteAccuracy = 0;

			absoluteAccuracy = acc; // 93 for beta1, 54 for beta2
			optimizedM = new int[d.length];
			LamdaFunction lamdaFunc = new LamdaFunction(m, d, f);
			BisectionSolver solver = new BisectionSolver(absoluteAccuracy);
			double lamda = solver.solve(40000, lamdaFunc, -1, 0);

			for (int i = 0; i < optimizedM.length; i++)
			{
				if (f[i] == 0 || d[i] == 0)
				{
					optimizedM[i] = 0;
				}
				else
				{
					optimizedM[i] = (int) ((double) d[i] / INVARIANT
							* Math.log(1.0D - (double) f[i] * PbfUtil.INVARIANT / lamda / d[i]));
				}
			}
		}
		return optimizedM;
	}
	
	public static int[] getOptimizedMForBeta2(int m, int[] d, int[] f, int queryLength)
	{
		int[] optimizedM = null;
		if (d.length != f.length)
		{
			// TODO
		}
		else
		{
			double absoluteAccuracy = 0;

			absoluteAccuracy = 1E-53; // 93 for beta1, 54 for beta2
			optimizedM = new int[d.length];
			LamdaFunction lamdaFunc = new LamdaFunction(m, d, f);
			BisectionSolver solver = new BisectionSolver(absoluteAccuracy);
			double lamda = solver.solve(40000, lamdaFunc, -1, 0);

			for (int i = 0; i < optimizedM.length; i++)
			{
				if (f[i] == 0 || d[i] == 0)
				{
					optimizedM[i] = 0;
				}
				else
				{
					optimizedM[i] = (int) ((double) d[i] / INVARIANT
							* Math.log(1.0D - (double) f[i] * PbfUtil.INVARIANT / lamda / d[i]));
				}
			}
		}
		return optimizedM;
	}

	public static int[] getOptimizedKForBeta1(int[] m, int[] d, int kMax)
	{
		int[] optimizedK = null;
		if (m.length != d.length)
		{
			// TODO
		}
		else
		{
			optimizedK = new int[m.length];
			for (int i = 0; i < optimizedK.length; i++)
			{
				if (m[i] == 0 || d[i] == 0)
				{
					optimizedK[i] = 0;
				}
				else
				{
					optimizedK[i] = (int) ((double) m[i] / d[i] * Math.log(2) + 1) + 1;
					if (kMax > 0 && optimizedK[i] > kMax)
					{
						optimizedK[i] = kMax;
					}
				}
			}
		}
		return optimizedK;
	}

	/**
	 * Get the total number of levels.
	 * 
	 * @param maxT
	 *            from 0
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

	/**
	 * Merge the BitSets of two consecutive beta2 pbfs into one.
	 * Note: the structures of the two pbfs should be the same, i.e., hash numbers and level numbers.
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static BitSet mergeBeta2(TemporalRangeBloomFilterV2 rbf1, TemporalRangeBloomFilterV2 rbf2)
	{
		BitSet bs1 = rbf1.getBitSet();
		BitSet bs2 = rbf2.getBitSet();
		int bitNumOfRbf1 = rbf1.getSize();
		int bitNumOfRbf2 = rbf2.getSize();
		int bitNum = MathUtil.gcd(bitNumOfRbf1, bitNumOfRbf2);
		BitSet bitSet = new BitSet(bitNum);
		for (int i = 0; i < bitNumOfRbf1; i++)
		{
			if (bs1.get(i) == true)
			{
				bitSet.set(i % bitNum);
			}
		}
		for (int i = 0; i < bitNumOfRbf2; i++)
		{
			if (bs2.get(i) == true)
			{
				bitSet.set(i % bitNum);
			}
		}
		return bitSet;
	}
	
	public static BitSet merge(BitSet bs1, int bitNumOfBs1, BitSet bs2, int bitNumOfBs2)
	{
		int bitNum = MathUtil.gcd(bitNumOfBs1, bitNumOfBs2);
		BitSet bitSet = new BitSet(bitNum);
		for (int i = 0; i < bitNumOfBs1; i++)
		{
			if (bs1.get(i) == true)
			{
				bitSet.set(i % bitNum);
			}
		}
		for (int i = 0; i < bitNumOfBs2; i++)
		{
			if (bs2.get(i) == true)
			{
				bitSet.set(i % bitNum);
			}
		}
		return bitSet;
	}

	public static void main(String[] args)
	{
		System.out.println(getLevelNum(127));
		int m = 10000000;
		int[] d = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 12800};
		int[] f = {2, 2, 2, 2, 2, 2, 2, 1};
		
		int[] mm = getOptimizedMForBeta1(m, d, f, 128);
		int sum = 0;
		
		for (int i = 0; i < mm.length; i++)
		{
			System.out.println(mm[i]);
			sum += mm[i];
		}
		
		System.out.println(sum);
		
	}
}

class LamdaFunction implements UnivariateFunction
{
	private int[] d;
	private int[] f;
	private long m;

	public LamdaFunction(long m, int[] d, int[] f)
	{
		this.m = m;
		this.d = d;
		this.f = f;
	}

	public double value(double x)
	{
		double result = 0;
		for (int i = 0; i < d.length; i++)
		{
			if (f[i] == 0 || d[i] == 0)
			{

			}
			else
			{
				result = result + (double) d[i]
						* Math.log(1.0D - (double) f[i] * PbfUtil.INVARIANT / x / d[i]);
			}
		}
		result = result - (double) m * PbfUtil.INVARIANT;
		return result;
	}
}