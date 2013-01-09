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
package org.streamspinner.harmonica.gui;

import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.*;

import java.text.*;

/**
 * 描画する１ノードを表すクラス
 *
 * @author snic
 * @version 1.0 (2006.8.6)
 */
public class PaintableNode {
	private int width = 0;
	private int height = 0;
	private int x = 0;
	private int y = 0;
	private int base_size_x = 0;
	private int base_size_y = 0;
	private int vacancy = 0;
	private int node_x = 0;
	private int node_y = 0;
	private int[] input_point = null;
	private int[] output_point = null;
	private OperatorCost operator_cost = null;
	private PaintableNode left = null;
	private PaintableNode right = null;
	private String text = null;
	private double insertion_rate = 0.0;

	/**
	 * 初期化．スケールを指定する．
	 *
	 * @param base_size_x ノードの大きさ(X軸)
	 * @param base_size_y ノードの大きさ(y軸)
	 * @param vacancy ノード間の大きさ
	 */
	public PaintableNode(int base_size_x, int base_size_y, int vacancy){
		this.base_size_x = base_size_x;
		this.base_size_y = base_size_y;
		this.vacancy = vacancy;
		width = base_size_x + vacancy;
		height = base_size_y + vacancy;
		input_point = new int[2];
		output_point = new int[2];
	}

	/**
	 * 描画に使われている演算のコストを取得する．
	 *
	 * @return 演算のコスト
	 */
	public OperatorCost getOperatorCost(){ return operator_cost; }

	/**
	 * 左側のノードを取得する．
	 *
	 * @return 左側のノード
	 */
	public PaintableNode getLeftNode(){ return left; }
	
	/**
	 * 右側のノードを取得する．
	 *
	 * @return 右側のノード
	 */
	public PaintableNode getRightNode(){ return right; }
	
	/**
	 * X軸座標を取得する．
	 *
	 * @return X軸座標
	 */
	public int getX(){ return x; }

	/**
	 * 描画するノードのX座標を取得する．
	 *
	 * @return ノードのX軸座標
	 */
	public int getNodeX(){ return node_x; }

	public void setInsertionRate(double insertion_rate){
		this.insertion_rate = insertion_rate;
	}
	public double getInsertionRate(){
		return insertion_rate;
	}

	/**
	 * Y軸座標を取得する．
	 *
	 * @return Y軸座標
	 */
	public int getY(){ return y; }
	
	/**
	 * 描画するノードのY軸座標を取得する．
	 *
	 * @return ノードのY軸座標
	 */
	public int getNodeY(){ return node_y; }

	/**
	 * 横幅を取得する．
	 *
	 * @return 横幅
	 */
	public int getWidth(){ return width; }

	/**
	 * 縦幅を取得する．
	 *
	 * @return 縦幅
	 */
	public int getHeight(){ return height; }

	public void setSize(int x, int y){
		width = x;
		height = y;
	}

	/**
	 * 演算のコストを設定する．
	 *
	 * @param operator_cost コスト
	 */
	public void setOperatorCost(OperatorCost operator_cost){
		this.operator_cost = operator_cost;
	}

	/**
	 * 左側のノードを設定する．
	 *
	 * @param left 左側のノード
	 */
	public void setLeftNode(PaintableNode left){
		this.left = left;

		int lw = left.getWidth();
		int lh = left.getHeight();
		
		if(right == null){
			height += lh;
			width = lw;
		}else{
			int rw = right.getWidth();
			int rh = right.getHeight();

			if(lh > rh){ height += lh - rh; }
			width = lw + rw + vacancy;
		}
	}

	/**
	 * 右側のノードを設定する．
	 *
	 * @param right 右側のノード
	 */
	public void setRightNode(PaintableNode right){
		this.right = right;

		int rw = right.getWidth();
		int rh = right.getHeight();

		if(left == null){
			height += rh + vacancy;
			width = rw;
		}else{
			int lw = left.getWidth();
			int lh = left.getHeight();

			if(rh > lh){ height += rh - lh; }
			width = rw + lw + vacancy;
		}
	}

	/**
	 * ノードの入口を設定する．
	 *
	 * @param ip ノードの入口([x,y])
	 */
	public void setInputPoint(int[] ip){
		input_point = ip;
	}
	
