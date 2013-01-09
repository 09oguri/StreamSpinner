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

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class HarmonicaTableCellRenderer implements TableCellRenderer{
	private JCheckBox chb = null;
	private JLabel lbl = null;
	private Color bgcolor = null;
	private Color fgcolor = null;
	private Color selected_color = null;
	private Color selected_text_color = null;
	private Color pure_selected_color = null;
	private Color new_color = null;

	public HarmonicaTableCellRenderer(){
		super();
		chb = new JCheckBox(){
			public void paint(Graphics g){
				super.paint(g);

				paint_selected_border(g);
			}

			private void paint_selected_border(Graphics g){
				Color c = getBackground();
		
				if(c != selected_color) return;

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
				g2.setColor(SystemColor.activeCaptionBorder);
				g2.setStroke(stroke);
				int[] x = {0, getWidth()-1, getWidth()-1, 0, 0};

				int[] y = {0, 0, getHeight()-1, getHeight()-1, 0};

				Polygon pol = new Polygon(x, y, x.length);
				g2.draw(pol);
			}
		};

		chb.setBackground(Color.WHITE);
		chb.setHorizontalAlignment(SwingConstants.CENTER);

		lbl = new JLabel(){
			public void paint(Graphics g){
				super.paint(g);

				paint_selected_border(g);
			}

			private void paint_selected_border(Graphics g){
				Color c = getBackground();
		
				if(c != selected_color) return;

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
				g2.setColor(SystemColor.activeCaptionBorder);
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
		};

		lbl.setBackground(Color.WHITE);
		lbl.setOpaque(true);
		bgcolor = Color.WHITE;
		fgcolor = Color.BLACK;
		selected_color = SystemColor.activeCaption;
		selected_text_color = SystemColor.activeCaptionText;
		pure_selected_color = SystemColor.inactiveCaption;
		new_color = new Color(255,255,180);
	}

	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int column)
	{
		HarmonicaQueryViewer h = (HarmonicaQueryViewer)table;
		if(column < 2){
			if(hasFocus){
				lbl.setBackground(selected_color);
				lbl.setForeground(selected_text_color);
			}else{
				if(h.getNewRows().contains(row))
					lbl.setBackground(new_color);
				else
					lbl.setBackground(bgcolor);
				lbl.setForeground(fgcolor);

				if(isSelected)
					lbl.setBackground(pure_selected_color);
			}

			lbl.setIcon(null);
			lbl.setText(value.toString());
			if(column == 1)
				lbl.setHorizontalAlignment(SwingConstants.LEFT);
			else
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
			return lbl;

		}else{
			boolean b = ((Boolean)value).booleanValue();
			if(b){
				chb.setSelected(true);
			}else{
				chb.setSelected(false);
			}

			if(h.selected_col == column && h.selected_row == row){
				chb.setBackground(selected_color);
				chb.setForeground(selected_text_color);
			}else{
				if(h.getNewRows().contains(row))
					chb.setBackground(new_color);
				else
					chb.setBackground(bgcolor);
				chb.setForeground(fgcolor);

				if(isSelected)
					chb.setBackground(pure_selected_color);
			}

			return chb;
		}
	}
}
