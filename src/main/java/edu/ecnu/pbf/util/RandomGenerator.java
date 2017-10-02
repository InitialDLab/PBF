package edu.ecnu.pbf.util;

import java.util.Random;

public class RandomGenerator
{
	public static String getRandomString(int length)
	{
		String base = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++)
		{
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
