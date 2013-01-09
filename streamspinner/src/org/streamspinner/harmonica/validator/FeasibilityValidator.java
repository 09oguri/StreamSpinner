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
import org.w3c.dom.Document;

/**
 * �⍇���������i�����\���ǂ����𔻒肷�锻���̃C���^�t�F�[�X�D
 *
 * @author snic
 * @version 1.0 (2006.8.4)
 */
public interface FeasibilityValidator {
	/**
	 * �⍇���L�q����i�����\���𔻒肵�C���茋�ʂ�Ԃ��D
	 * @param statement �⍇���L�q
	 * @return ���茋�� 
	 */
	public ValidatingResult getResultAfterValidate(String statement)
	throws HarmonicaException;

	/**
	 * �⍇���L�q����i�����\���𔻒肵�C���茋�ʂ�Ԃ��D
	 * @param statement �⍇���L�q
	 * @return DOM(�����\�łȂ��ꍇ��null) 
	 */
	public Document validateQuery(String statement) 
	throws HarmonicaException;
	
	/**
	 * �����̃��j�^�����O�p�̃��X�i��o�^����D
	 * @param listener ���j�^�����O���s�����X�i
	 */
	public void addFeasibilityValidatorListener
	(FeasibilityValidatorListener listener);

	/**
	 * �����̃��j�^�����O���s���Ă��郊�X�i�̓o�^����������D
	 *
	 * @param listener ���j�^�����O���s���Ă��郊�X�i
	 */
	public void removeFeasibilityValidatorListener
	(FeasibilityValidatorListener listener);

	/*
	 * ����ς݂̖⍇�������f�B�G�[�^�ɓo�^����D
	 *
	 * @param query ����ς݂̖⍇��
	 */
	//public String startQuery(ValidatingResult query);

	/*
	 * ���f�B�G�[�^�ɓo�^����Ă���⍇�����L�����Z������D
	 */
	//public void stopQuery(String qid);
}
