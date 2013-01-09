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
package org.streamspinner.harmonica.query;

import java.io.*;

import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.query.hamql.*;

/**
 * Harmonica�ň����e��񌹂��Ǘ�����N���X�D
 *
 * <PRE>
 * 1.0.1 2006.8.3 �萔��enum���g���Ē�`����悤�ɂ����D
 * </PRE>
 * @author snic
 * @version 1.0.1 (2006.8.3)
 */
public class HarmonicaSource implements Serializable {
	private String source = null;
	private String rename = null;
	private long window = -1;
	private long original_time = 0;
	private boolean sliding = false;
	private HamQLQuery sub_query = null;
	private SourceType type = SourceType.SOURCE;

	/**
	 * �������D���ɉ�������ĂȂ��D
	 */
	public HarmonicaSource(){}

	/**
	 * ���⍇���ŏ������D<br>
	 * #HarmonicaSource.getType() �� SourceType.SUB_QUERY �Ɏ����ݒ�D
	 *
	 * @param sub_query HamQL�⍇���L�q�ɂ�镛�⍇���D
	 */
	public HarmonicaSource(HamQLQuery sub_query){
		setQuery(sub_query);
	}
	/**
	 * ��񌹂̏��ŏ������D<br>
	 * ���Ȃ݂ɁC�N�_(original_time)�̈������ɒ��ӂ��K�v�D<br>
	 * original_time�̋L�q�̎d���ɂ���āCStreamArchiver�ł͈ȉ��̂悤�Ɉ����D
	 * <pre>
	 *	original_time = 0 �� now�����̓s�x�Z�b�g�����D
	 *	original_time &lt; 0 �� now+original_time�ɃZ�b�g�����D
	 *	original_time &gt; 0 �� original_time�����̂܂܃Z�b�g�����D
	 * </pre>
	 *
	 * @param source ��񌹂̖��O
	 * @param window ���ԃE�C���h�E�̕�
	 * @param original_time �N�_�ƂȂ鎞��
	 */
	public HarmonicaSource(String source, long window, long original_time){
		setSource(source);
		setWindowSize(window);
		setOriginalTime(original_time);
	}

	/**
	 * ��񌹂����⍇�����𔻒肷�邽�߂̕ϐ����Z�b�g����D<br>
	 * SourceType.SOURCE �� ���<br>
	 * SourceType.SUB_QUERY �� ���⍇��<br>
	 *
	 * @param type �^�C�v
	 */
	public void setType(SourceType type){
		this.type = type;
	}
	/**
	 * �^�C�v���擾����D
	 *
	 * @return �^�C�v
	 */
	public SourceType getType(){
		return this.type;
	}
	/**
	 * AS�ɂ�郊�l�[����ݒ肷��D
	 *
	 * @param rename ���l�[��
	 */
	public void setRename(String rename){
		this.rename = rename;
	}
	/**
	 * AS�ɂ���Ďw�肳�ꂽ�ʖ����擾����D
	 *
	 * @return �ʖ�(�ݒ肳��Ă��Ȃ��Ƃ���null)
	 */
	public String getRename(){
		return rename;
	}
	/**
	 * ��񌹂��Z�b�g����D<br>
	 * �����I��TYPE_SOURCE���ݒ肳���D
	 *
	 * @param source ��񌹂̖��O
	 */
	public void setSource(String source){
		this.source = source;
		this.type = SourceType.SOURCE;
	}
	/**
	 * ���ԃE�C���h�E�̑傫�����Z�b�g����D
	 *
	 * @param window ���ԃE�C���h�E�̑傫��
	 */
	public void setWindowSize(long window){
		this.window = window;
	}
	/**
	 * �N�_�̎������Z�b�g����D
	 *
	 * @param original_time �N�_�̎���
	 */
	public void setOriginalTime(long original_time){
		this.original_time = original_time;
	}
	/**
	 * ���⍇�����Z�b�g����D<br>
	 * �����I��TYPE_SUB_QUERY���ݒ肳���D
	 *
	 * @param sub_query ���⍇��
	 */
	public void setQuery(HamQLQuery sub_query){
		this.sub_query = sub_query;
		this.type = SourceType.SUB_QUERY;
	}
	/**
	 * 1�x�̏����ŃE�C���h�E��S�^�v���X���C�f�B���O�����邩�ǂ������w�肷��D
	 *
	 * @param sliding �X���C�f�B���O���������Ƃ���true
	 */
	public void setSliding(boolean sliding){ this.sliding = sliding; }
	/**
	 * �X���C�f�B���O�̐ݒ���擾����D
	 *
	 * @return �X���C�f�B���O����Ƃ���true
	 */
	public boolean getSliding(){ return this.sliding; }
	/**
	 * ��񌹂̖��O���擾����D
	 *
	 * @return ��񌹂̖��O;
	 */
	public String getSource(){ return source; }
	/**
	 * ���ԃE�C���h�E�̑傫�����擾����D
	 *
	 * @return ���ԃE�C���h�E�̑傫��
	 */
	public long getWindowSize(){ return window; }
	/**
	 * �N�_�ƂȂ鎞�����擾����D
	 *
	 * @return �N�_�ƂȂ鎞��
	 */
	public long getOriginalTime(){ return original_time; }
	/**
	 * ���⍇�����擾����D
	 *
	 * @return ���⍇���I�u�W�F�N�g
	 */
	public HamQLQuery getQuery(){ return sub_query; }
	/**
	 * ��񌹂𕶎���ɕϊ�����D
	 *
	 * @return ��񌹂�HamQL�I�\��
	 */
	public String toString(){
		String str = "";
		if(type == SourceType.SOURCE){
			str = source+"["+window;
			if(!sliding){
			   	str += ",";
				if(original_time == 0) str += "now";
				else if(original_time < 0) str += "now" + original_time;
				else str += original_time;
			}
			str += "]";
		}else if(type == SourceType.SUB_QUERY){
			str = "(" + sub_query + ")";
		}
		if(rename != null) str += " AS " + rename;
		return str;
	}
}
