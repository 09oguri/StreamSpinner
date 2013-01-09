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

import java.util.*;
import javax.xml.stream.*;
import java.io.*;

public class DAG{
	public static int _id = 1;
	public ArrayList<Node> root_nodes = new ArrayList<Node>();
	public ViewTable view_table = new ViewTable();
	public HashMap<String, Node> index = new HashMap<String, Node>();
	public int id = -1;
	public String master = null;

	public double processing_cost = 0;
	public double inserting_cost = 0;
	public double read_cost = 0;

	private Map<String, ArrayList<Node>> prev_node_ids = new TreeMap<String, ArrayList<Node>>();
	private ConfigureReader conf = null;
	private String xml = null;
	private String xml_applied_view = null;

	private void writeNodeToXML(XMLStreamWriter xw, Node n, ArrayList<Node>written) throws XMLStreamException{
		if(written.contains(n)) return;

		if(n.type.equals("source")){
			xw.writeCharacters("\n\t");
			xw.writeEmptyElement("source");
			xw.writeAttribute("id",n.id);
			xw.writeAttribute("name",n.value);
			xw.writeAttribute("window",n.window);
			xw.writeAttribute("window_at","0");

			written.add(n);
		}else{
			xw.writeCharacters("\n\t");
			xw.writeStartElement("operator");
			xw.writeAttribute("id",n.id);
			xw.writeAttribute("type",n.type);

			if(n.type.equals("store")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","table");
				xw.writeAttribute("value",n.value);
			}else if(n.type.equals("selection")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","predicate");
				xw.writeAttribute("value",n.predicate);
			}else if(n.type.equals("projection")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","attribute");
				xw.writeAttribute("value",n.attribute);
			}else if(n.type.equals("join")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","predicate");
				xw.writeAttribute("value",n.predicate);
			}

			for(Node pn : n.prev_nodes){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("input");
				xw.writeAttribute("refid",pn.id);
			}

			for(Node nn : n.next_nodes){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("output");
				xw.writeAttribute("refid",nn.id);
			}
			xw.writeCharacters("\n\t");
			xw.writeEndElement();
			written.add(n);

			if(n.type.equals("root")) return;
		}

		for(Node nn : n.next_nodes){
			writeNodeToXML(xw, nn, written);
		}
	}


	public String toXML(){
		if(xml != null) return xml;

		try{
			StringWriter writer = new StringWriter();
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter xw = factory.createXMLStreamWriter(writer);

			xw.writeStartDocument();
			xw.writeCharacters("\n");
			xw.writeStartElement("plan");
			xw.writeAttribute("master",master);

			DAG dag_clone = clone();
			ArrayList<Node> sources = dag_clone.searchNodes("source");
			ArrayList<Node> written = new ArrayList<Node>();
			for(Node n : sources){
				writeNodeToXML(xw, n, written);
			}

			xw.writeCharacters("\n");
			xw.writeEndElement();
			xw.writeCharacters("\n");
			xw.writeEndDocument();
			xw.writeCharacters("\n");
			xml = writer.toString();
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}catch(XMLStreamException e){
			e.printStackTrace();
			return null;
		}

		return xml;
	}

