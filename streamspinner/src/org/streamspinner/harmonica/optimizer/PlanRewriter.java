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

public class PlanRewriter{
	public static int count = 0;
	private DAG dag = null;
	private Node target = null;
	private ConfigureReader conf = null;

	//public ProcessResult original_plan = null;
	public PlanRewriter(DAG dag, Node target, ConfigureReader conf){
		this.dag = dag;
		this.target = target;
		this.conf = conf;
	}

	public ProcessResult generateOptimizedPlan(){
		TreeSet<ProcessResult> result = generatePlanRanking();

		/*
		System.out.println("*** Result Ranking ***");
		for(ProcessResult pr : result){
			System.out.println(pr);
			pr.dag.printDAG();
		}
		System.out.println();
		*/

		if(result == null) return null;
		return result.first();
	}

	public TreeSet<ProcessResult> generatePlanRanking(){
		if(target == null) return null;

		ProcessResult pr = null;
		// 大元の処理プランのコスト計算
		/*
		if(isMergeOriginal){
			//System.out.println("*** Original Plan Cost ***");
			pr = new ProcessResult();
			pr.dag = dag.clone();
			//pr.dag.printDAG();
			calculateCost(pr);
			//System.out.println();
		}
		*/

		// マージ可能な出力候補を抽出
		ArrayList<Node> candidate = new ArrayList<Node>();
		for(Node nn : target.getNextNodes()){
			if(!containsJoin(nn) && containsStore(nn)) candidate.add(nn);
		}

		if(candidate.size() <= 1) return null;

		/*
		System.out.println("Target Node : " + target.id);
		System.out.print("Candidate Nodes :");
		for(Node c : candidate){
			System.out.print(" "+c.id);
		}
		System.out.println();
		*/

		// マージの組み合わせ候補を抽出
		ArrayList<ArrayList<Node>> combination = null;
		if(conf.isShort) combination = createCombinationAppendType(candidate);
		else combination = createCombination(candidate);

		/*
		System.out.print("Combination :");
		for(ArrayList<Node> n : combination){
			System.out.print(" [");
			for(Node en : n){
				System.out.print(" " + en.id);
			}
			System.out.print(" ]");
		}
		System.out.println();
		System.out.println();
		*/

		// マージ候補の組み合わせからDAGを作成
		TreeSet<ProcessResult> result = developDAG(combination);

		return result;
	}

	private void print(Node[] n){
		System.out.print("[");
		for(Node o : n){
			System.out.print(" "+o.id);
		}
		System.out.println(" ]");
	}

	private TreeSet<ProcessResult> integrate(Vector<Node> node){
		if(node.size() < 2) return null;

		DAG clone_dag = dag.clone();
		Node clone_target = clone_dag.index(target.id);

		// sort
		Vector<Node> sorted_set = new Vector<Node>();
		boolean isSorted = false;
		for(Node n : node){
			Node clone_n = clone_dag.index(n.id);
			Node sorted_node = sortingOperators(clone_n, clone_target.output_schema);

			boolean isPlanChanged = checkPlanChanged(clone_n, sorted_node);

			if(isPlanChanged){
				changeNextNodePath(clone_target, clone_n.id, sorted_node);
				isSorted = true;
			}
			sorted_set.add(sorted_node);
		}
		if(isSorted){
			System.out.println("*** Sort Phase ***");
			System.out.println("--- before sort ---");
			dag.printDAG();
			System.out.println("--- after sort ---");
			clone_dag.printDAG();
		}
		// sort complete

		// integrate
		boolean isIntegratable = checkIntegratable(clone_target, sorted_set);
		if(!isIntegratable) return null;
		System.out.println("\n*** Integration Phase ***");
		
		Node new_target = integrateNodes(clone_target, sorted_set);
		//clone_dag.printDAG();
		PlanRewriter inner_opt = new PlanRewriter(clone_dag, new_target, conf);
		return inner_opt.generatePlanRanking();
	}

