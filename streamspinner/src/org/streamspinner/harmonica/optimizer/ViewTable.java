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

public class ViewTable{
	public HashMap<String, Node> view = new HashMap<String, Node>();
	public HashMap<String, String> source = new HashMap<String, String>();

	public ViewTable(){}

	public void addView(String table_name, String source_name, Node root_node){
		view.put(table_name, root_node);
		source.put(table_name, source_name);
	}
	public void removeView(String table_name){
		view.remove(table_name);
		source.remove(table_name);
	}

	public void printTable(){
		for(String s : view.keySet()){
			System.out.print(s+"\t");
			if(source.get(s) != null){ System.out.print(source.get(s).toString()+"\t"); }
			else System.out.println("null");
			System.out.print(view.get(s).toString()+"\t");
			if(view.get(s).getNextNodes().length != 0){ 
				System.out.println("next:" +view.get(s).getNextNodes()[0].id+"\t"); 
			}else{
			   	System.out.println("zero");
			}
		}
	}
	public void printAllPath(){
		for(String s : view.keySet()){
			System.out.println(s);
			view.get(s).printDeepNode();
			System.out.println();
		}
	}

	public ViewTable clone(){
		ViewTable new_table = new ViewTable();
		new_table.view = copy(view);
		new_table.source = new HashMap<String, String>(source);

		return new_table;
	}

	private HashMap<String, Node> copy(HashMap<String, Node> h){
		HashMap<String, Node> tmp = new HashMap<String, Node>();
		for(String s : h.keySet()){
			tmp.put(s, deepCopy(h.get(s)));
		}

		return tmp;
	}
	private Node deepCopy(Node n){
		Node new_node = n.deepClone();

		for(Node nn : n.getNextNodes()){
			new_node.addNextNode(deepCopy(nn));
		}

		return new_node;
	}
}