	private String writeSourceNodeToXMLAppliedView(XMLStreamWriter xw, Node n) 
	throws XMLStreamException{

		if(!view_table.view.containsKey(n.value)){
			xw.writeCharacters("\n\t");
			xw.writeEmptyElement("source");
			xw.writeAttribute("id",n.id);
			xw.writeAttribute("name",n.value);
			xw.writeAttribute("window",n.window);
			xw.writeAttribute("window_at","0");
			return n.id;
		}

		Node prev_source = view_table.view.get(n.value);
		prev_source.id = n.id;
		prev_source.window = n.window;
		String pid = writeSourceNodeToXMLAppliedView(xw, prev_source);

		Node target = null;
		String[] next_ids = new String[n.next_nodes.size()];
		for(int i=0;i < next_ids.length; i++){
			next_ids[i] = n.next_nodes.get(i).id;
		}
		for(Node nn : prev_source.getNextNodes()){
			target = writeOperatorNodeToXMLAppliedView(xw, nn, next_ids);
		}
		//System.out.println(target);

		/*
		String view_id = target.id+".root_view";
		xw.writeCharacters("\n\t");
		xw.writeStartElement("operator");
		xw.writeAttribute("id",view_id);
		xw.writeAttribute("type","view");
		
		for(Node pn : target.prev_nodes){
			xw.writeCharacters("\n\t\t");
			xw.writeEmptyElement("input");
			xw.writeAttribute("refid",pn.id);
			//xw.writeAttribute("refid",pid);
		}

		for(Node nn : n.next_nodes){
			xw.writeCharacters("\n\t\t");
			xw.writeEmptyElement("output");
			xw.writeAttribute("refid",nn.id);
		}

		xw.writeCharacters("\n\t");
		xw.writeEndElement();
		*/

		return target.prev_nodes.get(0).id;
	}

	private Node writeOperatorNodeToXMLAppliedView(XMLStreamWriter xw, Node n, String[] next_ids) 
	throws XMLStreamException{
		if(n.type.equals("root")) return n;

		xw.writeCharacters("\n\t");
		xw.writeStartElement("operator");
		xw.writeAttribute("id",n.id);
		xw.writeAttribute("type",n.type);

		if(n.type.equals("selection")){
			xw.writeCharacters("\n\t\t");
			xw.writeEmptyElement("parameter");
			xw.writeAttribute("name","predicate");
			xw.writeAttribute("value",n.predicate);
		}else if(n.type.equals("projection")){
			xw.writeCharacters("\n\t\t");
			xw.writeEmptyElement("parameter");
			xw.writeAttribute("name","attribute");
			xw.writeAttribute("value",n.attribute);
		}

		for(Node pn : n.prev_nodes){
			xw.writeCharacters("\n\t\t");
			xw.writeEmptyElement("input");
			xw.writeAttribute("refid",pn.id);
		}

		for(Node nn : n.next_nodes){
			if(!nn.type.equals("root")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("output");
				xw.writeAttribute("refid",nn.id);
			}else{
				for(String ids : next_ids){
					xw.writeCharacters("\n\t\t");
					xw.writeEmptyElement("output");
					xw.writeAttribute("refid",ids);
					//xw.writeAttribute("refid",nn.id+".root_view");
				}
			}
		}

		xw.writeCharacters("\n\t");
		xw.writeEndElement();
		
		for(Node nn : n.getNextNodes()){
			return writeOperatorNodeToXMLAppliedView(xw, nn, next_ids);
		}

		return null;
	}

	private void writeNodeToXMLAppliedView(XMLStreamWriter xw, Node n, ArrayList<Node>written, String prev_id) 
	throws XMLStreamException{
		if(written.contains(n)) return;

		String pid = null;
		if(n.type.equals("source")){
			pid = writeSourceNodeToXMLAppliedView(xw, n);
			written.add(n);
		}else{
			xw.writeCharacters("\n\t");
			xw.writeStartElement("operator");
			xw.writeAttribute("id",n.id);
			xw.writeAttribute("type",n.type);

			if(n.type.equals("store")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","table");
				xw.writeAttribute("value",n.value);

				for(Node pn : n.prev_nodes){
					xw.writeCharacters("\n\t\t");
					xw.writeEmptyElement("input");
					xw.writeAttribute("refid",pn.id);
				}

				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("output");
				xw.writeAttribute("refid",n.id+".root");
				xw.writeCharacters("\n\t");
				xw.writeEndElement();
				written.add(n);		

				xw.writeCharacters("\n\t");
				xw.writeStartElement("operator");
				xw.writeAttribute("id",n.id+".root");
				xw.writeAttribute("type","root");
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("input");
				xw.writeAttribute("refid",n.id);
				xw.writeCharacters("\n\t");
				xw.writeEndElement();

				return;
				
			}else if(n.type.equals("selection")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","predicate");
				xw.writeAttribute("value",n.predicate);
			}else if(n.type.equals("projection")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","attribute");
				xw.writeAttribute("value",n.attribute);
			}else if(n.type.equals("join")){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("parameter");
				xw.writeAttribute("name","predicate");
				xw.writeAttribute("value",n.predicate);
			}

			if(prev_id == null){
				for(Node pn : n.prev_nodes){
					xw.writeCharacters("\n\t\t");
					xw.writeEmptyElement("input");
					xw.writeAttribute("refid",pn.id);
				}
			}else{
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("input");
				xw.writeAttribute("refid",prev_id);
			}

			for(Node nn : n.next_nodes){
				xw.writeCharacters("\n\t\t");
				xw.writeEmptyElement("output");
				xw.writeAttribute("refid",nn.id);
			}

			xw.writeCharacters("\n\t");
			xw.writeEndElement();
			written.add(n);

			if(n.type.equals("root")) return;
		}

		for(Node nn : n.next_nodes){
			writeNodeToXMLAppliedView(xw, nn, written , pid);
		}
	}

