package com.longbowzz.bptest;

import java.awt.Color;
import java.awt.Graphics;

public class Ship{
	//input:  自身坐标，自身速度 ， 月球位置， 月球速度， 剩余燃料， 剩余生命
	//output: 四指令发动机
	private static int hidN =50;	//每层节点个数
	private static int  attN =10;	//输入节点个数
	private static int  outN =4;	//输出节点个数
	private static int  layerN = 1; //隐藏层层数
	private static int  SAMPLE_COUNT = 0;
	private static int  TRAIN_COUNT = 1000000;
	private static int  TEST_COUNT = 0;
	private static double RATE = 0.02;	//迭代步长
	
	private double[] input = new double[attN];
	private double[] output = new double[outN];

	
	private static double MUTANT_STEP = 0.1d;
	private static double MUTANT_RATE = 0.5d;
	private static final int MAX_LIFE = 2000;
	private static final int MAX_FEUL = 100;
	
	public Ball ship;
	public int ID;
	public int parentID;
	public int ancestorID;
	public int generation; //代
	/**
	 * 生命耗尽即死亡
	 */
	public int life;
	/**
	 * 燃料耗尽即死亡
	 */
	public int fuel;
	public float score;
	
	
	public NeuralNet net;
	
	public double[] gene; //用来储存基因
	
	public Ship(){
		
	}
		
	
	/**
	 * 构造祖代
	 * @param id
	 * @param initball
	 */
	public Ship(int id, Ball earth, int scale , double offset){
		ID = id;
		parentID = id;
		ancestorID = id;
		generation = 0;
		ship = new Ball(1,  0.01,    offset+earth.sx, 23.0*scale*2+earth.sy, 10.0+earth.vx, 0.0+earth.vy,getShipColor(ancestorID)); ;
		fuel = MAX_FEUL; //初始燃料
		life = MAX_LIFE;
		net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		gene = net.getWeights();
		
	}
	
	/**
	 * 完全复制
	 * @param oldship
	 */
	public Ship(Ship parent,Ball earth, int scale , double offset){
		ID  = parent.ID;
		parentID = parent.parentID;
		ancestorID = parent.ancestorID;
		generation = parent.generation;
//		ship = parent.ship;
		ship = new Ball(1,  0.01,    offset+earth.sx, 23.0*scale*2+earth.sy, 10.0+earth.vx, 0.0+earth.vy, getShipColor(ancestorID)); 
		fuel = MAX_FEUL; //初始燃料
		life = MAX_LIFE;
		net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		net.putWeights(parent.gene);
		gene = net.getWeights();
		score = 0;
	}
	
	
	/**
	 * 单性繁殖
	 * @param id
	 * @param parent
	 */
	public Ship(int id, Ship parent,Ball earth, int scale , double offset){
		ID = id;
		parentID = parent.ID;
		ancestorID = parent.ancestorID;
		generation = parent.generation+1;
		ship = new Ball(1,  0.01,    offset+earth.sx, 23.0*scale*2+earth.sy, 10.0+earth.vx, 0.0+earth.vy, getShipColor(ancestorID)); ;
		fuel = MAX_FEUL; //初始燃料
		life = MAX_LIFE;
		net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		if(Math.random()<MUTANT_RATE){
			if(parent.gene == null)
				System.out.println("parent.gene = null");
			gene = mutant(parent.gene);
			net.putWeights(gene);
		}else{
			if(parent.gene == null)
				System.out.println("parent.gene = null");
			net.putWeights(parent.gene);
			gene = net.getWeights();
		}
	}
	
	
	public Ship getClone(){
		Ship clone = new Ship();
		clone.ID  = ID;
		clone.parentID = parentID;
		clone.ancestorID = ancestorID;
		clone.generation = generation;
		clone.ship = ship;
		clone.fuel = fuel;
		clone.life = life;
		clone.net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		clone.net.putWeights(gene);
		clone.gene = net.getWeights();
		clone.score = score;
		return clone;
		
	}
	
	/**
	 * 基因突变函数,选择5%的点进行编译; 编译尺度MUTANT_STEP;
	 * @param gene
	 * @return
	 */
	private double[] mutant(double[] gene){
		int len = gene.length;
		int mutantCnt = (int) (len*0.05f);
		if(mutantCnt==0)mutantCnt=1;
		
		for(int i=0; i<mutantCnt; i++){
			int pos = (int) (Math.random()*len);
			gene[pos]= gene[pos] + (0.5-Math.random())*MUTANT_STEP;
		}
		
		return gene;
		
	}
	
	public void setInput(double moonx,double moony,double moonvx, double moonvy, int w, int h, int scale){
		//input:  自身坐标xy，自身速度xy ， 月球位置xy， 月球速度xy， 剩余燃料， 剩余生命
		double wmax= scale*w/2;
		double hmax= scale*h/2;
		input[0] = normalize(ship.sx,-wmax,wmax);
		input[1] = normalize(ship.sy, -hmax, hmax);
		input[2] = normalize(ship.vx,-200,200);
		input[3] = normalize(ship.vy,-200,200);
		input[4] = normalize(moonx,-wmax,wmax);
		input[5] = normalize(moony, -hmax, hmax);
		input[6] = normalize(moonvx,-200,200);
		input[7] = normalize(moonvy,-200,200);
		input[8] = normalize(fuel, 0, 100);
		input[9] = normalize(life, 0, MAX_LIFE);
	}

	public double[] tryOnce(){
		return net.update(input);	
	}
	

	   /**
     * 归一化 0~1
     * @param value
     * @param min
     * @param max
     * @return
     */
    private static double normalize(double value, double min, double max){
    	double maxdiff = max-min;
    	return (value-min)/maxdiff;
    }
    
    /**
     * 反归一化 0~1
     * @param value
     * @param min
     * @param max
     * @return
     */
    private static double denormalize(double value, double min, double max){
    	double maxdiff = max-min;
    	return value*maxdiff+min;
    }
    
    
    
    
    void drawAncestor(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ship.c);
		g.drawString(""+ancestorID,ship.x+ship.size/2+2, ship.y+ship.size/2+10);	  	
}
	
    void drawID(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ship.c);
		g.drawString(""+ID,ship.x+ship.size/2+2, ship.y-ship.size/2+10);
		
    }	
    
    void drawParent(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ship.c);
		g.drawString(""+parentID,ship.x+ship.size/2+2, ship.y+ship.size/2+10);	  	
    }
    
    public static Color getShipColor(int id){
    	int R = 127+id%127;
    	int G = (id/255)%255;
    	int B = id%255;
    	return new Color(0xff000000|R<<16|G<<8|B);
    }
    
}