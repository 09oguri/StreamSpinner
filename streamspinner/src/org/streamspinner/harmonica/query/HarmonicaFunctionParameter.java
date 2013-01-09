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

import org.streamspinner.query.*;

import java.util.*;

/**
 * FunctionParameter�̃��l�[���Ή��ŁD
 *
 * @author snic
 * @version 1.0 (2006.7.26)
 */
public class HarmonicaFunctionParameter extends FunctionParameter {
	private String rename = null;

	public HarmonicaFunctionParameter(String str){
		super(str);
	}
	public HarmonicaFunctionParameter(String f, AttributeList a){
		super(f,a);
	}

	/**
	 * �ʖ���ݒ肷��D
	 * @param rename �ʖ�
	 */
	public void setRename(String rename){
		this.rename = rename;
	}
	/**
	 * ��`����Ă���ʖ����擾����D
	 * @return �ʖ�
	 */
	public String getRename(){
		return this.rename;
	}
	public String toString(){
		String str = super.toString();
		if(rename != null) str += " AS " + rename;

		return str;
	}
}
