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
import javax.swing.table.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import org.streamspinner.connection.*;
import org.streamspinner.*;

import org.jfree.data.xy.*;
import org.jfree.data.general.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.data.time.*;
import org.jfree.ui.*;

public class CQGraphTerminal extends JFrame 
	implements CQRowSetListener, ActionListener{

	private DefaultCQRowSet StreamSpinnerConnection = null;
	private String url = null;
	private JFreeChart chart = null;
	private DefaultTableModel model = null;
	private TimeSeriesCollection c = null;

	private JPanel jContentPane = null;
	private JPanel jPanel0 = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel11 = null;
	private JPanel jPanel12 = null;
	private JPanel jPanel2 = null;
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;
	private JTable jTable = null;
	private JTextArea jTextArea = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JPanel jPanel = null;
	private Map<Integer, TimeSeries> col_series_map = null;
	private int base = 0;

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy
				(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setVerticalScrollBarPolicy
				(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane.setBounds(new java.awt.Rectangle(0,0,340,150));
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	private JScrollPane getJScrollPane1() {					
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setBounds(new Rectangle(0,151,400,259));
			jScrollPane1.setViewportView(getJTable());
		}
		return jScrollPane1;
	}

	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
			String[] str = {" "," "," "};
			DefaultTableModel tm = new DefaultTableModel(str,0);
			jTable.setModel(tm);
		}
		return jTable;
	}

	private JPanel getJPanel0(){
		if(jPanel0 != null) return jPanel0;
		jPanel0 = new JPanel();
		jPanel0.setMaximumSize
			(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel0.setPreferredSize
			(new Dimension(400, Short.MAX_VALUE));
		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.PAGE_AXIS));
		jPanel0.add(getJPanel1());
		jPanel0.add(getJPanel2());

		return jPanel0;
	}

	private JPanel getJPanel1(){
		if(jPanel1 != null) return jPanel1;
		jPanel1 = new JPanel();
		jPanel1.setMaximumSize
			(new Dimension(Short.MAX_VALUE, 150));
		jPanel1.setPreferredSize(new Dimension(400, 150));
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));
		jPanel1.add(getJPanel11());
		jPanel1.add(getJPanel12());

		return jPanel1;
	}

	private JPanel getJPanel11(){
		if(jPanel11 != null) return jPanel11;
		jPanel11 = new JPanel();
		jPanel11.setMaximumSize
			(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel11.setPreferredSize(new Dimension(340, 150));
		jPanel11.setLayout(new BoxLayout(jPanel11, BoxLayout.LINE_AXIS));
		jPanel11.add(getJScrollPane());

		return jPanel11;
	}

	private JPanel getJPanel12(){
		if(jPanel12 != null) return jPanel12;
		jPanel12 = new JPanel();
		jPanel12.setMaximumSize
			(new Dimension(60, Short.MAX_VALUE));
		jPanel12.setPreferredSize(new Dimension(60, 150));
		jPanel12.setLayout(new BoxLayout(jPanel12, BoxLayout.PAGE_AXIS));
		jPanel12.add(Box.createVerticalGlue());
		jPanel12.add(getJButton());
		jPanel12.add(Box.createVerticalGlue());
		jPanel12.add(getJButton1());
		jPanel12.add(Box.createVerticalGlue());

		return jPanel12;
	}

	private JPanel getJPanel2(){
		if(jPanel2 != null) return jPanel2;
		jPanel2 = new JPanel();
		jPanel2.setMaximumSize
			(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.LINE_AXIS));
		jPanel2.add(getJScrollPane1());
		jPanel2.setBackground(Color.RED);

		return jPanel2;
	}

	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
		}
		return jTextArea;
	}

	public CQGraphTerminal(String url) {
		super();

		this.url = url;
		initialize();
	}

	private void initialize() {
		col_series_map = new HashMap<Integer, TimeSeries>();

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(810, 450);
		this.setContentPane(getJContentPane());
		this.setTitle("ñ‚çáÇπÉtÉHÅ[ÉÄ - //"+url+"/StreamSpinnerServer");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				terminate();
			}
		});
	}

	public void terminate(){
		if(StreamSpinnerConnection != null){
			try{
				StreamSpinnerConnection.stop();
				StreamSpinnerConnection = null;
			}catch(CQException re){
				re.printStackTrace();
			}
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout
				(new BoxLayout(jContentPane, BoxLayout.LINE_AXIS));
			jContentPane.add(getJPanel0());
			jContentPane.add(getJPanel());
			/*
			jContentPane.setLayout(null);		
			jContentPane.add(getJScrollPane(), null);						
			jContentPane.add(getJScrollPane1(), null);						
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJButton1(), null);				
			jContentPane.add(getJPanel(), null);
			*/
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("ìoò^");
			jButton.addActionListener(this);
			jButton.setMaximumSize(new Dimension(60,40));
			jButton.setPreferredSize(new Dimension(60,40));
			jButton.setBounds(new Rectangle(341,30,59,40));
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("éQè∆");
			jButton1.addActionListener(this);
			jButton1.setMaximumSize(new Dimension(60,40));
			jButton1.setPreferredSize(new Dimension(60,40));
			jButton1.setBounds(new Rectangle(341,80,59,40));
		}
		return jButton1;
	}

	public void actionPerformed(ActionEvent e){
		JButton jb = ((JButton)e.getSource());

		if(jb.getText().equals("ìoò^")){
			try{
				StreamSpinnerConnection = new DefaultCQRowSet();
				StreamSpinnerConnection.setUrl
					("//"+url+"/StreamSpinnerServer");
				StreamSpinnerConnection.setCommand
					(getJTextArea().getText());
				StreamSpinnerConnection.addCQRowSetListener(this);
				StreamSpinnerConnection.start();
				jb.setEnabled(false);
				getJButton1().setEnabled(false);
			}catch(CQException ce){
				ce.printStackTrace();
			}
		}else if(jb.getText().equals("éQè∆")){
			show_open_hamql_dialog();
		}
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
				return "HamQLñ‚çáÇπ(*.hamql)ÅCSpinQLñ‚çáÇπ(*.cq)";
			}
		};

		return filter;
	}

	private void show_open_hamql_dialog(){
		FileFilter filter = createFilter();
		
		JFileChooser jf = new JFileChooser("conf/query/");
		jf.setFileFilter(filter);

		jf.setDialogTitle("HamQL (SpinQL) ñ‚çáÇπÇäJÇ≠");

		int returnVal = jf.showOpenDialog(this);
		if(returnVal != JFileChooser.APPROVE_OPTION) return;

		File f = jf.getSelectedFile();

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

			getJTextArea().setText(buf.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void dataDistributed(CQRowSetEvent e){
		try{
			base = 0;
			CQRowSet rs = (CQRowSet)e.getSource();

			CQRowSetMetaData meta = rs.getMetaData();

			if(chart == null){
				c = new TimeSeriesCollection();
				boolean first_loop = true;
				rs.beforeFirst();
				String[] t_obj = new String[meta.getColumnCount()];
				String[] t_val = new String[meta.getColumnCount()];

				while(rs.next()){
					for(int i=1;i<=meta.getColumnCount();i++){
						double val = 0;

						t_obj[i-1] = meta.getColumnName(i);
						
						if(meta.getColumnTypeName(i).equals
								(DataTypes.STRING)){
							t_val[i-1] = rs.getString(i);
							continue;
						}
						if(meta.getColumnTypeName(i).equals
								(DataTypes.OBJECT)){
							t_val[i-1] = rs.getObject(i).toString();
							continue;
						}
						if(meta.getColumnTypeName(i).equals
								(DataTypes.LONG)){
							long lval = rs.getLong(i);
							val = (double)lval;
							t_val[i-1] = String.valueOf(lval);
						}else if(meta.getColumnTypeName(i).equals
								(DataTypes.DOUBLE)){
							val = rs.getDouble(i);
							t_val[i-1] = String.valueOf(val);
						}else{
							t_val[i-1] = rs.getString(i);
							continue;
						}

						if(val < 1000000 || Double.isNaN(val)){
							TimeSeries ts = updateTimeSeries
								(meta.getColumnName(i), i, val);
						}

					}
					if(model == null){
						model = new DefaultTableModel(t_obj,0);
						getJTable().setModel(model);
					}

					model.addRow(t_val);
					while(model.getRowCount() > 100){
						model.removeRow(0);
					}

					first_loop = false;
				}

				chart = ChartFactory.createTimeSeriesChart
					("","","",c,true,true,true);
				
				XYPlot plot = chart.getXYPlot();
				plot.setBackgroundPaint(Color.BLACK);
				plot.setRangeGridlinePaint(Color.WHITE);
				plot.getDomainAxis().setAutoRange(true);
				plot.getRangeAxis().setAutoRange(true);

				ValueAxis axis = plot.getDomainAxis();
				axis.setLowerMargin(0.03);
				axis.setUpperMargin(0.03);
				
				ChartPanel panel = (ChartPanel)getJPanel();
				panel.setChart(chart);
			}else{
				String[] t_val = new String[meta.getColumnCount()];
				rs.beforeFirst();
				while(rs.next()){
					for(int i=1;i<=meta.getColumnCount();i++){
						double val = 0;
						
						if(meta.getColumnTypeName(i).equals
								(DataTypes.STRING)){
							t_val[i-1] = rs.getString(i);
							continue;
						}
						if(meta.getColumnTypeName(i).equals
								(DataTypes.OBJECT)){
							t_val[i-1] = rs.getObject(i).toString();
							continue;
						}
						if(meta.getColumnTypeName(i).equals
								(DataTypes.LONG)){
							long lval = rs.getLong(i);
							val = (double)lval;
							t_val[i-1] = String.valueOf(lval);
						}else if(meta.getColumnTypeName(i).equals
								(DataTypes.DOUBLE)){
							val = rs.getDouble(i);
							t_val[i-1] = String.valueOf(val);
						}else{
							t_val[i-1] = rs.getString(i);
							continue;
						}

						if(val < 1000000 || Double.isNaN(val)){
							TimeSeries ts = updateTimeSeries
								(meta.getColumnName(i), i, val);
						}
					}

					model.addRow(t_val);
					while(model.getRowCount() > 100){
						model.removeRow(0);
					}
				}
				repaint();
			}
		}catch(CQException ce){
			ce.printStackTrace();
		}
	}

	private Millisecond getMillisecond(){
		Calendar c = Calendar.getInstance();
		c.add(c.MILLISECOND,base);
		base++;
		Millisecond mil = new Millisecond(c.getTime());

		return mil;
	}

	private TimeSeries updateTimeSeries(String name, int col, double val){
		TimeSeries ts = c.getSeries(name);

		if(ts == null){
			ts = new TimeSeries(name, Millisecond.class);
			ts.setMaximumItemCount(100);
			col_series_map.put(col, ts);
			c.addSeries(ts);
		}

		if(Double.isNaN(val)){
			ts.add(getMillisecond(),null);
		}else{
			ts.add(getMillisecond(),val);
		}
		return ts;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {

			c = new TimeSeriesCollection();
			JFreeChart ch = ChartFactory.createTimeSeriesChart
					("","","",c,true,true,true);
			XYPlot plot = ch.getXYPlot();
			plot.setBackgroundPaint(Color.BLACK);
			plot.setRangeGridlinePaint(Color.WHITE);

			jPanel = new ChartPanel(ch,false);
			jPanel.setMaximumSize
				(new Dimension(400, Short.MAX_VALUE));
			jPanel.setPreferredSize
				(new Dimension(400, Short.MAX_VALUE));
		}
		return jPanel;
	}

}
