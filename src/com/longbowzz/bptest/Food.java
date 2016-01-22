package com.longbowzz.bptest;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Food extends Goo{
	
	/**
	 * 构造祖代
	 * @param id
	 * @param initball
	 */
	public Food(int id, int w, int h){
		ID = id;
		parentID = id;
		ancestorID = id;
		generation = 0;
		attrSTR = 0;
		ball = new Ball(1, 0.01f, Math.random()*w, Math.random()*h, 0, 0, Color.green);
		life = MAX_LIFE;
	}	
		
	
	@Override
	public boolean canMate() {
		// TODO Auto-generated method stub
		return false;
	}
	

	void draw(Graphics g) {
		Color c = ball.c;
		int x = ball.x;
		int y = ball.y;
		int size = 1;
		g.setColor(c);
		
//		g.fillOval(x-size/2, y-size/2, size, size);
//		g.drawOval(x-size/2, y-size/2, size, size);
		//画绿十字
		g.drawLine(x-1, y-1, x+1, y+1);
		g.drawLine(x-1, y+1, x+1, y-1);
		
	}
	
}