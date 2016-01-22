package com.longbowzz.bptest;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.sun.corba.se.spi.ior.MakeImmutable;

public class Goo{
	//attr 每个个体的内秉属性，可以被遗传和变异
	public float attrSTR = 1;
	public float attrPER = 1;
	public float attrEND = 1;
	public float attrCHR = 1;
	public float attrINT = 1;
	public float attrAGL = 1;
	public float attrLCK = 1;
	public static final float MAX_ATTR = 20;
	public static final float DELTA_ATTR = 0.1f;
	
	//神经网络，表现应激性
	//input:  速度1，速度2 ， 目标相对角度， 目标距离，当前是否被卡住，能否直接看到目标，special属性
	//output: 速度1，速度2
	protected final static int  Default_hidN =50;	//每层节点个数
	protected final static int  Default_attN =8;	//输入节点个数
	protected final static int  Default_outN =4;	//输出节点个数 vx,vy, eat?, mate?
	protected final static int  Default_layerN = 1; //隐藏层层数
	
	public  int hidN =Default_hidN;	//每层节点个数
	public  int  attN =Default_attN;	//输入节点个数
	public  int  outN =Default_outN;	//输出节点个数 vx,vy, eat?, mate?
	public  int  layerN = Default_layerN; //隐藏层层数

	protected static double RATE = 0.01;	//迭代步长
	
	protected  double[] input = new double[attN];


	
	private static double MUTANT_STEP = 0.2d;
	private static double MUTANT_RATE = 0.3d;
	protected static final int MAX_LIFE = 1000;
	public static final int MATE_INTERVAL = MAX_LIFE/20; // (int) (MAX_LIFE*0.1f);
	public static final int EAT_INTERVAL = 0;  //(int) (MAX_LIFE*0.05f);
	public static final double MAX_SPEED_VEC = 5.0f;
	public static final double SPEED_INTERVAL = 0.5f;
	public static final int EAT_RESTORE_LIFE = MAX_LIFE/4;
	
	public float speedFactor =1.0f;
	public Ball ball;
	public int ID;
	public int parentID;
	public int ancestorID;
	public int generation; //代
//	public boolean mated = false; //是否繁衍过
	public int mateTime= MAX_LIFE-MATE_INTERVAL; //当life<=mateTime 时才可以mate
	public int eatTime= MAX_LIFE-EAT_INTERVAL; //当life<=mateTime 时才可以mate
	public Goo target = null;
	public int score=0;  //分数
	public boolean isStopped = false;
	public boolean isTargetHide = false;
	
	public double v1, v2;
	public double lastv1, lastv2;
	/**
	 * 生命耗尽即死亡
	 */
	public int life = MAX_LIFE;

	
	
	public NeuralNet net;
	
	public double[] gene; //用来储存基因
	
	
	//决策
	public double eatValue;
	public double mateValue;
	
	//排序器
	public final static Comparator<Goo> SCORE_COMPARE = new Comparator<Goo>() {

		@Override
		public int compare(Goo goo0, Goo goo1) {
			if(goo0.score>goo1.score)
				return -1;
			else if(goo0.score==goo1.score)
				return 0;
			else 
				return 1;
		}

	};
	
	
	public void init(String[] args){
	
	}
	
	public Goo(){
		//派生类会修改此处
		init(null);
	}
		
	
	/**
	 * 构造祖代
	 * @param id
	 * @param initball
	 */
	public Goo(int id, int w, int h){
		this();
		ID = id;
		parentID = id;
		ancestorID = id;
		generation = 0;
		Random rand = new Random();
		attrSTR = 2+(0.5f-rand.nextFloat())*2;	
		attrPER = 2+(0.5f-rand.nextFloat())*2;	
		attrEND = 2+(0.5f-rand.nextFloat())*2;	
		attrCHR = 2+(0.5f-rand.nextFloat())*2;	
		attrINT = 2+(0.5f-rand.nextFloat())*2;	
		attrAGL = 2+(0.5f-rand.nextFloat())*2;	
		attrLCK = 2+(0.5f-rand.nextFloat())*2;	
		ball = getNewBall(w, h);
		ball.size = (int) attrSTR;
//		life = (int) (MAX_LIFE + MAX_LIFE * 0.1* attrEND);
		life = MAX_LIFE;
		mateTime = life - MATE_INTERVAL;
		eatTime = life -EAT_INTERVAL;
		net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		gene = net.getWeights();
		
	}
	