	/**
	 * ノードの出口を設定する．
	 *
	 * @param op ノードの出口([x,y])
	 */
	public void setOutputPoint(int[] op){
		output_point = op;
	}

	/**
	 * 位置を設定する．
	 */
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}

	/**
	 * ノードの位置を設定する．
	 *
	 * @param node_x ノードのX軸座標
	 * @param node_y ノードのY軸座標
	 */ 
	public void setNodeLocation(int node_x, int node_y){
		this.node_x = node_x;
		this.node_y = node_y;
		input_point[0] = node_x + base_size_x / 2;
		input_point[1] = node_y + base_size_y;
		output_point[0] = node_x + base_size_x / 2;
		output_point[1] = node_y;
	}

	/**
	 * ノードの入口の座標を取得する．
	 *
	 * @return 入口の座標([x,y])
	 */
	public int[] getInputPoint(){ return input_point; }

	/**
	 * ノードの出口の座標を取得する．
	 *
	 * @return 出口の座標([x,y])
	 */
	public int[] getOutputPoint(){ return output_point; }

	/**
	 * このノードを説明するテキストを取得する．
	 *
	 * @return ノードを説明する文書．
	 */
	public String getText(String sep){
		//if(text != null) return text;

		OperatorCost o = operator_cost;
		StringBuilder buf = new StringBuilder();

		buf.append("id="+o.getID()+sep);

		switch(o.getNodeType()){
			case SOURCE:
			case RDB:
			case HARMONICA:
				buf.append("type="+o.getNodeType()+sep);
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("name="+o.getText());
				text = buf.toString();
				return text;
			default:
		}

		buf.append("type="+o.getType()+sep);

		DecimalFormat df = new DecimalFormat("###.#########");

		switch(o.getType()){
			case EVAL:
				buf.append("function="+o.getText()+sep);
				buf.append("selectivity="+o.getSelectivity()+sep);
				buf.append("condition="+o.getCondition()+sep);
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("intensity="+o.getOutputIntensity()+sep);
				buf.append("cost="+df.format(o.getCost())+sep);
				buf.append("processing time="+
						df.format(o.getProcessingTime())+sep);
				break;
			case SELECTION:
			case JOIN:
				buf.append("selectivity="+o.getSelectivity()+sep);
				buf.append("predicate="+o.getCondition()+sep);
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("intensity="+o.getOutputIntensity()+sep);
				buf.append("cost="+df.format(o.getCost())+sep);
				buf.append("processing time="+
						df.format(o.getProcessingTime())+sep);
				break;
			case PROJECTION:
			case GROUP:
				buf.append("attribute="+o.getCondition()+sep);
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("intensity="+o.getOutputIntensity()+sep);
				buf.append("cost="+df.format(o.getCost())+sep);
				buf.append("processing time="+
						df.format(o.getProcessingTime())+sep);
				break;
			case CARTESIAN_PRODUCT:
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("intensity="+o.getOutputIntensity()+sep);
				buf.append("cost="+df.format(o.getCost())+sep);
				buf.append("processing time="+
						df.format(o.getProcessingTime())+sep);
				break;
			case RENAME:
				buf.append("attribute="+o.getText()+sep);
				break;
			case INSERTION:
				buf.append("table="+o.getText()+sep);
				buf.append("insertion rate="+insertion_rate+sep);
				break;
			case ROOT:
				buf.append("ROOT");
				break;
			case UNION:
				buf.append("base_refid="+o.getText()+sep);
				buf.append("rate="+o.getOutputRate()+sep);
				buf.append("window="+o.getOutputWindow()+sep);
				buf.append("intensity="+o.getOutputIntensity()+sep);
				buf.append("cost="+df.format(o.getCost())+sep);
				buf.append("processing time="+
						df.format(o.getProcessingTime())+sep);
				break;
			default:
				debug("unsupported operator.");
		}
		text = buf.toString();
		
		return text;
	}

	/**
	 * 文字列表現として返す．
	 *
	 * @return 文字列表現
	 */
	public String toString(){
		String str = operator_cost.getID();
		if(left != null){
		   str = "\n\t" + str + "\n" + left.toString();
		}
		if(right != null){
			str += "\t\t" + right.toString();
		}
		return str;
	}

	private void debug(Object o){
		HarmonicaManager.debug("PaintableNode",o);
	}
}
