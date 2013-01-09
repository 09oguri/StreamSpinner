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

public class ProcessResult implements Comparable<ProcessResult> {
	public DAG dag = null;
	public boolean isFeasible = false;
	public double processing_cost = 0;
	public double inserting_cost = 0;
	public double cost_after_reading = 0;

	public int compareTo(ProcessResult pr){
		if(dag.equals(pr.dag)) return 0;
		if(isFeasible && !pr.isFeasible) return -1;
		if(!isFeasible && pr.isFeasible) return 1;

		int res = 0;
		if(isFeasible){
			res = (int)(cost_after_reading*10000 - pr.cost_after_reading*10000);
			if(res != 0) return res;
			res = (int)(pr.inserting_cost*10000 - inserting_cost*10000);
		}else{
			res = (int)(inserting_cost*10000 - pr.inserting_cost*10000);
		}

		if(res != 0) return res;

		res = (int)(processing_cost*10000 - pr.processing_cost*10000);
		if(res != 0) return res;

		return dag.id - pr.dag.id;
	}

	public String toString(){
		StringBuilder buf = new StringBuilder();
		buf.append("Plan> ");
		buf.append("DAG_ID="+dag.id);
		buf.append(",isFeasible="+isFeasible);
		buf.append(",processing_cost="+processing_cost);
		buf.append(",inserting_cost="+inserting_cost);
		buf.append(",cost_after_reading="+cost_after_reading);
		buf.append(",store_op=[");
		if(dag != null)
		for(Node n : dag.searchNodes("store")) buf.append(" "+n.id);
		buf.append(" ]");

		return buf.toString();
	}
}