	/**
	 * 利用基因初始化生命体，
	 * @param id
	 * @param w
	 * @param h
	 * @param initGene
	 * @param shouldMutant  是否变异
	 */
	public Goo(int id, int w, int h, double[] initGene, boolean shouldMutant){
		this(id, w, h);
		if(!shouldMutant){
			net.putWeights(initGene);
			gene = net.getWeights();
		}else{
			net.putWeights(geneMate(initGene, initGene));
			gene = net.getWeights();
		}
	}
	
	
	/**
	 * 继承某一goo的基因
	 * @param id
	 * @param goo
	 * @param w
	 * @param h
	 */
	public Goo(int id,Goo ancestor, int w, int h){
		this();
		ID = id;
		parentID = ancestor.ID;
		ancestorID = ancestor.ancestorID;
		generation = ancestor.generation;
//		Random rand = new Random();
//		attrSTR = 2+(0.5f-rand.nextFloat())*2;	
//		attrPER = 2+(0.5f-rand.nextFloat())*2;	
//		attrEND = 2+(0.5f-rand.nextFloat())*2;	
//		attrCHR = 2+(0.5f-rand.nextFloat())*2;	
//		attrINT = 2+(0.5f-rand.nextFloat())*2;	
//		attrAGL = 2+(0.5f-rand.nextFloat())*2;	
//		attrLCK = 2+(0.5f-rand.nextFloat())*2;	
		attrSTR = 1;
		attrPER = 1;
		attrEND = 1;
		attrCHR = 1;
		attrINT = 1;
		attrAGL = 1;
		attrLCK = 1;
		
		ball = getNewBall(w, h);
		ball.size = (int) attrSTR;
		
//		life = (int) (MAX_LIFE + MAX_LIFE * 0.1*attrEND);
		life = MAX_LIFE;
		
		mateTime = life - MATE_INTERVAL;
		eatTime = life -EAT_INTERVAL;
		
		net = new NeuralNet(attN, outN, layerN, hidN, RATE);
		net.putWeights(ancestor.gene);
		gene = net.getWeights();
		
	}
	
	
	/**
	 * 双性繁殖，有丝分裂
	 * @param id
	 * @param goo1
	 * @param goo2
	 * @return
	 */
	public static Goo mate(int id, Goo dad, Goo mom){
		Goo child = new Goo();
		child.ID = id;
		child.parentID = dad.generation>= mom.generation? dad.ID:mom.ID;
		child.ancestorID = dad.generation>= mom.generation? dad.ancestorID:mom.ancestorID;
		child.generation = Math.max(dad.generation, mom.generation)+1;
//		Random rand = new Random();
//		float r = rand.nextFloat();
//		child.attrSTR = r*dad.attrSTR + (1-r)*mom.attrSTR +(0.5f-rand.nextFloat())*2;
//		r = rand.nextFloat();
//		child.attrPER = r*dad.attrPER + (1-r)*mom.attrPER +(0.5f-rand.nextFloat())*2;
//		r = rand.nextFloat();
//		child.attrEND = r*dad.attrEND + (1-r)*mom.attrEND +(0.5f-rand.nextFloat())*2;
//		r = rand.nextFloat();
//		child.attrCHR = r*dad.attrCHR + (1-r)*mom.attrCHR +(0.5f-rand.nextFloat())*2;		
//		r = rand.nextFloat();
//		child.attrINT = r*dad.attrINT + (1-r)*mom.attrINT +(0.5f-rand.nextFloat())*2;	
//		r = rand.nextFloat();
//		child.attrAGL = r*dad.attrAGL + (1-r)*mom.attrAGL +(0.5f-rand.nextFloat())*2;	
//		r = rand.nextFloat();
//		child.attrLCK = r*dad.attrLCK + (1-r)*mom.attrLCK +(0.5f-rand.nextFloat())*2;	
		child.attrSTR = dad.attrSTR;
		child.attrPER = dad.attrPER;
		child.attrEND = dad.attrEND;
		child.attrCHR = dad.attrCHR;
		child.attrINT = dad.attrINT;
		child.attrAGL = dad.attrAGL;
		child.attrLCK = dad.attrLCK;
		
		
		child.ball = new Ball((int) child.attrSTR, 0.01, dad.ball.sx, dad.ball.sy,
				-(dad.ball.vx+mom.ball.vx)/2, -(dad.ball.vy+mom.ball.vy)/2, getGooColorById(child.ancestorID));
		
//		child.life = (int) (MAX_LIFE + MAX_LIFE * 0.1* child.attrEND);
		child.life = MAX_LIFE;
		child.mateTime = child.life-MATE_INTERVAL;
		child.eatTime = child.life - EAT_INTERVAL;
		child.net  = new NeuralNet(child.attN, child.outN, child.layerN, child.hidN, RATE);
		child.gene = geneMate(dad.gene, mom.gene);
		child.net.putWeights(child.gene);
		
		return child;
		
	}
	
	


