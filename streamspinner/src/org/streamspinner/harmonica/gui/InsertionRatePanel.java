/*
 * Copyright 2005-2009 StreamSpinner Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streamspinner.harmonica.gui;

import org.streamspinner.harmonica.*;

import javax.swing.*;
import java.util.*;
import java.awt.*;

public class InsertionRatePanel extends JComponent {
	private Map<Integer, Double> insertion_rate_map = null;
	private double x_axis_size = 0;
	private double y_axis_size = 0;
	private int width = 0;
	private int height = 0;
	private int bar_size = 100;
	private double max_x = 0.0;
	private double max_y = 0.0;
	private DBConnector con = null;

	public InsertionRatePanel(DBConnector con, int width, int height){
		super();

		this.con = con;
		this.width = width;
		this.height = height;
		
		init();
	}

	private void init(){
		updateInsertionRate();
	}

	public void updateInsertionRate(){
		insertion_rate_map = con.getInsertionRate();
		calcurateSize();

		repaint();
	}

	public void paint(Graphics g){
		super.paint(g);

		paintArrow(g);

		paintValue(g);

		paintMax(g);
	}

	private void paintMax(Graphics g){
		g.drawString
			(String.valueOf(0),
			 bar_size/2-20,
			 height-bar_size/2+20);
		g.drawString
			(String.valueOf(max_y),
			 bar_size/2-40, 
			 bar_size/2+10);
		g.drawString
			(String.valueOf((int)max_x),
			 width-bar_size/2-10,
			 height-bar_size/2+20);
	}

	private void paintValue(Graphics g){
		double ppx = -1;
		double ppy = -1;

		for(int i : insertion_rate_map.keySet()){
			double px = i;
			double py = insertion_rate_map.get(i);
			paintPoint(g, px,py, ppx, ppy);
			ppx = px;
			ppy = py;
		}
	}
	private void paintPoint
		(Graphics g, double px, double py, double ppx, double ppy){

		double dx = (double)px*x_axis_size + bar_size/2.0;
		double dy = height - bar_size/2.0 - py*y_axis_size;

		g.fillArc((int)dx-3,(int)dy-3,6,6,0,360);

		if(ppx < 0 || ppy < 0) return;

		double ddx = (double)ppx*x_axis_size + bar_size/2.0;
		double ddy = height - bar_size/2.0 - ppy*y_axis_size;
		g.drawLine((int)ddx, (int)ddy, (int)dx, (int)dy);
	}

	private void drawArrow
		(Graphics2D g, 
		 double x0, 
		 double y0, 
		 double x1, 
		 double y1, 
		 int l)
	{
		double theta;
    	int x,y;
	    double dt = Math.PI / 6.0;
   		theta = Math.atan2((double)(y1-y0),(double)(x1-x0));
   		g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);
   		x = (int)(x1-l*Math.cos(theta-dt));
		y = (int)(y1-l*Math.sin(theta-dt));
    	g.drawLine((int)x1,(int)y1,x,y);
    	x = (int)(x1-l*Math.cos(theta+dt));
   		y = (int)(y1-l*Math.sin(theta+dt));
    	g.drawLine((int)x1,(int)y1,x,y);
	}

	private void paintArrow(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		// y axis
		drawArrow(g2,
				bar_size/2.0,
				height - bar_size/2.0,
				bar_size/2.0,
				bar_size/2.0,
				10);

		// x axis
		drawArrow(g2,
				bar_size/2.0,
				height - bar_size/2.0,
				width - bar_size/2.0,
				height - bar_size/2.0,
				10);

	}

	private void calcurateSize(){
		TreeMap<Integer, Double> hm = 
			(TreeMap<Integer, Double>)insertion_rate_map;

		max_x = 0.0;
		for(int i : hm.keySet()){
			if(i > max_x) max_x = i;
		}
		x_axis_size = (width - bar_size) / max_x;

		max_y = 0.0;
		for(double d : hm.values()){
			if(d > max_y) max_y = d;
		}
		y_axis_size = (height - bar_size) / max_y;
	}

	public Dimension getPreferrdSize(){
		return new Dimension(width, height);
	}
}
