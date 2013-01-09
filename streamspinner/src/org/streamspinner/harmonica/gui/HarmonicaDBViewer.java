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

import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Image;
import java.awt.AWTEvent;
import java.awt.Component;

public class HarmonicaDBViewer 
	extends JTree 
	implements InsertDataListener, 
			   SchemaListChangeListener,
			   MainDBChangeListener{
	private List<DBConnector> cons = null;
	private DefaultMutableTreeNode root = null;
	private HarmonicaTreeCellRenderer renderer = null;
	private StreamArchiver archiver = null;
	private int pre_x = -1;
	private int pre_y = -1;
	private Vector<String> db_names = null;
	private DBConnector main_conn = null;
	private JPopupMenu db_menu = null;
	private JPopupMenu table_menu = null;
	private JMenuItem main_db_item = null;
	private JMenuItem main_db_item5 = null;
	private JMenu db_title_menu = null;
	private JMenu table_title_menu = null;
	private JMenuBar info_menu_bar = null;
	private JMenuBar default_menu_bar = null;

	private JMenu getDBTitleMenu(String title){
		if(db_title_menu != null){
			db_title_menu.setText(title+"(D)");
			return db_title_menu;
		}

		db_title_menu = new JMenu();
		db_title_menu.setText(title+"(D)");
		db_title_menu.setMnemonic(KeyEvent.VK_D);
		
		JMenuItem item1 = null;
		JMenuItem item2 = null;
		JMenuItem item3 = null;
		JMenuItem item4 = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			item1 = new JMenuItem("書き込みレートの表示(I)", KeyEvent.VK_I);
			item2 = new JMenuItem("SQLフォームの表示", KeyEvent.VK_S);
			main_db_item5 = 
				new JMenuItem("メインに使用する(M)", KeyEvent.VK_M);
			item3 = new JMenuItem("書き込みレートの更新(U)", KeyEvent.VK_U);
			item4 = new JMenuItem("接続を閉じる(C)", KeyEvent.VK_C);
		}else{
			item1 = new JMenuItem("Show Insertion Rate Map", KeyEvent.VK_I);
			item2 = new JMenuItem("Show SQL Form", KeyEvent.VK_S);
			main_db_item5 = new JMenuItem("Set Main DB", KeyEvent.VK_M);
			item3 = new JMenuItem
				("Update Insertion Rate Map", KeyEvent.VK_U);
			item4 = new JMenuItem("Close this connection", KeyEvent.VK_C);
		}
		
		item1.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showInsertionRateFrame(p);
				}
			});
		item2.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showSQLFrame(p);
				}
			});
		main_db_item5.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					changeMainDB(p);
				}
			});
		item3.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showUpdateFrame(p);
				}
			});
		item4.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					close_connection(p);
				}
			});

		db_title_menu.add(item2);
		db_title_menu.addSeparator();
		db_title_menu.add(item1);
		db_title_menu.add(item3);
		db_title_menu.addSeparator();
		db_title_menu.add(main_db_item5);
		db_title_menu.add(item4);

		return db_title_menu;
	}

	private JPopupMenu getDBMenu(){
		if(db_menu != null) return db_menu;

		JMenuItem item1 = null;
		JMenuItem item2 = null;
		JMenuItem item3 = null;
		JMenuItem item4 = null;
		
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			item1 = new JMenuItem("書き込みレートの表示(I)", KeyEvent.VK_I);
			item2 = new JMenuItem("SQLフォームの表示", KeyEvent.VK_S);
			main_db_item = 
				new JMenuItem("メインに使用する(M)", KeyEvent.VK_M);
			item3 = new JMenuItem("書き込みレートの更新(U)", KeyEvent.VK_U);
			item4 = new JMenuItem("接続を閉じる(C)", KeyEvent.VK_C);
		}else{
			item1 = new JMenuItem("Show Insertion Rate Map", KeyEvent.VK_I);
			item2 = new JMenuItem("Show SQL Form", KeyEvent.VK_S);
			main_db_item = new JMenuItem("Set Main DB", KeyEvent.VK_M);
			item3 = new JMenuItem
				("Update Insertion Rate Map", KeyEvent.VK_U);
			item4 = new JMenuItem("Close this connection", KeyEvent.VK_C);
		}
		
		item1.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showInsertionRateFrame(p);
				}
			});
		item2.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showSQLFrame(p);
				}
			});
		main_db_item.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					changeMainDB(p);
				}
			});
		item3.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					showUpdateFrame(p);
				}
			});
		item4.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					TreePath p = getSelectionPath();
					close_connection(p);
				}
			});

		db_menu = new JPopupMenu();
		db_menu.setName("DB Menu");
		db_menu.add(item2);
		db_menu.addSeparator();
		db_menu.add(item1);
		db_menu.add(item3);
		db_menu.addSeparator();
		db_menu.add(main_db_item);
		db_menu.add(item4);

		return db_menu;
	}

	private JMenu getTableTitleMenu(String title){
		if(table_title_menu != null){
			table_title_menu.setText(title+"(T)");
		   	return table_title_menu;
		}

		table_title_menu = new JMenu();
		table_title_menu.setText(title+"(T)");
		table_title_menu.setMnemonic(KeyEvent.VK_T);


		JMenuItem item1 = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			item1 = new JMenuItem("全タプルの表示(A)", KeyEvent.VK_A);
		}else{
			item1 = new JMenuItem("Show All Tuples", KeyEvent.VK_A);
		}

		item1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TreePath p = getSelectionPath();
				showTableFrame(p);
			}	
		});

		table_title_menu.add(item1);

		return table_title_menu;
	}

	private JPopupMenu getTableMenu(){
		if(table_menu != null) return table_menu;

		JMenuItem item1 = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			item1 = new JMenuItem("全タプルの表示(A)", KeyEvent.VK_A);
		}else{
			item1 = new JMenuItem("Show All Tuples", KeyEvent.VK_A);
		}

		item1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TreePath p = getSelectionPath();
				showTableFrame(p);
			}
		});

		table_menu = new JPopupMenu();
		table_menu.setName("Table Menu");
		table_menu.add(item1);

		return table_menu;
	}

	public HarmonicaDBViewer(){
		super();

		super.setRowHeight(20);

		renderer = new HarmonicaTreeCellRenderer();
		this.setCellRenderer(renderer);

		archiver = HarmonicaManager.getStreamArchiver();
		archiver.addInsertDataListener(this);
		archiver.addSchemaListChangeListener(this);
		archiver.addMainDBChangeListener(this);

		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		default_menu_bar = m.getJMenuBar();

		update();
	}

	public void update(){
		root = new DefaultMutableTreeNode("Harmonica");

		cons = archiver.getConnectors();
		db_names = new Vector<String>();
		for(DBConnector con : cons){
			DefaultMutableTreeNode n = 
				new DefaultMutableTreeNode(con.toString());
			db_names.add(con.toString());

			if(main_conn == null && 
					con.toString().equals(archiver.getMainDB())){
				main_conn = con;
			}
			
			List<Schema> l = null;
			try{
				l = con.getSchemaList();
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}

			for(Schema s : l){
				DefaultMutableTreeNode sn = 
					new DefaultMutableTreeNode(s.getBaseTableNames()[0]);
				n.add(sn);
			}

			root.add(n);
		}

		TreeModel model = new DefaultTreeModel(root);
		super.setModel(model);

		super.clearSelection();
		for(int i=0;i<super.getRowCount();i++){
			super.expandRow(i);
		}

		HarmonicaMonitor hm = HarmonicaManager.getHarmonicaMonitor();
		if(hm.getJMenuBar() != default_menu_bar){
			hm.setJMenuBar(default_menu_bar);
		}
	}



	public String getMainDB(){
		return main_conn.toString();
	}

	public List<String> getDBNames(){
		return db_names;	
	}

	private String getBelongDatabaseFromConnector(String table_name){
		for(DBConnector d : cons){
			try{
				Schema s = d.getSchema(table_name);
				if(s == null) continue;

				return d.toString();
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
		}
		return null;
	}

	private DefaultMutableTreeNode getDBMSNode(String name){
		for(int i=0;i<root.getChildCount();i++){
			DefaultMutableTreeNode node = 
				(DefaultMutableTreeNode)root.getChildAt(i);

			String node_name = (String)node.getUserObject();

			if(name.equals(node_name)){
				return node;
			}
		}

		return null;
	}

	private DefaultMutableTreeNode getTableNode(String table_name){
		for(int i=0;i<root.getChildCount();i++){
			TreeNode node = root.getChildAt(i);

			for(int j=0;j<node.getChildCount();j++){
				DefaultMutableTreeNode mnode = 
					(DefaultMutableTreeNode)node.getChildAt(j);

				if(mnode.equals(table_name)){
					return mnode;
				}
			}
		}
		return null;
	}

	private void addTableToTree(String child){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);

		// テーブルがどのDBMSに属しているかを取得
		String parent = getBelongDatabaseFromConnector(child);

		if(parent == null) return;

		// 木におけるDBMSの位置を取得
		DefaultMutableTreeNode parent_node = getDBMSNode(parent);

		if(parent_node == null) return;

		// その位置に子（テーブル）を追加
		parent_node.add(node);
	}

	private void showInsertionRateFrame(TreePath p){
		String db = p.getPathComponent(1).toString();
		DBConnector con = null;

		for(DBConnector c : cons){
			if(c.toString().equals(db)){
				con = c;
				break;
			}
		}

		if(con == null) return;

		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		JDesktopPane desktop = m.getDesktopPane();
		int width = 496;
		int height = 310;
		InsertionRatePanel ip = new InsertionRatePanel(con,width,height);

		String sub_title = null;
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			sub_title = " - 書き込みレート";
		else
			sub_title = " - Insertion Rate Map";

		JInternalFrame iframe = new JInternalFrame
			(con.toString() + sub_title, 
			 false, 
			 true, 
			 false, 
			 true);

		iframe.setBounds(
				m.getIFrameNextX(),
				m.getIFrameNextY(),
				width+4, 
				height+40);

		iframe.getContentPane().add(ip);
		iframe.setBackground(Color.WHITE);

		try{
			Image img = null;
			if(con != main_conn)
				img = ImageFactory.getInstance().getImage(ImageType.DB_ICON);
			else
				img = ImageFactory.getInstance().getImage
					(ImageType.DB_MAIN_ICON);
			Icon ico = new ImageIcon(img);
			iframe.setFrameIcon(ico);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}


		desktop.add(iframe);

		iframe.setVisible(true);
	}

	private void showSQLFrame(TreePath p){
		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		JDesktopPane desktop = m.getDesktopPane();
		String db = p.getPathComponent(1).toString();
			
		InternalDBQueryFrame idbframe = null;
		for(DBConnector c : cons){
			if(c.toString().equals(db)){
				idbframe = new InternalDBQueryFrame(c);
			}
		}

		if(idbframe == null){
			HarmonicaManager.createdException
				(new HarmonicaException("no DB connector."));
			return;
		}

		if(db.equals(main_conn.toString())){
			try{
				Image img = ImageFactory.getInstance().getImage
					(ImageType.DB_MAIN_ICON);
				Icon ico = new ImageIcon(img);
				idbframe.setFrameIcon(ico);
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
		}

		idbframe.setBounds(
				m.getIFrameNextX(),
				m.getIFrameNextY(),
				idbframe.getWidth(), 
				idbframe.getHeight());

		desktop.add(idbframe);

		idbframe.setVisible(true);
	}

	private void showUpdateFrame(TreePath p){
		String db = p.getPathComponent(1).toString();
		DBConnector con = null;
		
		for(DBConnector c : cons){
			if(c.toString().equals(db)){
				con = c;
				break;
			}
		}

		if(con == null) return;

		CalculateInsertionRateFrame cal_frame = 
			new CalculateInsertionRateFrame(
					HarmonicaManager.getHarmonicaMonitor(),
					con);

		cal_frame.setVisible(true);
	}

	private void close_connection(TreePath p){
		String db = p.getPathComponent(1).toString();
		try{
			archiver.deleteDB(db);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	public void addDBMSConnection(DBConnector con){
		update();
	}

	public void deleteDBMSConnection(DBConnector con){
		update();
	}

	private void changeMainDB(TreePath p){
		String db = p.getPathComponent(1).toString();
		archiver.setMainDB(db);
	}

	private void showTableFrame(TreePath p){
		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		JDesktopPane desktop = m.getDesktopPane();
		String db = p.getPathComponent(1).toString();
		String table = p.getPathComponent(2).toString();

		InternalSchemaFrame schema_frame = null;
		for(DBConnector c : cons){
			if(c.toString().equals(db)){
				schema_frame = new InternalSchemaFrame(c,table);
				break;
			}
		}

		if(schema_frame == null){
			HarmonicaManager.createdException
				(new HarmonicaException("no DB connector."));
			return;
		}

		schema_frame.setBounds(
				m.getIFrameNextX(),
				m.getIFrameNextY(),
				schema_frame.getWidth(), 
				schema_frame.getHeight());

		desktop.add(schema_frame);
		schema_frame.setVisible(true);
	}

	private void click_root_node(){
		HarmonicaMonitor hm = HarmonicaManager.getHarmonicaMonitor();

		if(hm.getJMenuBar() != default_menu_bar)
			hm.setJMenuBar(default_menu_bar);
	}

	private void click_db_node(String name){
		HarmonicaMonitor hm = HarmonicaManager.getHarmonicaMonitor();

		JMenu jm = getDBTitleMenu(name);
		if(name.equals(main_conn.toString())){
			main_db_item5.setEnabled(false);
		}else{
			main_db_item5.setEnabled(true);
		}
		JMenuBar bar = new JMenuBar();
		bar.add(jm);
		bar.setVisible(true);
		hm.setJMenuBar(bar);
		hm.validate();
		hm.repaint();
	}

	private void click_table_node(String name){
		HarmonicaMonitor hm = HarmonicaManager.getHarmonicaMonitor();

		JMenu jm = getTableTitleMenu(name);
		JMenuBar bar = new JMenuBar();
		bar.add(jm);
		bar.setVisible(true);
		hm.setJMenuBar(bar);
		hm.validate();
		hm.repaint();
	}

	protected void processMouseEvent(MouseEvent e){
		super.processMouseEvent(e);

		TreePath p = getPathForLocation(e.getX(), e.getY());

		if(e.getID() == e.MOUSE_PRESSED){
			HarmonicaMonitor hm = HarmonicaManager.getHarmonicaMonitor();
			if(p == null){
				if(hm.getJMenuBar() != default_menu_bar){
					hm.setJMenuBar(default_menu_bar);
				}

			   	super.clearSelection();
				return;
			}
			
			super.setSelectionPath(p);
			String name = p.getLastPathComponent().toString();

			switch(p.getPathCount()){
				case 1: // root
					click_root_node();
					return;
				case 2: // db
					click_db_node(name);
					return;
				case 3: // table
					click_table_node(name);
					return;
			}
			return;
		}

		if(e.getID() == e.MOUSE_CLICKED){
			if(p == null) return;

			pre_x = e.getX();
			pre_y = e.getY();
			
			String name = p.getLastPathComponent().toString();
			if(SwingUtilities.isRightMouseButton(e)){
				switch(p.getPathCount()){
					case 1: // root
						return;
					case 2: // db
						JPopupMenu m = getDBMenu();
						if(name.equals(main_conn.toString())){
							main_db_item.setEnabled(false);
						}else{
							main_db_item.setEnabled(true);
						}
						m.show
							(e.getComponent(),e.getX(),e.getY());
						return;
					case 3: // table
						getTableMenu().show
							(e.getComponent(),e.getX(),e.getY());
						return;
				}
			}
		}
	}

	public void arrivedInsertData(String table_name, TupleSet tuples){
		renderer.addTarget(table_name);
		super.repaint();

		TimerThread t = new TimerThread(table_name, renderer, this);
		t.start();
	}

	private class TimerThread extends Thread{
		HarmonicaTreeCellRenderer renderer = null;
		HarmonicaDBViewer tree = null;
		String table_name = null;
		TimerThread(String table_name, 
			 HarmonicaTreeCellRenderer renderer, 
			 HarmonicaDBViewer tree){
			this.table_name = table_name;
			this.renderer = renderer;
			this.tree = tree;
		}

		public void run(){
			
			try{ 
				this.sleep(200); 
			}catch(InterruptedException e){
				HarmonicaManager.createdException
					(new HarmonicaException(e));
			}

			renderer.removeTarget(table_name);
			tree.repaint();
		}
	}

	public void addTable(Schema schema){
		update();
	}

	public void deleteTable(String table_name){
		update();
	}

	public void dbChanged(DBConnector old_db, DBConnector new_db){
		main_conn = new_db;
		update();
	}

	public void terminate(){
		if(archiver == null) return;

		archiver.removeInsertDataListener(this);
		archiver.removeSchemaListChangeListener(this);
		archiver.removeMainDBChangeListener(this);

		archiver = null;

		debug("Terminated.");
	}

	private void debug(Object o){
		HarmonicaManager.debug("DBTree",o);
	}
}
