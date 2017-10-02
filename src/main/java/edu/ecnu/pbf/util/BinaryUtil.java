package edu.ecnu.pbf.util;

import java.util.ArrayList;
import java.util.HashMap;

public class BinaryUtil
{
	public static final int[] bitMask = { 0xffffffff, 0xfffffffe, 0xfffffffc,
			0xfffffff8, 0xfffffff0, 0xffffffe0, 0xffffffc0, 0xffffff80,
			0xffffff00, 0xfffffe00, 0xfffffc00, 0xfffff800, 0xfffff000,
			0xffffe000, 0xffffc000, 0xffff8000, 0xffff0000, 0xfffe0000,
			0xfffc0000, 0xfff80000, 0xfff00000, 0xffe00000, 0xffc00000,
			0xff800000, 0xff000000, 0xfe000000, 0xfc000000, 0xf8000000,
			0xf0000000, 0xe0000000, 0xc0000000, 0x80000000, 0x00000000 };

	public static final int[] levelSize = { 0, 1 << 0, 1 << 1, 1 << 2, 1 << 3,
			1 << 4, 1 << 5, 1 << 6, 1 << 7, 1 << 8, 1 << 9, 1 << 10, 1 << 11,
			1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17, 1 << 18,
			1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25,
			1 << 26, 1 << 27, 1 << 28, 1 << 29, 1 << 30 };

	public static HashMap<Integer, Integer> getBinaryDecomposition(int start,
			int end)
	{
		HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
		for (int i = start; i <= end;)
		{
			int level = getBinaryLevel(i, end);
			resultMap.put(i, level);
			i = i + (1 << (level - 1));
		}
		return resultMap;
	}

	public static int getBinaryLevel(int startKey, int endKey)
	{
		int level = 1;
		if (startKey == endKey)
		{
			level = 1;
		}
		else if (0 == startKey)
		{
			int size = endKey - startKey + 1;
			int start = 1;
			int end = 31;
			int middle = (start + end) / 2;
			while (start < end)
			{
				if (levelSize[middle] < size)
				{
					start = middle + 1;
				}
				else
				{
					end = middle;
				}
				middle = (start + end) / 2;
			}
			level = middle;
		}
		else
		{
			int start = 1;
			int end = 32;
			int middle = (start + end) / 2;
			while (start < end)
			{
				if (0 == (startKey & ~bitMask[middle]))
				{
					start = middle + 1;
				}
				else
				{
					end = middle;
				}
				middle = (start + end) / 2;
			}
			level = middle;
			while ((startKey + (1 << (level - 1))) > (endKey + 1))
			{
				level--;
			}
		}
		return level;
	}

	/**
	 * Get starting time points of {@code level}'s dyadic ranges which cover
	 * [startTime, endTime]
	 * 
	 * @param startTime
	 * @param endTime
	 * @param level
	 * @return
	 */
	public static ArrayList<Long> getStartKey(long startTime, long endTime,
			int level)
	{
		ArrayList<Long> startKey = new ArrayList<Long>();
		if (30 < level || 0 > level)
		{

		}
		else
		{
			long i = startTime / levelSize[level + 1];
			long startKeyTemp = i * levelSize[level + 1];
			while (startKeyTemp <= endTime)
			{
				startKey.add(startKeyTemp);
				startKeyTemp += levelSize[level + 1];
			}
		}
		return startKey;
	}

	public static void main(String[] args)
	{
		HashMap<Integer, Integer> map = getBinaryDecomposition(87296, 88319);
		System.out.println(map);
		
		long start = System.nanoTime();
		for (int i = 0; i < 10000000; i++)
		{
			getBinaryDecomposition(i * 3, i * 3 + 127);
		}
		
		long end = System.nanoTime();
		System.out.println("time: " + (end - start) / 1000);
	}
}