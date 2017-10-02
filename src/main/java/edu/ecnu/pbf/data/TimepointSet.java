package edu.ecnu.pbf.data;

import java.util.ArrayList;

/**
 * This is a set class for Dataset, which is used to record 
 * the bytes and appearing time points for specific element.
 * @author Jinwei
 *
 */
public class TimepointSet
{
	private byte[] element;
	private ArrayList<Long> timeSet;
	
	public TimepointSet(byte[] element)
	{
		this.element = element;
		this.timeSet = new ArrayList<Long>();
	}

	public void addTimepoint(long timepoint)
	{
		this.timeSet.add(timepoint);
	}
	
	public byte[] getElement()
	{
		return this.element;
	}

	public ArrayList<Long> getTimeSet()
	{
		return this.timeSet;
	}
}
