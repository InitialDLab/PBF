package edu.ecnu.pbf.util;

public class MathUtil
{
	public static int gcd(int a, int b)
	{
		while (b != 0)
		{
			int temp = a % b;
			a = b;
			b = temp;
		}
		return a;
	}
	
	public static int getRandomIndex(int start, int end)
	{
		return start + (int)(Math.random() * (end - start + 1));
	}
	
	public static boolean isHitPercent(int percent)
	{
		boolean result = false;
		int randomPercent = (int) (Math.random() * 100);
		if (randomPercent < percent)
		{
			result = true;
		}
		
		return result;
	}
	
	public static void main(String[] args)
	{
		for (int i = 0; i < 100; i++)
		{
			System.out.println(isHitPercent(2));
		}
		int[] a = getGeometricSequence(100, 4, 1.4);
		for (int i = 0; i < a.length; i++)
		{
			System.out.println(a[i]);
		}
		System.out.println(gcd(0, 100));
	}
	
	public static int[] getGeometricSequence(int sum, int length, double radio)
	{
		int[] result = new int[length];
		int a = (int)((double)sum * (1D - radio) / (1D - Math.pow(radio, length)));
		for (int i = 0; i < length; i++)
		{
			result[i] = a;
			a = (int)(a * radio);
		}
		return result;
	}
}