	/**
	 * 基因突变函数,选择5%的点进行编译; 编译尺度MUTANT_STEP;
	 * @param geneDad
	 * @param geneMom
	 * @return
	 */
	public static double[] geneMate(double[] geneDad,double[] geneMom){
		int len = geneDad.length;
		double[] geneChild = new double[len];
		int mutantCnt = (int) (len*MUTANT_RATE*Math.random());
		if(mutantCnt==0)mutantCnt=1;
		//merge gene
		for(int i=0; i<len; i++){
			geneChild[i] = (Math.random()>0.5)? geneDad[i] : geneMom[i];
			
		}
		
		//mutant
		for(int i=0; i<mutantCnt; i++){
			int pos = (int) (Math.random()*len);
			geneChild[pos]= geneChild[pos] + (0.5-Math.random())*MUTANT_STEP;
		}
		
		return geneChild;
		
	}	
	
	public void prepareUpdate(int w, int h){
		if(target != null){
			setInput(target, w, h);
		}
	}
	
	
	
	public void setInput(Goo target, int w, int h){
		//input:  速度1，速度2 ， 目标相对角度， 目标距离，special属性
		//output: 速度1，速度2

		//计算相对位置
		double sx =ball.x;
		double sy = ball.y;
		double tx = target.ball.x;
		double ty = target.ball.y;
		//自己的朝向
		double svx = ball.vx;
		double svy = ball.vy;

		double dx = tx - sx;
		double dy = ty - sy;
//		double temp1 = svx*dx + svy*dy;
//		double temp2 = Math.sqrt(svx*svx+svy*svy)*Math.sqrt(dx*dx+dy*dy);
//		double theta = 0d;
//		if(svx*svy==0 || dx*dy==0){
//			theta=0;
//		}else{
//			theta = Math.acos(temp1/temp2);
		
		double thetaS = Math.atan2(svy, svx);//(-p,p]
		double thetaD = Math.atan2(dy, dx);//(-p,p]
		double thetaDiff = thetaD - thetaS; //(-2p,2p)
		if(thetaDiff<=-Math.PI)
			thetaDiff += (2*Math.PI);
		if(thetaDiff>Math.PI)
			thetaDiff -= (2*Math.PI);  
		//Now thetaDiff is (-p,p]
		
		double dist = Math.sqrt(dx*dx+dy*dy);
		

//		}
		
		
		input[0] = normalize(v1,-MAX_SPEED_VEC,MAX_SPEED_VEC);
		input[1] = normalize(v2,-MAX_SPEED_VEC,MAX_SPEED_VEC);
		
		input[2] = normalize(thetaDiff,-Math.PI,Math.PI);
		input[3] = normalize(dist,0,w);
		input[4] = isStopped?0:1;
		input[5] = isTargetHide?0:1;
		
		
		input[6] = normalize(attrSTR, 0, MAX_ATTR);
		input[7] = normalize(target.attrSTR, 0, MAX_ATTR);
		
//		input[9] = normalize(attrPER, 0, 20);
//		input[10] = normalize(attrEND, 0, 20);
//		input[11] = normalize(attrCHR, 0, 20);
//		input[12] = normalize(attrINT, 0, 20);
//		input[13] = normalize(attrAGL, 0, 20);
//		input[14] = normalize(attrLCK, 0, 20);
//		input[16] = normalize(target.attrPER, 0, 20);
//		input[17] = normalize(target.attrEND, 0, 20);
//		input[18] = normalize(target.attrCHR, 0, 20);
//		input[19] = normalize(target.attrINT, 0, 20);
//		input[20] = normalize(target.attrAGL, 0, 20);
//		input[21] = normalize(target.attrLCK, 0, 20);
	}

	public double[] tryOnce(){
		return net.update(input);	
	}
	
