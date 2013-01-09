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
import org.streamspinner.engine.*;
import org.streamspinner.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CalculateInsertionRateFrame extends JDialog {

	private JPanel jContentPane = null;
	private JComboBox jComboBox = null;
	private JComboBox jComboBox1 = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JLabel jLabel1 = null;
	private JDialog me = null;
	private String[] s1 = null;
	private String[] s2 = null;
	private int[] i1 = null;
	private int[] i2 = null;
	private DBConnector con = null;

	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox(s1);
			jComboBox.setBounds(new Rectangle(32,15,218,27));
		}
		return jComboBox;
	}

	private JComboBox getJComboBox1() {
		if (jComboBox1 == null) {
			jComboBox1 = new JComboBox(s2);
			jComboBox1.setBounds(new Rectangle(304,15,218,27));
		}
		return jComboBox1;
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton(){
				String head = "[Harmonica] ";
				private void execCalc(){
					Thread t = new Thread(){
						public void run(){
							getJButton1().setEnabled(false);
							getJButton().setEnabled(false);

							HarmonicaMonitor m = 
								HarmonicaManager.getHarmonicaMonitor();
							JTextArea a = m.getHarmonicaTerminal();
							String mes = "Recalculating insertion rate.";
							String db_name = "("+con.toString()+")";
							a.setText(head + mes + db_name);

							int num = 
								i1[getJComboBox().getSelectedIndex()];
							int sec = 
								i2[getJComboBox1().getSelectedIndex()];

							Vector<Schema> v = new Vector<Schema>();

							int ad = 100/(num-1);
							for(int i=0;i<num;i++){
								String[] names = new String[2+i*ad];
								String[] types = new String[2+i*ad];
								for(int j=0;j<i*ad+2;j++){
									names[j] = "t.n"+j;
									types[j] = DataTypes.LONG;
								}
								Schema s = new Schema("t",names,types);
								v.add(s);
							}

							con.updateInsertionRate
								(v.toArray(new Schema[1]),sec*1000);

							getJButton1().setEnabled(true);
							getJButton().setEnabled(true);

							mes = "Calculated.("+con.toString()+")";
							a.setText(head + mes);
						}
					};

					t.start();
				}
				protected void processMouseEvent(MouseEvent e){
					super.processMouseEvent(e);

					if(e.getID() == e.MOUSE_CLICKED){
						execCalc();
					}
				}
			};
			jButton.setBounds(new Rectangle(285,75,117,27));
			jButton.setMnemonic(KeyEvent.VK_O);
			jButton.setText("OK");
		}
		return jButton;
	}

	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton(){
				protected void processMouseEvent(MouseEvent e){
					super.processMouseEvent(e);

					if(e.getID() == e.MOUSE_CLICKED){
						me.setVisible(false);
						me.dispose();
					}
				}
			};
			jButton1.setBounds(new Rectangle(429,75,117,27));
			jButton1.setMnemonic(KeyEvent.VK_C);
			jButton1.setText("Close");
		}
		return jButton1;
	}

	public CalculateInsertionRateFrame(Frame f, DBConnector con){
		super(f,true);
		this.con = con;
		initialize();
	}

	private void initialize() {
		s1 = new String[]{"2 points",
			"5 points",
			"10 points",
			"15 points",
			"20 points",
			"25 points",
			"30 points"
			};
		i1 = new int[]{2,5,10,15,20,25,30};
		s2 = new String[]{"1 second",
			"10 seconds",
			"30 seconds",
			"1 minute",
			"2 minutes", 
			"3 minutes",
			"4 minutes",
			"5 minutes"
			};
		i2 = new int[]{1,10,30,60,120,180,240,300};

		int dx = 570;
		int dy = 160;
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = screen.width;
		int y = screen.height;
		this.setBounds((x-dx)/2,(y-dy)/2,dx,dy);
		this.setResizable(false);
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			this.setTitle(con.toString()+" - 書き込みレート更新用フォーム");
		else
			this.setTitle(con.toString()+" - Update Insertion Rate Map Form");
		this.setContentPane(getJContentPane());
		
		me = this;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel1 = new JLabel();
			jLabel1.setBounds(new java.awt.Rectangle(266,15,25,27));
			jLabel1.setText("*");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJComboBox(), null);
			jContentPane.add(getJComboBox1(), null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(jLabel1, null);
		}
		return jContentPane;
	}

}
