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
import java.io.*;

import org.streamspinner.harmonica.*;

public class PlanGenerator {
	public ConfigureReader conf = null;
	public PlanGenerator(){
		try{
			conf = new ConfigureReader();
			DBFunction dbf = new DBFunction();
			System.out.println("Please Wait");
			dbf.updateInsertionRate(conf,3,10,60000);
			System.out.println("End Wait");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static int table_counter = 1;
	public ProcessResult original_plan = null;

	public ArrayList<TreeSet<ProcessResult>> generatePlanFromString(String dag) throws Exception{
		DAG._id = 1;
		DAGBuilder builder = new DAGBuilder(dag, conf);

		return generatePlan(builder);
	}

	public ArrayList<TreeSet<ProcessResult>> generatePlanFromFile(String fname) throws Exception{
		StringBuilder buf = new StringBuilder();

		try{
			FileReader file = new FileReader(fname);
			BufferedReader buffer = new BufferedReader(file);

			String line;
			while((line = buffer.readLine()) != null){
				buf.append(line);
			}

			buffer.close();
			file.close();
		}catch(IOException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return null;
		}

	   	String dag = buf.toString();
		/**/ 
		// èëÇ´çûÇ›ÉåÅ[ÉgÇÃåvéZ
		//DBFunction db = new DBFunction();
		//db.updateInsertionRate(conf, point, attr_size, running_time(ms))
		//db.updateInsertionRate(conf, 3, 50, 30000);
		/**/

		DAG._id = 1;
		DAGBuilder builder = new DAGBuilder(dag, conf);

		return generatePlan(builder);
	}

	private ArrayList<TreeSet<ProcessResult>> generatePlan(DAGBuilder builder) throws Exception{
		ArrayList<TreeSet<ProcessResult>> output = new ArrayList<TreeSet<ProcessResult>>();
		original_plan = null;
		DAG dag = builder.dag;
		Validator validator = new Validator(conf);
		ProcessResult result = new ProcessResult();

		result.isFeasible = validator.validate(dag);
		result.dag = dag;
		result.cost_after_reading = result.dag.read_cost;
		result.inserting_cost = result.dag.inserting_cost;
		result.processing_cost = result.dag.processing_cost;
		original_plan = result;

		if(result.isFeasible){
			TreeSet<ProcessResult> tspr = new TreeSet<ProcessResult>();
			tspr.add(result);

			output.add(tspr);
		   	return output;
		}

		Map<Integer, ArrayList<Node>> map = dag.getBranches();

		TreeSet<Integer> ts = new TreeSet<Integer>();
		//System.out.println("\n*** Split Nodes ***");
		for(int i : map.keySet()){
			ts.add(i);
			/*
			System.out.println(i);
			for(Node n : map.get(i)){
				System.out.println("\t"+n.id);
			}
			*/
		}
		System.out.println();
		
		while(ts.size() != 0){
			int target = ts.last();
			ArrayList<Node> v = map.get(target);
			for(Node n : v){
				PlanRewriter opt = new PlanRewriter(dag, dag.index(n.id), conf);
				TreeSet<ProcessResult> results = opt.generatePlanRanking();
				//TreeSet<ProcessResult> first_plan_only = new TreeSet<ProcessResult>();
				//first_plan_only.add(results.first());
				//output.add(first_plan_only);
				output.add(results);

				// If first plan is feasible, return optimize phase.
				ProcessResult best_plan = results.first();
				if(best_plan.isFeasible){
					/*
					TreeSet<ProcessResult> tspr = new TreeSet<ProcessResult>();
					tspr.add(best_plan);

					output.add(tspr);
					//output.add(results);
					*/

		   			return output;
				}

				dag = best_plan.dag;
			}
			ts.remove(target);
		}

		return output;
	}

	private void test_insertion_rate(ConfigureReader conf){
		Random r = new Random();

		for(int i=0;i<10;i++){
			double d = r.nextDouble()*100;
			System.out.println(d+ " " + conf.insertion_rate(d));
		}
	}

	private void printConf(ConfigureReader conf){
		System.out.println("*** Initial information ***");
		System.out.println("> selection cost : " + conf.cost("selection"));
		System.out.println("> projection cost : " + conf.cost("projection"));
		System.out.println("> join cost : " + conf.cost("join"));
		System.out.println();
		System.out.println("> Sources and Input rates");
		for(String source : conf.input_rate.keySet()){
			System.out.println("\t"+source+"\t"+conf.input_rate(source));
		}
		System.out.println();
		System.out.println("> Initial input rates");
		for(double k : conf.insertion_rate.keySet()){
			System.out.println("\t"+k+"\t"+conf.insertion_rate(k));
		}
		System.out.println();
	}
}
