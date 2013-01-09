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

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConnectorCreaterDialog extends JDialog 
	implements ActionListener, ItemListener{

	private JPanel jContentPane = null;
	private JButton jButton = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JComboBox jComboBox = null;
	private JTextField jTextField = null;
	private JTextField jTextField1 = null;
	private JTextField jTextField2 = null;
	private JTextField jTextField3 = null;
	private JTextField jTextField4 = null;

	public ConnectorCreaterDialog(Frame owner, String title) {
		super(owner, title);
		initialize();
	}

	private void initialize() {
		this.setSize(500, 300);
		this.setModal(true);
		this.setResizable(false);
		this.setContentPane(getJContentPane());
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel5 = new JLabel();
			jLabel5.setBounds(new java.awt.Rectangle(33,190,119,25));
			jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel5.setText("追加オプション");
			else
				jLabel5.setText("Additional Options");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new java.awt.Rectangle(33,155,119,25));
			jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel4.setText("接続用パスワード");
			else
				jLabel4.setText("Password");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new java.awt.Rectangle(33,120,119,25));
			jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel3.setText("接続ユーザ名");
			else
				jLabel3.setText("User");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new java.awt.Rectangle(33,85,119,25));
			jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel2.setText("データベース名");
			else
				jLabel2.setText("Name");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new java.awt.Rectangle(33,50,119,25));
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel1.setText("データベースのアドレス");
			else
				jLabel1.setText("URL");
			jLabel = new JLabel();
			jLabel.setBounds(new java.awt.Rectangle(33,15,119,25));
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jLabel.setText("データベースの製品名");
			else
				jLabel.setText("Product");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJComboBox(), null);
			jContentPane.add(getJTextField(), null);
			jContentPane.add(getJTextField1(), null);
			jContentPane.add(getJTextField2(), null);
			jContentPane.add(getJTextField3(), null);
			jContentPane.add(getJTextField4(), null);
		}
		return jContentPane;
	}

	public void itemStateChanged(ItemEvent e){
		String product = (String)getJComboBox().getSelectedItem();

		if(product.equals("MySQL") || product.equals("PostgreSQL")){
			getJTextField().setText("localhost");
			getJTextField1().setText("harmonica");
			getJTextField2().setText("harmonica");
			getJTextField3().setText("harmonica");
			getJTextField4().setText
				("useUnicode=true&characterEncoding=SJIS");
		}else if(product.equals("HSQLDB")){
			getJTextField().setText("localhost");
			getJTextField1().setText("conf/db/harmonica");
			getJTextField2().setText("sa");
			getJTextField3().setText("");
			getJTextField4().setText("");
		}else{
			getJTextField().setText("localhost");
			getJTextField1().setText("harmonica");
			getJTextField2().setText("harmonica");
			getJTextField3().setText("harmonica");
			getJTextField4().setText("");
		}
	}

	public void actionPerformed(ActionEvent e){
		Thread t = new Thread(){
			public void run(){
				getJButton().setEnabled(false);

				String connector = null;
				String url = null;
				String db = null;
				String user = null;
				String password = null;
				String option = null;

				String s = (String)getJComboBox().getSelectedItem();
				if(s.equals("MySQL")) 
					connector = 
						"org.streamspinner.harmonica.MySQLConnector";
				else if(s.equals("PostgreSQL")) 
					connector = 
						"org.streamspinner.harmonica.PostgreSQLConnector";
				else if(s.equals("SQL Server")) 
					connector = 
						"org.streamspinner.harmonica.SQLServerConnector";
				else
					connector = 
						"org.streamspinner.harmonica.HSQLDBConnector";

				url = (String)getJTextField().getText();
				db = (String)getJTextField1().getText();
				user = (String)getJTextField2().getText();
				password = (String)getJTextField3().getText();
				option = (String)getJTextField4().getText();

				StreamArchiver arc = HarmonicaManager.getStreamArchiver();
				try{
					arc.addDB(connector,url,db,user,password,option,null);
				}catch(HarmonicaException e){
					HarmonicaManager.createdException(e);
				}

				getJButton().setEnabled(true);
				setVisible(false);
			}
		};

		t.start();
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new java.awt.Rectangle(340,224,120,26));
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jButton.setText("接続");
			else
				jButton.setText("Connect");
			jButton.addActionListener(this);
		}
		return jButton;
	}

	private JComboBox getJComboBox() {
		if (jComboBox != null) return jComboBox;
		
		String[] conns = {
			"HSQLDB",
			"MySQL",
			"PostgreSQL",
			"SQL Server"
		};

		jComboBox = new JComboBox(conns);
		jComboBox.setBounds(new java.awt.Rectangle(156,16,305,23));
		jComboBox.addItemListener(this);
		jComboBox.setSelectedItem("HSQLDB");

		return jComboBox;
	}

	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(new java.awt.Rectangle(156,51,305,23));
			jTextField.setText("localhost");
		}
		return jTextField;
	}

	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new java.awt.Rectangle(156,86,305,23));
			jTextField1.setText("conf/db/harmonica");
		}
		return jTextField1;
	}

	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new java.awt.Rectangle(156,121,305,23));
			jTextField2.setText("sa");
		}
		return jTextField2;
	}

	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JPasswordField();
			jTextField3.setBounds(new java.awt.Rectangle(156,156,305,23));
			jTextField3.setText("");
		}
		return jTextField3;
	}

	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setBounds(new java.awt.Rectangle(156,191,305,23));
			jTextField4.setText("");
		}
		return jTextField4;
	}
}
