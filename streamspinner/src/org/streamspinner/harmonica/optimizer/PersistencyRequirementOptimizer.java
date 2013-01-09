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

import org.streamspinner.harmonica.*;

public class PersistencyRequirementOptimizer{
	private PlanGenerator generator = null;
	public PersistencyRequirementOptimizer(){
		generator = new PlanGenerator();
	}

	/**
	 * XMLÇ©ÇÁç≈ìKÉvÉâÉìÇçÏê¨Ç∑ÇÈ
	 */
	public ProcessPlan optimize(String dag) throws HarmonicaException{
		ArrayList<TreeSet<ProcessResult>>  result = null;

		try{
			result = generator.generatePlanFromString(dag);
		}catch(Exception e){
			throw new HarmonicaException(e);
		}

		if(result == null) return null;
		ProcessResult pr = result.get(result.size()-1).first();

		ProcessPlan plan = new ProcessPlan(pr.dag.toXMLAppliedView(),pr.dag.view_table);

		return plan;
	}
}
