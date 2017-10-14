package edu.ecnu.pbf.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.ecnu.pbf.CommonConstants;
import edu.ecnu.pbf.sketch.fm.HyperLogLog;
import edu.ecnu.pbf.util.PbfUtil;

/**
 * for granularity
 */
public class DatasetG
{
//	private HashMap<String, ArrayList<Long>> dataset;  // size() = n'
	private HashMap<String, TimepointSet> dataset;
	private HashSet<String> originalData;  // size() = n
	private ArrayList<HashSet<String>> levelSet;  // get(i).size() = di for beta2
	private ArrayList<HyperLogLog> levelSetSketch; // for beta-2
	private ArrayList<HashSet<String>> bfArray; // get(i).size() = d(i-1) for beta1
	private ArrayList<HyperLogLog> bfArraySketch; // for beta1;
	private int[] d; // di is the number of distinct elements in b(i-1) for beta1
	private int[] dSketch; // for beta1
	private int number;
	private int maxTimestmap;
	private int levelNum;
	private int gLevel;
	private int maxT;
	
	private int granularity;
	
	public DatasetG()
	{
		this.dataset = new HashMap<String, TimepointSet>();
		this.originalData = new HashSet<String>();
		this.levelSet = new ArrayList<HashSet<String>>();
		this.levelSetSketch = new ArrayList<HyperLogLog>();  // add
		for (int i = 0; i < CommonConstants.MAX_LEVEL_NUM; i++)
		{
			levelSet.add(new HashSet<String>());
			//levelSetSketch.add(HyperLogLog.builder().build());
			levelSetSketch.add(null);
		}
		this.number = 0;
		this.maxTimestmap = 0;
		this.levelNum = 0;
	}
	
	public DatasetG(int g, int granularity)
	{
		this.dataset = new HashMap<String, TimepointSet>();
		this.originalData = new HashSet<String>();
		this.levelSet = new ArrayList<HashSet<String>>();
		this.levelSetSketch = new ArrayList<HyperLogLog>();  // add 1013
		for (int i = 0; i < CommonConstants.MAX_LEVEL_NUM; i++)
		{
			levelSet.add(new HashSet<String>());
			levelSetSketch.add(HyperLogLog.builder().build());
//			levelSetSketch.add(null);
		}
		this.number = 0;
		this.maxTimestmap = 0;
		this.levelNum = 0;
		this.gLevel = 1;
		int tempG = g;
		while ((tempG = tempG / 2) > 0)
		{
			this.gLevel++;
		}
		
		this.granularity = granularity;
	}
	
	/**
	 * Load the data from file.
	 * @param fileName 
	 */
	public void loadFromFile(String fileName)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String tempStr = null;
			
