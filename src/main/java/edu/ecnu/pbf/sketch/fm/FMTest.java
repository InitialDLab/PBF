package edu.ecnu.pbf.sketch.fm;

public class FMTest
{
	public static void main(String[] args)
	{
//		FlajoletMartinSketch fm = new FlajoletMartinSketch(64, 1);
//		for (int i = 0; i < 1000; i++)
//		{
//			fm.update(i);
//		}
//		System.out.println(fm.estimateCount());
		
		FlajoletMartin fm = new FlajoletMartin(64, 16, 2);
		for (int i = 0; i < 2000000; i++)
		{
			fm.offer(i + "i");
		}
		System.out.println(fm.cardinality());
	}
}
