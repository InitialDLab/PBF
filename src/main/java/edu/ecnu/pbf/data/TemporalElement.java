package edu.ecnu.pbf.data;

public class TemporalElement
{
	private long timepoint;
	private byte[] element;
	
	public TemporalElement(long timepoint, byte[] elemnt)
	{
		this.timepoint = timepoint;
		this.element = elemnt;
	}

	public long getTimepoint()
	{
		return timepoint;
	}

	public byte[] getElement()
	{
		return element;
	}
	
}
