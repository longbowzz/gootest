package com.longbowzz.bptest;


import java.util.Random;

/**
 * 神经元细胞
 * @author zhangzhen
 *
 */
public class Neuron{
	public static final boolean DEBUG = false;
	/**
	 * 进入神经元输入的个数
	 */
	public int inputNum; 
	public double[] weightArray;
	
	public Neuron(int numInputs){
		Random rand = new Random();
		inputNum = numInputs + 1;
		weightArray = new double[inputNum];
		for(int i=0; i<inputNum; i++){
			if(DEBUG)
				weightArray[i] = 0.1f;
        	else
        		weightArray[i] = 1.0d - rand.nextDouble()*2.0d;	//-1~1	
			
		}
	}
	
}
