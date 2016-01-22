package com.longbowzz.bptest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;




class Ball{
	int bID;
	boolean live=true;
	double m;
	int size=4;
	Color c;
	double sx,sy;
	double vx,vy;
	double ax,ay;
	int x,y;
	
	Ball(int size, double mass, double s_x, double s_y, double v_x, double v_y, Color col){
		//bID=bid;
		this.size=size;
		m=mass;
		sx=s_x; sy=s_y;		
		vx=v_x; vy=v_y;
		useSxyAsXy();
		c=col;
	}
	
	void setPointPos(Point p){
		x=p.x;
		y=p.y;
	}
	
	void useSxyAsXy(){
		x = (int) sx;
		y = (int) sy;
	}
	
	void draw(Graphics g){
			g.setColor(c);
			
			g.fillOval(x-size/2, y-size/2, size, size);
			g.drawOval(x-size/2, y-size/2, size, size);
			//g.fillRect(x-size/2, y-size/2, size, size);
	}
	
	
	void drawTail(Graphics g){
			g.setColor(c);

			g.fillRect(x, y, 1, 1);
	}

	void drawClear(Graphics g){
			g.setColor(Color.black);		
			if(size<2){
				g.drawOval(x-size/2, y-size/2, size, size);
			}
			else				
				g.fillOval(x-size/2, y-size/2, size, size);

//			g.drawOval(x-size/2, y-size/2, size, size);
			//g.fillRect(x-size/2, y-size/2, size, size);
	}
	
	void drawMass(Graphics g){
			g.setColor(c);
			g.drawString(""+(int)m,x+size/2, y-size/2);			
	}
  void drawBid(Graphics g){
			g.setColor(c);
			g.drawString("["+bID+"]",x+size/2+2, y+size/2+10);	  	
  }
  
  public String toString(){
	  return ""+"ball["+bID+"] x="+sx+", y="+sy+",vx="+vx+ ",vy="+vy+",ax="+ax+",ay="+ay;
  }
  
}