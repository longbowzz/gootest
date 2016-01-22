package com.longbowzz.bptest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



/**
 * 学习手工训练的结果
 * @author zhangzhen
 *
 */
public class MemTrainMode{
	
	static boolean TESTMODE = false;
	static String TEST_WEIGHT_FILE = "S01_MEM_BLK_humanTrained.dat";
	
	static String PRE_WEIGHT_FILE = null; //在上一次的基础上继续学习。 如果为空则直接学习
	static String PLAYER = MemSpecialWarStudy.PLAYER;
	static String WEIGHT_FILE = PLAYER+"humanTrained.dat";
	static File studyInputfile=new File(SpecialGooWar.OUTPUT_PATH,PLAYER+"study.input");
	static File studyOutputfile=new File(SpecialGooWar.OUTPUT_PATH,PLAYER+"study.output");
	

	static int TRAIN_COUNT = 10000;

	
    public static void main(String args[]){
    	MemGoo goo = new MemGoo(0, 0, 0);
    	NeuralNet net = goo.net;
    	
    	try {
			ArrayList<String> inputs = ReadSampleData(studyInputfile);
			ArrayList<String> outputs = ReadSampleData(studyOutputfile);
			
			if(TESTMODE){ //测试模式预初始化神经网络
				net.putWeights(SpecialGooWar.loadWeights(SpecialGooWar.OUTPUT_PATH+TEST_WEIGHT_FILE));
			}else if(PRE_WEIGHT_FILE!= null){
				net.putWeights(SpecialGooWar.loadWeights(SpecialGooWar.OUTPUT_PATH+PRE_WEIGHT_FILE));
			}
			
			int SAMPLE_COUNT = inputs.size();
			
			double[][] sample_input = new double[SAMPLE_COUNT][goo.attN];
			double[][] sample_output = new double[SAMPLE_COUNT][goo.outN];
			
			for(int i=0; i<SAMPLE_COUNT; i++){
				String[] inStr = inputs.get(i).split(",");
				String[] outStr =outputs.get(i).split(",");
				if(inStr.length!=goo.attN || outStr.length!= goo.outN){
					System.out.println("INPUT or OUT error format!!!");
					throw new IOException();
				}else{
					for(int j=0; j<inStr.length; j++){
						sample_input[i][j] = Double.parseDouble(inStr[j]);						
					}
					
					if(!TESTMODE){
						for(int j=0; j<outStr.length; j++){
							sample_output[i][j] = Double.parseDouble(outStr[j]);						
						}						
					}
				}
			}

			if(TESTMODE){//测试打印结果
				for(int i=0; i<SAMPLE_COUNT; i++){
					sample_output[i] = net.update(sample_input[i]);
					for(int j=0; j<goo.outN;j++){
						System.out.print(sample_output[i][j]+"\t");						
					}
					System.out.print("\n");	
				}
				
			}else{ //训练模式
				double dis = 0;	//训练误差
				//开始学习样本
				for(int i=0; i<TRAIN_COUNT ; i++){
					for(int j=0;j<SAMPLE_COUNT;j++){
						dis = net.trainOnce(sample_input[j], sample_output[j]);
						if(Neuron.DEBUG){
							System.out.println("----------------------Train #"+i+"sample#"+j+", dis= "+dis);
							net.dumpAllWeights();					
						}
						
					}
					if(i%100==0)
						System.out.println("Train #"+i+", dis= "+dis);
					if(dis<=1.0E-6) //训练终止条件
						break;
				}
				System.out.println("NET训练后系数：");
				net.dumpAllWeights();    	
				
				//输出训练后系数
				try {
					SpecialGooWar.saveWeights(SpecialGooWar.OUTPUT_PATH+WEIGHT_FILE, net.getWeights());
					net.putWeights(SpecialGooWar.loadWeights(SpecialGooWar.OUTPUT_PATH+WEIGHT_FILE)); //再读回来，纯测试用
					System.out.println("校准读写系数：");
					net.dumpAllWeights();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//输出结果
				for(int i=0; i<SAMPLE_COUNT; i++){
					for(int j=0; j<goo.outN;j++){
						System.out.print(sample_output[i][j]+"\t");						
					}
					System.out.print("\n");						
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
		
    
    
    
    public static ArrayList<String> ReadSampleData(File file) throws IOException{
        if(!file.exists()||file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br=new BufferedReader(new FileReader(file));
        String temp=null;
        ArrayList<String> lines = new ArrayList<String>();

        temp=br.readLine();
        while(temp!=null){
           
        	lines.add(temp);
            temp=br.readLine();
        }
        return lines;
    }     
	
}