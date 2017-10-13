package edu.ecnu.pbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.StringTokenizer;

public class QueryProcess
{
	public static void main(String[] args)
	{
		HashSet<String> set = new HashSet<String>();
		String inputFileName = "d:/dataset/1011/edgar-query-86400";
		String outputFileName = "d:/dataset/1011/edgar-query-14400";
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
			FileWriter writer = new FileWriter(outputFileName, false);;
			String tempStr = null;
			while ((tempStr = reader.readLine()) != null)
			{
				tempStr.trim();
				
				StringTokenizer tokenizer = new StringTokenizer(tempStr);
				String startTime = null;
				String endTime = null;
				long firstTimestamp = -1;
				if (null == (startTime = tokenizer.nextToken()))
				{
					continue;
				}
				else if (null == (endTime = tokenizer.nextToken()))
				{
					continue;
				}
				else
				{
					firstTimestamp = Long.parseLong(startTime);
					// update the max timestamp T
					if (firstTimestamp >= 14400 - 130)//if (timestamp >= 57600) 32768
					{
						break;
					}
//					else if (timestamp > 32768 - 130 && timestamp < 32768)
//					{
//						continue;
//					}
					else
					{
						writer.append(tempStr);
						writer.append("\r\n");
						set.add(tempStr);
					}
				}
			}
			
			System.out.println("distinct number: " + set.size());
			reader.close();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
