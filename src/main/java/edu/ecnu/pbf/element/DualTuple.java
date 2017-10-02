package edu.ecnu.pbf.element;

public class DualTuple
{
	private int first;
	private int second;
	
	public DualTuple(int first, int second)
	{
		this.first = first;
		this.second = second;
	}
	
	public int getFirst()
	{
		return this.first;
	}
	
	public int getSecond()
	{
		return this.second;
	}
	
	public String toString()
	{
		return "(" + first + ","  + second + ")";
	}
}