	public String toXMLAppliedView(){
		if(xml_applied_view != null) return xml_applied_view;

		try{
			StringWriter writer = new StringWriter();
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter xw = factory.createXMLStreamWriter(writer);

			xw.writeStartDocument();
			xw.writeCharacters("\n");
			xw.writeStartElement("plan");
			xw.writeAttribute("master",master);

			DAG clone_dag = clone();
			ArrayList<Node> sources = clone_dag.searchNodes("source");
			ArrayList<Node> written = new ArrayList<Node>();
			for(Node n : sources){
				writeNodeToXMLAppliedView(xw, n, written, null);
			}

			xw.writeCharacters("\n");
			xw.writeEndElement();
			xw.writeCharacters("\n");
			xw.writeEndDocument();
			xw.writeCharacters("\n");
			xml_applied_view = writer.toString();
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}catch(XMLStreamException e){
			e.printStackTrace();
			return null;
		}

		return xml_applied_view;
	}

	public ArrayList<Node> searchNodes(String type){
		ArrayList<Node> nodes = new ArrayList<Node>();

		for(Node n : root_nodes){
			searchNodeElement(n, type, nodes);
		}

		return nodes;
	}
	private void searchNodeElement(Node n, String type, ArrayList<Node> checked){
		if(n.type.equals(type)){
			if(!checked.contains(n)) checked.add(n);
		}

		for(Node pn : n.getPrevNodes()){
			searchNodeElement(pn, type, checked);
		}
	}

	public String toString(){
		StringBuilder buf = new StringBuilder();
		buf.append("DAG root :");
		for(Node n : root_nodes){
			buf.append(" "+n.id);
		}

		return buf.toString();
	}

	public DAG(ConfigureReader conf, String master){
		this.conf = conf;
		this.id = _id++;
		this.master = master;
	}

	public Node index(String id){
		return index.get(id);
	}

	public DAG clone(){
		DAG new_dag = new DAG(conf, master);
		HashMap<String, Node> cloned_node = new HashMap<String, Node>();

		long t1 = System.currentTimeMillis();
		for(Node n : root_nodes){
			clone_element(new_dag, n, cloned_node);
		}
		long t2 = System.currentTimeMillis();
		
		new_dag.completeDAG();
		new_dag.processing_cost = processing_cost;
		new_dag.inserting_cost = inserting_cost;
		new_dag.read_cost = read_cost;

		new_dag.view_table = view_table.clone();
		long t3 = System.currentTimeMillis();

		//System.out.println("clone_dag=>(clone_elements:clone_view_table),"+(t2-t1)+","+(t3-t2));

		return new_dag;
	}

