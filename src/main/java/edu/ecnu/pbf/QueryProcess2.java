package edu.ecnu.pbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.StringTokenizer;

public class QueryProcess2
{
	public static void main(String[] args)
	{
		HashSet<String> set = new HashSet<String>();
		String inputFileName = "d:/dataset/1013/wc_qry";
		String outputFileName = "d:/dataset/1013/wc-qry-100000-new";
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
			FileWriter writer = new FileWriter(outputFileName, false);;
			String tempStr = null;
			int d = 6;
			while ((tempStr = reader.readLine()) != null)
			{
				tempStr.trim();
				
				StringTokenizer tokenizer = new StringTokenizer(tempStr);
				String startTime = null;
				String endTime = null;
				long firstTimestamp = -1;
				long secondTimestamp = -1;
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
					secondTimestamp = Long.parseLong(endTime);
					// update the max timestamp T
					if (firstTimestamp >= 200000)//86200)//14400 - 130)//if (timestamp >= 57600) 32768
					{
						continue;
					}
//					else if (timestamp > 32768 - 130 && timestamp < 32768)
//					{
//						continue;
//					}
					else
					{
//						writer.append(tempStr);
//						writer.append("\r\n");
//						writer.append(tempStr);
//						writer.append("\r\n");
						writer.append((firstTimestamp - 1) + " " + (secondTimestamp - 1));
						writer.append("\r\n");
						writer.append((firstTimestamp - 1) + " " + (secondTimestamp - 1));
						writer.append("\r\n");
						
//						if (firstTimestamp > 200 && firstTimestamp < 86200)
//						{
//							for (int i = 1; i <= d; i++)
//							{
//								writer.append((firstTimestamp - 1 + 86016 * i) + " " + (secondTimestamp - 1 + 86016 * i));
//								writer.append("\r\n");
//								writer.append((firstTimestamp - 1 + 86016 * i) + " " + (secondTimestamp - 1 + 86016 * i));
//								writer.append("\r\n");								
//							}
//						}
						
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
