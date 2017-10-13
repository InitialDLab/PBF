package edu.ecnu.pbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Edgar
{
	public static void main(String[] args)
	{
		HashSet<String> set = new HashSet<String>();
		String inputFileName = "d:/dataset/1011/edgar";
		String outputFileName = "d:/dataset/1011/edgar-14400";
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
			FileWriter writer = new FileWriter(outputFileName, false);;
			String tempStr = null;
			while ((tempStr = reader.readLine()) != null)
			{
				tempStr.trim();
				
				StringTokenizer tokenizer = new StringTokenizer(tempStr);
				String elementStr = null;
				String timestampStr = null;
				long timestamp = -1;
				if (null == (elementStr = tokenizer.nextToken()))
				{
					continue;
				}
				else if (null == (timestampStr = tokenizer.nextToken()))
				{
					continue;
				}
				else
				{
					timestamp = Long.parseLong(timestampStr);
					// update the max timestamp T
					if (timestamp >= 14400)//if (timestamp >= 57600) 32768
					{
						break;
					}
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
