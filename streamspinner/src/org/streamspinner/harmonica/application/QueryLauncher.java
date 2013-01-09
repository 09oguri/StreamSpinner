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
package org.streamspinner.harmonica.application;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;


public class QueryLauncher extends JFrame {

	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem jOpenItem = null;
	private JTextField jTextField = null;
	private JButton jButton = null;
	private int location=0;
	private Vector<CQGraphTerminal> v = null;

	private JMenuItem getJOpenItem() {
		if (jOpenItem == null) {
			jOpenItem = new JMenuItem();
			jOpenItem.setText("新規問合せフォーム(T)");
			jOpenItem.setMnemonic(java.awt.event.KeyEvent.VK_T);
			jOpenItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					show_graph_terminal();
				}
			});
		}
		return jOpenItem;
	}

	private JTextField getJTextField(){
		if(jTextField != null) return jTextField;

		jTextField = new JTextField();
		jTextField.setText("localhost");

		return jTextField;
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("新規問合せフォーム");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					show_graph_terminal();
				}
			});
		}
		return jButton;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			String laf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(laf);	
		}catch(Exception e){}

		QueryLauncher application = new QueryLauncher();
		application.setVisible(true);
	}

	/**
	 * This is the default constructor
	 */
	public QueryLauncher() {
		super();
		initialize();
	}

	private void initialize() {
		v = new Vector<CQGraphTerminal>();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(getJJMenuBar());
		this.setSize(250, 150);
		this.setContentPane(getJContentPane());
		this.setTitle("ストリームビューア");
		this.addWindowListener(new java.awt.event.WindowAdapter() {      
			public void windowClosing(java.awt.event.WindowEvent e) {
				terminate();
			}
		});
	}

	private void show_graph_terminal(){
		CQGraphTerminal terminal = 
			new CQGraphTerminal(getJTextField().getText());
		v.add(terminal);
		location += 10;
		terminal.setLocation(location,location);
		terminal.setVisible(true);
	}

	private void terminate(){
		for(CQGraphTerminal c : v){
			c.terminate();
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJTextField(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJButton(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
		}
		return jJMenuBar;
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("ファイル(F)");
			fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);
			fileMenu.add(getJOpenItem());
			fileMenu.addSeparator();
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("終了(X)");
			exitMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_X);
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					terminate();
					System.exit(0);
				}
			});
		}
		return exitMenuItem;
	}

} 
