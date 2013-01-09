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
package org.streamspinner.harmonica.optimizer;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.io.*;

import org.streamspinner.harmonica.gui.ImageType;
import org.streamspinner.harmonica.gui.ImageFactory;

public class ResultFrame extends JFrame implements WindowStateListener, MouseListener, ActionListener{
	public Vector<JTabbedPane> tabs = new Vector<JTabbedPane>();
	public JTree tree = null;
	public JTable cost_table = null;
	public DefaultTableModel cost_table_model = null;
	public DefaultMutableTreeNode tree_root = null;
	public DefaultTreeModel model = null;
	public JSplitPane splitPanel = null;
	public JButton open_btn = null;
	public JButton reload_btn = null;
	public JToolBar toolbar = null;
	public File currentFile = null;
	public InfiniteProgressPanel glassPane = null;
	
	public PaintNode[][] tile = null;
	public PlanGenerator generator = null;

	private int divider_location = 180;

	public ResultFrame(){
		super();

		generator = new PlanGenerator();

		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception ex){
		    System.out.println("Error L&F Setting");
		}

		init();
	}

	private JTabbedPane createTab(){
		JTabbedPane tab = new JTabbedPane();
		tab.setSize(800,700);

		return tab;
	}

	private DefaultMutableTreeNode makeTreeNode(String title){
		DefaultMutableTreeNode tn = new DefaultMutableTreeNode(title);

		return tn;
	}

	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals("OPEN")){
			JFileChooser chooser = new JFileChooser("query");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Multiple Continuous Query","mcq");
			//FileNameExtensionFilter filter = new FileNameExtensionFilter("Continuous Query","xml","cq");
			chooser.setFileFilter(filter);
			int rval = chooser.showOpenDialog(this);
			if(rval == JFileChooser.APPROVE_OPTION){
				currentFile = chooser.getSelectedFile();
				run();
			}
			return;
		}
		if(e.getActionCommand().equals("RELOAD")){
			run();
			return;
		}
	}

	public void windowStateChanged(WindowEvent e){
		if(getExtendedState() == MAXIMIZED_BOTH){
			
		}
	}

	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){
		if(!SwingUtilities.isLeftMouseButton(e)) return;
		if(e.getSource() instanceof JTree){
			int row = tree.getRowForLocation(e.getX(),e.getY());
			if(row <= 0) return;
			JTabbedPane tab = tabs.get(row-1);
			splitPanel.setRightComponent(tab);
			splitPanel.setDividerLocation(divider_location);
			repaint();
			return;
		}
	}

	private void init(){
		this.setTitle("Plan Viewer");
		this.setSize(1000,700);
		this.setLocationRelativeTo(null);

		Image open_img = null, reload_img = null;
		try{
			open_img = ImageFactory.getInstance().getImage(ImageType.OPEN_ICON);
			reload_img = ImageFactory.getInstance().getImage(ImageType.RELOAD_ICON);
		}catch(Exception e){}

		open_btn = new JButton("Open Query", new ImageIcon(open_img));
		open_btn.addActionListener(this);
		open_btn.setActionCommand("OPEN");
		reload_btn = new JButton("Recalculation", new ImageIcon(reload_img));
		reload_btn.addActionListener(this);
		reload_btn.setActionCommand("RELOAD");

		toolbar = new JToolBar();
		toolbar.add(open_btn);
		toolbar.add(reload_btn);
		toolbar.setFloatable(false);

		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel panel = new JPanel();
		
		JSplitPane centerPane = new JSplitPane();
		centerPane.setLeftComponent(leftPane);
		centerPane.setRightComponent(panel);
		centerPane.setDividerLocation(divider_location);

		this.getContentPane().add(toolbar, BorderLayout.NORTH);
		this.getContentPane().add(centerPane, BorderLayout.CENTER);
		this.addWindowStateListener(this);
	}

	private void updateConfigure(ConfigureReader conf){
		HashMap<String, Double> cost = new HashMap<String, Double>();
		HashMap<String, Double> input_rate = new HashMap<String, Double>();

		boolean isShort = true;
		for(int i=0; i<cost_table_model.getRowCount(); i++){
			cost_table.editCellAt(-1,-1);
			if(cost_table_model.getValueAt(i,0) == null) continue;
			if(((String)cost_table_model.getValueAt(i,0)).equals("cost")){
				cost.put(
					(String)cost_table_model.getValueAt(i,1),
					new Double((String)cost_table_model.getValueAt(i,2))
				);
			}else if(((String)cost_table_model.getValueAt(i,0)).equals("input_rate")){
				input_rate.put(
					(String)cost_table_model.getValueAt(i,1),
					new Double((String)cost_table_model.getValueAt(i,2)));
			}else if(((String)cost_table_model.getValueAt(i,0)).equals("isShort")){
				if(cost_table_model.getValueAt(i,2).equals("false")) isShort = false;
			}
		}

		conf.cost = cost;
		conf.input_rate = input_rate;
		conf.isShort = isShort;
	}

	private class CreatePlan extends Thread{
		private ResultFrame frame = null;
		private PlanGenerator generator = null;
		private String fname = null;
		CreatePlan(ResultFrame frame, PlanGenerator generator, String fname){
			this.frame = frame;
			this.generator = generator;
			this.fname = fname;
		}

		public void run(){
			ArrayList<TreeSet<ProcessResult>> result = null;
			for(int i = 0; i< 5; i++){
				result = null;
				System.gc();
				try{
					long t1 = System.currentTimeMillis();
					result = generator.generatePlanFromFile(fname);
					long t2 = System.currentTimeMillis();

					System.out.println("Processing Time="+(t2-t1));
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			frame.invokeUpdate(result);
		}
	}

	public void run(){
		if(currentFile == null) return;

		if(cost_table != null){
			updateConfigure(generator.conf);
		}

		glassPane = new InfiniteProgressPanel("Optimizing query plans ...", 16, 0.80f, 15.0f, 200);
		JPanel jcom = (JPanel)getContentPane();
		glassPane.setSize(jcom.getWidth(), jcom.getHeight());
		setGlassPane(glassPane);
		glassPane.start();
		Thread t = new CreatePlan(this, generator, currentFile.getPath());
		t.start();
	}

	public void invokeUpdate(ArrayList<TreeSet<ProcessResult>> result){

		ProcessResult original_plan = generator.original_plan;
		
		// exp
		System.out.println("deep,number,id,processing_cost,writing_cost,view_processing_cost,isFeasible");
		System.out.println("0,0,"+original_plan.dag.id+","+original_plan.processing_cost+","+
				original_plan.inserting_cost+","+original_plan.cost_after_reading+","+original_plan.isFeasible);

		tile = null;
		tabs = new Vector<JTabbedPane>();
		JTabbedPane tab = createTab();
		tabs.add(tab);
		createPanel(original_plan, "Original Plan", tab);

		tree_root = makeTreeNode(currentFile.getName());
		model = new DefaultTreeModel(tree_root);
		DefaultMutableTreeNode tn = makeTreeNode("Original Plan");
		model.insertNodeInto(tn,tree_root,0);
		
		tree = new JTree();
		tree.addMouseListener(this);
		tree.setModel(model);
		tree.setRootVisible(true);
		//table.setPreferredSize(new Dimension(200,600));
		JScrollPane sc = new JScrollPane();
		sc.setViewportView(tree);
		sc.setPreferredSize(new Dimension(divider_location,(int)(getHeight()*0.4)));
		sc.getVerticalScrollBar().setUnitIncrement(10);

		cost_table = makeCostTable(generator.conf);
		JScrollPane sc2 = new JScrollPane();
		sc2.setViewportView(cost_table);
		sc2.setPreferredSize(new Dimension(divider_location,(int)(getHeight()*0.6)));
		sc2.getVerticalScrollBar().setUnitIncrement(10);

		JSplitPane left_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		left_split.setTopComponent(sc);
		left_split.setBottomComponent(sc2);
		left_split.setDividerLocation((int)(getHeight()*0.4));
		left_split.setOneTouchExpandable(true);

		splitPanel = new JSplitPane();
		splitPanel.setLeftComponent(left_split);
		splitPanel.setRightComponent(tab);
		splitPanel.setDividerLocation(divider_location);
		splitPanel.setOneTouchExpandable(true);

		JPanel contentPane = (JPanel)this.getContentPane();
		contentPane.removeAll();
		contentPane.add(splitPanel, BorderLayout.CENTER);
		contentPane.add(toolbar, BorderLayout.NORTH);
		contentPane.validate();
		contentPane.repaint();

		//System.out.println(result.size());
		//System.out.println(result.get(0).size());
		if(result.size() != 0 && result.get(0).size() != 0){
			int deep = 1;
			for(TreeSet<ProcessResult> split_level : result){
				tab = createTab();
				tabs.add(tab);
				int number = 1;
				for(ProcessResult current : split_level){
					createPanel(current, "("+number+") DAG"+current.dag.id, tab);
					
					// exp
					System.out.println(deep+","+number+","+current.dag.id+","+current.processing_cost+","+
							current.inserting_cost+","+current.cost_after_reading+","+current.isFeasible);

					number++;
				}
				DefaultMutableTreeNode dmtn = makeTreeNode("Split "+deep);
				model.insertNodeInto(dmtn, tree_root, deep);

				deep++;
			}
		}
		glassPane.stop();
	}

	private JTable makeCostTable(ConfigureReader conf){
		String[] header = new String[]{"Category","Name","Value"};

		Vector<String[]> data = new Vector<String[]>();
		for(String key : conf.cost.keySet()){
			String[] str = new String[3];
			str[0] = "cost";
			str[1] = key;
			str[2] = String.valueOf(conf.cost.get(key));
			data.add(str);
		}
		for(String key : conf.input_rate.keySet()){
			String[] str = new String[3];
			str[0] = "input_rate";
			str[1] = key;
			str[2] = String.valueOf(conf.input_rate.get(key));
			data.add(str);
		}

		data.add(new String[]{"isShort","value",String.valueOf(conf.isShort)});

		data.add(new String[3]);
		data.add(new String[3]);
		data.add(new String[3]);

		cost_table_model = new DefaultTableModel(data.toArray(new String[0][]),header);
		cost_table = new JTable(cost_table_model);

		return cost_table;
	}

	private void countJoin(Node n, Vector<Node> joins){
		if(n.type.equals("join")){
		   	if(!joins.contains(n)){
				joins.add(n);
			}

		}

		for(Node pn : n.getPrevNodes()){
			countJoin(pn, joins);
		}
	}

	private void sortRootNode(DAG dag){
		ArrayList<Node> tmp = new ArrayList<Node>();

		for(int i = 0; i < dag.root_nodes.size(); i++){
			//print(String.valueOf(i),tmp);
			Node target = dag.root_nodes.get(i);
			if(i == 0){
			   	tmp.add(target);
				continue;
			}
			if(target.id == null){
			   	tmp.add(target);
				continue;
			}
			if(target.id.startsWith("integrate")){
			   	tmp.add(target);
				continue;
			}
			for(int j = 0; j < i ; j++){
				if(tmp.get(j).id.startsWith("integrate")){
					tmp.add(j, target);
					break;
				}
				if(Double.parseDouble(target.id) - Double.parseDouble(tmp.get(j).id) < 0){
					tmp.add(j, target);
					break;
				}
				if(j == i - 1){
					tmp.add(target);
					break;
				}
			}
		}

		dag.root_nodes = tmp;
	}

	private void createPanel(ProcessResult current, String title, JTabbedPane tab){
		DAG dag = current.dag;

		sortRootNode(dag);
		/*
		System.out.println("---");
		for(Node ro : dag.root_nodes) System.out.println(ro.id);
		*/

		TreeMap<String, PaintNode> nodes = new TreeMap<String, PaintNode>();

		int max_height = 0;
		int max_width = 0;
		Vector<Node> joins = new Vector<Node>();
		for(Node n : dag.root_nodes){
			int val = createPaintNodes(n, nodes, 0);
			if(val > max_height) max_height = val;

			countJoin(n, joins);
		}

		max_width = joins.size() + dag.root_nodes.size();

		tile = new PaintNode[max_width][max_height];

		Node[] sorted_set = sortNodes(dag.root_nodes);
		/*
		Node[] sorted_set = new Node[dag.root_nodes.size()];
		for(Node n : dag.root_nodes){
			sortNodes(n, sorted_set);
		}
		for(Node ssn : sorted_set)
		System.out.print(" "+ssn.id);
		System.out.println();
		*/

		int line = 0;
		for(Node n : sorted_set){
			int gap = makeTile(n, nodes, line);
			line += gap;
		}

		// source ”z’u
		deploySources(dag.searchNodes("source"), nodes);


		for(Node n : dag.root_nodes){
			makePointer(n, nodes);
		}

		//printTile();

		addPanel(title, current, tab);
	}

	private void print(String mes, Vector<Node> n){
		System.out.print(mes +" >");
		for(Node nn : n){
			System.out.print(" "+nn.id);
		}
		System.out.println();
	}

	private void deploySources(ArrayList<Node> sources, TreeMap<String, PaintNode> pns){
		Vector<Node> tmp = new Vector<Node>();
		for(Node n : sources){
			//print("deploy",n.next_nodes);
			for(Node nn : n.getNextNodes()){
				//System.out.println(nn.id);
				if(!nn.type.equals("join")){
					PaintNode npn = pns.get(nn.id);
					PaintNode pn = pns.get(n.id);
					tile[npn.x][tile[npn.x].length-1] = pn;
					pn.x = npn.x;
					pn.y = tile[npn.x].length - 1;
					tmp.add(n);
					break;
				}
			}
		}

		sources.removeAll(tmp);
		tmp.clear();

		for(Node n : sources){
			for(Node nn : n.getNextNodes()){
				PaintNode npn = pns.get(nn.id);
				PaintNode pn = pns.get(n.id);
				if(tile[npn.x][tile[npn.x].length-1] != null) continue;

				tile[npn.x][tile[npn.x].length-1] = pn;
				pn.x = npn.x;
				pn.y = tile[npn.x].length - 1;
				tmp.add(n);
				break;
			}
		}

		sources.removeAll(tmp);
		tmp.clear();

		for(Node n : sources){
			for(int i=0;i<tile.length ;i++){
				if(tile[i][tile[i].length-1] == null){
					PaintNode pn = pns.get(n.id);
					tile[i][tile[i].length-1] = pn;
					pn.x = i;
					pn.y = tile[i].length - 1;
					break;
				}
			}
		}
	}

	private void getSourceName(Node n, Vector<String> str){
		if(n.type.equals("source")){
			if(!str.contains(n.value)) str.add(n.value);
			return;
		}

		for(Node pn : n.getPrevNodes()){
			getSourceName(pn, str);
		}
	}
	private Node[] sortNodes(ArrayList<Node> roots){
		TreeMap<String, Vector<Node>> map = new TreeMap<String, Vector<Node>>();

		for(Node r : roots){
			Vector<String> str = new Vector<String>();
			getSourceName(r, str);
			for(String s : str){
				if(map.containsKey(s)){
					Vector<Node> v = map.get(s);
					v.add(r);
				}else{
					Vector<Node> v = new Vector<Node>();
					v.add(r);
					map.put(s,v);
				}
			}
		}

		Vector<Vector<Node>> tmp = new Vector<Vector<Node>>();
		int i=0;
		for(String str : map.keySet()){
			//System.out.println(map.get(str).size());
			if(i == 0){
				tmp.add(map.get(str));
				i++;
				continue;
			}
			Vector<Node> target = map.get(str);
			boolean isMatched = false;
			for(int j=0; j < i; j++){
				if(isOverWrapped(target, tmp.get(j))){
					if(target.size() < tmp.get(j).size()){
						tmp.insertElementAt(target,j);
						isMatched = false;
						break;
					}else{
						isMatched = true;
					}
				}else if(isMatched){
					tmp.insertElementAt(target,j+1);
					isMatched = false;
				}
				if(j == i - 1){
					tmp.add(target);
				}
			}
			i++;
		}

		Vector<Node> sorted = new Vector<Node>();
		//System.out.println("----");
		for(Vector<Node> ns : tmp){
			for(Node vne : ns){
				//System.out.println(vne.id);
			   	if(!sorted.contains(vne)) sorted.add(vne);
			}
		}
		//System.out.println("----");

		return sorted.toArray(new Node[0]);
	}
	private boolean isOverWrapped(Vector<Node> v1, Vector<Node> v2){
		for(Node n : v1){
			if(v2.contains(n)) return true;
		}

		return false;
	}

	private void sortNodes(Node root, Node[] sorted_root){
		if(sorted_root[0] == null){
		   	sorted_root[0] = root;
			return;
		}

		for(int i=0; i<sorted_root.length-1;i++){
			if(sorted_root[i] == null){
			   	sorted_root[i] = root;
				return;
			}
			int rval = compare_roots(sorted_root[i], root);
			if(rval <= 0){
				Node tmp = sorted_root[i];
				sorted_root[i] = root;
				for(int j=i+1;j<sorted_root.length;j++){
					Node tmp2 = sorted_root[j];
					sorted_root[j] = tmp;
					tmp = tmp2;
				}
				return;
			}
		}
		sorted_root[sorted_root.length-1] = root;
	}

	private int compare_roots(Node root1, Node root2){
		String id1 = searchFirstSplit(root1);
		String id2 = searchFirstSplit(root2);

		//System.out.println(root1.id+" "+id1+" "+root2.id+" "+id2);

		if(id2 == null) return 1;
		if(id1 == null) return -1;
		if(id1.equals(id2)) return -1;
		return 1;
	}
	private String searchFirstSplit(Node root){
		if(root.next_nodes.size() >= 2 ) return root.id;

		if(root.prev_nodes.size() == 0) return null;

		return searchFirstSplit(root.prev_nodes.get(0));
	}

	private void addPanel(String title, ProcessResult pr, JTabbedPane tab){
		PaintNode[][] cp = new PaintNode[tile.length][tile[0].length];
		for(int i=0;i<cp.length;i++)
			for(int j=0;j<cp[i].length;j++)
				cp[i][j] = tile[i][j];

		QueryPanel panel = new QueryPanel(cp, pr);
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(panel);
		sp.getVerticalScrollBar().setUnitIncrement(10);

		if(pr.dag.view_table.view.size() != 0){
			JTable jv = makeViewTable(pr.dag.view_table);
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(jv);
			scroll.setPreferredSize(new Dimension(850,150));
			scroll.getVerticalScrollBar().setUnitIncrement(10);

			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			split.setTopComponent(sp);
			split.setBottomComponent(scroll);
			split.setPreferredSize(new Dimension(850,700));
			split.setDividerLocation((int)((this.getHeight()-20)*0.8));
			split.setOneTouchExpandable(true);

			tab.addTab(title, split);
		}else{
			tab.addTab(title, sp);
		}
	}

	private void createOperatorPath(Node n, StringBuilder buf){
		if(n.id == null){
			buf.append(n.value);
		}else{
			if(n.next_nodes.size() != 0) buf.append("->["+n.id+"]");
			else return;
		}
		for(Node nn : n.next_nodes){
			createOperatorPath(nn, buf);
		}
	}

	private JTable makeViewTable(ViewTable view_table){
		String[] header = new String[]{"Original Name","Target Relation","Operations"};
		Vector<String[]> data = new Vector<String[]>();

		for(String key : view_table.view.keySet()){
			String[] data_element = new String[3];
			data_element[0] = key;
			data_element[1] = view_table.source.get(key);
			StringBuilder buf = new StringBuilder();
			createOperatorPath(view_table.view.get(key), buf);
			data_element[2] = buf.toString();
			data.add(data_element);
		}

		DefaultTableModel tm = new DefaultTableModel(data.toArray(new String[0][]),header);

		JTable jt = new JTable(tm);
		//RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tm);
		//jt.setRowSorter(sorter);

		return jt;
	}

	private void makePointer(Node n , TreeMap<String, PaintNode> nodes){
		PaintNode pn = nodes.get(n.id);

		for(Node prev_n : n.getPrevNodes()){
			PaintNode ppn = nodes.get(prev_n.id);
			if(!ppn.next.contains(pn)) ppn.next.add(pn);
			if(!pn.prev.contains(ppn)) pn.prev.add(ppn);
			makePointer(prev_n, nodes);
		}
	}

	private int makeTile(Node n, TreeMap<String, PaintNode> nodes, int pos){
		if(n.type.equals("source")) return 1;
		PaintNode pn = nodes.get(n.id);
		if(pn.x >= 0) return 1;

		int len = tile[pos].length - pn.level;
		if(tile[pos][len] == null){
			tile[pos][len] = pn;
			pn.x = pos;
			pn.y = len;
		}

		if(n.type.equals("source")) return 1;

		int gap = 0;
		for(Node prev_n : n.getPrevNodes()){
			gap += makeTile(prev_n, nodes, pos);
		}

		return 1;
	}

	private void printTile(){
		for(int i=0; i < tile[0].length; i++){
			for(int j=0;j<tile.length;j++){
				if(tile[j][i] != null)
					System.out.print("\t"+tile[j][i].node.id+"("+tile[j][i].x+","+tile[j][i].y+")");
				else
					System.out.print("\tnull");
			}
			System.out.println();
		}
	}

	private int createPaintNodes(Node n, TreeMap<String, PaintNode> nodes, int level){
		if(n.type.equals("source")){
			level = 1;
			PaintNode pn = new PaintNode(n);
			pn.level = level;
			nodes.put(n.id, pn);
			return level;
		}

		int tmp_level = 0;
		for(Node prev_n : n.getPrevNodes()){
			tmp_level = createPaintNodes(prev_n, nodes, level);
			if(tmp_level > level) level = tmp_level;
		}

		PaintNode pn = new PaintNode(n);
		pn.level = ++level;
		nodes.put(n.id, pn);

		return level;
	}
}
