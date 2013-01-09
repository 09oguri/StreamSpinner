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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class QueryNodeToolTip extends JToolTip{
	private JLabel label = null;

	public QueryNodeToolTip(){
		super();

		this.setOpaque(false);
		
		label = new JLabel();
		label.setOpaque(false);
		//label.setForeground(new Color(200,0,0));
		label.setForeground(Color.BLACK);
		this.setLayout(null);
		this.add(label);
	}

	public Dimension getPreferredSize(){
		Dimension d = super.getPreferredSize();
		return new Dimension((int)d.getWidth()+20,(int)d.getHeight()+20);
	}

	public void paintComponent(Graphics g){
		Component parent = this.getParent();
		if(parent != null){
			if(parent instanceof JComponent){
				JComponent p = (JComponent)parent;
				if(p.isOpaque()) p.setOpaque(false);
			}
		}

		Shape rect = new Rectangle2D.Float(
				0,
				0,
				this.getWidth()-1,
				this.getHeight()-1
				);

		Graphics2D g2 = (Graphics2D)g;
		//g2.setColor(new Color(150,150,255,220));
		g2.setColor(new Color(238,232,170,230));
		g2.fill(rect);

		g2.setColor(Color.BLUE);
		g2.setColor(new Color(255,165,0));
		g2.setStroke(new BasicStroke(5));
		g2.draw(rect);
		g2.setStroke(new BasicStroke(0));
		
		String text = this.getComponent().getToolTipText();
		label.setText(text);
		label.setBounds(0,0,this.getWidth(),this.getHeight());
	}

	public void setToolTipText(String str){
		super.setToolTipText(str);
	}
}
