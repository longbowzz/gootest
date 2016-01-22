package com.longbowzz.bptest;
//<applet code = Tribody width=400 height=300></applet>
import java.util.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import sun.font.EAttribute;

import java.io.*; 



/** 用来手动学习
 * S.P.E.C.I.A.L
 * S: size\atk
 * P: range\ hit rate
 * E: HP\ def
 * C: mate rate
 * I: range\rate\decision rate
 * A: dodge rate\ speed\turn rate
 * L: random
 * @author zhangzhen
 *
 */

public class MemSpecialWarStudy extends JApplet implements Runnable{
	static boolean isApplet=false;
	
	static String INPUT_PATH = SpecialGooWar.INPUT_PATH;
	static String OUTPUT_PATH = SpecialGooWar.OUTPUT_PATH;

	static String WEIGHT_FILE = "war_S20160114-study.dat";
	
	static String PLAYER = "S02_MEM_BLK_";

	static final boolean TEST_MODE = true;  //true: 使用训练出来的系数控制goos[0]  false:手动操作
	static final boolean TRACE_WINNER = true;
	static final boolean NEED_TRAIN  = false;
	static final int FEED_MODE = 0;  //0=散列式投喂  1=单点投喂
	
	static final Color bg = Color.BLACK;


  int mode=0;			
  int algor=1;        //0-先位置再速度(use dt)  1-先速度再位移(no dt) 
  
//  int screenW = 800;
//  int screenH = 600;
  
  boolean notail=true;
   //boolean notail=false;
  boolean isCheckBondary=false;
  
  boolean doShowView=false;
  boolean doShowMass=false;
  boolean doShowBid=false;
  boolean doShowParent = false;
  boolean doCheckFinish=false;
  boolean doCheckCollision=true;
  boolean doShowScore = false;
  boolean isFinish=false;
  boolean doFixPos=false;
  boolean doFocus=false;
  boolean doDraw = true;
  int doDrawMode = 2;  //0 全不画  1 只画测试飞船  2 全画
  boolean doShowInfo=false;
  int doDrawExtra = 0;  //0 不画， 1画目标， 2画半径
  
	boolean isDeath=false;
	boolean isSelectingP=true;
	boolean isSelectingV=false;
	//boolean isRandom=false;
	int tribodymode=0;	//0-随机 1-巢状 2-自定义 3-读取文件
	double tmpm;
	String mstr="";
	boolean isSameTrack=false;
	int SelectingPx=0,SelectingPy=0,SelectingVx=0,SelectingVy=0;
	String deathInfo="";
	BufferedReader br;
	BufferedWriter bw; 
	String str="";

	double dspeed = 1;
	
	boolean mouseControl = false;
	int mousex,mousey;
	
  boolean autoFly=true;
  //set meeting point
  double mtx=0.0;
  double mty=0.0;
  int mtt=0;
  
  

	double dt=1.0;

	int BLK_CNT = 30;
	
	int goocnt = TEST_MODE?100:10;
	int WINNER_CNT = 0;
	int SEED_CNT = 0;
		
	int FOOD_CNT = (FEED_MODE==0)?50:1;
	int CHILD_CNT = 2; //一次生2个
	int gooRemains = goocnt;
	int cnt=1;
	int timecnt=0;
	int maxGen = 0; //最大的代数
	int bestScore = 0;
	int round=0; //第几轮

	int mateCnt =0;
	int eatCnt = 0;
	int deadCnt =0 ;
	float mateRate = 0;

	
	Ball[] balls = new Ball[cnt];
//	MemGoo[] goos = new MemGoo[goocnt];
	LinkedList<MemGoo> goos = new LinkedList<MemGoo>();
	LinkedList<Food> foods = new LinkedList<Food>(); //最弱小的goo，无法进攻和繁殖，随机增长
	ArrayList<MemGoo> winners = new ArrayList<MemGoo>();
	ArrayList<Blocker> blks = new ArrayList<Blocker>();
	File studyInputfile=new File(OUTPUT_PATH,PLAYER+"study.input");
	File studyOutputfile=new File(OUTPUT_PATH,PLAYER+"study.output");
	File studyInputReffile=new File(OUTPUT_PATH,PLAYER+"study.inputRef");
	Map map;

	
	double mx= this.getSize().width/2;
	double my= this.getSize().height/2;
	int step= TEST_MODE?30:300;

