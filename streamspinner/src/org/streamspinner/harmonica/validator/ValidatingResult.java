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
package org.streamspinner.harmonica.validator;

import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.query.*;

import java.util.*;

/**
 * 問合せの判定結果を格納するためのクラス．
 *
 * @author snic
 * @version 1.0 (2006.8.4)
 */
public class ValidatingResult{
	private HamQLQueryTree tree = null;
	private Map<String, OperatorCost> costs = null;
	private HamQLQuery recommend_hamql = null;
	private double processing_time = 0.0;
	private double output_rate = 0.0;
	private double insertion_rate = 0.0;
	private boolean is_queriable = false;

	public ValidatingResult(){}

	/**
	 * 問合せ木を設定する．
	 * @param tree 問合せ木
	 */
	public void setQueryTree(HamQLQueryTree tree){this.tree = tree;}

	/**
	 * 問合せ木を取得する．
	 * @return 問合せ木
	 */
	public HamQLQueryTree getQueryTree(){return tree;}

	/**
	 * 書込可能性判定の判定結果を取得する．
	 *
	 * @return 書込可能性判定の判定結果
	 */
	public boolean isInsertable(){
		if(output_rate <= insertion_rate) return true;

		return false;
	}

	/**
	 * 処理可能性判定の判定結果を取得する．
	 *
	 * @return 処理可能性判定の判定結果
	 */
	public boolean isProcessable(){
		if(processing_time <= 1.0) return true;

		return false;
	}

	/**
	 * 問合せ可能性判定を設定する．
	 *
	 * @param feasible 問合せ可能性判定
	 */
	public void setFeasible(boolean feasible){
		this.is_queriable = feasible;
	}

	/**
	 * この要求が処理可能かの判定結果を取得する．
	 *
	 * @return 問合せ可能性判定の判定結果
	 */
	public boolean isFeasible(){
		if(is_queriable) return true;
		return (isInsertable() && isProcessable());
	}

	/**
	 * 問合せ木のコストを設定する．
	 * @param costs 判定に用いた問合せのIDとコストのMap
	 */
	public void setCost(Map<String, OperatorCost> costs){
		this.costs = costs;
	}

	/**
	 * 問合せ木のコストを取得する．
	 * @return 問合せIDと問合せ木のコストのMap
	 */
	public Map<String, OperatorCost> getCost(){return costs;}

	/**
	 * 現在の問合せに似た少し制約の緩い問合せを設定する．
	 * @param recommend_hamql 類似の問合せ
	 */
	public void setRecommendQuery(HamQLQuery recommend_hamql){
		this.recommend_hamql = recommend_hamql;
	}

	/**
	 * 現在の問合せに似た少し制約の緩い問合せを取得する．
	 * @return 制約の緩い問合せ
	 */
	public HamQLQuery getRecommendQuery(){return recommend_hamql;}

	/**
	 * コメントを取得する．
	 *
	 * @return comment
	 */
	public String getComment(){
		StringBuilder buf = new StringBuilder();
		if(!isProcessable()){
			double d = processing_time;
			int parcent = (int)((100 * (d-1)) / d);
		   	buf.append("入力データに対して 約");
		   	buf.append(parcent); 
			buf.append("% のサンプリングが必要です．");
		}

		return buf.toString();
	}
	/**
	 * この問合せの見積り処理時間を取得する．
	 *
	 * @return 見積り処理時間
	 */
	public double getProcessingTime(){
		return this.processing_time;
	}
	/**
	 * この問合せの見積り処理時間を設定する．
	 *
	 * @param processing_time 見積り処理時間
	 */
	public void setProcessingTime(double processing_time){
		this.processing_time = processing_time;
	}

	/**
	 * この問合せの書込レートを設定する．
	 *
	 * @param insertion_rate 書込レート
	 */
	public void setInsertionRate(double insertion_rate){
		this.insertion_rate = insertion_rate;
	}

	/**
	 * この問合せの出力レートを設定する．
	 *
	 * @param output_rate 出力レート
	 */
	public void setOutputRate(double output_rate){
		this.output_rate = output_rate;
	}

	/**
	 * この問合せの書込レートを取得する．
	 *
	 * @return 書込レート
	 */
	public double getInsertionRate(){
		return this.insertion_rate;
	}

	/**
	 * この問合せの出力レートを取得する．
	 *
	 * @return 出力レート
	 */
	public double getOutputRate(){
		return this.output_rate;
	}

	/**
	 * オブジェクトの文字列表現を取得する．
	 *
	 * @return オブジェクトの文字列表現
	 */
	public String toString(){
		String str = "";

		str += "XML Result Tree\n";
		str += tree.toString();
		if(costs != null){
			str += "COSTS\n";
			str += costs.toString()+"\n";
		}else{
			str += "No Continuous Query\n";
		}
		str += "VALIDATOR RESULT\n";
		str += isFeasible();
		str += "\nRECOMMEND\n";
		str += getComment();

		return str;
	}
}
