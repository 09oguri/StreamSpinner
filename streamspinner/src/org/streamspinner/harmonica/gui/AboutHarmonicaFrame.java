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

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Locale;

public class AboutHarmonicaFrame extends JDialog {

	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JTextArea jTextArea = null;

	private JTextArea getJTextArea() {
		StringBuilder buf = new StringBuilder();
		buf.append("Harmonica [ ver. ");
		buf.append(HarmonicaManager.version);
		buf.append(" ]\n\n");
		buf.append("Powered By StreamSpinner");
		buf.append(" (http://www.streamspinner.org/)");
		if (jTextArea == null) {
			jTextArea = new JTextArea(){
				protected void processMouseEvent(MouseEvent e){
					super.processMouseEvent(e);

					if(e.getID() == e.MOUSE_CLICKED){
						terminate();
					}
				}
			};
			jTextArea.setEditable(false);
			jTextArea.setText(buf.toString());
			jTextArea.setBackground(SystemColor.control);
			jTextArea.setBounds(0,0,0,320);
			jTextArea.setOpaque(false);
		}
		return jTextArea;
	}

	public AboutHarmonicaFrame() {
		super();
		initialize();
	}
	public AboutHarmonicaFrame(Frame f){
		super(f);
		initialize();
	}

	protected void processMouseEvent(MouseEvent e){
		super.processMouseEvent(e);

		if(e.getID() == e.MOUSE_CLICKED){
			terminate();
		}
	}
	public void terminate(){
		super.setVisible(false);
		super.dispose();
	}
	private void initialize() {
		int dx = 518;
		int dy = 450;
		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = screen.width;
		int y = screen.height;
		this.setBounds((x-dx)/2,(y-dy)/2,dx,dy);
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			this.setTitle("Harmonica‚É‚Â‚¢‚Ä");
		else
			this.setTitle("About Harmonica");
		this.setContentPane(getJContentPane());
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		this.setResizable(false);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			try{
				BufferedImage img = 
					(BufferedImage)ImageFactory.getInstance().getImage
					(ImageType.HARMONICA_LOGO);
				jLabel.setIcon(new ImageIcon(img));
				jLabel.setBounds(0,0,img.getWidth(),img.getHeight());
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
			jContentPane = new JPanel();
			jContentPane.add(jLabel);
			jContentPane.add(getJTextArea());
		}
		return jContentPane;
	}
}