	private String merge_attribute(String attr1, String attr2){
		Vector<String> name = new Vector<String>();

		String[] tmp1 = attr1.split(",");
		for(String tmp : tmp1){
			if(!name.contains(tmp)){
				name.add(tmp);
			}
		}

		tmp1 = attr2.split(",");
		for(String tmp : tmp1){
			if(!name.contains(tmp)){
				name.add(tmp);
			}
		}

		StringBuilder buf = new StringBuilder();
		int i = 0;
		for(String n : name){
			if(i != 0) buf.append(",");
			buf.append(n);
			i++;
		}

		return buf.toString();
	}

	private Node integrateNodes(Node clone_target, Vector<Node> node_set){
		Node tmp = node_set.get(0);
		Node new_node = new Node();
		new_node.type = tmp.type;
		new_node.id = "integrate";

		System.out.print("Integrate Target Node : ");
		for(Node n : node_set){
			System.out.print(" "+n.id);
			if(new_node.type.equals("selection")){
				if(new_node.predicate == null){
					new_node.predicate = n.predicate;
					new_node.selectivity = n.selectivity;
				}else{
					new_node.predicate += " OR " + n.predicate;
					new_node.selectivity = new_node.selectivity + n.selectivity;
					if(new_node.selectivity > 1) new_node.selectivity = 1;
				}
			}
			if(new_node.type.equals("projection")){
				if(new_node.attribute == null){
					new_node.attribute = n.attribute;
					new_node.output_schema = n.output_schema;
				}else{
					new_node.attribute = merge_attribute(new_node.attribute, n.attribute);
					new_node.output_schema = new_node.output_schema.merge(n.output_schema);
				}
			}
			new_node.next_nodes.add(n);
			n.prev_nodes = new ArrayList<Node>();
			n.prev_nodes.add(new_node);
			new_node.id += "_" + n.id;
			clone_target.next_nodes.remove(n);
		}
		new_node.prev_nodes.add(clone_target);
		clone_target.next_nodes.add(new_node);
		System.out.println();

		if(new_node.type.equals("projection")){
			for(Node n : new_node.getNextNodes()){
				if(!new_node.output_schema.equalsAllElements(n.output_schema)) continue;
				new_node.removeNextNode(n);
				new_node.id = n.id;
				System.out.println("Insert Projection Operator : " + new_node.id);
			}
		}

		return new_node;
	}

	private boolean checkIntegratable(Node clone_target, Vector<Node> nodes){
		if(clone_target.id.startsWith("integrate")) return false;

		boolean isSelectionMatch = true;
		boolean isProjectionMatch = true;
		for(Node n : nodes){
			if(!n.type.equals("selection")){
				isSelectionMatch = false;
			}
			if(!n.type.equals("projection")){
				isProjectionMatch = false;
			}
		}

		if(isSelectionMatch || isProjectionMatch) return true;

		return false;
	}

	private boolean checkPlanChanged(Node n1, Node n2){
		if(!n1.id.equals(n2.id)) return true;

		if(n1.type.equals("store")) return false;
		if(n1.type.equals("root")) return false;

		return checkPlanChanged(n1.getNextNodes()[0], n2.getNextNodes()[0]);
	}

	private void changeNextNodePath(Node n, String from_id, Node to_node){
		Node wanted = null;
		for(Node nn : n.next_nodes){
			if(nn.id.equals(from_id)){
				wanted = nn;
				break;
			}
		}
		if(wanted == null) return;
		n.next_nodes.remove(wanted);
		n.next_nodes.add(to_node);

		return;
	}

