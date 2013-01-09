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
 * �e���Z�̃R�X�g��ێ�����N���X
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
	 * 1���͉��Z�p�̏�����
	 *
	 * @param type ���Z�̃^�C�v
	 * @param id �m�[�hID
	 * @param input_rate ���Z�̓��̓��[�g
	 * @param input_window ���Z�̃E�C���h�E
	 * @param input_intensity ���Z�ɓ��͂����^�v���̏W��x
	 * @param selectivity ���Z�̑I��
	 * @param cost ���Z�̏����R�X�g
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
	 * 2���͉��Z�p�̏�����
	 *
	 * @param type ���Z�̃^�C�v
	 * @param id �m�[�hID
	 * @param left_input_rate ���Z�̍����̓��̓��[�g
	 * @param left_input_window ���Z�̍����̃E�C���h�E
	 * @param left_input_intensity ���Z�ɍ���������͂����^�v���̏W��x
	 * @param right_input_rate ���Z�̉E���̓��̓��[�g
	 * @param right_input_window ���Z�̉E���̃E�C���h�E
	 * @param right_input_intensity ���Z�ɉE��������͂����^�v���̏W��x
	 * @param selectivity ���Z�̑I��
	 * @param cost ���Z�̏����R�X�g
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
	 * ��񌹃^�C�v�̏ꍇ�̏�����
	 *
	 * @param id �m�[�hID
	 * @param arrival_rate �������[�g
	 * @param window �E�C���h�E
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
	 * �����̕�����ݒ肷��D
	 *
	 * @param condition ����
	 */
	public void setCondition(String condition){
		this.condition = condition;
	}
	/**
	 * �������擾����D
	 *
	 * @return ����
	 */
	public String getCondition(){ return this.condition; }

	/**
	 * �m�[�h�ɕK�v�ȏ���I�m�ɐݒ肷��D
	 *
	 * @param text ���R�ȃe�L�X�g
	 */
	public void setText(String text){ this.text = text; }

	/**
	 * �e�L�X�g���擾����D
	 *
	 * @return �e�L�X�g
	 */
	public String getText(){ return this.text; }

	/**
	 * �m�[�h�̃^�C�v���擾����D
	 *
	 * @return �m�[�h�̃^�C�v
	 */
	public NodeType getNodeType(){ return node_type; }
	/**
	 * �m�[�h�̃^�C�v��ݒ肷��D<BR>
	 * OperatorCost�N���X�ł́C���������Ɏ����I�ɏ�񌹂����Z���𔻒f
	 * ���Ă���D<BR>
	 * ���̃��\�b�h�ł́C��񌹂̂Ƃ��ɂ���ɂ��ߍׂ����ݒ肪�K�v�ȏꍇ��
	 * ���p����D
	 *
	 * @param node_type �m�[�h�^�C�v(���ɁCstream��rdb��harmonica��)
	 */
	public void setNodeType(NodeType node_type){
		this.node_type = node_type;
	}

	/**
	 * ���Z�̃^�C�v���擾����D
	 *
	 * @return ���Z�̃^�C�v
	 */
	public OperatorType getType(){ return type; }

	/**
	 * �m�[�hID���擾����D
	 *
	 * @return �m�[�hID
	 */
	public String getID(){ return id; }

	/**
	 * ���Z�̓��̓��[�g���擾����D
	 *
	 * @return ���Z�̓��̓��[�g�̔z��([�����C�E��] or [1����])
	 */
	public double[] getInputRate(){
		if(input_rate2 < 0) return new double[]{input_rate};
		return new double[]{input_rate,input_rate2};
	}
	/**
	 * ���Z�̃E�C���h�E���擾����D
	 *
	 * @return ���Z�̃E�C���h�E�̔z��([�����C�E��] or [1����])
	 */
	public double[] getInputWindow(){
		if(input_window2 < 0) return new double[]{input_window};
		return new double[]{input_window,input_window2};
	}
	/**
	 * ���Z�̓��̓^�v���̏W��x���擾����D
	 *
	 * @return ���Z�̓��̓^�v���̏W��x�̔z��([�����C�E��] or [1����])
	 */
	public double[] getInputIntensity(){
		if(input_intensity2 < 0) return new double[]{input_intensity};
		return new double[]{input_intensity,input_intensity2};
	}
	/**
	 * ���Z�̑I�𗦂��擾����D
	 *
	 * @return ���Z�̑I��
	 */
	public double getSelectivity(){
		return selectivity;
	}
	/**
	 * ���Z�̏����R�X�g���擾����D
	 *
	 * @return ���Z�̏����R�X�g
	 */
	public double getCost(){
		return cost;
	}

	/**
	 * �o�̓��[�g���擾����D
	 *
	 * @return �o�̓��[�g
	 */
	public double getOutputRate(){
		if(output_rate >= 0) return output_rate;
		calcurate();
		return output_rate;
	}
	/**
	 * �o�̓E�C���h�E���擾����D
	 *
	 * @return �o�̓E�C���h�E
	 */
	public double getOutputWindow(){
		if(output_window >= 0) return output_window;
		calcurate();
		return output_window;
	}
	/**
	 * �o�̓^�v���̏W��x���擾����D
	 *
	 * @return �o�̓^�v���̏W��x
	 */
	public double getOutputIntensity(){
		if(output_intensity >= 0) return output_intensity;
		calcurate();
		return output_intensity;
	}
	/**
	 * �P�ʎ��ԓ�����̏������Ԃ��擾����D
	 *
	 * @return ���Z�̒P�ʎ��ԓ�����̏�������
	 */
	public double getProcessingTime(){
		if(processing_time >= 0) return processing_time;
		calcurate();
		return processing_time;
	}
	/**
	 * �����R�X�g�����ꂼ��v�Z����D
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
	 * ������\�����擾����D
	 *
	 * @return ������\��
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
	 * DEBUG�p
	 */
	private void debug(Object o){
		HarmonicaManager.debug("OperatorCost",o);
	}
}
