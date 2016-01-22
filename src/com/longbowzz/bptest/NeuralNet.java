package com.longbowzz.bptest;


import java.util.ArrayList;


/**
 * 神经网络类
 * @author zhangzhen
 *
 */
public class NeuralNet{
	public static final double BIAS = 1.0d; //偏移值
	public static final double RESPONSE = 1.0d; //s响应系数
	
	
	private int inputNum;
	private int outputNum;
	private int hiddenLayerNum;
	private int neuronPerHidenLayer;
	
	private int totalWeightNum;
	private  double rate = 0.01d;
	
	private ArrayList<NeuronLayer> layers;
	
	/**
	 * 
	 * @param input
	 * @param output
	 * @param layernum
	 * @param neuPerLayer
	 */
	public NeuralNet(int input, int output, int layernum, int neuPerLayer, double r){
		inputNum = input;
		outputNum = output;
		hiddenLayerNum = layernum;
		neuronPerHidenLayer = neuPerLayer;
		rate = r;
		createNet();
	}
	
	/**
	 * 创建神经网络
	 */
	private void createNet(){
		layers = new ArrayList<NeuronLayer>();
		totalWeightNum = 0;
		if(hiddenLayerNum>0){
			//创建第一个隐藏层
			layers.add(new NeuronLayer(neuronPerHidenLayer, inputNum));
			totalWeightNum += neuronPerHidenLayer*(inputNum+1);  //包含bias
			//创建其他隐藏层
			for(int i=1 ; i<hiddenLayerNum ;i++){
				layers.add(new NeuronLayer(neuronPerHidenLayer, neuronPerHidenLayer));
				totalWeightNum += neuronPerHidenLayer*(neuronPerHidenLayer+1);
			}
			//创建输出层
			layers.add(new NeuronLayer(outputNum, neuronPerHidenLayer));
			totalWeightNum += outputNum*(neuronPerHidenLayer+1);
		}else{ //无隐藏层，直接创建输出层
			layers.add(new NeuronLayer(outputNum, inputNum));
			totalWeightNum += outputNum*(neuronPerHidenLayer+1);
		}
		
	}
	
	/**
	 * 读取当前所有权重， 需编码
	 * @return
	 */
	public double[] getWeights(){
		int idx = 0;
		double[] weights = new double[totalWeightNum];
		for(int i=0 ;i<layers.size(); i++){
			NeuronLayer layer = layers.get(i);
			for(int j=0; j<layer.neuronNum; j++){
				Neuron neu = layer.neuronList.get(j);
				for(int k=0; k<neu.inputNum; k++){
					weights[idx++] = neu.weightArray[k];
				}
			}
		}
		return weights;
	}
	
	/**
	 * 获取权重总数
	 * @return
	 */
	public int getWeightNum(){
//		int sum = 0;
//		for(int i=0 ;i<hiddenLayerNum; i++){
//			NeuronLayer layer = layers.get(i);
//			for(int j=0; j<layer.neuronNum; j++){
//				Neuron neu = layer.neuronList.get(j);
//				sum += neu.inputNum;
//			}
//		}
//		
//		return sum;
		return totalWeightNum;
	}
	
	/**
	 * 用新权重代替老权重
	 * @param weights
	 */
	public void putWeights(double[] weights){
		int idx = 0;
		for(int i=0 ;i<layers.size(); i++){
			NeuronLayer layer = layers.get(i);
			for(int j=0; j<layer.neuronNum; j++){
				Neuron neu = layer.neuronList.get(j);
				for(int k=0; k<neu.inputNum; k++){
					 neu.weightArray[k]=weights[idx++];
				}
			}
		}		
	}
	
	
	public void dumpAllWeights(){
		StringBuilder sb = new StringBuilder();
		for(int i=0 ;i<layers.size(); i++){
			NeuronLayer layer = layers.get(i);
			sb.append("Layer "+i+": ");
			for(int j=0; j<layer.neuronNum; j++){
				Neuron neu = layer.neuronList.get(j);
				sb.append("[");
				for(int k=0; k<neu.inputNum; k++){
					sb.append(neu.weightArray[k]+",");
				}
				sb.append("] ");
			}
			sb.append("\n");
		}
//		Log.e("GNM673", sb.toString());
		System.out.print(sb.toString());
	}
	
	
	
	/**
	 * S型响应曲线
	 * @param activation
	 * @param response
	 * @return
	 */
	public double sigmoid(double activation, double response){
		
		return 1.0d / (1.0d + Math.exp(-1.0d*activation/response));
	}
	
	/**
	 * 根据一组输入确定输出
	 * @param input
	 * @return
	 */
	public double[] update(double[] input){
		//保存每层产生的输出
		double[] output = new double[neuronPerHidenLayer];
		//检测输入是否合法
		if(input.length != inputNum){
			return null;
		}
		
		for(int i=0; i<layers.size();i++){
			if(i>0){
				input = output;
				output = new double[layers.get(i).neuronNum];
			}
			//对每个神经元
			for(int j=0; j<layers.get(i).neuronNum; j++){
				double netinput =0;
				Neuron neu = layers.get(i).neuronList.get(j);
				int inputnum = neu.inputNum;
				//对每一个权重
				for(int k=0; k<inputnum-1; k++){
					netinput += input[k]*neu.weightArray[k];
				}
				//加入偏移值
				netinput += BIAS*neu.weightArray[inputnum-1];
				output[j] = sigmoid(netinput, RESPONSE);
			}
		}
		
		return output;
	}
	
