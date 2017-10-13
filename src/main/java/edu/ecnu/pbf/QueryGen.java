package edu.ecnu.pbf;

import edu.ecnu.pbf.data.DataStream;

public class QueryGen
{
	public static void main(String[] args)
	{
		int T = 28800; // 32768
		int partitionNum = 2;
		int totalT = 86400; //T * partitionNum;
		
		String queryFileName = "d:/dataset/1011/edgar-query-86400";
		DataStream.genQueryStartTime(queryFileName, 400000, totalT - 130);
	}
}
