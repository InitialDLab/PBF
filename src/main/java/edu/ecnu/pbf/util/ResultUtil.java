package edu.ecnu.pbf.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class ResultUtil
{
	public static double handleResult(ArrayList<Double> resultArray, int start, int number)
	{
		double result = 0;
		Collections.sort(resultArray);
		int s = start > 0 ? start : 0;
		number = number > 0 ? number : 1;
		int e = s + number - 1;
		e = e < resultArray.size() ? e : resultArray.size() - 1;
		double sum = 0;
		for (int i = s; i <= e; i++)
		{
			sum = sum + resultArray.get(i);
		}
		result = sum / number;
		return result;
	}

	public static String handleDouble(double input, int decimalPlace)
	{
		String result = "";
		StringTokenizer tokenizer = new StringTokenizer(String.valueOf(input), ".");
		if (tokenizer.hasMoreTokens())
		{
			result = tokenizer.nextToken();
		}
		if (tokenizer.hasMoreTokens())
		{
			decimalPlace = decimalPlace > 0 ? decimalPlace : 0;
			String decimalStr = tokenizer.nextToken();
			decimalPlace = decimalPlace <= decimalStr.length() ? decimalPlace : decimalStr.length();
			result = result + "." + decimalStr.substring(0, decimalPlace);
		}
		return result;
	}

	public static void main(String[] args)
	{
		double a = 12.234567812;
		System.out.println(handleDouble(a, 0));
		System.out.println(handleDouble(a, 1));
		System.out.println(handleDouble(a, 2));
		System.out.println(handleDouble(a, 3));
		System.out.println(handleDouble(a, 4));
		System.out.println(handleDouble(a, 8));
		System.out.println(handleDouble(a, 9));
		System.out.println(handleDouble(a, 10));
		System.out.println(handleDouble(a, 20));

	}
}
