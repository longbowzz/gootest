package com.longbowzz.bptest;


import java.util.ArrayList;

/**
 * 神经细胞层
 * @author zhangzhen
 *
 */
public class NeuronLayer{
	/**
	 * 本层神经元个数
	 */
	public int neuronNum;
	/**
	 * 神经元列表
	 */
	public ArrayList<Neuron> neuronList;
	
	
	public NeuronLayer(int neuronCount, int inputCountPerNeuron){
		neuronNum = neuronCount;
		neuronList = new ArrayList<Neuron>();
		for(int i=0; i< neuronNum ; i++){
			neuronList.add(new Neuron(inputCountPerNeuron));
		}
	}
	
	
}