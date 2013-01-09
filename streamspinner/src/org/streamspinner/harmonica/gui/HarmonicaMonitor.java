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

import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Locale;

import org.streamspinner.harmonica.*;

public class HarmonicaMonitor extends JFrame 
	implements MouseListener, ItemListener{
	private static HarmonicaMonitor monitor = new HarmonicaMonitor();
	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu toolMenu = null;
	private JMenu styleMenu = null;
	private JMenu styleSubMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem hamqlMenuItem = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem connectMenuItem = null;
	private JMenuItem debugMenuItem = null;
	private JMenuItem style1MenuItem = null;
	private JMenuItem style2MenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private ButtonGroup styleGroup = null;
	private JSplitPane centerSplitPane = null;
	private JSplitPane leftSplitPane = null;
	private JSplitPane rightSplitPane = null;
	private JDesktopPane jDesktopPane = null;
	private JTextArea jTextArea = null;
	private JScrollPane terminarScrollPane = null;
	private JScrollPane queryScrollPane = null;
	private JTable queryTable = null;
	private JScrollPane dbScrollPane = null;
	private JTree dbTree = null;
	private JToolBar toolbar = null;
	private JToolBar leftbar = null;
	private JTabbedPane jTabbedPane = null;
	private boolean running = true;
	private static boolean isInitializing = false;
	private int iframex = 0;
	private int iframey = 0;

	private HarmonicaMonitor() {
		super();
	}

	public static HarmonicaMonitor getInstance(){
		if(!isInitializing) monitor.initialize();
		return monitor;
	}

	private void initialize() {
		try{
			this.setIconImage(ImageFactory.getInstance().getImage(
						ImageType.HARMONICA_ICON));
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Harmonica Monitor");

		isInitializing = true;

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height - getJToolBar().getHeight();
		this.setBounds(w/6,h/6,w*2/3,h*2/3);
	}

	public void mouseClicked(MouseEvent e){}

	public void mouseReleased(MouseEvent e){
		if(!SwingUtilities.isLeftMouseButton(e)) return;

		JComponent c = (JComponent)(e.getSource());

		if(c == null) return;
		if(c.getName() == null) return;

		if(c.getName().equals("NEW_HAMQL")){
			show_hamql_creator();
		}else if(c.getName().equals("OPEN_HAMQL")){
			show_open_hamql_dialog();
		}else if(c.getName().equals("CREATE_CONNECTOR")){
			show_create_connector_dialog();
		}
	}
	public void mousePressed(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void itemStateChanged(ItemEvent e){
		if(!isInitializing) return;
		JRadioButtonMenuItem rb = (JRadioButtonMenuItem)e.getSource();
		if(rb.getName().equals("style1")){
			if(rb.isSelected()) change_style();
		}else if(rb.getName().equals("style2")){
			if(rb.isSelected()) change_style1();
		}
	}

	private void change_style(){
		if(jContentPane == null) jContentPane = new JPanel();

		jContentPane.removeAll();

		if(toolbar != null){
			toolbar.updateUI();
			toolbar.setVisible(false);
			toolbar.removeAll();
			toolbar = null;
		}

		if(leftSplitPane != null){
			leftSplitPane.removeAll();
			leftSplitPane = null;
		}

		if(rightSplitPane != null){
			rightSplitPane.removeAll();
			rightSplitPane = null;
		}

		if(centerSplitPane != null){
			centerSplitPane.removeAll();
			centerSplitPane = null;
		}
		
		jContentPane.add(getJToolBar(),BorderLayout.NORTH);
		jContentPane.add(getLeftToolBar(),BorderLayout.WEST);
		jContentPane.add(getDesktopPane(),BorderLayout.CENTER);
		jContentPane.validate();
		jContentPane.repaint();
	}

	private void change_style1(){
		if(jContentPane == null) jContentPane = new JPanel();

		jContentPane.removeAll();

		if(toolbar != null){
			toolbar.updateUI();
			toolbar.setVisible(false);
			toolbar.removeAll();
			toolbar = null;
		}

		if(jTabbedPane != null){
			jTabbedPane.removeAll();
			jTabbedPane = null;
		}

		if(leftbar != null){
			leftbar.updateUI();
			leftbar.setVisible(false);
			leftbar.removeAll();
			leftbar = null;
		}

		jContentPane.add(getJToolBar(),BorderLayout.NORTH);
		jContentPane.add(getCenterSplitPane(),BorderLayout.CENTER);
		jContentPane.validate();

		centerSplitPane.setDividerLocation(250);
		leftSplitPane.setDividerLocation(0.5);
		rightSplitPane.setDividerLocation(getHeight()-150);

		jContentPane.repaint();
	}

	private void show_hamql_creator(){
		InternalHamQLFrame iframe = new InternalHamQLFrame();
		iframe.setBounds(
			getIFrameNextX(),
			getIFrameNextY(),
			iframe.getWidth(),
			iframe.getHeight()
		);
		getJDesktopPane().add(iframe);
		iframe.setVisible(true);

	}

	private void show_open_hamql_dialog(){
		FileFilter filter = createFilter();
		
		JFileChooser jf = new JFileChooser("conf/harmonica/hamql/");
		jf.setFileFilter(filter);

		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			jf.setDialogTitle("HamQL (SpinQL) 問合せを開く");
		else
			jf.setDialogTitle("Open HamQL (or SpinQL) query file");

		int returnVal = jf.showOpenDialog(this);
		if(returnVal != JFileChooser.APPROVE_OPTION) return;

		File f = jf.getSelectedFile();

		InternalHamQLFrame iframe = new InternalHamQLFrame();
		
		try{
			FileReader reader = new FileReader(f);
			BufferedReader br = new BufferedReader(reader);

			StringBuilder buf = new StringBuilder();
			String tmp = null;
			while((tmp = br.readLine()) != null){
				buf.append(tmp+"\n");
			}
			br.close();
			reader.close();

			iframe.setHamQL(buf.toString());
		}catch(Exception e){
			HarmonicaManager.createdException
				(new HarmonicaException("no file("+f.getName()+")"));
		}

		iframe.setBounds(
			getIFrameNextX(),
			getIFrameNextY(),
			iframe.getWidth(),
			iframe.getHeight()
		);
		getJDesktopPane().add(iframe);
		iframe.setVisible(true);
	}

	private void show_create_connector_dialog(){
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height - getJToolBar().getHeight();

		ConnectorCreaterDialog dig = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			dig = new ConnectorCreaterDialog(this,"DBコネクタの追加");
		else
			dig = new ConnectorCreaterDialog(this,"Add a DB Connector");

		w = (w - dig.getWidth())/2;
		h = (h - dig.getHeight())/2;

		dig.setLocation(w,h);
		dig.setVisible(true);
	}

	private FileFilter createFilter(){
		FileFilter filter = new FileFilter(){
			public boolean accept(File f){
				if(f.isDirectory()) return true;
				if(!f.isFile()) return false;
				if(f.getName().matches("([^.]+\\.hamql|[^.]+\\.cq)$"))
					return true;

				return false;
			}
			public String getDescription(){
				if(HarmonicaManager.locale.equals(Locale.JAPAN))
					return "HamQL問合せ(*.hamql)，SpinQL問合せ(*.cq)";
				return "HamQL query(*.hamql)，SpinQL query(*.cq)";
			}
		};

		return filter;
	}

	private JToolBar getJToolBar(){
		if(toolbar != null) return toolbar;

		toolbar = new JToolBar(){
			protected void paintBorder(Graphics g){
				if(super.getOrientation() == JToolBar.HORIZONTAL){ // 横
					int w = super.getWidth();
					int h = super.getHeight();
					
					g.setColor(SystemColor.controlLtHighlight);
					g.drawLine(4,3,4,h-3);
					g.drawLine(4,3,6,3);

					g.setColor(SystemColor.controlShadow);
					g.drawLine(7,3,7,h-3);
					g.drawLine(5,h-3,7,h-3);

				}else{ // 縦
					int w = super.getWidth();
					int h = super.getHeight();
					
					g.setColor(SystemColor.controlLtHighlight);
					g.drawLine(4,3,4,21);
					g.drawLine(4,3,6,3);

					g.setColor(SystemColor.controlShadow);
					g.drawLine(7,3,7,21);
					g.drawLine(5,19,7,21);
				}
			}
		};
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			toolbar.setName("メニューバー");
		else
			toolbar.setName("MenuBar");
		toolbar.setSize(78,24);
		toolbar.setPreferredSize(new Dimension(78,24));
		toolbar.setLayout(null);
		toolbar.setFloatable(true);
		toolbar.setBorderPainted(true);

		try{
			Image img = ImageFactory.getInstance().getImage
				(ImageType.HAMQL_ICON);
			Icon ico = new ImageIcon(img);
			JButton btn = new JButton();
			btn.setIcon(ico);
			btn.addMouseListener(this);
			btn.setName("NEW_HAMQL");
			btn.setBounds(10,1,22,22);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				btn.setToolTipText("HamQLを作成する");
			else
				btn.setToolTipText("create new hamql");
			toolbar.add(btn);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		try{
			Image img2 = ImageFactory.getInstance().getImage
				(ImageType.OPEN_ICON);
			Icon ico2 = new ImageIcon(img2);
			JButton btn2 = new JButton();
			btn2.setIcon(ico2);
			btn2.addMouseListener(this);
			btn2.setName("OPEN_HAMQL");
			btn2.setBounds(32,1,22,22);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				btn2.setToolTipText("HamQLを開く");
			else
				btn2.setToolTipText("open hamql");
			toolbar.add(btn2);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		try{
			Image img3 = ImageFactory.getInstance().getImage
				(ImageType.CONNECTOR_ICON);
			Icon ico3 = new ImageIcon(img3);
			JButton btn3 = new JButton();
			btn3.setIcon(ico3);
			btn3.addMouseListener(this);
			btn3.setName("CREATE_CONNECTOR");
			btn3.setBounds(56,1,22,22);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				btn3.setToolTipText("DBコネクタを追加する");
			else
				btn3.setToolTipText("Add a DB connector");
			toolbar.add(btn3);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		return toolbar;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJToolBar(),BorderLayout.NORTH);
			jContentPane.add(getLeftToolBar(),BorderLayout.WEST);
			jContentPane.add(getDesktopPane(),BorderLayout.CENTER);
		}

		return jContentPane;
	}

	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			jJMenuBar.add(getToolMenu());
			jJMenuBar.add(getStyleMenu());
			jJMenuBar.add(getHelpMenu());
		}
		return jJMenuBar;
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				fileMenu.setText("ファイル(F)");
			else
				fileMenu.setText("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private JMenu getToolMenu(){
		if(toolMenu != null) return toolMenu;
		toolMenu = new JMenu();
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			toolMenu.setText("ツール(T)");
		else
			toolMenu.setText("Tool");

		toolMenu.setMnemonic(KeyEvent.VK_T);
		toolMenu.add(getHamqlMenuItem());
		toolMenu.add(getOpenMenuItem());
		toolMenu.addSeparator();
		toolMenu.add(getConnectMenuItem());
		toolMenu.addSeparator();
		toolMenu.add(getDebugMenuItem());

		return toolMenu;
	}

	private JMenu getStyleMenu(){
		if(styleMenu != null) return styleMenu;
		styleMenu = new JMenu();
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			styleMenu.setText("スタイル(S)");
		else
			styleMenu.setText("Style");

		styleMenu.setMnemonic(KeyEvent.VK_S);
		styleMenu.add(getStyleSubMenu());
		return styleMenu;
	}

	private JMenu getStyleSubMenu(){
		if(styleSubMenu != null) return styleSubMenu;
		styleSubMenu = new JMenu();
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			styleSubMenu.setText("選択(S)");
		else
			styleSubMenu.setText("Select");

		styleSubMenu.setMnemonic(KeyEvent.VK_S);
		styleSubMenu.add(getStyle1MenuItem());
		styleSubMenu.add(getStyle2MenuItem());
		return styleSubMenu;
	}

	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				helpMenu.setText("ヘルプ(H)");
			else
				helpMenu.setText("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}

	private JMenuItem getHamqlMenuItem(){
		if(hamqlMenuItem != null) return hamqlMenuItem;

		hamqlMenuItem = new JMenuItem();
		
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			hamqlMenuItem.setText("新規HamQL(N)");
		else
			hamqlMenuItem.setText("New HamQL");
		
		hamqlMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
		hamqlMenuItem.addMouseListener(this);
		hamqlMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				show_hamql_creator();
			}	
		});

		return hamqlMenuItem;
	}
	private JMenuItem getOpenMenuItem(){
		if(openMenuItem != null) return openMenuItem;

		openMenuItem = new JMenuItem();
	
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			openMenuItem.setText("HamQLを開く(O)");
		else
			openMenuItem.setText("Open HamQL");
		
		openMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_O);
		openMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				show_open_hamql_dialog();
			}	
		});

		return openMenuItem;
	}
	private JMenuItem getConnectMenuItem(){
		if(connectMenuItem != null) return connectMenuItem;

		connectMenuItem = new JMenuItem();
	
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			connectMenuItem.setText("DBコネクタの追加(A)");
		else
			connectMenuItem.setText("Add DB Connector");
		
		connectMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
		connectMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				show_create_connector_dialog();
			}	
		});

		return connectMenuItem;
	}

	private JMenuItem getDebugMenuItem(){
		if(debugMenuItem != null) return debugMenuItem;

		debugMenuItem = new JMenuItem();
		String on_menu = null;
		String off_menu = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			on_menu = "デバッグモードの開始(D)";
			off_menu = "デバッグモードを停止(D)";
		}else{
			on_menu = "Turn on DEBUG MODE";
			off_menu = "Turn off DEBUG MODE";
		}

		debugMenuItem.setText(on_menu);

		debugMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
		debugMenuItem.addActionListener(new ActionListener(){
			String on_menu = null;
			String off_menu = null;
			boolean isOn = true;
			public void actionPerformed(ActionEvent e){
				setDebugMode(isOn);

				if(isOn) isOn = false;
				else isOn = true;
			}	
		});

		return debugMenuItem;
	}

	private void setDebugMode(boolean isOn){
		String on_menu = null;
		String off_menu = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			on_menu = "デバッグモードの開始(D)";
			off_menu = "デバッグモードを停止(D)";
		}else{
			on_menu = "Turn on DEBUG MODE";
			off_menu = "Turn off DEBUG MODE";
		}

		JMenuItem m = getDebugMenuItem();
		if(isOn){
			HarmonicaManager.show_debug = true;
			HarmonicaManager.show_exception = true;
			m.setText(off_menu);
		}else{
			HarmonicaManager.show_debug = false;
			HarmonicaManager.show_exception = false;
			m.setText(on_menu);
		}
	}

	private JMenuItem getStyle1MenuItem() {
		if (style1MenuItem == null) {
			style1MenuItem = new JRadioButtonMenuItem();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				style1MenuItem.setText("タブスタイル(T)");
			else
				style1MenuItem.setText("Tab style");
			style1MenuItem.setMnemonic(java.awt.event.KeyEvent.VK_T);
			style1MenuItem.setName("style1");
			style1MenuItem.addItemListener(this);
			style1MenuItem.setSelected(true);
			if(styleGroup == null) styleGroup = new ButtonGroup();
			styleGroup.add(style1MenuItem);
		}

		return style1MenuItem;
	}

	private JMenuItem getStyle2MenuItem() {
		if (style2MenuItem == null) {
			style2MenuItem = new JRadioButtonMenuItem();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				style2MenuItem.setText("スプリットスタイル(S)");
			else
				style2MenuItem.setText("Split style");
			style2MenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
			style2MenuItem.setName("style2");
			style2MenuItem.addItemListener(this);
			if(styleGroup == null) styleGroup = new ButtonGroup();
			styleGroup.add(style2MenuItem);
		}

		return style2MenuItem;
	}

	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				exitMenuItem.setText("閉じる(C)");
			else
				exitMenuItem.setText("Close");
			exitMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_C);
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					monitor.setVisible(false);
				}
			});
		}
		return exitMenuItem;
	}

	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				aboutMenuItem.setText("Harmonicaについて");
			else
				aboutMenuItem.setText("About");
			aboutMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AboutHarmonicaFrame about_frame = 
						new AboutHarmonicaFrame(
							HarmonicaManager.getHarmonicaMonitor());
					about_frame.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	private JSplitPane getCenterSplitPane() {
		if (centerSplitPane == null) {
			centerSplitPane = new JSplitPane();
			centerSplitPane.setDividerSize(5);
			centerSplitPane.setOneTouchExpandable(true);
			centerSplitPane.setLeftComponent(getLeftSplitPane());
			centerSplitPane.setRightComponent(getRightSplitPane());
			centerSplitPane.setMaximumSize
				(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		}
		return centerSplitPane;
	}

	private JSplitPane getLeftSplitPane() {
		if (leftSplitPane == null) {
			leftSplitPane = new JSplitPane();
			leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			leftSplitPane.setOneTouchExpandable(true);
			leftSplitPane.setTopComponent(getQueryScrollPane());
			leftSplitPane.setBottomComponent(getDbScrollPane());
		}
		return leftSplitPane;
	}

	private JToolBar getLeftToolBar(){
		if(leftbar != null) return leftbar;
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height - getJToolBar().getHeight();

		leftbar = new JToolBar(){
			protected void paintBorder(Graphics g){
				if(super.getOrientation() == JToolBar.HORIZONTAL){ // 横
					int w = super.getWidth();
					int h = super.getHeight();
					
					g.setColor(SystemColor.controlLtHighlight);
					g.drawLine(4,3,4,21);
					//g.drawLine(4,3,4,h-3);
					g.drawLine(4,3,6,3);

					g.setColor(SystemColor.controlShadow);
					g.drawLine(7,3,7,21);
					g.drawLine(5,21,7,21);
					//g.drawLine(7,3,7,h-3);
					//g.drawLine(5,h-3,7,h-3);

				}else{ // 縦
					int w = super.getWidth();
					int h = super.getHeight();
					
					g.setColor(SystemColor.controlLtHighlight);
					g.drawLine(3,5,3,8);
					g.drawLine(3,5,20,5);
					//g.drawLine(3,5,w-7,5);

					g.setColor(SystemColor.controlShadow);
					g.drawLine(21,5,21,8);
					//g.drawLine(w-6,5,w-6,8);
					g.drawLine(4,8,21,8);
					//g.drawLine(4,8,w-6,8);
				}
			}
		};

		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			leftbar.setName("サイドバー");
		else
			leftbar.setName("SideBar");
		leftbar.setOrientation(JToolBar.VERTICAL);
		leftbar.setPreferredSize(new java.awt.Dimension(250,h/4));
		leftbar.add(getJTabbedPane());
		leftbar.setFloatable(true);
		leftbar.setBorderPainted(true);

		return leftbar;
	}

	private JTabbedPane getJTabbedPane(){
		if(jTabbedPane != null) return jTabbedPane;

		jTabbedPane = new JTabbedPane();
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			jTabbedPane.addTab
				("データベース",null,getDbScrollPane(),"DBの一覧");
			jTabbedPane.addTab
				("問合せ",null,getQueryScrollPane(),"問合せの一覧");
			jTabbedPane.addTab
				("コンソール",null,getTerminarScrollPane(),"コンソール");
		}else{
			jTabbedPane.addTab
				("Query",null,getQueryScrollPane(),"List of queries");
			jTabbedPane.addTab
				("DB",null,getDbScrollPane(),"List of databases");
			jTabbedPane.addTab
				("Console",null,getTerminarScrollPane(),"Console");
		}

		return jTabbedPane;
	}

	private JSplitPane getRightSplitPane() {
		if (rightSplitPane == null) {
			rightSplitPane = new JSplitPane();
			rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			rightSplitPane.setOneTouchExpandable(true);
			rightSplitPane.setTopComponent(getJDesktopPane());
			rightSplitPane.setBottomComponent(getTerminarScrollPane());
		}
		return rightSplitPane;
	}

	public JDesktopPane getDesktopPane(){
		return getJDesktopPane();
	}
	private JDesktopPane getJDesktopPane() {
		if (jDesktopPane == null) {
			jDesktopPane = new JDesktopPane();
			jDesktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
			jDesktopPane.setBackground(Color.LIGHT_GRAY);
		}
		return jDesktopPane;
	}

	public int getIFrameNextX(){
		int val = iframex;
		iframex += 5;
		return val;
	}
	public int getIFrameNextY(){
		int val = iframey;
		iframey += 5;
		return val;
	}

	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new HarmonicaTerminal();
			jTextArea.setEditable(false);
			jTextArea.setLineWrap(true);
			jTextArea.setTabSize(4);
			jTextArea.setWrapStyleWord(true);
		}
		return jTextArea;
	}

	public JTextArea getHarmonicaTerminal(){
		return getJTextArea();
	}

	private JScrollPane getTerminarScrollPane() {
		if (terminarScrollPane == null) {
			terminarScrollPane = new JScrollPane();
			terminarScrollPane.setViewportView(getJTextArea());
			terminarScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		}
		return terminarScrollPane;
	}

	private JScrollPane getQueryScrollPane() {
		if (queryScrollPane == null) {
			queryScrollPane = new JScrollPane();
			queryScrollPane.setViewportView(getQueryTable());
			queryScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		}
		return queryScrollPane;
	}

	private JTable getQueryTable() {
		if (queryTable == null) {
			queryTable = new HarmonicaQueryViewer();
		}
		return queryTable;
	}

	private JScrollPane getDbScrollPane() {
		if (dbScrollPane == null) {
			dbScrollPane = new JScrollPane();
			dbScrollPane.setViewportView(getDbTree());
			dbScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		}
		return dbScrollPane;
	}
	
	public JTable getQueryViewer(){
		return getQueryTable();
	}
	public JTree getDBViewer(){
		return getDbTree();
	}

	private JTree getDbTree() {
		if (dbTree == null) {
			dbTree = new HarmonicaDBViewer();
		}
		return dbTree;
	}

	protected void processWindowEvent(WindowEvent e){
		super.processWindowEvent(e);

		if(e.getID() == WindowEvent.WINDOW_CLOSING){
			this.setVisible(false);
		}
	}

	public void terminate(){
		if(!running) return;
			
		((HarmonicaDBViewer)dbTree).terminate();
		((HarmonicaQueryViewer)queryTable).terminate();
		debug("Terminated.");

		running = false;
	}
	private void debug(Object o){
		HarmonicaManager.debug("Monitor",o);
	}
} 
