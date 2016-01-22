package com.longbowzz.bptest;

import java.awt.Color;
import java.awt.Graphics;


public class Blocker{
	public double startx,starty,endx,endy;
	
	public Blocker(double x1, double y1, double x2, double y2){
		startx =x1;
		starty = y1;
		endx = x2;
		endy = y2;
	}
	
	public void draw(Graphics g){
		g.setColor(Color.DARK_GRAY);
		g.drawLine((int)startx, (int)starty, (int)endx, (int)endy);
	}
	
	/**
	 * 直线是否与本blocker相交
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public boolean isCross(double x1, double y1,double x2, double y2){
		double kb=0;
		double kl=0;
		double bb=0;
		double bl=0;

		
		//先排除特殊情况
		if(x1==x2){
			if(startx == endx)
				return false;
			else{
				kb = (endy-starty)/(endx-startx);
				bb = (startx*endy - endx*starty)/(startx-endx);
				double crossy = kb*x1+bb;
				if(crossy < Math.min(starty, endy) || crossy> Math.max(starty, endy)){
					return false;
				}else
					return true;
			}
		}
		
		if(startx == endx){
			if(x1 == x2)
				return false;
			else{
				kl = (y2-y1)/(x2-x1);
				bl = (x1*y2 - x2*y1)/(x1-x2);
				double crossy = kl*startx + bl;
				if(crossy < Math.min(y1, y2) || crossy> Math.max(y1, y2)){
					return false;
				}else
					return true;
			}
		}
				
		kl = (y2-y1)/(x2-x1);
		bl = (x1*y2 - x2*y1)/(x1-x2);
		
		
		kb = (endy-starty)/(endx-startx);
		bb = (startx*endy - endx*starty)/(startx-endx);

		
		
		if(kl==kb)
			return false;
		
		double crossx = (bb - bl)/(kl - kb);
		

		
		if(crossx < Math.min(x1, x2) || crossx> Math.max(x1, x2) || crossx < Math.min(startx, endx) || crossx> Math.max(startx, endx)){
//			if(Math.abs(y1-starty)<1d){//调试，接近时打印
//				System.out.println("PASS---line\t "+x1+"\t"+y1+"\t"+x2+"\t"+y2+"\t blocker\t "
//						+startx+"\t"+starty+"\t"+endx+"\t"+endy);
//				System.out.println("kl="+kl+"\t,bl="+bl+"\t,kb="+kb+",\tbb="+bb);
//				System.out.println("crossx="+crossx);				
//			}
			return false;
		}else{
//			if(Math.abs(y1-starty)<1d){//调试，接近时打印
//				System.out.println("BLOCKED---line\t "+x1+"\t"+y1+"\t"+x2+"\t"+y2+"\t blocker\t "
//						+startx+"\t"+starty+"\t"+endx+"\t"+endy);
//				System.out.println("kl="+kl+"\t,bl="+bl+"\t,kb="+kb+",\tbb="+bb);
//				System.out.println("crossx="+crossx);				
//			}
			return true;
		}
			

	}
	
}


