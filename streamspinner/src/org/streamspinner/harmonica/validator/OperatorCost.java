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

import org.streamspinner.harmonica.*;

/**
 * 各演算のコストを保持するクラス
 *
 * @author snic
 * @version 1.0
 */
public class OperatorCost {
	private NodeType node_type;
	private OperatorType type;
	private double input_rate, input_window, input_intensity;
	private double input_rate2 = -1.0;
   	private double input_window2 = -1.0;
	private double input_intensity2 = -1.0;
	private double output_rate = -1.0;
	private double output_window = -1.0;
   	private double output_intensity = -1.0;
	private double selectivity;
	private double cost;
	private String id;
	private double processing_time = 0.0;
	private int continuou = 1;
	private String text = null;
	private String condition = null;

	/**
	 * 1入力演算用の初期化
	 *
	 * @param type 演算のタイプ
	 * @param id ノードID
	 * @param input_rate 演算の入力レート
	 * @param input_window 演算のウインドウ
	 * @param input_intensity 演算に入力されるタプルの集約度
	 * @param selectivity 演算の選択率
	 * @param cost 演算の処理コスト
	 */
	public OperatorCost(
			OperatorType type,
			String id,
			double input_rate,
			double input_window,
			double input_intensity,
			double selectivity,
			double cost
	){
		this.node_type = NodeType.OPERATOR;
		this.type = type;
		this.id = id;
		this.input_rate = input_rate;
		this.input_window = input_window;
		this.input_intensity = input_intensity;
		this.selectivity = selectivity;
		this.cost = cost;
	}
	/**
	 * 2入力演算用の初期化
	 *
	 * @param type 演算のタイプ
	 * @param id ノードID
	 * @param left_input_rate 演算の左側の入力レート
	 * @param left_input_window 演算の左側のウインドウ
	 * @param left_input_intensity 演算に左側から入力されるタプルの集約度
	 * @param right_input_rate 演算の右側の入力レート
	 * @param right_input_window 演算の右側のウインドウ
	 * @param right_input_intensity 演算に右側から入力されるタプルの集約度
	 * @param selectivity 演算の選択率
	 * @param cost 演算の処理コスト
	 */
	public OperatorCost(
			OperatorType type,
			String id,
			double left_input_rate,
			double left_input_window,
			double left_input_intensity,
			double right_input_rate,
			double right_input_window,
			double right_input_intensity,
			double selectivity,
			double cost
	){
		this(type,
			id,
			left_input_rate,
			left_input_window,
			left_input_intensity,
			selectivity,
			cost
		);

		this.input_rate2 = right_input_rate;
		this.input_window2 = right_input_window;
		this.input_intensity2 = right_input_intensity;
	}

	/**
	 * 情報源タイプの場合の初期化
	 *
	 * @param id ノードID
	 * @param arrival_rate 到着レート
	 * @param window ウインドウ
	 */
	public OperatorCost(String id, double arrival_rate, double window){
		this.node_type = NodeType.SOURCE;
		this.type = OperatorType.UNKNOWN;
		this.id = id;
		this.output_rate = arrival_rate;
		this.output_window = window;
		this.output_intensity = 1.0;
		this.input_rate = output_rate;
		this.input_window = output_window;
		this.input_intensity = output_intensity;
		this.cost = 0.0;
		this.processing_time = 0.0;
	}

	/**
	 * 条件の文書を設定する．
	 *
	 * @param condition 条件
	 */
	public void setCondition(String condition){
		this.condition = condition;
	}
	/**
	 * 条件を取得する．
	 *
	 * @return 条件
	 */
	public String getCondition(){ return this.condition; }

	/**
	 * ノードに必要な情報を的確に設定する．
	 *
	 * @param text 自由なテキスト
	 */
	public void setText(String text){ this.text = text; }

	/**
	 * テキストを取得する．
	 *
	 * @return テキスト
	 */
	public String getText(){ return this.text; }

	/**
	 * ノードのタイプを取得する．
	 *
	 * @return ノードのタイプ
	 */
	public NodeType getNodeType(){ return node_type; }
	/**
	 * ノードのタイプを設定する．<BR>
	 * OperatorCostクラスでは，初期化時に自動的に情報源か演算かを判断
	 * している．<BR>
	 * このメソッドでは，情報源のときにさらにきめ細かい設定が必要な場合に
	 * 利用する．
	 *
	 * @param node_type ノードタイプ(特に，streamかrdbかharmonicaか)
	 */
	public void setNodeType(NodeType node_type){
		this.node_type = node_type;
	}

	/**
	 * 演算のタイプを取得する．
	 *
	 * @return 演算のタイプ
	 */
	public OperatorType getType(){ return type; }

	/**
	 * ノードIDを取得する．
	 *
	 * @return ノードID
	 */
	public String getID(){ return id; }

