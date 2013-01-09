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

import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class InternalDBQueryFrame 
	extends JInternalFrame{

	private JPanel jContentPane = null;
	private JTextArea queryArea = null;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane = null;
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane1 = null;
	private JTable jTable = null;
	private DBConnector conn = null;
	private JButton jButton1 = null;

	private JTextArea getQueryArea() {
		if (queryArea == null) {
			queryArea = new JTextArea();
			queryArea.setText("");
			queryArea.setFont(new Font(null, Font.PLAIN, 16));
		}
		return queryArea;
	}

	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setOneTouchExpandable(true);
			jSplitPane.setTopComponent(getJPanel());
			jSplitPane.setBottomComponent(getJScrollPane1());
			jSplitPane.setDividerLocation(150);
		}
		return jSplitPane;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getQueryArea());
		}
		return jScrollPane;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new java.awt.Insets(5,2,35,5);
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.ipadx = 21;
			gridBagConstraints3.ipady = 6;
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new java.awt.Insets(42,2,5,5);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.ipadx = 22;
			gridBagConstraints2.ipady = 4;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.gridheight = 2;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 392;
			gridBagConstraints1.ipady = 112;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.insets = new java.awt.Insets(6,5,4,1);
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJScrollPane(), gridBagConstraints1);
			jPanel.add(getJButton(), gridBagConstraints2);
			jPanel.add(getJButton1(), gridBagConstraints3);
		}
		return jPanel;
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton(){
				protected void processMouseEvent(MouseEvent e){
					super.processMouseEvent(e);

					if(e.getID() == e.MOUSE_CLICKED){
						String query = getQueryArea().getText();
						
						boolean b = query.matches
							("^\\s*[Ss][Ee][Ll][Ee][Cc][Tt](\\s|.)*");
						
						HarmonicaMonitor m = 
							HarmonicaManager.getHarmonicaMonitor();
						JTextArea t = m.getHarmonicaTerminal();
						String mes = null;
						if(HarmonicaManager.locale.equals(Locale.JAPAN))
							mes = "[SQL Form] SQLを " + conn.toString() + 
								"に送信しました．";
						else
							mes = "[SQL Form] send SQL to " 
								+ conn.toString() + ".";

						t.setText(mes);

						if(b){
							getData(query);
						}
						else{
						   	updateData(query);
							HarmonicaDBViewer v = 
								(HarmonicaDBViewer)m.getDBViewer();

							v.update();
						}

					}
				}
			};
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jButton.setText("送信");
			else
				jButton.setText("Send");
		}
		return jButton;
	}

	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton(){
				protected void processMouseEvent(MouseEvent e){
					super.processMouseEvent(e);

					if(e.getID() == e.MOUSE_CLICKED){
						getJTable().setModel(new DefaultTableModel());
						getQueryArea().setText("");
					}
				}
			};
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				jButton1.setText("消去");
			else
				jButton1.setText("Clear");
		}
		return jButton1;
	}

	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJTable());
		}
		return jScrollPane1;
	}

	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
		}
		return jTable;
	}

	public InternalDBQueryFrame(DBConnector conn) {
		super(conn.toString() +" - SQL Form" ,true,true,true,true);
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			this.setTitle(conn.toString() + " - SQLフォーム");
		}
		this.conn = conn;
		initialize();
	}

	private void initialize() {
		this.setSize(500, 350);
		this.setContentPane(getJContentPane());
		try{
			Image img = 
				ImageFactory.getInstance().getImage(ImageType.DB_ICON);
			Icon ico = new ImageIcon(img);
			this.setFrameIcon(ico);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	private boolean updateData(String query){
		try{
			conn.executeUpdateNoQueue(query);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return false;
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

		String[] cols = null;
		Object[] o = null;
		
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			cols = new String[]{"時刻","メッセージ"};
			o = new Object[]{df.format(cal.getTime()),
				"DBに更新要求を送信しました"};
		}else{
			cols = new String[]{"Timestamp","Message"};
			o = new Object[]{df.format(cal.getTime()),
				"Sent the update requirement to DB"};
		}
		
		DefaultTableModel tm = new DefaultTableModel(null,cols);
		tm.addRow(o);

		getJTable().setModel(tm);

		return true;
	}

	private boolean getData(String query){
		ResultSet rs = null;
		TupleSet ts = null;
		try{
			rs = conn.executeQuery(query);
			ts = conn.makeTupleSet(rs);
			rs.close();
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return false;
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return false;
		}

		try{
			Schema s = ts.getSchema();
			String[] cols = new String[s.size()];

			for(int i=0;i<s.size();i++) cols[i] = s.getAttributeName(i);
			DefaultTableModel tm = new DefaultTableModel(null, cols);

			ts.beforeFirst();
			while(ts.next()){
				Tuple t = ts.getTuple();
				Object[] o = new Object[cols.length];
				for(int i=0;i<o.length;i++) o[i] = t.getObject(i);
				tm.addRow(o);
			}

			getJTable().setModel(tm);
		}catch(StreamSpinnerException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return false;
		}
		return true;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(
					new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
			jContentPane.add(getJSplitPane(), null);
		}
		return jContentPane;
	}

} 
