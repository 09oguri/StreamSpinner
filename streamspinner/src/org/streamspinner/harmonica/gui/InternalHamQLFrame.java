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

import org.streamspinner.connection.CQException;
import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.validator.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Locale;

public class InternalHamQLFrame extends JInternalFrame 
	implements MouseListener{

	private JPanel jContentPane = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JButton jButton1 = null;
	private JTextArea jTextArea = null;

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
			jButton.setText("送信");
			else
			jButton.setText("Send");
			jButton.setBounds(410,42,70,30);
			jButton.addMouseListener(this);
		}
		return jButton;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
			jScrollPane.setBounds(1,1,400,162);
		}
		return jScrollPane;
	}

	public void mouseClicked(MouseEvent e){
		JButton c = (JButton)e.getSource();

		if(!SwingUtilities.isLeftMouseButton(e)) return;

		if(c.getText().equals("Send") || c.getText().equals("送信")){
			sendQuery(getJTextArea().getText());
		}else if(c.getText().equals("Clear") || c.getText().equals("消去")){
			getJTextArea().setText("");
		}
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}

	private void sendQuery(String hamql){
		FeasibilityValidator fv = 
			HarmonicaManager.getFeasibilityValidator();

		try{
			fv.validateQuery(hamql);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jButton1.setText("消去");
			else
				jButton1.setText("Clear");

			jButton1.setBounds(410,92,70,30);
			jButton1.addMouseListener(this);
		}
		return jButton1;
	}

	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setBackground(java.awt.Color.white);
			jTextArea.setFont(new Font(null, Font.PLAIN, 16));
			jTextArea.setLineWrap(true);
		}
		return jTextArea;
	}

	public InternalHamQLFrame(){
		super("HamQL Query Form", false, true, false, true);
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			this.setTitle("HamQL問合せフォーム");
		}
		initialize();
	}

	public void setHamQL(String hamql){
		getJTextArea().setText(hamql);
	}

	private void initialize() {
		this.setSize(500, 200);
		this.setContentPane(getJContentPane());

		try{
			Image img = ImageFactory.getInstance().getImage
				(ImageType.HAMQL_ICON);
			Icon ico = new ImageIcon(img);
			super.setFrameIcon(ico);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new java.awt.Color(204,204,255));
			jContentPane.add(getJScrollPane());
			jContentPane.add(getJButton());
			jContentPane.add(getJButton1());
		}
		return jContentPane;
	}
}
