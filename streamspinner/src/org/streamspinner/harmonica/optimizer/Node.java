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
import java.util.regex.*;

public class Node{
	// Operator and Source properties
	public String type = null;
	public String value = null;
	public String window = null;
	public String predicate = null;
	public String attribute = null;
	public String id = null;
	public double selectivity = 1;
	public SchemaInformation output_schema = null;

	// Estimate properties
	public double output_rate = -1;
	public double active_tuple = -1;
	public boolean isEstimated = false;
	public double time = 0;
	public double tuple_rate = 0;

	private int position = -1;

	public ArrayList<Node> next_nodes = null;
	public ArrayList<Node> prev_nodes = null;
	
	public Node(){
		next_nodes = new ArrayList<Node>();
		prev_nodes = new ArrayList<Node>();
	}

	public int size(){
		if(type.equals("root")) return 0;

		int size = 0;
		for(Node nn : next_nodes){
			int tmp = nn.size();
			if(size < tmp) size = tmp;
		}

		return size + 1;
	}

	public void convertTableName(String tname){
		Pattern p = Pattern.compile("([^\\.\\s\\n,\\d][^\\.\\s\\n,]*)\\.([^\\.\\s\\n,\\d][^\\.\\s\\n,]*)");
		if(type.equals("selection")){
			if(predicate == null) return;
			Matcher m = p.matcher(predicate);
			StringBuffer buf = new StringBuffer();
			while(m.find()){
				m.appendReplacement(buf, tname+".$1_$2");
			}
			m.appendTail(buf);
			predicate = buf.toString();
		}else if(type.equals("projection")){
			if(attribute == null) return;
			Matcher m = p.matcher(attribute);
			StringBuffer buf = new StringBuffer();
			while(m.find()){
				m.appendReplacement(buf, tname+".$1_$2");
			}
			m.appendTail(buf);
			attribute = buf.toString();
			/*
			String[] each_attr = attribute.split(",");
			for(int i=0; i< each_attr.length: i++){
				String[] n_a = each_attr[i].split(".");
				each_attr[i] = tname + "." + n_a[0] + "_" + n_a[1];
			}
			StringBuilder buf = new StringBuilder();
			int i = 0;
			for(String str : each_attr){
				if(i != 0) buf.append(",");
				buf.append(str);
				i++;
			}
			attribute = buf.toString();
			*/
		}
	}

	public boolean equals(Node n){
		if(hashCode() == n.hashCode()) return true;
		if(n.id.equals(id)) return true;
		return false;
	}

	public void insertNextNode(Node n){
		if(!n.prev_nodes.contains(this)) n.prev_nodes.add(this);
		n.next_nodes = this.next_nodes;
		
		for(Node nn : n.next_nodes){
			nn.prev_nodes.remove(this);
			nn.prev_nodes.add(n);
		}

		this.next_nodes = new ArrayList<Node>();
		this.next_nodes.add(n);
	}

	public void insertPrevNode(Node n){
		if(!n.next_nodes.contains(this)) n.next_nodes.add(this);
		n.prev_nodes = this.prev_nodes;
		
		for(Node nn : n.prev_nodes){
			nn.next_nodes.remove(this);
			nn.next_nodes.add(n);
		}

		this.prev_nodes = new ArrayList<Node>();
		this.prev_nodes.add(n);
	}

	public Node clone(){
		Node n = new Node();
		n.type = type;
		n.value = value;
		n.window = window;
		n.predicate = predicate;
		n.attribute = attribute;
		n.id = id;
		n.selectivity = selectivity;
		n.output_schema = output_schema;

		//n.next_nodes = copy(next_nodes);
		//n.prev_nodes = copy(prev_nodes);

		return n;
	}

	public Node deepClone(){
		Node new_node = clone();

		new_node.output_rate = output_rate;
		new_node.active_tuple = active_tuple;
		new_node.isEstimated = isEstimated;
		new_node.time = time;

		return new_node;
	}

	public void printDeepNode(){
		printCost();
		for(Node n : getNextNodes()){
			n.printDeepNode();
		}
	}