	/**
	 * 训练一次，返回本次误差
	 * @param input
	 * @param expectedoutput
	 * @return
	 */
	public double trainOnce(double input[], double expectedoutput[]){
		//先执行一次update，保存中间每一级输出
		//保存每层产生的输出
		double[] origininput = input;
		double[] output = new double[neuronPerHidenLayer];
		double[][] tempout = new double[hiddenLayerNum][neuronPerHidenLayer]; //缓存每一层输出结果
		//检测输入是否合法
		if(input.length != inputNum){
			return 0;
		}
		
		for(int i=0; i<layers.size();i++){
			if(i>0){
				input = output;
				output = new double[layers.get(i).neuronNum];
			}
			//对每个神经元
			for(int j=0; j<layers.get(i).neuronNum; j++){
				double netinput =0;
				Neuron neu = layers.get(i).neuronList.get(j);
				int inputnum = neu.inputNum;
				//对每一个权重
				for(int k=0; k<inputnum-1; k++){
					netinput += input[k]*neu.weightArray[k];
				}
				//加入偏移值
				netinput += BIAS*neu.weightArray[inputnum-1];
				output[j] = sigmoid(netinput, RESPONSE);
				if(i<hiddenLayerNum)
					tempout[i][j] = output[j];
			}
		}
		
		if(Neuron.DEBUG){
			//打印每层输出
			//隐藏层输出
			for(int i=0;i<tempout.length;i++){
				System.out.println("-----隐藏层 #"+i+"输出-----");
				for(int j=0;j<tempout[i].length;j++){
					System.out.print(tempout[i][j]+",");
				}
				System.out.println();
			}
			System.out.println("-----输出层输出 -----");
			for(int j=0;j<output.length;j++){
				System.out.print(output[j]+",");
			}
			System.out.println();
			
		}
		
		
		
		double[] wcout = new double[output.length];  //dim
		double[][] wchide = new double[hiddenLayerNum][neuronPerHidenLayer];		//dik
		//权值调整
		//对于输出层k=m  有  di m =Xi m (1-Xi m )(Xi m -Yi )
		//对于普通层有： dik = xik(1-xik)sum(wi*d(k+1))
		double dis=0;  //本样本整体误差
		//计算每个输出单元的误差项
		for(int i=0; i<output.length;i++){
			wcout[i] = output[i]*(1-output[i])*(expectedoutput[i]-output[i]); //-dim
			dis+=Math.pow(expectedoutput[i]-output[i], 2);
		}
		//计算最后一个隐藏层，每个隐藏单元的误差项
		NeuronLayer layer = layers.get(layers.size()-2);
		NeuronLayer outlayer = layers.get(layers.size()-1);
		for(int j=0; j<layer.neuronNum; j++){
			double wche=0;
			for(int k=0; k<outlayer.neuronNum;k++){	//sum(wli*dl(k+1))
				wche += outlayer.neuronList.get(k).weightArray[j]*wcout[k];
			}
			wchide[hiddenLayerNum-1][j] = tempout[hiddenLayerNum-1][j]*(1-tempout[hiddenLayerNum-1][j])*wche;
			if(Neuron.DEBUG)
				System.out.println("wchide["+(hiddenLayerNum-1)+"]["+j+"]="+wchide[hiddenLayerNum-1][j]);
		}		
		
		
		//计算其余每个隐藏层，每个隐藏单元的误差项
		for(int i=hiddenLayerNum-2; i>=0 ; i--){
			NeuronLayer thislayer = layers.get(i);
			NeuronLayer nextLayer = layers.get(i+1);
			for(int j=0; j<thislayer.neuronNum; j++){ //thislayer.neuronNum should equal to nextLayer.neuronNum
				double wche=0;
				for(int k=0; k<nextLayer.neuronNum;k++){	//sum(wli*dl(k+1))
					wche += nextLayer.neuronList.get(k).weightArray[j]*wchide[i+1][k];
				}
				wchide[i][j] = tempout[i][j]*(1-tempout[i][j])*wche;
			}
		}
		
		//改变输出层权值   Wij(t+1) = Wij(t) - rate*dik*xj(k-1)
		for(int i=0; i<outlayer.neuronNum;i++){
			Neuron neu = outlayer.neuronList.get(i);
			for(int  j=0; j<neu.weightArray.length-1;j++){
				neu.weightArray[j]+=rate* wcout[i]*tempout[hiddenLayerNum-1][j];
			}
			neu.weightArray[neu.weightArray.length-1] = rate*wcout[i]*BIAS; //要不要*-1？
		}
		//改变各隐藏层权值
		for(int i=hiddenLayerNum-1; i>0 ; i--){
			NeuronLayer thislayer = layers.get(i);
			NeuronLayer upperLayer = layers.get(i-1);
			for(int j=0; j<thislayer.neuronNum;j++){
				Neuron neu = thislayer.neuronList.get(j);
				for(int k=0; k<neu.weightArray.length-1;k++){
					neu.weightArray[k]+=rate* wchide[i][j]*tempout[i-1][j];
				}
				neu.weightArray[neu.weightArray.length-1] = rate*wchide[i][j]*BIAS;
			}			
		}
		
		//改变输入隐藏层权值
		NeuronLayer thislayer = layers.get(0);
		for(int i=0; i<thislayer.neuronNum;i++){
			Neuron neu = thislayer.neuronList.get(i);
			for(int  j=0; j<neu.weightArray.length-1;j++){
//				if(Neuron.DEBUG){
//					System.out.println("beforeupdate-weightArray["+j+"]="+neu.weightArray[j]+",wchide="+wchide[0][i]+",input="+origininput[j]);
//				}
				neu.weightArray[j]+=rate* wchide[0][i]*origininput[j];
//				if(Neuron.DEBUG){
//					System.out.println("neu["+i+"]。weightArray["+j+"]="+neu.weightArray[j]);
//				}
			}
			neu.weightArray[neu.weightArray.length-1] = rate*wchide[0][i]*BIAS; 
		}	
		
		return dis;
	}
	
	
}