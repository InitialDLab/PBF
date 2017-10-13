package edu.ecnu.pbf.sketch.fm;

import java.util.LinkedList;
import java.util.List;

public class FlajoletMartinSketch
{
	private static final double PHI = 0.77351D;
	private int bitmapSize;
	private int numHashes;
	private FM_HashFunction[] hashes;
	private boolean[][] bitmaps; // bitmaps[i] is the bitmap for the i-th hash
									// function

	public FlajoletMartinSketch(int size, int nh)
	{
		bitmapSize = size;
		numHashes = nh;

		bitmaps = new boolean[numHashes][bitmapSize];
		hashes = new FM_HashFunction[numHashes];

		generateHashFunctions();
	}

	/**
	 * Case in which we need all FM sketches of nodes to have the same hash
	 * functions
	 */
	public FlajoletMartinSketch(int size, FM_HashFunction[] hashArray)
	{
		bitmapSize = size;
		numHashes = hashArray.length;

		bitmaps = new boolean[numHashes][bitmapSize];
		hashes = new FM_HashFunction[numHashes];

		for (int i = 0; i < numHashes; i++)
		{
			hashes[i] = hashArray[i];
		}
	}

	public int getBitmapSize()
	{
		return bitmapSize;
	}

	public int getNumHashes()
	{
		return numHashes;
	}

	public FM_HashFunction[] getHashFunctions()
	{
		return hashes;
	}

	public boolean[] getBitmap(int index)
	{
		return bitmaps[index];
	}

	public void setBitmap(int index, boolean[] b)
	{
		bitmaps[index] = b;
	}

	private void generateHashFunctions()
	{
		List<Pair> hashParameters = new LinkedList<Pair>();
		for (int i = 0; i < numHashes; i++)
		{
			hashes[i] = generateUniqueHashFunction(hashParameters, bitmapSize);
		}
	}

	private static FM_HashFunction generateUniqueHashFunction(List<Pair> params, int size)
	{
		// Generate odd number for m...
		int m = 0;
		do
		{
			m = (int) (Integer.MAX_VALUE * Math.random());
		}
		while (m % 2 == 0);

		// ...and n, checking that the new pair is unique
		int n = 0;
		do
		{
			n = (int) (Integer.MAX_VALUE * Math.random());
		}
		while ((n % 2 == 0) || exists(params, m, n));

		Pair newPair = new Pair(m, n);
		params.add(newPair);

		return new FM_HashFunction(m, n, size);
	}

	private static boolean exists(List<Pair> params, int m, int n)
	{
		Pair newPair = new Pair(m, n);
		for (Pair p : params)
		{
			if (newPair.equals(p))
				return true;
		}

		return false;
	}

	public static FM_HashFunction[] generateFMHashFunctions(int bitmapSize, int numHashes)
	{
		List<Pair> hashParameters = new LinkedList<Pair>();
		FM_HashFunction[] hashes = new FM_HashFunction[numHashes];

		for (int i = 0; i < numHashes; i++)
		{
			hashes[i] = generateUniqueHashFunction(hashParameters, bitmapSize);
		}

		return hashes;
	}

	public void update(long id)
	{
		for (int i = 0; i < numHashes; i++)
		{
			FM_HashFunction hashFunction = hashes[i];
			long v = hashFunction.hash(id);
			int bit = rho(v);

			if (!bitmaps[i][bit])
			{
				bitmaps[i][bit] = true;
			}
		}
	}

	private int rho(long v)
	{
		int rho = 0;
		for (int i = 0; i < bitmapSize; i++)
		{
			if ((v & 0x01) == 0)
			{
				v = v >> 1;
				rho++;
			}
			else
			{
				break;
			}
		}

		return rho == bitmapSize ? 0 : rho;
	}

	public long estimateCount()
	{
		boolean isFMempty = true;
		int sum = 0;

		for (int i = 0; i < numHashes; i++)
		{
			if (!isBitmapZero(bitmaps[i]))
				isFMempty = false;

			sum += (getFirstZeroBit(bitmaps[i]));
		}

		if (isFMempty)
			return 0;

		double r = sum * 1.0 / numHashes;

		return (long) (Math.pow(2, r) / PHI);
	}

	private static int getFirstZeroBit(boolean[] b)
	{
		for (int i = 0; i < b.length; i++)
		{
			if (b[i] == false)
			{
				return i;
			}
		}

		return b.length;
	}

	private static boolean isBitmapZero(boolean[] b)
	{
		for (int i = 0; i < b.length; i++)
		{
			if (b[i] == true)
			{
				return false;
			}
		}

		return true;
	}

	/** Bitwise-OR sum of secondSketch to this sketch */
	public void sum(FlajoletMartinSketch secondSketch)
	{
		if (secondSketch.getBitmapSize() != bitmapSize || secondSketch.getNumHashes() != numHashes)
		{
			System.out.println("Error, the two sketches have different sizes");
			return;
		}

		for (int i = 0; i < numHashes; i++)
		{
			boolean secondBitmap[] = secondSketch.getBitmap(i);

			for (int j = 0; j < bitmapSize; j++)
			{
				if (!bitmaps[i][j] && secondBitmap[j])
					bitmaps[i][j] = true;
			}
		}
	}
}

class Pair
{
	public final int m;
	public final int n;

	public Pair(final int m, final int n)
	{
		this.m = m;
		this.n = n;
	}

	public int getM()
	{
		return m;
	}

	public int getN()
	{
		return n;
	}

	public boolean equals(Object o)
	{
		Pair p = (Pair) o;
		return (this.m == p.getM() && this.n == p.getN());
	}
}
