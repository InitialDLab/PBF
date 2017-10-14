package edu.ecnu.pbf.task;

import java.util.ArrayList;

public class Test
{
	public static void main(String[] args)
	{
		Integer a = null;
		Integer b = null;
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(a);
		list.add(b);
		list.add(null);
		
		Integer i = list.get(1);
		i = new Integer(3);
		System.out.println(list.size());
		System.out.println(list.get(0));
	}
}
