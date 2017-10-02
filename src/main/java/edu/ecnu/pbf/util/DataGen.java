package edu.ecnu.pbf.util;

import java.io.FileWriter;
import java.io.IOException;

public class DataGen
{
	public static void GenStringToFile(String filePrefix, int fileNum,
			int strLength, int fileSize)
	{
		for (int i = 1; i <= fileNum; i++)
		{
			String outFileName = filePrefix + i;// args[0];
			FileWriter writer = null;

			try
			{
				writer = new FileWriter(outFileName, false);
				String tempString = null;
				//int recordNum = 2000000; // Integer.parseInt(args[1]);
				for (int start = 0; start < fileSize; start++)
				{
					tempString = RandomGenerator.getRandomString(strLength);
					writer.append(tempString);
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
		
	}
}
