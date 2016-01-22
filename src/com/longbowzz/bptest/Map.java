package com.longbowzz.bptest;

import java.util.ArrayList;

public class Map{
	
	//region纵横数量
	public static int REGION_ROW = 4;
	public static int REGION_COL = 4;
	public Region[][] regions = new Region[REGION_COL][REGION_ROW];
	
	private int width;
	private int height;
	private int regionWidth;
	private int regionHeight;
	
	public Map(int w, int h){
		width = w ;
		height = h;
		regionWidth = width/REGION_COL;
		regionHeight = height/REGION_ROW;
		for(int i=0; i<REGION_ROW; i++){
			for(int j=0; j<REGION_COL; j++){
				regions[i][j] = new Region(j*regionWidth, i*regionHeight);
			}
		}
	}
	
	/**
	 * 每帧清空region,重新生成goolist
	 */
	public void clearRegion(){
		for(int i=0; i<REGION_ROW; i++){
			for(int j=0; j<REGION_COL; j++){
				regions[i][j].goolist.clear();
			}
		}		
	}
	
	/**
	 * 将goo添加到位置所在的region
	 * @param goo
	 */
	public void addGoo2Region(Goo goo){
		int i = (int) (goo.ball.sy/regionHeight);
		int j = (int) (goo.ball.sx/regionWidth);
		if(i<0)i=0;
		if(j<0)j=0;
		if(i>=REGION_ROW)i=REGION_ROW-1;
		if(j>=REGION_COL)j=REGION_COL-1;
		regions[i][j].goolist.add(goo);
	}
	
	/**
	 * 寻找离指定坐标最近的goo
	 * @param id
	 * @param x
	 * @param y
	 * @return
	 */
	public Goo findNearestGoo(int id, double x, double y){
		//首先判断本region是否有goo
		int i = (int) (y/regionHeight);
		int j = (int) (x/regionWidth);
		if(i >= REGION_ROW || j >=REGION_COL || i<0 ||j<0)
			return null;
		double mindist2 = Double.MAX_VALUE;
		Goo nearest = null;
		//先在本区找
		for(Goo goo : regions[i][j].goolist){
			double dx = goo.ball.x - x;
			double dy = goo.ball.y - y;
			double dist2 = dx*dx + dy*dy;
			if(dist2 < mindist2  && id != goo.ID){
				nearest = goo;
				mindist2 = dist2;
			}
		}
		//无论本region有没有发现，寻找相邻的region
//		if(nearest == null){
			for(int di=-1; di<=1;di++){
				for(int dj=-1; dj<=1; dj++){
					if(i+di<0 || i+di>=REGION_ROW || j+dj<0 || j+dj>=REGION_COL)
						continue;
					else{
						for(Goo goo : regions[i+di][j+dj].goolist){
							double dx = goo.ball.x - x;
							double dy = goo.ball.y - y;
							double dist2 = dx*dx + dy*dy;
							if(dist2 < mindist2 && id != goo.ID){
								nearest = goo;
								mindist2 = dist2;
							}
						}
					}
				}
				
			}
//		}
		
		return nearest;
	}
	
	
	public static class Region{
		public int startX; //左上角x坐标
		public int startY;	//左上角y坐标
		public ArrayList<Goo> goolist;
		
		public Region(int x, int y){
			startX = x;
			startY = y;
			goolist = new ArrayList<Goo>();
		}
	}
	
}