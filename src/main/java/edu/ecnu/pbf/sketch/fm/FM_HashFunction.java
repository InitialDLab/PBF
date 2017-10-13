package edu.ecnu.pbf.sketch.fm;

public class FM_HashFunction
{
	public static long P = ((1 << 31) - 1);
	private int m;
	private int n;
	private int bitmapSize;

	public FM_HashFunction(int m, int n, int bs) {
		this.m = m;
		this.n = n;
		bitmapSize = bs;
		
		if (bitmapSize > 64) {
			throw new IllegalArgumentException("Bitmap size should be at most 64");
		}
	}

	public long hash(long id) {
		return ((m * id + n) % P);
	}
}
