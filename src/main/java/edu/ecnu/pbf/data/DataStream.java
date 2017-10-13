package edu.ecnu.pbf.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import edu.ecnu.pbf.util.BinaryUtil2;
import edu.ecnu.pbf.util.MathUtil;
import edu.ecnu.pbf.util.RandomGenerator;

public class DataStream
{
	/**
	 * Generate data for streaming partition.
	 * 
	 * @param fileName
	 * @param universeNum
	 * @param lastTime
	 */
	public static void genData(String fileName, int universeNum, int itemNum, int lastTime,
			int dataPercent, int queryPercent)
	{
		int hotIndex = (int)(universeNum * ((double)dataPercent / 100) - 1);
		
		ArrayList<String> universeData = new ArrayList<String>();
		for (int i = 0; i < universeNum; i++)
		{
			universeData.add(RandomGenerator.getRandomString(10));
		}
		
		ArrayList<Integer> genTime = new ArrayList<Integer>();
		for (int i = 0; i < itemNum; i++)
		{
			genTime.add(MathUtil.getRandomIndex(0, lastTime));
		}
		Collections.sort(genTime);

		ArrayList<String> genData = new ArrayList<String>();
		
		for (int i = 0; i < itemNum; i++)
		{
			int itemIndex = 0;
			if (MathUtil.isHitPercent(queryPercent))
			{
				itemIndex = MathUtil.getRandomIndex(0, hotIndex);
			}
			else
			{
				itemIndex = MathUtil.getRandomIndex(hotIndex + 1, universeNum - 1);
			}
			
//			System.out.println(itemIndex);
			
			if (itemIndex < 0 || itemIndex >= universeNum)
			{
				continue;
			}
			else
			{
				genData.add(universeData.get(itemIndex) + " " + genTime.get(i));				
			}
		}

		FileWriter writer = null;
		try
		{
			writer = new FileWriter(fileName, false);
			// int recordNum = 2000000; // Integer.parseInt(args[1]);
			for (int i = 0; i < genData.size(); i++)
			{
				writer.append(genData.get(i));
				writer.append("\r\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
	}
	
	public static void genQueryStartTime(String fileName, int itemNum, int lastTime)
	{
		ArrayList<Integer> genTime = new ArrayList<Integer>();
		int sum = 0;
		while(true)
		{
			int randomTime = MathUtil.getRandomIndex(0, lastTime);
			if (BinaryUtil2.getBinaryDecomposition(randomTime, randomTime + 127).size() < 7)
			{
				genTime.add(randomTime);
				sum++;
				if (sum >= itemNum)
				{
					break;
				}
			}
		}
		Collections.sort(genTime);
		
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(fileName, false);
			// int recordNum = 2000000; // Integer.parseInt(args[1]);
			for (int i = 0; i < genTime.size(); i++)
			{
				writer.append(genTime.get(i) + " " + (genTime.get(i) + 127));
				writer.append("\r\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
		
	}
	
	public static void genQueryStartTimeTrick(String fileName, int itemNum, int lastTime, int g)
	{
		ArrayList<Integer> genTime = new ArrayList<Integer>();
		int sum = 0;
		while(true)
		{
			int randomTime = MathUtil.getRandomIndex(0, lastTime / g);
			int start = randomTime * g;
			genTime.add(start);
			sum++;
			if (sum >= itemNum)
			{
				break;
			}
		}
		Collections.sort(genTime);
		
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(fileName, false);
			// int recordNum = 2000000; // Integer.parseInt(args[1]);
			for (int i = 0; i < genTime.size(); i++)
			{
				writer.append(genTime.get(i) + " " + (genTime.get(i) + 127));
				writer.append("\r\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
		
	}

	public static void main(String[] args)
	{
//		String fileName = "d:/dataset/test.dat";
//		DataStream.genData(fileName, 100, 20, 100, 20, 80);
		String fileName2 = "d:/dataset/time.dat";
		DataStream.genQueryStartTime(fileName2, 20, 1000);
	}
}