  boolean isstart=false;


	
	Thread my_thread = null;
	Random r = new Random();
	Canvas cvs = new Canvas()/*{
		public void paint(Graphics g){
			g.drawImage(off_screen, 0, 0, null);
			System.out.println("in canvas paint");
		}	
	}*/;
	Image off_screen;
	Graphics g;
	

	public void init(){
		isApplet=true;
		setLayout(new BorderLayout());
		
		add("Center",cvs);

	}

	public void start(){
		my_thread=new Thread(this);
		my_thread.start();
		
	}
	
	public void run(){
		//开始界面
		//初始化作图区
		isstart=true;
		mode = 3;
		
		add("Center",cvs);		
		cvs.setBackground(Color.black);
		g= cvs.getGraphics();
		g.setColor (Color.black);
		g.fillRect (0, 0, this.getSize().width, this.getSize().height);		
		//repaint();
		map = new Map(this.getSize().width, this.getSize().height);

		
		while(isstart==false){	
			update(g);						
			try{Thread.sleep(50);}catch(InterruptedException e){throw new RuntimeException(e);}		
		}
		System.out.println("mode="+mode);

		validate();

		

		double deltay;
		
		double[] initGene = null;
		try {
			initGene = loadWeights(OUTPUT_PATH+PLAYER+"humanTrained.dat");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//goo0要用户自己控制
		MemGoo mygoo;
		if(!TEST_MODE)
			mygoo = new MemGoo(-9999, getSize().width, getSize().height);
		else
			mygoo = new MemGoo(-9999, getSize().width, getSize().height, initGene, true); //使用已保存的个体
		mygoo.attrSTR = 5;
		mygoo.ball.size = 10;
		mygoo.ball.c = Color.red;
		mygoo.ball.sx = getSize().width/2;
		mygoo.ball.sy = getSize().width/2;
		mygoo.ball.useSxyAsXy();
		mygoo.ball.vx = 0;
		mygoo.ball.vy = 0;
		mygoo.life = Goo.MAX_LIFE*10;
		
		goos.add(mygoo);
		map.addGoo2Region(mygoo);	 //同类也是食物	
		

		
		for(int i=1; i<goocnt; i++){
			MemGoo goo;
			if(initGene == null || i>=SEED_CNT)
				goo = new MemGoo(IDPool.getID(), getSize().width, getSize().height); //使用随机个体
			else
				goo = new MemGoo(IDPool.getID(), getSize().width, getSize().height, initGene, true); //使用已保存的个体
			
//			goo.ball.vx=0;
//			goo.ball.vy=0;
//			goo.ball.sx=goos.get(0).ball.sx+100;
//			goo.ball.sy=goos.get(0).ball.sy;;
			goos.add(goo);
			map.addGoo2Region(goo);	 //同类也是食物	
		}
		
		//添加障碍物
		for(int i=0; i<BLK_CNT;i++){
			int x1 = (int) (Math.random()*getSize().width);
			int y1 = (int) (Math.random()*getSize().height);
			int dx = (int) ((0.5d-Math.random())*2*getSize().width*0.2); //10分之一长*
			int dy = (int) ((0.5d-Math.random())*2*getSize().height*0.2); //10分之一长*
			
			Blocker block = new Blocker(x1, y1, x1+dx, y1+dy);
			blks.add(block);
		}
//		blks.add(new Blocker(0,getSize().height/2 , getSize().width, getSize().height/2));
			
		//开始模拟		

		off_screen=createImage(getSize().width, getSize().height);
		g=off_screen.getGraphics();
		cvs.addMouseListener(new MouseInputAdapter(){
			public void mouseClicked(MouseEvent e) {
				this_mouseClicked(e);
			}
			
			public void mousePressed(MouseEvent e){
				this_mousePressed(e);
			}
			

			
			public void mouseReleased(MouseEvent e){
				this_mouseReleased(e);
			}
		});		
		cvs.addMouseMotionListener(new MouseMotionListener() {
			
			public void mouseDragged(MouseEvent e){
				this_mouseDragged(e);
			}
			
			public void mouseMoved(MouseEvent e){
				this_mouseMoved(e);
			}
		});
		cvs.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){
				this_keyPressed(e);
			}		
			public void keyReleased(KeyEvent e) {
				//this_keyReleased(e);
			}	 
			public void keyTyped(KeyEvent e) {
				//this_keyTyped(e);
			}	        	     		  	
		});	
		
		g.setColor (Color.BLACK);
		g.fillRect (0, 0, this.getSize().width, this.getSize().height);		
		
		
		

		
		
		
		while( isFinish==false ){
			if(notail==true){
				g.setColor (Color.BLACK);
				g.fillRect (0, 0, getSize().width, getSize().height);			
			}
			
			//绘制goo的轨迹，并进行应激反应
			for(MemGoo goo : goos){
				if(goo.ID<0 && !TEST_MODE) {//人工操控
					
					//发现临近的goo,分区查找法
					goo.target = map.findNearestGoo(goo.ID, goo.ball.sx, goo.ball.sy);		
					//看看目标是否被阻挡了
					goo.isTargetHide = false;
					if(goo.target != null){
						for(Blocker blk : blks){
							if(blk.isCross(goo.ball.sx, goo.ball.sy, goo.target.ball.sx, goo.target.ball.sy)){
								goo.isTargetHide = true;
								break;
							}
						}						
					}
					goo.prepareUpdate(getSize().width, getSize().height);
					
					if(mouseControl){
						//当前速度方向（朝向）
						double svx = goo.ball.vx;
						double svy = goo.ball.vy;
						double thetaS = Math.atan2(svy, svx);//(-p,p]
						
						double dx = mousex - goo.ball.sx; //操控矢量指向鼠标
						double dy = mousey - goo.ball.sy;
						
						double speedCx = 10*dx*Goo.MAX_SPEED_VEC/getSize().width;
						double speedCy =  10*dy*Goo.MAX_SPEED_VEC/getSize().height;
//						speedCx = Math.min(speedCx, Goo.MAX_SPEED_VEC/Math.sqrt(2));
//						speedCy = Math.min(speedCy, Goo.MAX_SPEED_VEC/Math.sqrt(2));
						double maxspeed = Goo.MAX_SPEED_VEC/Math.sqrt(2);
						if(speedCx>maxspeed)
							speedCx = maxspeed;
						if(speedCy>maxspeed)
							speedCy = maxspeed;
						if(speedCx<-maxspeed)
							speedCx = -maxspeed;
						if(speedCy<-maxspeed)
							speedCy = -maxspeed;
						
						double thetaC = Math.atan2(speedCy, speedCx);//(-p,p] 
						
						double turnvalue = thetaC - thetaS; //(-2p ,2p)
						if(turnvalue<= -Math.PI)
							turnvalue+=(2*Math.PI);
						if(turnvalue>Math.PI)
							turnvalue-=(2*Math.PI); ////(-p,p] 
						double diffV = 2*turnvalue *Goo.MAX_SPEED_VEC/Math.PI;
						
//						System.out.println("thetaS-C = "+thetaS+"\t"+thetaC+"\t"+diffV+"\t"+dx+"\t"+dy);
						

						
						goo.ball.vx=speedCx;
						goo.ball.vy=speedCy;
						
						double newspeed = Math.sqrt(speedCx*speedCx + speedCy*speedCy);
						
						//反推v1 v2
						goo.v1=(2*newspeed - diffV)/2;
						goo.v2=(2*newspeed + diffV)/2;
						

						
						goo.eatValue = 1;
						goo.mateValue = 1;
//						if(goo.v1> Goo.MAX_SPEED_VEC || goo.v2> Goo.MAX_SPEED_VEC)
//							System.out.println("OverSpeed("+maxspeed+"): "+goo.v1+", "+goo.v2+" diffV="+diffV+" ,Vxy="+speedCx+","+speedCy);
						
					}else{  //手离则停
						goo.ball.vx=0;
						goo.ball.vy=0;
						goo.v1=0;
						goo.v2=0;
						goo.eatValue = 1;
						goo.mateValue = 1;
					}
					
					try {
						if(mouseControl){
							logStudyInputInfo(studyInputfile, goo);
							logStudyInputRefInfo(studyInputReffile,goo);
							logStudyOutputInfo(studyOutputfile, goo);							
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					continue;
				}
				
				if(goo.life>0){

					//发现临近的goo,分区查找法
					goo.target = map.findNearestGoo(goo.ID, goo.ball.sx, goo.ball.sy);		
					//看看目标是否被阻挡了
					goo.isTargetHide = false;
					if(goo.target != null){
						for(Blocker blk : blks){
							if(blk.isCross(goo.ball.sx, goo.ball.sy, goo.target.ball.sx, goo.target.ball.sy)){
								goo.isTargetHide = true;
								break;
							}
						}						
					}
					
					//神经网络应激反应
					goo.prepareUpdate(getSize().width, getSize().height);
					
					double[] dOutVec = goo.tryOnce();

					goo.v1 = Goo.denormalize(dOutVec[0], -Goo.MAX_SPEED_VEC, Goo.MAX_SPEED_VEC);
					goo.v2 = Goo.denormalize(dOutVec[1], -Goo.MAX_SPEED_VEC, Goo.MAX_SPEED_VEC);
					
					//test
//					goo.v1 = goos.get(0).v1; 
//					goo.v2 = goos.get(0).v2;
					
					
					//根据V1 V2修正vx vy
					//当前速度方向（朝向）
					double svx = goo.ball.vx;
					double svy = goo.ball.vy;
					double thetaS = Math.atan2(svy, svx);//(-p,p]
					double newSpeed = Math.abs(goo.v1+goo.v2)/2;
					double turnValue = Math.PI*(goo.v2-goo.v1)/(2*Goo.MAX_SPEED_VEC); //两轮差分决定转向 [-2*Goo.MAX_SPEED_VEC,2*Goo.MAX_SPEED_VEC]~(-Pi,Pi]
					double newtheta = thetaS+turnValue;//(-2p,2p)
					goo.ball.vx = newSpeed*Math.cos(newtheta);
					goo.ball.vy = newSpeed*Math.sin(newtheta);
					

					
					goo.eatValue = dOutVec[2];
					goo.mateValue = dOutVec[3];
					
					
//					if(goo.ID == goos.get(0).ID){
//						goo.dumpInput();
//						System.out.println("speed out="+goo.ball.vx+","+goo.ball.vy);
//					}
				}
			}
			
					
			//清空地图
			map.clearRegion();

			
			//计算goo新位置
			for(MemGoo goo : goos){		
				if(goo.life>0){			

				
					calcPos(goo, getSize().width, getSize().height);	
					
//					System.out.println("goo"+goo.ID+" pos x="+goo.ball.sx+",y="+goo.ball.sy);	


					
					//更新map
					map.addGoo2Region(goo);//同类也是目标
					
					if(goo.ball.x>=0 && goo.ball.x<=getSize().width && goo.ball.y>=0 && goo.ball.y<=getSize().height){
						if(notail==false){
							goo.ball.draw(g);
//							goo.ball.drawClear(g);
						}
						else{
							goo.ball.draw(g);
//							goo.drawSTR(g);
//							goo.drawRange(g);
							if(doDrawExtra==1)
								goo.drawTargetPointer(g);
							else if(doDrawExtra==2)
								goo.drawRange(g);
							
							if(doShowBid)
								goo.drawAncestor(g);
							if(doShowParent)
								goo.drawParent(g);
							
							if(goo.ID<0 ){
								if(mouseControl){
									g.setColor(Color.red);
									g.drawLine(goo.ball.x, goo.ball.y, mousex, mousey);									
								}
								goo.drawTargetPointer(g);
							}
							
						}
					}
				}
			}
						
			
			if(notail==true && doShowInfo==true)
				showInfo();

			//重填食物
		      for(Food food : foods){
		    	//绘制食物
		    	 food.draw(g);
//		    	 food.drawID(g);
		    	 map.addGoo2Region(food);
		      }      
		      
		      //画墙
		      for(Blocker blk : blks){
		    	  blk.draw(g);
		      }

		      
      showTime();	
      
    //计算goo之间的关系，吃或被吃、交配、下崽（卵）	
      calcGoos();

      //每回200合重新 放置食物
//      if(timecnt%200==99){
    	  int suppliment = FOOD_CNT - foods.size();
    	  //每回合补满

    	  for(int i=0; i< suppliment; i++){
    		  Food food = new Food(-1,getSize().width, getSize().height);
    		  if(!TEST_MODE && FEED_MODE==1){ //只把食物放在身边
    			  double sx = goos.get(0).ball.sx + (0.5-Math.random())*getSize().width/4;
    			  double sy = goos.get(0).ball.sy + (0.5-Math.random())*getSize().height/4;
    			  if(sx<0)sx=0;
    			  if(sy<0)sy=0;
    			  if(sx>getSize().width)sx=getSize().width;
    			  if(sy>getSize().height)sy=getSize().height;
    			  food.ball.sx = sx;
    			  food.ball.sy = sy;
    			  food.ball.useSxyAsXy();
    		  }
    		  foods.add(food);
    		  map.addGoo2Region(food);
    	  }    	  
//      }


      timecnt++;	
      cvs.getGraphics().drawImage(off_screen,0,0,this);
			try{Thread.sleep(step);}catch(InterruptedException e){throw new RuntimeException(e);}			
			
			
//      		if(timecnt%5==0)
//      			System.out.println("(Best count food Gen mate eat dead rate)="+bestScore+"\t"+goos.size()+"\t"+foods.size()+"\t"+maxGen+"\t"+mateCnt+"\t"+eatCnt+"\t"+deadCnt+"\t"+(mateRate));
      		//最终的5个胜利者保留基因，并传承下一次
    		if(goos.size()<=WINNER_CNT || timecnt>=Goo.MAX_LIFE*10){
      			System.out.println("ROUND "+round+"(Best count food Gen mate eat dead rate)="+bestScore+"\t"+goos.size()+"\t"+foods.size()+"\t"+maxGen+"\t"+mateCnt+"\t"+eatCnt+"\t"+deadCnt+"\t"+(mateRate));
      			round++;

    			if(goos.size()>0){ //防止出现全死绝情况
    				winners.clear();

    				Collections.sort(goos, Goo.SCORE_COMPARE);//从大到小排序
    				for(int i=0; i<Math.min(goos.size(), WINNER_CNT); i++){
    					MemGoo goo = goos.get(i);
    					winners.add(goo);
    					System.out.println("Winner-"+i+":\t score="+goo.score+"\t ID="+goo.ID+"\t ancestor="+goo.ancestorID);
    				}
    				
    				
    			}
    			//重制新的goos,1/3来自胜利者
    			//剩余2/3引入新基因
    			goos.clear();
    			for(int i=0; i<goocnt; i++){
    				MemGoo newgoo;
    				int maxwinnerchild = goocnt*3/4;
    				
    				//幸运者每个繁殖3个个体，再补充上随机个体
    				if(winners.size()>0 && i<maxwinnerchild){ //每个繁殖3个个体
    					int idx =i*winners.size()/maxwinnerchild;
    					newgoo = (MemGoo) MemGoo.mate(IDPool.getID(), winners.get(idx), winners.get(idx)); //自我复制
    					newgoo = new MemGoo(newgoo.ID , newgoo, getSize().width, getSize().height);//重新定义位置
    				}else{
    					newgoo = new MemGoo(IDPool.getID(), getSize().width, getSize().height);
    				}
    				goos.add(newgoo);
    				map.addGoo2Region(newgoo);
    			}
    			//回合结束，重新布置食物
    			foods.clear();
    			for(int i=0; i< FOOD_CNT; i++){
    				Food food = new Food(-1,getSize().width, getSize().height);
    				foods.add(food);
    				map.addGoo2Region(food);
    			}
    			//重新添加障碍物
    			blks.clear();
    			for(int i=0; i<BLK_CNT;i++){
    				int x1 = (int) (Math.random()*getSize().width);
    				int y1 = (int) (Math.random()*getSize().height);
    				int dx = (int) ((0.5d-Math.random())*2*getSize().width*0.2); //10分之一长*
    				int dy = (int) ((0.5d-Math.random())*2*getSize().height*0.2); //10分之一长*
    				
    				Blocker block = new Blocker(x1, y1, x1+dx, y1+dy);
    				blks.add(block);
    			}
    			//重置基因
    			timecnt=0;
    			mateCnt=0;
    			eatCnt =0;
				//拖尾模式清屏
				if(!notail){
					g.setColor (Color.BLACK);
					g.fillRect (0, 0, getSize().width, getSize().height);							
				}
    		}
		}
		
		//退出
		System.out.println("SIMULATION FINISHED!!!");
		//完成输出
		if(winners.size()>0){
			try {
				saveWeights(OUTPUT_PATH+WEIGHT_FILE, winners.get(0).net.getWeights());
				winners.get(winners.size()-1).net.dumpAllWeights();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		}else{
			System.out.println("no winner");
		}
		while(true){
		}
	}
	
	public void paint(Graphics g){
	    cvs.repaint();
			//g.drawImage(off_screen,0,0,this);
  }



	void calcPos(Goo goo,int w, int h){
			double newx,newy;
			newx = goo.ball.sx+goo.ball.vx*dt;
			newy =goo.ball.sy+goo.ball.vy*dt;	
			if(newx<0)
				newx=0;
			if(newy<0)
				newy=0;
			if(newx>=w)
				newx=w-1;
			if(newy>=h)
				newy=h-1;
			
			//检测block碰撞

			for(Blocker blk : blks){
				if(blk.isCross(goo.ball.sx,goo.ball.sy,newx, newy)){ //遇阻停下来
					goo.ball.vx = 0;
					goo.ball.vy = 0;
					goo.isStopped = true;
//					System.out.println("BLOCKED!!!! line\t "+b.x+"\t"+b.y+"\t"+b.sx+"\t"+b.sy+"\t blk \t"
//							+blk.startx+"\t"+blk.starty+"\t"+blk.endx+"\t"+blk.endy);
					return; //不做任何移动
				}
			}
			goo.isStopped = false;
			goo.ball.sx = newx;
			goo.ball.sy = newy;
			goo.ball.x = (int) newx;
			goo.ball.y = (int) newy;

			
	}
	

	




  void showInfo(){
  	Ball b;
  	String str;
  	for(int i=0; i<cnt; i++){
  		b=balls[i];
  		g.setColor(b.c);
  		str="Ball "+i+": pos="+(int)b.sx+","+(int)b.sy;
  		g.drawString(str,10,10+i*15);
  		str="speed="+(float)b.vx+",";
  		g.drawString(str,170,10+i*15);
  		g.drawString(""+(float)b.vy,350,10+i*15);
  		str="a="+(float)b.ax+",";
  		g.drawString(str,500,10+i*15);
  		g.drawString(""+(float)b.ay,660,10+i*15);
  		
  		
  	}
  	
  }


	void showTime(){
		g.setColor(Color.YELLOW);
		g.fillRect(10,this.getSize().height-30, 70,28);
		g.setColor(Color.BLACK);
		g.drawString("Time:"+timecnt,10,this.getSize().height-15);
		g.drawString("Speed:"+(int)dt,10,getSize().height-3 );
		

	}
	


	


  void this_mouseClicked(MouseEvent e){
		  int w=this.getSize().width;
		  int h=this.getSize().height;
			Point p = e.getPoint();
	
			if(isstart==true){			

				
							
				if(notail==true){
					notail=false;
					doShowMass=false;
					doShowBid=false;
				}
				else
					notail=true;		
			}		  
  }	

  void this_mousePressed(MouseEvent e){
	  int w=this.getSize().width;
	  int h=this.getSize().height;
	  Point p = e.getPoint();

	  
	  //拖拽控制
	  MemGoo mygoo = goos.get(0);
	  int dx = p.x - mygoo.ball.x; 
	  int dy = p.y - mygoo.ball.y;
	  int dist = (int) Math.sqrt(dx*dx+dy*dy);
	  if(dist<mygoo.ball.size/2){
		  System.out.println("Controlling...");
		  mouseControl = true;
		  mousex = p.x;
		  mousey = p.y;
	  }else
		  mouseControl = false;
  }
  
  void this_mouseDragged(MouseEvent e){
	  Point p = e.getPoint();
	  int w=this.getSize().width;
	  int h=this.getSize().height;	
	  mousex = p.x;
	  mousey = p.y;
//	  System.out.println("dragging..."+mousex+","+mousey);
  }
  
  void this_mouseMoved(MouseEvent e){
	  Point p = e.getPoint();
	  int w=this.getSize().width;
	  int h=this.getSize().height;	
	  mousex = p.x;
	  mousey = p.y;
//	  System.out.println("moving..."+mousex+","+mousey);
  }	
	
	void this_mouseReleased(MouseEvent e){
		System.out.println("Stop Control...");
		mouseControl = false;
	}
	

	void this_keyPressed(KeyEvent e){
		int kcode=e.getKeyCode();
		//System.out.println("key "+kcode+" is pressed!"+KeyEvent.VK_A);
//		if(kcode==KeyEvent.VK_A){
//			scale*=2;
//			//System.out.println("key "+kcode+" is pressed!");
//			System.out.println("scale="+scale);
//		}
//		else if(kcode==KeyEvent.VK_S && scale>2){
//			scale/=2;
//			//System.out.println("key "+kcode+" is pressed!");
//			System.out.println("scale="+scale);
//		}else
		if(kcode==KeyEvent.VK_M){
			if(doShowMass==true || notail==false)
				doShowMass=false;
			else
				doShowMass=true;				
		}
		else if(kcode==KeyEvent.VK_N){
			if(doShowBid==true || notail==false)
				doShowBid=false;
			else
				doShowBid=true;				
		}
		else if(kcode==KeyEvent.VK_P){
			if(doShowParent==true || notail==false)
				doShowParent=false;
			else
				doShowParent=true;				
		}else if(kcode==KeyEvent.VK_Q){
			if(doShowScore==true || notail==false)
				doShowScore=false;
			else
				doShowScore=true;				
		}else if(kcode==KeyEvent.VK_D){
/*			if(doDraw==true)
				doDraw=false;
			else
				doDraw=true;	*/	
			if(doDrawMode==0)
				doDrawMode=1;
			else if(doDrawMode==1)
				doDrawMode=2;
			else if(doDrawMode==2)
				doDrawMode=0;
			
		}else if(kcode==KeyEvent.VK_SPACE){
			if(isFinish==false)
				isFinish=true;			
		}else if(kcode==KeyEvent.VK_R){
			if(doDrawExtra==0)
				doDrawExtra=1;
			else if(doDrawExtra==1)
				doDrawExtra=2;
			else if(doDrawExtra==2)
				doDrawExtra=0;	
		}				
		
		if(kcode==KeyEvent.VK_F){
			if(doFocus==true )
				doFocus=false;
			else
				doFocus=true;		
			System.out.println("doFocus="+doFocus);	
		}
		
		if(kcode==KeyEvent.VK_PAGE_UP){
			dt=dt+1.0;
		}
				
		if(kcode==KeyEvent.VK_PAGE_DOWN){
			dt=dt-1.0;
			if(dt<1.0)
				dt=1.0;
		}
		if(kcode==KeyEvent.VK_V){
			if(doShowView==true )
				doShowView=false;
			else
				doShowView=true;		
		}


	}
	

	
	
/////////////////////////神经网络+遗传算法/////////////////////////
    //计算goo之间的关系，吃或被吃、交配、下崽（卵）	
	private void calcGoos(){
		ArrayList<MemGoo> newborns = new ArrayList<MemGoo>();
		//生命衰减
//		mateRate = 1.0f- (float)goos.size()/goocnt;

		for(MemGoo goo : goos){
			//生命衰减,超出容量后加速死亡
			int dLife =1;

			goo.life -= dLife;
			
			//跌落死亡
//			if(goo.ball.x<0 || goo.ball.y<0 || goo.ball.x>getSize().width || goo.ball.y>getSize().height){
//				goo.life = -1;
//			}
			
			//根据target执行操作
			if(goo.life > 0 && goo.target != null){
				double dx = goo.ball.x - goo.target.ball.x;
				double dy = goo.ball.y - goo.target.ball.y;
				double dist =Math.sqrt(dx*dx+dy*dy);

				if(dist < goo.getRange()){
					// 交配
//					if(Math.random() < mateRate*mateRate  && goo.mateValue>0.5f 
//							&& goo.canMate() && goo.target.canMate() &&
//							goo.parentID!= goo.target.parentID){//防止近亲结婚
					if( goo.canMate() && goo.target.canMate() &&
							goo.parentID!= goo.target.parentID && !(goo.target instanceof Food)){//防止近亲结婚,不是食物
						//draw mating effect
						g.setColor(Color.GREEN);
						g.drawLine(goo.ball.x, goo.ball.y, goo.target.ball.x, goo.target.ball.y);
						int childcnt = (int) (goo.mateValue/0.3);  //数值越大越会生
						for(int i=0;i<childcnt; i++){
							MemGoo child = MemGoo.mate(IDPool.getID(), goo, (MemGoo) goo.target);						
							newborns.add(child);
							if(child.generation > maxGen)
								maxGen = child.generation;							
						}
						
//						System.out.println("new born goo: ID="+child.ID+" Gen="+child.generation);
//						goo.life-=100;
						goo.mateTime = goo.life - Goo.MATE_INTERVAL;
						goo.mateValue = 0;
//						goo.target.life-=100;
						goo.target.mateTime = goo.target.life -  Goo.MATE_INTERVAL;
						
					}
					//只吃食物,同家族不吃
					
					if(goo.eatValue > 0.5f 
							&& goo.target.parentID != goo.ID 
							&& goo.parentID != goo.target.ID 
							&& goo.target.parentID != goo.parentID
							){
						if(goo.attrSTR >= goo.target.attrSTR ){
							//draw killing effect
							g.setColor(Color.RED);
							g.drawLine(goo.ball.x, goo.ball.y, goo.target.ball.x, goo.target.ball.y);
							goo.target.life = -1;
							goo.life += Goo.EAT_RESTORE_LIFE;
							goo.eatTime = goo.life - Goo.EAT_INTERVAL;
							goo.mateTime += Goo.EAT_RESTORE_LIFE;
							
							goo.eatValue = 0;	
							goo.score++;
							if(goo.score > bestScore)
								bestScore=goo.score;
							eatCnt++;
							if(goo.ID==-9999)
								System.out.println("mygoo "+goo.ID + " eat food. Score="+goo.score);
//							else
//								System.out.println("mygoo "+goo.ID + "Killed "+goo.target.ID+", Score="+goo.score);
						}
						
					}
					
				}
			}				
			
		}
		//再次确认死亡
		deadCnt = 0;
		Iterator<MemGoo> gooItr = goos.iterator();
		while(gooItr.hasNext()){
			MemGoo goo = gooItr.next();
			//生命衰减
			if(goo.life<=0){
				gooItr.remove();
				deadCnt ++;
			}
		}
		
		Iterator<Food> foodItr = foods.iterator();
		while(foodItr.hasNext()){
			Food food = foodItr.next();
			//生命衰减
			if(food.life<=0){
				foodItr.remove();
			}
		}		
		
		//添加新诞生的goo
		for(MemGoo goo : newborns){
			mateCnt++;
			goos.add(goo);
		}
	}
	
	

	
	private int getNewIndex(int total){
		
		double threshhold  =  1- Math.pow(1.0d/total, 1.0d/total);
		for(int i=0; i<total; i++){
			if(Math.random()<threshhold){ //确保最后一个选中概率为1/total
				return i;
			}
		}
		return 0;
	}
	
	private static void logStudyInputInfo(File file , MemGoo goo) throws IOException{
//		 File file=new File(path);
	        if(!file.exists()){
	        	file.getParentFile().mkdirs();
	        	file.createNewFile();    
	        }
	        FileOutputStream out=new FileOutputStream(file,true);   
	        
	        out.write(goo.dumpInput(false).getBytes("utf-8"));
	        out.close();		
	}
	
	private static void logStudyInputRefInfo(File file , MemGoo goo) throws IOException{
//		 File file=new File(path);
	        if(!file.exists()){
	        	file.getParentFile().mkdirs();
	        	file.createNewFile();    
	        }
	        FileOutputStream out=new FileOutputStream(file,true);   
	        
	        out.write(goo.dumpInput(true).getBytes("utf-8"));
	        out.close();		
	}
	
	private static void logStudyOutputInfo(File file , MemGoo goo) throws IOException{
//		 File file=new File(path);
	        if(!file.exists()){
	        	file.getParentFile().mkdirs();
	        	file.createNewFile();    
	        }
	        FileOutputStream out=new FileOutputStream(file,true);   
	        StringBuffer sb = new StringBuffer();
	        sb.append(Goo.normalize(goo.v1, -Goo.MAX_SPEED_VEC,Goo.MAX_SPEED_VEC)+",");
	        sb.append(Goo.normalize(goo.v2, -Goo.MAX_SPEED_VEC,Goo.MAX_SPEED_VEC)+",");
	        sb.append(Goo.normalize(goo.eatValue, 0,1)+",");
	        sb.append(Goo.normalize(goo.mateValue, 0,1)+"\n");
	        out.write(sb.toString().getBytes("utf-8"));
	        out.close();		
	}
	
    
    public static void saveWeights(String path, double[] weights) throws IOException{
        File file=new File(path);
        if(!file.exists()){
        	file.getParentFile().mkdirs();
        	file.createNewFile();    
        }else{
        	file.delete();
        	file.createNewFile();
        }
        FileOutputStream out=new FileOutputStream(file,true);   
        int len = weights.length;
        StringBuffer sb=new StringBuffer();
        for(int i=0; i<len; i++){
        	sb.append(""+weights[i]);
        	if(i<len-1)
        		sb.append(",");
        }
        out.write(sb.toString().getBytes("utf-8"));
        out.close();
        		
    }
    
    public static double[] loadWeights(String path) throws IOException{
        File file=new File(path);
        if(!file.exists()||file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br=new BufferedReader(new FileReader(file));
        String temp=br.readLine();
        String[] weightstrs = temp.split(",");
        double[] weights = new double[weightstrs.length];
        for(int i=0; i<weightstrs.length; i++){
        	try {
				weights[i] = Double.parseDouble(weightstrs[i]);
			} catch (NumberFormatException e) {
				weights[i] = 0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
		return weights;    	
    }	
	
    

	
	
	public static void main(String[] args){
		JApplet myApplet=new MemSpecialWarStudy();
		JFrame myFrame = new JFrame("SPECIAL");
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.getContentPane().add(myApplet);
		myFrame.setSize(640,640);
		myApplet.init();
		isApplet=false;
		myApplet.start();
		myFrame.setVisible(true);
		
	}		
}