	private ArrayList<Node> copy(ArrayList<Node> v){
		ArrayList<Node> nv = new ArrayList<Node>();
		for(Node n : v){
			nv.add(n);
		}

		return nv;
	}

	public void clearPosition(){
		position = -1;
	}

	public int position(){
		if(position > 0) return position;

		int pos = 1;
		for(Node n : getPrevNodes()){
			if(pos < n.position() + 1) pos = n.position() + 1;
		}

		position = pos;
		return pos;
	}

	public void addNextNode(Node n){
		if(!next_nodes.contains(n)) next_nodes.add(n);
		if(!n.prev_nodes.contains(this)) n.prev_nodes.add(this);
	}

	public void addPrevNode(Node n){
		if(!prev_nodes.contains(n)) prev_nodes.add(n);
		if(!n.next_nodes.contains(this)) n.next_nodes.add(this);
	}
	
	public void removeNextNode(Node n){
		for(Node nn : n.next_nodes){
			nn.prev_nodes.remove(n);
			nn.prev_nodes.add(this);
		}
		
		this.next_nodes.remove(n);
		this.next_nodes.addAll(n.next_nodes);
	}

	public void removePrevNode(Node n){
		for(Node nn : n.prev_nodes){
			nn.next_nodes.remove(n);
			nn.next_nodes.add(this);
		}
		
		this.prev_nodes.remove(n);
		this.prev_nodes.addAll(n.prev_nodes);
	}

	public void deleteNextNode(Node n){
		if(next_nodes.contains(n)) next_nodes.remove(n);
	}
	public void deletePrevNode(Node n){
		if(prev_nodes.contains(n)) prev_nodes.remove(n);
	}

	public Node[] getNextNodes(){return next_nodes.toArray(new Node[0]);}
	public Node[] getPrevNodes(){return prev_nodes.toArray(new Node[0]);}
	public void setNextNodes(Node[] node){
		ArrayList<Node> v = new ArrayList<Node>();
		for(Node n : node){
			v.add(n);
		}

		next_nodes = v;
		return;
	}
	public void setPrevNodes(Node[] node){
		ArrayList<Node> v = new ArrayList<Node>();
		for(Node n : node){
			v.add(n);
		}

		prev_nodes = v;
		return;
	}

	public String toString(){
		StringBuilder buf = new StringBuilder();

		buf.append("[");
		buf.append("id="+id);
		buf.append(",type="+type+",value="+value+",active_tuple="+active_tuple+",selectivity="+selectivity);
		buf.append(",predicate="+predicate+",attirbute="+attribute);
		buf.append(",next_nodes="+next_nodes.size());
		buf.append(",prev_nodes="+prev_nodes.size());
		buf.append(",schema="+output_schema);
		buf.append("]");

		return buf.toString();
	}

	public void printCost(){
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		buf.append("id="+id);
		buf.append(",output_rate="+output_rate);
		buf.append(",active_tuple="+active_tuple);
		buf.append(",cost="+time+"ms");
		buf.append(",output_schema="+output_schema);
		buf.append("]");

		System.out.println(buf);
	}

	
	public String toIDs(){
		StringBuilder buf = new StringBuilder();
		
		if(prev_nodes.size() == 0){
			buf.append("["+id);
			if(type.equals("root")) buf.append("(R)");
			if(type.equals("selection")) buf.append("(S)");
			if(type.equals("join")) buf.append("(J)");
			if(type.equals("projection")) buf.append("(P)");
			if(type.equals("store")) buf.append("(I)");
			buf.append("]");
			return buf.toString();
		}

		buf.append("{");
		buf.append("["+id);
		if(type.equals("root")) buf.append("(R)");
		if(type.equals("selection")) buf.append("(S)");
		if(type.equals("join")) buf.append("(J)");
		if(type.equals("projection")) buf.append("(P)");
		if(type.equals("store")) buf.append("(I)");
		buf.append("]");

		buf.append("<=");
		for(Node n : prev_nodes){
			buf.append(n.toIDs());
		}
		buf.append("}");

		return buf.toString();
	}
}