	private void clone_element(DAG new_dag, Node n, HashMap<String, Node> cloned_node){
		Node target = null;
		if(cloned_node.containsKey(n.id)){
			return;
			//target = cloned_node.get(n.id);
		}else{
			target = n.clone();
			cloned_node.put(target.id, target);
		}
		if(n.type.equals("source")){
			new_dag.addNode("-1",target);
		}
		for(Node pn : n.getPrevNodes()){
			long t1 = System.currentTimeMillis();
			clone_element(new_dag, pn, cloned_node);
			long t2 = System.currentTimeMillis();
			new_dag.addNode(pn.id, target);
			long t3 = System.currentTimeMillis();

			//if(t3-t2 > 30){
			//	System.out.println("clone_element=>(recall_clone_element:add_node),"+(t2-t1)+","+(t3-t2)+","+target.id+":"+pn.id);
			//}
		}
	}


	public Map<Integer, ArrayList<Node>> getBranches(){
		TreeMap<Integer, ArrayList<Node>> map = new TreeMap<Integer, ArrayList<Node>>();
		for(Node n : root_nodes){
			checkBranch(n, map);
		}

		return map;
	}
	private void checkBranch(Node n, Map<Integer, ArrayList<Node>> map){
		if(n.getNextNodes().length >= 2){
			int pos = n.position();
			if(!map.containsKey(pos)){
			   	ArrayList<Node> nodes = new ArrayList<Node>();
				nodes.add(n);
				map.put(pos,nodes);
			}else{
				ArrayList<Node> nodes = map.get(pos);
				if(!nodes.contains(n)) nodes.add(n);
			}
		}
		for(Node p : n.getPrevNodes()){
			checkBranch(p, map);
		}
	}

	public void resetPosition(){
		for(Node n : root_nodes){
			resetNodePosition(n);
		}
	}
	private void resetNodePosition(Node n){
		for(Node p : n.getPrevNodes()){
			p.clearPosition();
		}
		n.clearPosition();
	}

	public void printDAG(){
		System.out.println("*** DAG Info "+id+" ***");
		for(Node n : root_nodes){
			System.out.print("*");
			System.out.println(n.toIDs());
		}
		System.out.println();

		System.out.println("+++ View Table +++");
		view_table.printTable();
		System.out.println("++++++++++++++++++");
		//view_table.printAllPath();
	}

	public void printNodeList(){
		ArrayList<String> checked = new ArrayList<String>();
		System.out.println("*** Print Node List ***");
		for(Node n : root_nodes){
			printNodeElement(n, checked);
		}
	}
	private void printNodeElement(Node n, ArrayList<String> checked){
		for(Node cn : n.getPrevNodes()){
			printNodeElement(cn, checked);
		}
		if(!checked.contains(n.id)){
			System.out.println(n);
			checked.add(n.id);
		}
	}

	public void printNodeCostList(){
		ArrayList<String> checked = new ArrayList<String>();
		System.out.println("*** Print Node Cost List ***");
		for(Node n : root_nodes){
			printNodeCostElement(n, checked);
		}
	}
	private void printNodeCostElement(Node n, ArrayList<String> checked){
		for(Node cn : n.getPrevNodes()){
			printNodeCostElement(cn, checked);
		}
		if(!checked.contains(n.id)){
			n.printCost();
			checked.add(n.id);
		}
	}

	public void clearCost(){
		for(Node n : root_nodes){
			clearNodeCost(n);
		}
	}

	public void completeDAG(){
		for(Node n : root_nodes){
			analyzeNodeSchema(n);
		}
	}