	private Node sortingOperators(Node n, SchemaInformation start_schema){
		// selection -> projection -> store
		Node selection_node = null;
		Node projection_node = null;
		Node store_node = null;

		Node current = n;
		Node[] current_prev = current.getPrevNodes();

		while(true){
			if(current.type.equals("store")){
				store_node = current;
				break;
			}
			if(current.type.equals("selection")){
				current.output_schema = start_schema;
				if(selection_node == null){
					selection_node = current;
				}else{
					selection_node.predicate = selection_node.predicate + " OR " + current.predicate;
				}
			}
			if(current.type.equals("projection")){
				projection_node = current;
			}

			current = current.getNextNodes()[0];
		}

		current = null;
		Node start = null;
		if(selection_node != null){
			start = selection_node;
			selection_node.setPrevNodes(current_prev);
			current_prev = new Node[]{selection_node};
			current = selection_node;
		}

		if(projection_node != null){
			if(start == null){ start = projection_node; }

			projection_node.setPrevNodes(current_prev);
			current_prev = new Node[]{projection_node};
			if(current != null) current.setNextNodes(new Node[]{projection_node});
			current = projection_node;
		}

		if(start == null){ start = store_node; }
		store_node.setPrevNodes(current_prev);
		if(current != null) current.setNextNodes(new Node[]{store_node});

		return start;
	}

	private Vector<Node> createPath(Node store_node, Node start_node){
		Vector<Node> path = null;

		if(start_node.equals(store_node)){
			path = new Vector<Node>();
			Node n = start_node.deepClone();
			path.add(n);
			return path;
		}

		path = createPath(store_node.getPrevNodes()[0], start_node);
		Node n = store_node.deepClone();
		path.add(n);
		return path;
	}

	private Vector<Node> makeViewTableElement(Node start_node, Node[] store_nodes, String table_name){
		Vector<Node> view_table_element = new Vector<Node>();

		for(int i=0; i<store_nodes.length; i++){
			Node queue = new Node();
			queue.type = store_nodes[i].value;
			Node[] path = createPath(store_nodes[i], start_node).toArray(new Node[0]);
			for(int j=1;j<path.length;j++){
			   	path[j-1].insertNextNode(path[j]);
				//path[j-1].output_schema = path[j-1].output_schema.convertTableName(table_name);
				path[j-1].convertTableName(table_name);
			}
			path[path.length-1].type = "root";
			path[path.length-1].convertTableName(table_name);
			//path[path.length-1].output_schema = path[path.length-1].output_schema.convertTableName(table_name);
			path[0].insertPrevNode(queue);
			view_table_element.add(queue);
		}
		return view_table_element;
	}

	private ProcessResult pushdown(ArrayList<Node> node){
		DAG clone_dag = dag.clone();
		//System.out.println("\n*** Original Plan ***");
		//dag.printDAG();
		//clone_dag.printDAG();
		Node clone_target = clone_dag.index(target.id);

		String table_name = "merge_table"+PlanGenerator.table_counter++;
		SchemaInformation new_schema = null;
		Node store = null;
		ArrayList<String> output_node = new ArrayList<String>();
		for(Node sn : node){
			Node clone_sn = clone_dag.index(sn.id);

			// 道の先にある蓄積演算のIDを検索
			ArrayList<Node> stores = new ArrayList<Node>();
			searchStoreOperators(sn, stores);
			if(stores.size() <= 1 && node.size() == 1){
				System.out.println("pushdown> Only One Store Operator : "+stores.get(0).id);
				System.out.println();
				return null;
			}

			// IDから対象となっているDAGのNodeオブジェクトを取得
			Node[] store_nodes = new Node[stores.size()];
			for(int i=0;i<stores.size();i++){
				store_nodes[i] = clone_dag.index(stores.get(i).id);
			}

			// View Table用にポインタを作成する
			Vector<Node> view_table_element = makeViewTableElement(clone_sn, store_nodes, table_name);
			
			// 出力用に保持
			for(Node store_node : store_nodes) output_node.add(store_node.id);
			
			// 蓄積演算の前後のNodeオブジェクトを取得
			Node[][] stores_next = new Node[store_nodes.length][];
			Node[][] stores_prev = new Node[store_nodes.length][];
			for(int i=0;i<store_nodes.length;i++){
				stores_next[i] = store_nodes[i].getNextNodes();
				stores_prev[i] = store_nodes[i].getPrevNodes();
			}

			// それぞれの蓄積演算を取り除く
			for(int i=0;i<stores_prev.length;i++){
				stores_prev[i][0].removeNextNode(store_nodes[i]);
			}

			//clone_dag.printDAG();

			// 新しい蓄積演算の作成
			if(store == null){
			   	store = new Node();
				store.id = store_nodes[0].id;
				store.type = "store";
				store.value = table_name;
				store.output_schema = clone_target.output_schema;
				new_schema = clone_target.output_schema.convertTableName(table_name);
			}
			if(!clone_sn.equals(store_nodes[0])){ 
				clone_sn.insertPrevNode(store);
			}else{
				for(Node[] store_next_element : stores_next){
					for(Node nn : store_next_element){
						nn.insertPrevNode(store);
					}
				}
			}

			// View Tableの作成
			for(Node each_view : view_table_element){
				each_view.value = table_name;
				each_view.output_schema = new_schema;
				clone_dag.view_table.addView(each_view.type, table_name, each_view);
				each_view.type = "source";
			}
		}

		/*
		System.out.print("Merge target store operator : [");
		for(String str : output_node){
			System.out.print(" "+str);
		}
		System.out.println(" ] => [ "+store.id+" ]");
		*/

		ProcessResult pr = new ProcessResult();
		pr.dag = clone_dag;

		/*
		System.out.println("\n*** Optimized Plan ***");
		clone_dag.printDAG();
		System.out.println(store);
		System.out.println();
		*/

		return pr;
	}

