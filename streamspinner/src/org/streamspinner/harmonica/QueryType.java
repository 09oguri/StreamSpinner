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
package org.streamspinner.harmonica;

/**
 * HamQL�̃^�C�v
 *
 * @author snic
 * @version 1.0
 */
public enum QueryType {
	/**
	 * �e�[�u���쐬�v���D
	 */
	CREATE,
	/**
	 * �e�[�u���폜�v���D
	 */
	DROP,
	/**
	 * �~�ϗv���D
	 */
	INSERT,
	/**
	 * �⍇���v���D
	 */
	SELECT
};