	private void analyzeNodeSchema(Node n){
		Node[] prev = n.getPrevNodes();
		//System.out.println(n.id+" "+prev.length);

		if(prev.length == 0){
			if(n.type.equals("source")){
				if(n.output_schema == null) n.output_schema = conf.schema(n.value);
			}
			return;
		}
		if(prev.length == 1){
			analyzeNodeSchema(prev[0]);
			if(n.type.equals("selection")){
				n.output_schema = prev[0].output_schema;
			}else if(n.type.equals("store")){
				n.output_schema = prev[0].output_schema;
			}else if(n.type.equals("root")){
				n.output_schema = prev[0].output_schema;
			}else if(n.type.equals("projection")){
				String[] attrs = n.attribute.split(",");
				n.output_schema = prev[0].output_schema.projection(attrs);
			}
			return;
		}
		if(prev.length == 2){
			analyzeNodeSchema(prev[0]);
			analyzeNodeSchema(prev[1]);
			if(n.type.equals("join")){
				n.output_schema = prev[0].output_schema.join(prev[1].output_schema);
			}
			return;
		}
	}

	private void clearNodeCost(Node n){
		n.output_rate = -1;
		n.active_tuple = -1;
		n.time = 0;
		n.isEstimated = false;

		for(Node p : n.getPrevNodes()){
			clearNodeCost(p);
		}
	}

	/**
	 * @param prev_id 対象とするノードの一つ前の演算のID
	 * @param n 対象とするノード
	 */
	public void addNode(String prev_id, Node n){
		long t1 = System.currentTimeMillis();
		index.put(n.id, n);
		if(root_nodes.size() == 0){
			root_nodes.add(n);
			ArrayList<Node> v = new ArrayList<Node>();
			v.add(n);
			// prev_node_ids k=> 一つ前のノードID v=>kのIDを持つノードが指すべき次のノード
			prev_node_ids.put(prev_id,v);
			return;
		}

		long t2 = System.currentTimeMillis();
		// 現在のノードが指すべき次のノードがあるかを探索
		boolean isNewRoot = true;
		if(prev_node_ids.containsKey(n.id)){
			isNewRoot = false;
			ArrayList<Node> v = prev_node_ids.get(n.id);
			for(Node pn : v){
				n.addNextNode(pn);
				//if(pn.prev_nodes.contains(n)) pn.prev_nodes.add(n);
				//if(n.next_nodes.contains(pn)) n.next_nodes.add(pn);
			}
		}

		long t3 = System.currentTimeMillis();
		if(prev_node_ids.containsKey(prev_id)){ // すでに誰かが作った一個前のキーがある場合
			ArrayList<Node> v = prev_node_ids.get(prev_id);
			v.add(n);
		}else{ // ない場合
			ArrayList<Node> v = new ArrayList<Node>();
			v.add(n);
			prev_node_ids.put(prev_id,v);
		}

		long t4 = System.currentTimeMillis();
		// 現在のノードが指すべき前のノードがあるかを探索
		/*
		for(ArrayList<Node> vn : prev_node_ids.values()){
			for(Node nn : vn){
				if(nn.id.equals(prev_id)){
					n.addPrevNode(nn);
					//if(n.prev_nodes.contains(nn)) n.prev_nodes.add(nn);
					//if(nn.next_nodes.contains(n)) nn.next_nodes.add(n);
				}
			}
		}
		*/
		if(index.containsKey(prev_id)){
			Node nn = index.get(prev_id);
			n.addPrevNode(nn);
		}

		long t5 = System.currentTimeMillis();
		// 削除するRootノード検索
		ArrayList<Node> remove_node = new ArrayList<Node>();
		for(Node vn : root_nodes){
			if(vn.id.equals(prev_id)){
				remove_node.add(vn);
			}
		}

		long t6 = System.currentTimeMillis();

		/*if(t5-t1 > 2000){
			//System.out.println("addNode=>"+(t2-t1)+","+(t3-t2)+","+(t4-t3)+","+(t5-t4)+","+
				prev_node_ids.values().size());
			for(ArrayList<Node> vn : prev_node_ids.values()){
				System.out.print(" "+vn.size());
			}
		}*/

		// Rootノードの追加
		if(remove_node.size() != 0){
			// Rootノードの削除
			for(Node rn : remove_node){
				root_nodes.remove(rn);
			}

			// Rootノードの追加
			if(isNewRoot) root_nodes.add(n);

			return;
		}

		if(isNewRoot) root_nodes.add(n);
	}
}
