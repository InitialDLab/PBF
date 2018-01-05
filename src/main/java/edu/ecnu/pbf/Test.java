package edu.ecnu.pbf;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Test
{
	public static void main(String[] args)
	{
//		System.out.println(Math.log((double) 1 - Math.pow(0.95, ((double) 1 / (10 + 2 * 1)))));//i)))));
//
//		StandardDeviation sd = new StandardDeviation();
//		double[] a = { 1, 2, 3, 1 };
//		double[] b = { 1, 90, 2, 1 };
//		sd.setData(a);
//		System.out.println(sd.evaluate(a, (double)100, 0, 4));
		
		for (int i = 2000; i <= 10000; i += 1000)
		{
			System.out.print(i + " ");
			System.out.println((double)20000 / i);
		}
		
	}
}