	private void searchStoreOperators(Node n, ArrayList<Node> stores){
		if(n.type.equals("store")){
		   	if(!stores.contains(n))stores.add(n);
			return;
		}
		if(n.type.equals("root")) return;

		for(Node nn : n.getNextNodes()){
			searchStoreOperators(nn, stores);
		}
		return;
	}

	private TreeSet<ProcessResult> merge(ArrayList<Node> node){
		/*
		System.out.print("Merge target : [");
		for(Node n : node){
			System.out.print(" " + n.id);
		}
		System.out.println(" ]");
		*/

		TreeSet<ProcessResult> ts = new TreeSet<ProcessResult>();
		
		// プッシュダウンしてみてコスト計算
		ProcessResult result1 = pushdown(node);
		calculateCost(result1);
		if(result1 != null) ts.add(result1);

		count++;

		return ts;
	}

	private void calculateCost(ProcessResult result){
		if(result == null) return;

		Validator validator = new Validator(conf);
		result.isFeasible = validator.validate(result.dag);
		result.cost_after_reading = result.dag.read_cost;
		result.inserting_cost = result.dag.inserting_cost;
		result.processing_cost = result.dag.processing_cost;
		
		return;
	}

	private TreeSet<ProcessResult> developDAG(ArrayList<ArrayList<Node>> node_set){
		TreeSet<ProcessResult> result = new TreeSet<ProcessResult>();

		// 各マージにおける最適な処理プランを生成
		// integration phase
		//for(Vector<Node> node : node_set){
		//	TreeSet<ProcessResult> presult = integrate(node);
		//	if(presult != null && presult.size() != 0){
		//	   	result.addAll(presult);
		//		if(presult.first().isFeasible) return result;
		//	}
		//}

		// merge phase
		for(ArrayList<Node> node : node_set){
			TreeSet<ProcessResult> presult = merge(node);
			if(presult != null && presult.size() != 0){ 
				result.addAll(presult); 
				if(presult.first().isFeasible) return result;
			}
		}

		return result;
	}

	private ArrayList<ArrayList<Node>> createCombinationAppendType(ArrayList<Node> candidate){
		ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();
		
		candidate = sortCandidateNodes(candidate);
		
		for(int i = 1; i < candidate.size(); i++){
			ArrayList<Node> r = new ArrayList<Node>();
			for(int j=0; j <= i; j++){
				r.add(candidate.get(j));
			}
			result.add(r);
		}

		return result;
	}

