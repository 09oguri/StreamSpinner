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

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * DBTreeの描画に用いる部分を設定するクラス。
 *
 * @author snic
 * @version 1.0
 */
public class HarmonicaTreeCellRenderer 
	extends JLabel 
	implements TreeCellRenderer {
	private Vector<String> v = null;
	private Color selected_bgcolor = null;
	private Color arrived_bgcolor = null;
	private Color arrived_selected_bgcolor = null;
	private Font font = null;
	private Font main_font = null;
	private Color main_color = null;

	public HarmonicaTreeCellRenderer(){
		super();
		init();
	}

	private void init(){
		v = new Vector<String>();
		//font = new Font(null, Font.PLAIN, 16);
		main_color = new Color(200,0,0);
		selected_bgcolor = SystemColor.activeCaption;
		//selected_bgcolor = new Color(120,120,200);
		arrived_bgcolor = new Color(200,120,120);
		arrived_selected_bgcolor = new Color(120,120,200);

		super.setOpaque(true);
		super.setFont(font);
	}

	public void paint(Graphics g){
		super.paint(g);

		paint_selected_border(g);
	}

	private void paint_selected_border(Graphics g){
		Color c = getBackground();
		
		if(c != selected_bgcolor && c != arrived_selected_bgcolor)
			return;

		float dot[] = {1.0f};
		BasicStroke stroke = 
			new BasicStroke(
					1.0f, 
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 
					10.0f, 
					dot, 
					0.0f);

		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.BLACK);
		g2.setStroke(stroke);
		int[] x = {
			0, 
			getWidth()-1, 
			getWidth()-1, 
			0, 
			0
		};

		int[] y = {
			0,
			0,
			getHeight()-1,
			getHeight()-1,
			0
		};

		Polygon pol = new Polygon(x, y, x.length);
		g2.draw(pol);
	}
	
	public Component getTreeCellRendererComponent(
			JTree tree, Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		HarmonicaDBViewer view = (HarmonicaDBViewer)tree;

		super.setText(value.toString() + "  ");
		super.setFont(font);

		if(selected){
			super.setBackground(selected_bgcolor);
			super.setForeground(SystemColor.activeCaptionText);
		}else{
			super.setBackground(Color.WHITE);
			super.setForeground(Color.BLACK);
		}

		if(row == 0){ // HARMONICA
			setIconImage(ImageType.DBS_ICON);
			return this;
		}

		if(!leaf || view.getDBNames().contains(value.toString())){ // DB
			
			if(!value.toString().equals(view.getMainDB())){ // not main
				setIconImage(ImageType.DB_ICON);
			}else{ // main
				setIconImage(ImageType.DB_MAIN_ICON);
				super.setForeground(main_color);
				Graphics g = super.getGraphics();
			}

			return this;
		}

		// Table
		if(v.contains(value.toString())){
			setIconImage(ImageType.TABLE_UPDATE_ICON);

			if(selected){
				super.setBackground(arrived_selected_bgcolor);
			}else{
				super.setBackground(arrived_bgcolor);
			}			
		}else{
			setIconImage(ImageType.TABLE_ICON);
		}

		return this;
	}

	private void setIconImage(ImageType type){
		Image img = null;
			
		try{
			img = ImageFactory.getInstance().getImage(type);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		try{
		Icon ico = new ImageIcon(img);
		super.setIcon(ico);
		}catch(NullPointerException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
	}

	public void addTarget(String target){
		v.add(target);
	}
	public void removeTarget(String target){
		v.remove(target);
	}
	
}
