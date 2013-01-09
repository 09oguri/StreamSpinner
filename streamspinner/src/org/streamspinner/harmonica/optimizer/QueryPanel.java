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
package org.streamspinner.harmonica.optimizer;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import java.awt.event.*;

import org.streamspinner.harmonica.gui.ImageType;
import org.streamspinner.harmonica.gui.ImageFactory;
import org.streamspinner.harmonica.HarmonicaException;

public class QueryPanel extends JPanel implements ActionListener, MouseMotionListener{
	public PaintNode[][] tile = null;
	public int nodex = 90;
	public int nodey = 60;
	public int vacancy = 20;
	public ProcessResult result = null;
	public int cost_line = 80;
	public JPopupMenu popup_menu = null;

	public QueryPanel(PaintNode[][] tile, ProcessResult result){
		super();

		this.tile = tile;
		this.result = result;
		this.addMouseMotionListener(this);

		init();
	}

    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
		int x = (e.getX())/(nodex+vacancy);
		int y = (e.getY()-cost_line)/(nodey+vacancy);
		
		if(x < 0 || y < 0 || x >= tile.length || y >= tile[0].length){
			setToolTipText(null);
			return;
		}
		if(tile[x][y] == null){
			setToolTipText(null);
		}else{
			StringBuilder buf = new StringBuilder();
			buf.append("Operator ID : ");
			buf.append(tile[x][y].node.id);
			if(tile[x][y].node.type.equals("selection") || tile[x][y].node.type.equals("join")){
				buf.append(" [");
				buf.append(tile[x][y].node.predicate);
				buf.append("]");
			}else if(tile[x][y].node.type.equals("projection")){
				buf.append(" [");
				buf.append(tile[x][y].node.attribute);
				buf.append("]");
			}else if(tile[x][y].node.type.equals("store")){
				buf.append(" [");
				buf.append(tile[x][y].node.value);
				buf.append("]");
			}else if(tile[x][y].node.type.equals("source")){
				buf.append(" [");
				buf.append(tile[x][y].node.output_schema.toString());
				buf.append("]");
			}
			setToolTipText(buf.toString());
		}
	}

	private void init(){
		// size
		this.setSize(tile.length*(nodex+vacancy),tile[0].length*(nodey+vacancy)+cost_line+50);
		this.setPreferredSize(new Dimension(tile.length*(nodex+vacancy),tile[0].length*(nodey+vacancy)+cost_line+50));

		//JButton btn = new JButton("Show XML");
		JMenuItem btn = new JMenuItem("Show XML");
		btn.addActionListener(this);
		btn.setActionCommand("SHOW_XML");
		//JButton btn2 = new JButton("Show XML applied View");
		JMenuItem btn2 = new JMenuItem("Show XML applies View");
		btn2.addActionListener(this);
		btn2.setActionCommand("SHOW_XML_APPLIED_VIEW");

		popup_menu = new JPopupMenu();
		popup_menu.add(btn);
		popup_menu.add(btn2);

		this.setComponentPopupMenu(popup_menu);

		repaint();
	}

	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals("SHOW_XML")){
			JTextArea area = new JTextArea(result.dag.toXML());
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			area.setTabSize(2);
			JScrollPane jp = new JScrollPane(area);
			JFrame frame = new JFrame("DAG "+result.dag.id);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(500,350);
			frame.getContentPane().add(jp,BorderLayout.CENTER);
			frame.setLocationRelativeTo(null);
			frame.setAlwaysOnTop(true);
			frame.setVisible(true);
		}else if(e.getActionCommand().equals("SHOW_XML_APPLIED_VIEW")){
			JTextArea area = new JTextArea(result.dag.toXMLAppliedView());
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			area.setTabSize(2);
			JScrollPane jp = new JScrollPane(area);
			JFrame frame = new JFrame("DAG "+result.dag.id + " applies view");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(500,350);
			frame.getContentPane().add(jp,BorderLayout.CENTER);
			frame.setLocationRelativeTo(null);
			frame.setAlwaysOnTop(true);
			frame.setVisible(true);
		}
	}

    private void drawArrow
        (Graphics2D g,
         double x0,
         double y0,
         double x1,
         double y1,
         int l)
    {
		Stroke cs = g.getStroke();
		g.setStroke(new BasicStroke(2,2,2,2));

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
		g.setStroke(cs);
    }

	private void paintArrow(PaintNode n, Graphics g){
		if(n == null) return;
		Graphics2D g2 = (Graphics2D)g;

		int p1 = 12;
		int p2 = 25;
		int p3 = 38;
		int line = 8;

		for(PaintNode pn : n.next){
			double x1 = n.x*(nodex+vacancy) + (nodex-50)/2;
			double y1 = n.y*(nodey+vacancy) + 10 + cost_line;
			double x2 = pn.x*(nodex+vacancy) + (nodex-50)/2;
			double y2 = pn.y*(nodey+vacancy) + 60 + cost_line;
			if(pn.x < n.x - 1){
				x1 += p1;
				x2 += p3;
			}else if(pn.x < n.x + 2){
				x1 += p2;
				x2 += p2;
			}else{
				x1 += p3;
				x2 += p1;
			}
			drawArrow(g2,x1,y1,x2,y2,line);
		}
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);

		paintPlanRate(g);

		try{
			for(int j=0;j<tile[0].length;j++){
				for(int i=tile.length-1;i>=0;i--){
					paintInfo(tile[i][j], g);
					paintNode(tile[i][j], g);
					paintArrow(tile[i][j], g);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void paintPlanRate(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		Font font = new Font("Verdana",Font.PLAIN,12);
		g2.setFont(font);

		g2.drawString("Is Feasible : " + result.isFeasible, 10 , 15);
		g2.drawString("Processing Rate : "+result.processing_cost, 10, 30);
		g2.drawString("Write Rate : " + result.inserting_cost, 10, 45);
		g2.drawString("View Processing Rate : " + result.cost_after_reading, 10, 60);

	}

	private void paintInfo(PaintNode n, Graphics g){
		if(n == null) return;
		Graphics2D g2 = (Graphics2D)g;

		Font font = new Font("Verdana",Font.PLAIN,10);
		//Font font = new Font("MS UI Gothic",Font.PLAIN,12);
		g2.setFont(font);
		
		String[] buf = null;
		int start = 0;
		int str_max = 8;
		if(n.node.type.equals("source")){
			buf = new String[3];
			buf[0] = n.node.output_schema.toShortString();
			buf[1] = "É…="+n.node.output_rate;
			buf[2] = "w="+n.node.active_tuple;
			start = 28;
		}else if(n.node.type.equals("store")){
			buf = new String[2];
			buf[0] = n.node.value;
			buf[1] = "I="+n.node.tuple_rate;
			start = 35;
		}else if(n.node.type.equals("selection")){
			buf = new String[4];
			buf[0] = n.node.predicate;
			buf[1] = "f="+n.node.selectivity;
			buf[2] = "É…="+n.node.output_rate;
			buf[3] = "w="+n.node.active_tuple;
			start = 22;
		}else if(n.node.type.equals("projection")){
			buf = new String[3];
			buf[0] = n.node.attribute;
			buf[1] = "É…="+n.node.output_rate;
			buf[2] = "w="+n.node.active_tuple;
			start = 28;
		}else if(n.node.type.equals("join")){
			buf = new String[4];
			buf[0] = n.node.predicate;
			buf[1] = "f="+n.node.selectivity;
			buf[2] = "É…="+n.node.output_rate;
			buf[3] = "w="+n.node.active_tuple;
			start = 22;
		}else{
		}

		if(buf == null) return;

		for(int i=0; i<buf.length; i++){
			if(n.x < tile.length-1 && tile[n.x+1][n.y] != null){
				if(buf[i].length() > str_max + 2){
					buf[i] = buf[i].substring(0,str_max) + "...";
				}
			}

			int x = n.x*(nodex+vacancy)+(nodex-50)/2+50;
			int y = n.y*(nodey+vacancy)+start+i*12 + cost_line;
			g2.drawString(buf[i],x,y);
		}
	}

	private void paintNode(PaintNode n, Graphics g) throws Exception{
		if(n == null) return;

		Image img = null;
		if(n.node.type.equals("source")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_INFORMATION);
		}else if(n.node.type.equals("root")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_ROOT);
		}else if(n.node.type.equals("selection")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_SELECTION);
		}else if(n.node.type.equals("projection")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_PROJECTION);
		}else if(n.node.type.equals("join")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_JOIN);
		}else if(n.node.type.equals("store")){
			img = ImageFactory.getInstance().getImage(ImageType.OPT_STORE);
		}

		int x_axis = (nodex+vacancy)*n.x;
		int y_axis = (nodey+vacancy)*n.y;

		g.drawImage(img, x_axis+(nodex-50)/2, y_axis+10+cost_line, 50, 50, this);
	}
}
