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
 * �⍇���̔��茋�ʂ��i�[���邽�߂̃N���X�D
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
	 * �⍇���؂�ݒ肷��D
	 * @param tree �⍇����
	 */
	public void setQueryTree(HamQLQueryTree tree){this.tree = tree;}

	/**
	 * �⍇���؂��擾����D
	 * @return �⍇����
	 */
	public HamQLQueryTree getQueryTree(){return tree;}

	/**
	 * �����\������̔��茋�ʂ��擾����D
	 *
	 * @return �����\������̔��茋��
	 */
	public boolean isInsertable(){
		if(output_rate <= insertion_rate) return true;

		return false;
	}

	/**
	 * �����\������̔��茋�ʂ��擾����D
	 *
	 * @return �����\������̔��茋��
	 */
	public boolean isProcessable(){
		if(processing_time <= 1.0) return true;

		return false;
	}

	/**
	 * �⍇���\�������ݒ肷��D
	 *
	 * @param feasible �⍇���\������
	 */
	public void setFeasible(boolean feasible){
		this.is_queriable = feasible;
	}

	/**
	 * ���̗v���������\���̔��茋�ʂ��擾����D
	 *
	 * @return �⍇���\������̔��茋��
	 */
	public boolean isFeasible(){
		if(is_queriable) return true;
		return (isInsertable() && isProcessable());
	}

	/**
	 * �⍇���؂̃R�X�g��ݒ肷��D
	 * @param costs ����ɗp�����⍇����ID�ƃR�X�g��Map
	 */
	public void setCost(Map<String, OperatorCost> costs){
		this.costs = costs;
	}

	/**
	 * �⍇���؂̃R�X�g���擾����D
	 * @return �⍇��ID�Ɩ⍇���؂̃R�X�g��Map
	 */
	public Map<String, OperatorCost> getCost(){return costs;}

	/**
	 * ���݂̖⍇���Ɏ�����������̊ɂ��⍇����ݒ肷��D
	 * @param recommend_hamql �ގ��̖⍇��
	 */
	public void setRecommendQuery(HamQLQuery recommend_hamql){
		this.recommend_hamql = recommend_hamql;
	}

	/**
	 * ���݂̖⍇���Ɏ�����������̊ɂ��⍇�����擾����D
	 * @return ����̊ɂ��⍇��
	 */
	public HamQLQuery getRecommendQuery(){return recommend_hamql;}

	/**
	 * �R�����g���擾����D
	 *
	 * @return comment
	 */
	public String getComment(){
		StringBuilder buf = new StringBuilder();
		if(!isProcessable()){
			double d = processing_time;
			int parcent = (int)((100 * (d-1)) / d);
		   	buf.append("���̓f�[�^�ɑ΂��� ��");
		   	buf.append(parcent); 
			buf.append("% �̃T���v�����O���K�v�ł��D");
		}

		return buf.toString();
	}
	/**
	 * ���̖⍇���̌��ς菈�����Ԃ��擾����D
	 *
	 * @return ���ς菈������
	 */
	public double getProcessingTime(){
		return this.processing_time;
	}
	/**
	 * ���̖⍇���̌��ς菈�����Ԃ�ݒ肷��D
	 *
	 * @param processing_time ���ς菈������
	 */
	public void setProcessingTime(double processing_time){
		this.processing_time = processing_time;
	}

	/**
	 * ���̖⍇���̏������[�g��ݒ肷��D
	 *
	 * @param insertion_rate �������[�g
	 */
	public void setInsertionRate(double insertion_rate){
		this.insertion_rate = insertion_rate;
	}

	/**
	 * ���̖⍇���̏o�̓��[�g��ݒ肷��D
	 *
	 * @param output_rate �o�̓��[�g
	 */
	public void setOutputRate(double output_rate){
		this.output_rate = output_rate;
	}

	/**
	 * ���̖⍇���̏������[�g���擾����D
	 *
	 * @return �������[�g
	 */
	public double getInsertionRate(){
		return this.insertion_rate;
	}

	/**
	 * ���̖⍇���̏o�̓��[�g���擾����D
	 *
	 * @return �o�̓��[�g
	 */
	public double getOutputRate(){
		return this.output_rate;
	}

	/**
	 * �I�u�W�F�N�g�̕�����\�����擾����D
	 *
	 * @return �I�u�W�F�N�g�̕�����\��
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