			// get the max timestamp from the dataset
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
					timestamp = Long.parseLong(timestampStr) * granularity;
					// update the max timestamp T
					if (timestamp > this.maxTimestmap)
					{
						this.maxTimestmap = (int)timestamp;
					}
				}
			}
			reader.close();
			
			this.levelNum = PbfUtil.getLevelNum(maxTimestmap);
			this.maxT = CommonConstants.g[levelNum - 1] - 1;
			this.bfArray = new ArrayList<HashSet<String>>();
			this.bfArraySketch = new ArrayList<HyperLogLog>();  //1014
			int bfArraySize = 1 << (levelNum - gLevel + 1);
			for (int i = 0; i < bfArraySize; i++)
			{
				bfArray.add(new HashSet<String>());
				//bfArraySketch.add(HyperLogLog.builder().build());  // add by jinwei (20171013)
				bfArraySketch.add(null);
			}
			this.bfArraySketch.set(0, HyperLogLog.builder().build());  // 1014
			reader = new BufferedReader(new FileReader(fileName));
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
					timestamp = Long.parseLong(timestampStr) * granularity;
				}
				
				if (originalData.contains(tempStr))
				{
					// TODO
				}
				else
				{
					// add the original String of temporal pair
					originalData.add(tempStr);
					
					// record the temporal pair for the corresponding element
					TimepointSet timeSet = dataset.get(elementStr);
					if (timeSet == null)
					{
						TimepointSet newTimeSet = new TimepointSet(elementStr.getBytes());
						newTimeSet.addTimepoint(timestamp);
						dataset.put(elementStr, newTimeSet);
					}
					else
					{
						timeSet.addTimepoint(timestamp);
					}
					addToLevelSet(elementStr, timestamp);
					addToBfArray(elementStr, timestamp);
				}
				
				number++;
			}
			reader.close();
			
			d = new int[bfArraySize];
			dSketch = new int[bfArraySize];
			for (int i = 0; i < d.length; i++)
			{
				d[i] = bfArray.get(i).size();
				if (null == bfArraySketch.get(i))
				{
					dSketch[i] = 0;
				}
				else
				{
					dSketch[i] = (int)bfArraySketch.get(i).count();					
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Add an element to each level for counting the distinct number.
	 * This is for Beta2.
	 * @param elementStr
	 * @param timestamp
	 */
	public void addToLevelSet(String elementStr, long timestamp)
	{
		for (int i = 0; i < CommonConstants.MAX_LEVEL_NUM; i++)
		{
			String str = elementStr + (timestamp / CommonConstants.g[i]);
			this.levelSet.get(i).add(str);
			if (null == this.levelSetSketch.get(i))
			{
				this.levelSetSketch.set(i, HyperLogLog.builder().build());
			}
			this.levelSetSketch.get(i).addString(str); // 1014
		}
	}
	
	/**
	 * Add an element to each level.
	 * This is for Beta1.
	 * @param elementStr
	 * @param timestamp
	 */
	public void addToBfArray(String elementStr, long timestamp)
	{
		int index = 1;
		int start = 0;
		int end = this.maxT;
		for (int i = 0; i < levelNum - gLevel + 1; i++)
		{
			bfArray.get(index).add(elementStr);  
			if (null == bfArraySketch.get(index))  // 1014
			{
				bfArraySketch.set(index, HyperLogLog.builder().build());
			}
			bfArraySketch.get(index).addString(elementStr);  // 1014
			int mid = (start + end) / 2;
			if (mid < timestamp)
			{
				start = mid + 1;
				index = index * 2 + 1;
			}
			else
			{
				end = mid;
				index = index * 2;
			}
		}
		bfArray.get(0).add(elementStr + timestamp);
		bfArraySketch.get(0).addString(elementStr + timestamp);  //1014
	}
	
	public HashMap<String, TimepointSet> getDataset()
	{
		return this.dataset;
	}
	
	public HashSet<String> getOriginalData()
	{
		return this.originalData;
	}
	
	public int getMaxTimestamp()
	{
		return this.maxTimestmap;
	}
	
	public int getLevelNum()
	{
		return this.levelNum;
	}

	public ArrayList<HashSet<String>> getLevelSet()
	{
		return levelSet;
	}
	
	public ArrayList<HyperLogLog> getLevelSetSketch()
	{
		return levelSetSketch;
	}
	
	public int[] getD()
	{
		return d;
	}
	
	public int[] getDSketch()
	{
		return dSketch;
	}

	/**
	 * Get the total number of temporal pairs, i.e., N.
	 * @return
	 */
	public int size()
	{
		return this.number;
	}

	public static void main(String[] args)
	{
		String fileName = "d:/dataset/nw_dat";
		DatasetG dataset = new DatasetG(4, 1);
		dataset.loadFromFile(fileName);
		System.out.println(dataset.getDataset().size());
		System.out.println(dataset.size());
		System.out.println(dataset.getOriginalData().size());
		int[] d = dataset.getD();
		System.out.println(d[1]);
		System.out.println(d[2]);
		System.out.println(d[3]);
	}
}