	public String dumpInput(boolean readable){
//		if(target == null){
//			System.out.println("no target");
//			return;
//		}
//		System.out.println("input:======== target="+target.ID);
//		for(int i=0;i<input.length;i++){
//			System.out.println("input["+i+"]="+input[i]);
//		}
		StringBuilder sb = new StringBuilder();
		if(target!=null){
			if(readable){				
				sb.append(denormalize(input[0], -MAX_SPEED_VEC,MAX_SPEED_VEC));sb.append(",");
				sb.append(denormalize(input[1], -MAX_SPEED_VEC,MAX_SPEED_VEC));sb.append(",");
				sb.append(denormalize(input[2], -180,180));sb.append(",");
				sb.append(denormalize(input[3], 0,800));sb.append(",");
				sb.append(isStopped);sb.append(",");
				sb.append(isTargetHide);sb.append(",");
				sb.append(denormalize(input[6], 0, MAX_ATTR));sb.append(",");
				sb.append(denormalize(input[7], 0, MAX_ATTR));sb.append(",");
				
			}else{
				for(int i=0;i<input.length;i++){
					sb.append(input[i]);
					if(i<input.length-1)
						sb.append(",");
				}				
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	   /**
     * 归一化 0~1
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static double normalize(double value, double min, double max){
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
    public static double denormalize(double value, double min, double max){
    	double maxdiff = max-min;
    	return value*maxdiff+min;
    }
    
    
    
    
    void drawAncestor(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ball.c);
		g.drawString(""+ancestorID,ball.x+ball.size/2+2, ball.y+ball.size/2+10);	  	
}
	
    void drawID(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ball.c);
		g.drawString(""+ID,ball.x+ball.size/2+2, ball.y-ball.size/2+10);
		
    }	
    
    void drawParent(Graphics g){
//		g.setColor(new Color((0xff0000+ancestorID)|0xff000000));
    	g.setColor(ball.c);
		g.drawString(""+parentID,ball.x+ball.size/2+2, ball.y+ball.size/2+10);	  	
    }
    
	void drawRange(Graphics g){
		//交配过的不显示
//		if(mated)
//			return;
		int range = (int) getRange();
		if(life>mateTime)
			g.setColor(Color.BLUE);
		else
			g.setColor(ball.c);
		g.drawOval(ball.x-range, ball.y-range, 2*range, 2*range);		
//		g.drawString(""+(int)attrSTR,ball.x+ball.size/2+2, ball.y+ball.size/2+10);	  	
		
	}
	
	void drawSTR(Graphics g){
//		int range = (int) (attrSTR);
//		g.setColor(Color.gray);
//		g.drawOval(ball.x-range, ball.y-range, 2*range, 2*range);	
//		
    	g.setColor(ball.c);
    	
		g.drawString(String.format("%.2f", attrSTR),ball.x+ball.size/2+2, ball.y+ball.size/2+10);	  
		
	}
	
	void drawScore(Graphics g){
    	g.setColor(ball.c);
		g.drawString(""+score,ball.x+ball.size/2+2, ball.y+ball.size/2+10);	 			
	}
	
	void drawTargetPointer(Graphics g){
		if(target!=null){
			g.setColor(Color.GRAY);
			g.drawLine(ball.x, ball.y, target.ball.x, target.ball.y);
//			g.drawOval(target.ball.x, target.ball.y, 2, 2);
		}
	}
	
	public double getRange(){
//		return  attrPER*5;
		return 5;
	}
	
	
	public boolean canMate(){
		if(life>MAX_LIFE*1.f && life<=mateTime) //过老的个体不再能生殖?
			return true;
		else
			return false;
	}
    
    public static Color getGooColorById(int id){
    	Random rand = new Random(id);
    	
    	int R = (int) (127+rand.nextFloat()*127);
    	int G = (int) (rand.nextFloat()*255);
    	int B = (int) (rand.nextFloat()*255);
    	return new Color(0xff000000|R<<16|G<<8|B);
    }
    
    public static Color getGooColorByGen(int generation){
    	Random rand = new Random(generation);
    	int id = rand.nextInt();
    	int R = 127+id%127;
    	int G = (id/255)%255;
    	int B = id%255;
    	return new Color(0xff000000|R<<16|G<<8|B);
    }    
    
    protected Ball getNewBall(int w, int h){
//    	double angle = Math.random()*2*Math.PI;
//    	return new Ball(1,  0.01,   Math.sin(angle)*w,  Math.cos(angle)*w,
//				5*Math.sin(angle),5*Math.cos(angle), getShipColor(ancestorID)); 
//    	return new Ball(1,  0.01,   -2*w+1000,  2*h-1000,
//				0,0, getShipColor(ancestorID));
    	
    	return new Ball(1, 0.01f, Math.random()*w, Math.random()*h, (0.5-Math.random())*MAX_SPEED_VEC, (0.5-Math.random())*MAX_SPEED_VEC, getGooColorById(ancestorID));
    	
    }
    

    
}