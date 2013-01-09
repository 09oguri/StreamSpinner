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
import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.validator.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class HarmonicaQueryViewer 
	extends JTable
	implements FeasibilityValidatorListener,
		   InternalFrameListener,
		   MouseListener{
	private FeasibilityValidator validator = null;
	private Map<String, JInternalFrame> frame_map = null;
	private Map<String, ValidatingResult> id_map = null;
	private Map<Integer, String> sid_qid_map = null;
	private HarmonicaTableModel model = null;
	private JPopupMenu waiting_menu = null;
	private JPopupMenu running_menu = null;
	private JPopupMenu canceled_menu = null;
	private HashSet<Integer> newly_rows = null;
	private int previous_sid = 0;

	public int selected_col = -1;
	public int selected_row = -1;

	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseReleased(MouseEvent e){
		if(!SwingUtilities.isLeftMouseButton(e)) return;

		JComponent c = (JComponent)e.getSource();

		int row = super.getSelectedRow();
		if(c.getName().equals(ImageType.QUERY_CANCELED.toString())){
			// 消去ボタン
			delete_row(row);
		}
	}

	private void delete_row(int row){
		Integer sid = (Integer)(super.getValueAt(row,0));
		String qid = sid_qid_map.get(sid);
		ValidatingResult r = null;
		r = id_map.remove(qid);

		if(r == null){
			HarmonicaManager.createdException
				(new HarmonicaException("no target("+sid+")."));
			return;
		}

		model.removeRow(row);

		JTextArea terminal = 
			HarmonicaManager.getHarmonicaMonitor().getHarmonicaTerminal();
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			terminal.setText("[Harmonica] 問合せを消去しました ("+sid+")");
		else
			terminal.setText("[Harmonica] delete query ("+sid+")");
	}

	public void mousePressed(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}

	private void init_menus(){
		JMenuItem item1 = null;
		JMenuItem item2 = null;
		JMenuItem item3 = null;
		JMenuItem item4 = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			item1 = new JMenuItem
				("この問合せを登録する(R)", KeyEvent.VK_R);
			item2 = new JMenuItem
				("この問合せを停止する(C)", KeyEvent.VK_C);
			item3 = new JMenuItem("この行を消去する(D)", KeyEvent.VK_D);
			item4 = new JMenuItem("この行を消去する(D)", KeyEvent.VK_D);
		}else{
			item1 = new JMenuItem
				("Register this query to StreamSpinner", KeyEvent.VK_R);
			item2 = new JMenuItem
				("Canceled this query from StreamSpinner", KeyEvent.VK_C);
			item3 = new JMenuItem("Delete this row", KeyEvent.VK_D);
			item4 = new JMenuItem("Delete this row", KeyEvent.VK_D);
		}
		
		item1.addMouseListener(this);
		item1.setName(ImageType.QUERY_WAITING.toString());
		item2.addMouseListener(this);
		item2.setName(ImageType.QUERY_RUNNING.toString());
		item3.addMouseListener(this);
		item3.setName(ImageType.QUERY_CANCELED.toString());
		item4.addMouseListener(this);
		item4.setName(ImageType.QUERY_CANCELED.toString());

		waiting_menu = new JPopupMenu();
		waiting_menu.add(item1);
		waiting_menu.add(item3);

		running_menu = new JPopupMenu();
		running_menu.add(item2);

		canceled_menu = new JPopupMenu();
		canceled_menu.add(item4);
	}

	public HarmonicaQueryViewer(){
		super();
		init_menus();
		validator = HarmonicaManager.getFeasibilityValidator();
		validator.addFeasibilityValidatorListener(this);

		id_map = new HashMap<String, ValidatingResult>();
		sid_qid_map = new HashMap<Integer, String>();
		frame_map = new HashMap<String, JInternalFrame>();
		newly_rows = new HashSet<Integer>();

		String[] columns = null;
		if(HarmonicaManager.locale.equals(Locale.JAPAN)){
			columns = new String[]{"ID", 
								"発行された時刻", 
								"記述", 
								"処理木"};
		}else{
			columns = new String[]{"ID", 
								"Timestamp", 
								"HamQL", 
								"Tree"};
		}

		model = new HarmonicaTableModel(null, columns);

		super.setModel(model);

		int i=0;
		for(String c : columns){
			TableColumn column = super.getColumn(c);
			column.setCellRenderer(new HarmonicaTableCellRenderer());
			column.sizeWidthToFit();
			
			if(HarmonicaManager.locale.equals(Locale.JAPAN)){
				if(i == 0){
					column.setPreferredWidth(10);
				}else if(i == 2){
					column.setPreferredWidth(35);
				}else if(i == 3){
					column.setPreferredWidth(35);
				}
			}else{
				if(i == 0){
					column.setPreferredWidth(10);
				}else if(i == 2){
					column.setPreferredWidth(60);
				}else if(i == 3){
					column.setPreferredWidth(40);
				}
			}

			i++;
		}

		super.setDragEnabled(false);
		super.setRowHeight(20);
		super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		super.setColumnSelectionAllowed(false);
		super.getTableHeader().setReorderingAllowed(false);
		super.repaint();
	}

	protected void processMouseEvent(MouseEvent e){
		super.processMouseEvent(e);

		if(e.getID() == e.MOUSE_PRESSED){

			if(SwingUtilities.isRightMouseButton(e)){
				Point p = new Point(e.getX(), e.getY());
				selected_col = super.columnAtPoint(p);
				selected_row = super.rowAtPoint(p);
				newly_rows.remove(selected_row);
			}else if(SwingUtilities.isLeftMouseButton(e)){
				selected_col = super.getSelectedColumn();
				selected_row = super.getSelectedRow();
				newly_rows.remove(selected_row);
			}

			return;
		}

		if(e.getID() == e.MOUSE_CLICKED){

			if(SwingUtilities.isRightMouseButton(e)){
				Point p = new Point(e.getX(), e.getY());
				int col = super.columnAtPoint(p);
				int row = super.rowAtPoint(p);

				super.setColumnSelectionInterval(col,col);
				super.setRowSelectionInterval(row,row);

				canceled_menu.show(e.getComponent(),e.getX(),e.getY());

				return;
			}

			int col = super.getSelectedColumn();
			int row = super.getSelectedRow();

			if(col >= 2 && row >= 0){
				model.update(row,col);
				Integer sid = (Integer)(model.getValueAt(row,0));
				String qid = sid_qid_map.get(sid);
				Boolean b = (Boolean)(model.getValueAt(row,col));
				
				if(!b.booleanValue()){
					String fid = null;
					
					if(col == 2) fid = "Q"+qid;
					else fid = "T"+qid;
					
					JInternalFrame iframe = frame_map.get(fid);
					if(iframe == null) return;
					iframe.setVisible(false);
					iframe.removeInternalFrameListener(this);
					frame_map.remove(fid); 
					return;
				}

				ValidatingResult result = null;
					result = id_map.get(qid);

				JInternalFrame iframe = 
					new JInternalFrame(qid,true,true,true,true);
				String fid = null;
				HarmonicaMonitor m = 
					HarmonicaManager.getHarmonicaMonitor();
				JDesktopPane jdp = m.getDesktopPane();

				if(col == 2){
					fid = "Q"+qid;
					HamQLQuery hamql = result.getQueryTree().getQuery();
					JTextArea area = new JTextArea(hamql.toString());
					
					area.setFont(new Font(null,Font.PLAIN,14));

					area.setLineWrap(true);
					
					JScrollPane sbar = new JScrollPane(area, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					sbar.getVerticalScrollBar().setUnitIncrement(25);
					
					iframe.getContentPane().add(sbar);
					iframe.setBounds(m.getIFrameNextX(),
							m.getIFrameNextY(),
							500,
							150);
					iframe.setName("HamQL");

					try{
						Image img = ImageFactory.getInstance().getImage
							(ImageType.HAMQL_ICON);
						Icon ico = new ImageIcon(img);
						iframe.setFrameIcon(ico);
					}catch(HarmonicaException he){
						HarmonicaManager.createdException(he);
					}

				}else if(col == 3){
					fid = "T"+qid;

					QueryTreePanel qp = new QueryTreePanel();
					qp.setValidatingResult(result);
					qp.setBackground(Color.WHITE);
					
					JScrollPane sbar = new JScrollPane(qp, 
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					sbar.setBackground(Color.WHITE);
					sbar.getVerticalScrollBar().setUnitIncrement(25);
					qp.setParentScrollPane(sbar);

					iframe.getContentPane().add(sbar);
					iframe.setBounds(
							m.getIFrameNextX(),
							m.getIFrameNextY(),
							500,
							400);
					iframe.setName("QueryTree");
					iframe.setBackground(Color.WHITE);

					try{
						Image img = ImageFactory.getInstance().getImage
							(ImageType.TREE_ICON);
						Icon ico = new ImageIcon(img);
						iframe.setFrameIcon(ico);
					}catch(HarmonicaException he){
						HarmonicaManager.createdException(he);
					}
				}

				frame_map.put(fid,iframe);
				iframe.addInternalFrameListener(this);
				jdp.add(iframe);

				iframe.setVisible(true);
			}
		}
	}

	public void internalFrameActivated(InternalFrameEvent e){}
	public void	internalFrameClosed(InternalFrameEvent e){
		String qid = e.getInternalFrame().getTitle();
		String name = e.getInternalFrame().getName();
		int col = -1;

		if(name.equals("QueryTree")){
			col = 3;
		}else{
			col = 2;
		}

		for(int i=0;i<model.getRowCount();i++){
			Integer sid = (Integer)model.getValueAt(i, 0);
			String current_qid = sid_qid_map.get(sid);
			if(qid.equals(current_qid)){
				model.setValueAt(false, i, col);
				return;
			}
		}
	}
	public void	internalFrameClosing(InternalFrameEvent e){}
	public void	internalFrameDeactivated(InternalFrameEvent e){}
	public void	internalFrameDeiconified(InternalFrameEvent e){}
	public void	internalFrameIconified(InternalFrameEvent e){}
	public void	internalFrameOpened(InternalFrameEvent e){}

	public void generatedResult(ValidatingResult result){
		String qid = result.getQueryTree().getQuery().getID();
		id_map.put(qid, result);
		sid_qid_map.put(++previous_sid,qid);

		Object[] o = {new Integer(previous_sid),getTime(),false,false};
		
		HashSet<Integer> h = new HashSet<Integer>();
		h.add(0);
		for(Integer i : newly_rows){
			h.add(i+1);
		}

		model.insertRow(0,o);

		newly_rows = h;
		if(selected_row>=0) selected_row++;
	}

	public Set<Integer> getNewRows(){
		return newly_rows;
	}

	public Map<String, ValidatingResult> getIDMap(){
		Map<String, ValidatingResult> m = 
			new TreeMap<String, ValidatingResult>();

		for(String k : id_map.keySet()) m.put(k,id_map.get(k));

		return m;
	}

	private String getTime(){
		Calendar c = Calendar.getInstance();

		StringBuilder b = new StringBuilder(20);
		b.append(c.get(c.YEAR)+".");
		b.append(c.get(c.MONTH)+".");
		b.append(c.get(c.DATE)+" ");
		b.append(c.get(c.HOUR_OF_DAY)+":");
		b.append(c.get(c.MINUTE)+":");
		b.append(c.get(c.SECOND)+"");

		return b.toString();
	}

	public void startedQuery(String qid){
	}
	
	private void changeQueryIcon(String qid, ImageType type){
	}

	public void stoppedQuery(String qid){
	}

	public void terminate(){
		if(validator == null) return;
		validator.removeFeasibilityValidatorListener(this);
		validator = null;

		debug("Terminated.");
	}

	private void debug(Object o){
		HarmonicaManager.debug("QueryTable",o);
	}
}
