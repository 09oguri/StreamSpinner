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

public class Validator{
	public ConfigureReader conf = null;

	public Validator(ConfigureReader conf){
		this.conf = conf;
	}

	private double calcReadCost(DAG dag){
		if(dag.view_table == null) return 0;

		Vector<Node> sources = new Vector<Node>();
		for(Node n : dag.root_nodes){
			searchNode(n, sources,"source");
		}

		double read_cost = 0;
		Vector<Node> checked = new Vector<Node>();
		for(Node n : sources){
			// ÇPíiÇÃViewÇÃèàóù
			if(!dag.view_table.view.containsKey(n.value)) continue;
			Node target_source = dag.view_table.view.get(n.value);
			read_cost += calcReadCostFromViewTableElement(dag, target_source);
			String relation = dag.view_table.source.get(n.value);

			// à»ç~ÅAëΩíiÇÃViewÇÃèàóù
			while(dag.view_table.view.containsKey(relation)){
				target_source = dag.view_table.view.get(relation);

				// àÍìxâ¡éZÇµÇΩÉpÉXÇÕâ¡éZÇµÇ»Ç¢ 
				if(!checked.contains(target_source)){
					read_cost += calcReadCostFromViewTableElement(dag, target_source);
					checked.add(target_source);
				}

				relation = dag.view_table.source.get(relation);
			}
		}

		return read_cost;
	}

	private double calcReadCostFromViewTableElement(DAG dag, Node n){
		double time = 0;
		if(n.type.equals("root")) return time;
		if(n.id != null) time = dag.index(n.id).time;
		//System.out.println(n.id);

		for(Node nn : n.getNextNodes()){
			time += calcReadCostFromViewTableElement(dag, nn);
		}

		return time;
	}

	private void searchNode(Node n, Vector<Node> nodes, String type){
		for(Node pn : n.getPrevNodes()){
			searchNode(pn, nodes, type);
		}
		if(!n.type.equals(type)) return;
		if(nodes.contains(n)) return;

		nodes.add(n);

		return;
	}

	private double calcOpRateAfterStoreOp(Node store){
		//store.printCost();
		double cost = store.time;

		for(Node n : store.getNextNodes()){
			cost += calcOpRateAfterStoreOp(n);
		}

		return cost;
	}

	public boolean validate(DAG dag){
		double processing_time = 0;
		double writable_rate = 0;
		double after_read_processing_time = 0;

		for(Node n : dag.root_nodes){
			processing_time += calcOpRate(n);
		}

		//dag.printNodeCostList();
		//System.out.println();
		//System.out.println("(*) all cost="+processing_time);

		Vector<Node> stores = new Vector<Node>();
		for(Node n : dag.root_nodes){
			searchNode(n, stores, "store");
		}

		double after_all_store_operator_cost = 0;
		for(Node n : stores){
			after_all_store_operator_cost += calcOpRateAfterStoreOp(n);
		}

		//System.out.println("(*) after store cost="+after_all_store_operator_cost);

		// åÎç∑Çè¨Ç≥Ç≠Ç∑ÇÈÇΩÇﬂÇÃèàóù
		processing_time = (int)(processing_time*100000) - (int)(after_all_store_operator_cost*100000);
		processing_time = processing_time/100000;

		// í~êœâ¬î\îªíË
		/**/
		Vector<String> checked_id = new Vector<String>();
		for(Node n : dag.root_nodes){
			writable_rate += calcWritableRate(n, checked_id);
		}
		/**/

		after_read_processing_time = calcReadCost(dag);
		processing_time += after_read_processing_time;

		//System.out.println("(*) processing time="+processing_time);
		//System.out.println("(*) writable rate="+writable_rate);
		//System.out.println("(*) after_read="+after_read_processing_time);
		//System.out.println();

		dag.processing_cost = processing_time;
		dag.inserting_cost = writable_rate;
		dag.read_cost = after_read_processing_time;

		if(processing_time > 1) return false;
		if(writable_rate > 1) return false;

		return true;
	}

	private double calcWritableRate(Node n, Vector<String> checked){
		double rate = 0;
		if(n.type.equals("store")){
			if(checked.contains(n.id)) return 0;
			rate = calcWritableRateFromSchema(n);
			n.tuple_rate = rate;
			checked.add(n.id);
			//System.out.println(n.id+" "+n.output_schema+" "+rate);
			return rate;
		}

		for(Node nn : n.getPrevNodes()){
			rate += calcWritableRate(nn, checked);
		}

		return rate;
	}
	private double calcWritableRateFromSchema(Node n){
		double rate = 0;

		try{
			DBFunction db = new DBFunction();
			double tuple_size = db.tupleSize(n.output_schema);
			double insertion_rate = conf.insertion_rate(tuple_size);
			double query_rate = n.output_rate;

			rate += ((query_rate*1000) / insertion_rate);

			return rate;
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}

	private double calcOpRate(Node n){
		if(n.isEstimated) return 0;
		n.isEstimated = true;

		double time = 0;
		if(n.type.equals("source")){
			n.output_rate = conf.input_rate(n.value);
			if(n.active_tuple <= 0){
				n.active_tuple = Double.parseDouble(n.window) * n.output_rate;
			}
			n.time = time;

			return time;
		}

		Node[] p = n.getPrevNodes();
		if(p.length == 1){
			time += calcOpRate(p[0]);
			//System.out.println(p[0].id + " " +time);

			if(n.type.equals("root")){
				n.output_rate = p[0].output_rate;
				n.active_tuple = p[0].active_tuple;
			}

			if(n.type.equals("selection")){
				n.output_rate = n.selectivity * p[0].output_rate;
				n.active_tuple = n.selectivity * p[0].active_tuple;
				n.time = conf.cost("selection") * p[0].output_rate;
				time += n.time;
			}

			if(n.type.equals("projection")){
				n.output_rate = p[0].output_rate;
				n.active_tuple = p[0].active_tuple;
				n.time = conf.cost("projection") * p[0].output_rate;
				time += n.time;
			}
			
			if(n.type.equals("store")){
				n.output_rate = p[0].output_rate;
				n.active_tuple = p[0].active_tuple;
			}

			//n.printCost();

			return time;
		}

		if(p.length == 2){
			time += calcOpRate(p[0]);
			//System.out.println(p[0].id + " " +time);
			time += calcOpRate(p[1]);
			//System.out.println(p[1].id + " " + time);

			if(n.type.equals("join")){
				n.output_rate = n.selectivity * (p[0].output_rate * p[1].active_tuple + p[1].output_rate * p[0].active_tuple);
				n.active_tuple = n.selectivity * p[0].active_tuple * p[1].active_tuple;
				//System.out.println(conf.cost("join")+"*"+p[0].output_rate+"*"+p[1].output_rate);
				n.time = conf.cost("join") * (p[0].output_rate + p[1].output_rate);
				time += n.time;
			}

			//n.printCost();

			return time;
		}

		return time;
	}
}