	private ArrayList<Node> sortCandidateNodes(ArrayList<Node> candidate){
		TreeMap<Integer, ArrayList<Node>> no_relation_map = new TreeMap<Integer, ArrayList<Node>>();
		TreeMap<Integer, ArrayList<Node>> relation_map = new TreeMap<Integer, ArrayList<Node>>();

		ArrayList<Node> sources = dag.searchNodes("source");
		ArrayList<String> view_sources = new ArrayList<String>();
		ArrayList<String> str_sources = new ArrayList<String>();
		for(Node s : sources){
			str_sources.add(s.value);
		}

		ArrayList<String> tmp_sources = new ArrayList<String>(str_sources);
		while(true){
			for(String s : tmp_sources){
				if(dag.view_table.source.containsKey(s))
					view_sources.add(dag.view_table.source.get(s));
			}
			if(view_sources.size() == 0) break;
			str_sources.addAll(view_sources);
			tmp_sources = view_sources;
			view_sources = new ArrayList<String>();
		}

		//for(String s : str_sources) System.out.print(" "+s);
		//System.out.println();

		ArrayList<Node> tmp = null;
		for(Node n : candidate){
			int size = n.size();
			if(isContainsRelation(n,str_sources)){
				//System.out.println("relation:"+size + " "+ n.id);
				if(relation_map.containsKey(size)){
					tmp = relation_map.get(size);
					tmp.add(n);
				}else{
					tmp = new ArrayList<Node>();
					tmp.add(n);
					relation_map.put(size, tmp);
				}
			}else{
				//System.out.println("no relation:"+size + " "+ n.id);
				if(no_relation_map.containsKey(size)){
					tmp = no_relation_map.get(size);
					tmp.add(n);
				}else{
					tmp = new ArrayList<Node>();
					tmp.add(n);
					no_relation_map.put(size, tmp);
				}
			}
		}

		ArrayList<Node> sorted_nodes = new ArrayList<Node>();
		for(int index : no_relation_map.keySet()){
			sorted_nodes.addAll(no_relation_map.get(index));
		}
		for(int index : relation_map.keySet()){
			sorted_nodes.addAll(relation_map.get(index));
		}

		/*
		for(Node node : sorted_nodes){
			System.out.println(" "+node.id);
		}
		System.out.println();
		*/

		return sorted_nodes;
		//return candidate;
	}

	private boolean isContainsRelation(Node n, ArrayList<String> s){
		ArrayList<Node> stores = new ArrayList<Node>();
		searchStoreOperators(n, stores);
		
		for(Node node : stores){
			//System.out.println(node.value);
			if(s.contains(node.value)) return true;
		}

		return false;
	}

	private ArrayList<ArrayList<Node>> createCombination(ArrayList<Node> candidate){
		ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();

		if(candidate.size() == 2){
			for(Node n : candidate){
				ArrayList<Node> candidate_element = new ArrayList<Node>();
				candidate_element.add(n);
				result.add(candidate_element);
			}
			result.add(candidate);

			return result;
		}

		Node tn = candidate.remove(0);
		ArrayList<ArrayList<Node>> pr = createCombination(candidate);
		for(ArrayList<Node> rn : pr){
			ArrayList<Node> pr_element_and_tn = new ArrayList<Node>(rn);
			pr_element_and_tn.add(tn);
			result.add(pr_element_and_tn);
		}
		ArrayList<Node> vtn = new ArrayList<Node>();
		vtn.add(tn);
		pr.add(vtn);

		result.addAll(pr);

		ArrayList<ArrayList<Node>> st = new ArrayList<ArrayList<Node>>();
		for(ArrayList<Node> v : result){
			boolean isinserted = false;
			for(int i=0;i<st.size();i++){
				System.out.println(v.size()+" "+st.get(i).size());
				if(v.size() <= st.get(i).size()){
					st.add(i,v);
					isinserted = true;
					break;
				}
			}
			if(!isinserted) st.add(v);
		}

		result = st;

		return result;
	}

	private boolean containsJoin(Node n){
		if(n.type.equals("join")) return true;

		for(Node nn : n.getNextNodes()){
			if(containsJoin(nn)) return true;
		}

		return false;
	}

	private boolean containsStore(Node n){
		if(n.type.equals("store")) return true;

		for(Node nn : n.getNextNodes()){
			if(containsStore(nn)) return true;
		}

		return false;
	}
}
