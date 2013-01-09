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

import org.streamspinner.harmonica.*;
import org.streamspinner.query.*;

import java.util.*;
import java.io.*;

/**
 * Harmonica�ň�����񌹂��Ǘ����邽�߂̃N���X�D<br>
 * �����ɂ́CHarmonicaSource�I�u�W�F�N�g���i�[���Ă���D
 *
 * <PRE>
 * �ύX�����F
 * 1.1 2006.8.28 SourceSet�p�̃R���X�g���N�^�ǉ�
 * </PRE>
 *
 * @author snic
 * @version 1.0 (2006.8.28)
 */
public class HarmonicaSourceSet implements Serializable {
	private Set<HarmonicaSource> sources;

	/**
	 * �I�u�W�F�N�g�̏�����
	 */
	public HarmonicaSourceSet(){
		init();
	}

	/**
	 * StreamSpinner��SourceSet�ŏ�����
	 * �N�_�͑S�� now �ɃZ�b�g�����D
	 */
	public HarmonicaSourceSet(SourceSet source){
		init();

		Iterator it = source.iterator();
		while(it.hasNext()){
			String source_name = (String)it.next();
			long window_size = source.getWindowsize(source_name);
			long window_origin = source.getWindowOrigin(source_name);

			HarmonicaSource harmonica_source = 
				new HarmonicaSource(source_name,window_size,window_origin);

			add(harmonica_source);
		}
	}

	private void init(){
		sources = new LinkedHashSet<HarmonicaSource>();
	}

	/**
	 * ��񌹂̒ǉ����s���D
	 *
	 * @param source HarmonicaSource�I�u�W�F�N�g
	 */
	public void add(HarmonicaSource source){
		sources.add(source);
	}

	/**
	 * ��񌹂̃Z�b�g���擾����D<br>
	 *
	 * @return HarmonicaSource�I�u�W�F�N�g�̃Z�b�g
	 */
	public Set<HarmonicaSource> getSources(){
		return sources;
	}

	/**
	 * HarmonicaSource�I�u�W�F�N�g�̓����\����Ԃ��D<br>
	 * (��) Turbine[30], Sensor[10,now], (SELECT ...),
	 *
	 * @return FROM��
	 */
	public String toString(){
		String str = "";
		int i=0;
		for(HarmonicaSource source : sources){
			if(i != 0) str += ",";
			i++;
			str += source.toString();
		}
		return str;
	}
}
