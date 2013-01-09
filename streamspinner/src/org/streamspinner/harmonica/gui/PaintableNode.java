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
 * �`�悷��P�m�[�h��\���N���X
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
	 * �������D�X�P�[�����w�肷��D
	 *
	 * @param base_size_x �m�[�h�̑傫��(X��)
	 * @param base_size_y �m�[�h�̑傫��(y��)
	 * @param vacancy �m�[�h�Ԃ̑傫��
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
	 * �`��Ɏg���Ă��鉉�Z�̃R�X�g���擾����D
	 *
	 * @return ���Z�̃R�X�g
	 */
	public OperatorCost getOperatorCost(){ return operator_cost; }

	/**
	 * �����̃m�[�h���擾����D
	 *
	 * @return �����̃m�[�h
	 */
	public PaintableNode getLeftNode(){ return left; }
	
	/**
	 * �E���̃m�[�h���擾����D
	 *
	 * @return �E���̃m�[�h
	 */
	public PaintableNode getRightNode(){ return right; }
	
	/**
	 * X�����W���擾����D
	 *
	 * @return X�����W
	 */
	public int getX(){ return x; }

	/**
	 * �`�悷��m�[�h��X���W���擾����D
	 *
	 * @return �m�[�h��X�����W
	 */
	public int getNodeX(){ return node_x; }

	public void setInsertionRate(double insertion_rate){
		this.insertion_rate = insertion_rate;
	}
	public double getInsertionRate(){
		return insertion_rate;
	}

	/**
	 * Y�����W���擾����D
	 *
	 * @return Y�����W
	 */
	public int getY(){ return y; }
	
	/**
	 * �`�悷��m�[�h��Y�����W���擾����D
	 *
	 * @return �m�[�h��Y�����W
	 */
	public int getNodeY(){ return node_y; }

	/**
	 * �������擾����D
	 *
	 * @return ����
	 */
	public int getWidth(){ return width; }

	/**
	 * �c�����擾����D
	 *
	 * @return �c��
	 */
	public int getHeight(){ return height; }

	public void setSize(int x, int y){
		width = x;
		height = y;
	}

	/**
	 * ���Z�̃R�X�g��ݒ肷��D
	 *
	 * @param operator_cost �R�X�g
	 */
	public void setOperatorCost(OperatorCost operator_cost){
		this.operator_cost = operator_cost;
	}

	/**
	 * �����̃m�[�h��ݒ肷��D
	 *
	 * @param left �����̃m�[�h
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
	 * �E���̃m�[�h��ݒ肷��D
	 *
	 * @param right �E���̃m�[�h
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
	 * �m�[�h�̓�����ݒ肷��D
	 *
	 * @param ip �m�[�h�̓���([x,y])
	 */
	public void setInputPoint(int[] ip){
		input_point = ip;
	}
	
	/**
	 * �m�[�h�̏o����ݒ肷��D
	 *
	 * @param op �m�[�h�̏o��([x,y])
	 */
	public void setOutputPoint(int[] op){
		output_point = op;
	}

	/**
	 * �ʒu��ݒ肷��D
	 */
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}

	/**
	 * �m�[�h�̈ʒu��ݒ肷��D
	 *
	 * @param node_x �m�[�h��X�����W
	 * @param node_y �m�[�h��Y�����W
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
	 * �m�[�h�̓����̍��W���擾����D
	 *
	 * @return �����̍��W([x,y])
	 */
	public int[] getInputPoint(){ return input_point; }

	/**
	 * �m�[�h�̏o���̍��W���擾����D
	 *
	 * @return �o���̍��W([x,y])
	 */
	public int[] getOutputPoint(){ return output_point; }

	/**
	 * ���̃m�[�h���������e�L�X�g���擾����D
	 *
	 * @return �m�[�h��������镶���D
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
	 * ������\���Ƃ��ĕԂ��D
	 *
	 * @return ������\��
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