	/**
	 * 演算の入力レートを取得する．
	 *
	 * @return 演算の入力レートの配列([左側，右側] or [1入力])
	 */
	public double[] getInputRate(){
		if(input_rate2 < 0) return new double[]{input_rate};
		return new double[]{input_rate,input_rate2};
	}
	/**
	 * 演算のウインドウを取得する．
	 *
	 * @return 演算のウインドウの配列([左側，右側] or [1入力])
	 */
	public double[] getInputWindow(){
		if(input_window2 < 0) return new double[]{input_window};
		return new double[]{input_window,input_window2};
	}
	/**
	 * 演算の入力タプルの集約度を取得する．
	 *
	 * @return 演算の入力タプルの集約度の配列([左側，右側] or [1入力])
	 */
	public double[] getInputIntensity(){
		if(input_intensity2 < 0) return new double[]{input_intensity};
		return new double[]{input_intensity,input_intensity2};
	}
	/**
	 * 演算の選択率を取得する．
	 *
	 * @return 演算の選択率
	 */
	public double getSelectivity(){
		return selectivity;
	}
	/**
	 * 演算の処理コストを取得する．
	 *
	 * @return 演算の処理コスト
	 */
	public double getCost(){
		return cost;
	}

	/**
	 * 出力レートを取得する．
	 *
	 * @return 出力レート
	 */
	public double getOutputRate(){
		if(output_rate >= 0) return output_rate;
		calcurate();
		return output_rate;
	}
	/**
	 * 出力ウインドウを取得する．
	 *
	 * @return 出力ウインドウ
	 */
	public double getOutputWindow(){
		if(output_window >= 0) return output_window;
		calcurate();
		return output_window;
	}
	/**
	 * 出力タプルの集約度を取得する．
	 *
	 * @return 出力タプルの集約度
	 */
	public double getOutputIntensity(){
		if(output_intensity >= 0) return output_intensity;
		calcurate();
		return output_intensity;
	}
	/**
	 * 単位時間当たりの処理時間を取得する．
	 *
	 * @return 演算の単位時間当たりの処理時間
	 */
	public double getProcessingTime(){
		if(processing_time >= 0) return processing_time;
		calcurate();
		return processing_time;
	}
	/**
	 * 処理コストをそれぞれ計算する．
	 */
	private void calcurate(){
		if(node_type == NodeType.SOURCE) return;
		if(type == OperatorType.PROJECTION || 
		   type == OperatorType.CARTESIAN_PRODUCT){
			selectivity = 1.0;
		}
		switch(type){
			case SELECTION:
				output_rate = selectivity * input_rate;
				output_window = selectivity * input_window;
				output_intensity = input_intensity;
				processing_time = cost * input_rate;
				return;
			case PROJECTION:
				output_rate = selectivity * input_rate;
				output_window = selectivity * input_window;
				output_intensity = 1.0;
				processing_time = cost * input_rate;
				return;
			case JOIN:
			case CARTESIAN_PRODUCT:
				double left = selectivity * input_rate * input_window2;
				double right = selectivity * input_rate2 * input_window;
				output_rate = left + right;
				output_window = selectivity * input_window * input_window2;
				output_intensity = input_intensity;
				processing_time = cost * (input_rate + input_rate2);
				return;
			case GROUP:
				double g_num = 10000 / (selectivity * 10000);
				output_rate = input_rate * selectivity;
				output_window = g_num;
				output_intensity = input_window * selectivity;
				debug("intensity : "+ output_intensity);
				processing_time = cost * input_rate;
				return;
			case UNION:
				output_rate = input_rate + input_rate2;
				output_window = input_window + input_window2;
				output_intensity = input_intensity;
				processing_time = cost * (input_rate + input_rate2);
				return;
			case EVAL:
				output_rate = input_rate;
				output_window = input_window;
				output_intensity = input_intensity;
				processing_time = cost * input_rate;
				return;
			case RENAME:
				output_rate = input_rate;
				output_window = input_window;
				output_intensity = input_intensity;
				processing_time = 0.0;
				return;
			case INSERTION:
			case ROOT:
				return;
			default:
				debug("Unsupported Operator("+type+")");
				return;
		}
	}
	/**
	 * 文字列表現を取得する．
	 *
	 * @return 文字列表現
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder(100);

		sb.append("[\tNODE_TYPE="+node_type);
		sb.append(",\n\tTYPE="+type);
		sb.append(",\n\tID="+id);
		sb.append(",\n\tinput_rate="+input_rate);
		sb.append(",\n\tinput_window="+input_window);
		sb.append(",\n\tinput_intensity="+input_intensity);
		sb.append(",\n\tinput_rate2="+input_rate2);
		sb.append(",\n\tinput_window2="+input_window2);
		sb.append(",\n\tinput_intensity2="+input_intensity2);
		sb.append(",\n\toutput_rate="+output_rate);
		sb.append(",\n\toutput_window="+output_window);
		sb.append(",\n\toutput_intensity="+output_intensity);
		sb.append(",\n\tselectivity="+selectivity);
		sb.append(",\n\tcost="+cost);
		sb.append(",\n\tprocessing_time="+processing_time+"]\n");

		return sb.toString();
	}
	/**
	 * DEBUG用
	 */
	private void debug(Object o){
		HarmonicaManager.debug("OperatorCost",o);
	}
}
