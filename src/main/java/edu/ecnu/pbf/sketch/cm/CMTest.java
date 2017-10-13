package edu.ecnu.pbf.sketch.cm;

public class CMTest
{
	public static void main(String[] args)
	{
		int depth = 10;
		int width = 1000000;
		int seed = 0;
		CountMinSketchImpl cm = new CountMinSketchImpl(depth, width, seed);
		
		for (int i = 0; i < 10000; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				cm.add(i);				
			}
		}
		
		for (int i = 0; i < 10; i++)
		{
			System.out.println(cm.estimateCount(i));			
		}
	}
}