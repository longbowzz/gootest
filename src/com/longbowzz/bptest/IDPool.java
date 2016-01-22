package com.longbowzz.bptest;

public class IDPool{
	
	private static int maxID=0;
	
	public synchronized static int getID(){
		return maxID++;
	}
}