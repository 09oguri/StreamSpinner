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
import javax.swing.*;
import javax.swing.table.*;
import java.util.Locale;

public class InternalSchemaFrame extends JInternalFrame {
	private JPanel jContentPane = null;
	private JScrollPane schemaScrollPane = null;
	private JTable schemaTable = null;
	private DBConnector conn = null;
	private String table_name = null;

	private JScrollPane getSchemaScrollPane() {
		if (schemaScrollPane == null) {
			schemaScrollPane = new JScrollPane();
			schemaScrollPane.setViewportView(getSchemaTable());
		}
		return schemaScrollPane;
	}

	private JTable getSchemaTable() {
		if (schemaTable == null) {
			schemaTable = new JTable();
			schemaTable.getTableHeader().setReorderingAllowed(false);
		}
		return schemaTable;
	}

	public InternalSchemaFrame(DBConnector conn, String table_name){
		super(table_name, true, true, true, true);
		
		this.conn = conn;
		this.table_name = table_name;

		initialize();
	}

	private void initialize() {
		this.setSize(500, 350);
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			this.setTitle(table_name + " - ‘Sƒ^ƒvƒ‹");
		else
			this.setTitle(table_name + " - All Tuples");
		this.setContentPane(getJContentPane());
		
		try{
			Image img = 
				ImageFactory.getInstance().getImage(ImageType.TABLE_ICON);
			Icon ico = new ImageIcon(img);
			this.setFrameIcon(ico);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}


		getData();
	}

	private void getData(){
		String query = "SELECT * FROM " + table_name;

		ResultSet rs = null;
		TupleSet ts = null;
		try{
			rs = conn.executeQuery(query);
			ts = conn.makeTupleSet(rs);
			rs.close();
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
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

			getSchemaTable().setModel(tm);
		}catch(StreamSpinnerException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add
				(getSchemaScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

}
