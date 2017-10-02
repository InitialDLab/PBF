package edu.ecnu.pbf.data;

public class TMTQuery
{
	private long start;
	private long end;
	private String elementStr;
//	private boolean isQuery;
//	private boolean result;
	
	public TMTQuery(String elementStr, long start, long end)
	{
		this.elementStr = elementStr;
		this.start = start;
		this.end = end;
	}

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getEnd()
	{
		return end;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}

	public String getElementStr()
	{
		return elementStr;
	}

	public void setElementStr(String elementStr)
	{
		this.elementStr = elementStr;
	}
}
