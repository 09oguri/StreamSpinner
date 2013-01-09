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

/**
 * �`�悷��摜�̃^�C�v
 *
 * @author snic
 * @version 1.0
 */
public enum ImageType {
	/**
	 * �X�g���[�������\�ł���D
	 */
	PROCESSABLE,
	/**
	 * �X�g���[�������s�\�ł���D
	 */
	UNPROCESSABLE,
	/**
	 * �~�ω\�ł���D
	 */
	INSERTABLE,
	/**
	 * �~�ω\���ǂ����̔���͕K�v�Ȃ��D
	 */
	INSERTABLE_GRAY,
	/**
	 * �~�ϕs�\�ł���D
	 */
	UNINSERTABLE,
	/**
	 * �⍇�����o�^���ł���D
	 */
	QUERY_RUNNING,
	/**
	 * �⍇�����L�����Z�����ꂽ�D
	 */
	QUERY_CANCELED,
	/**
	 * �⍇���̍s����҂��Ă���Œ��ł���D
	 */
	QUERY_WAITING,
	/**
	 * DB�������ς��̃A�C�R��
	 */
	DBS_ICON,
	/**
	 * DB�̃A�C�R��
	 */
	DB_ICON,
	/**
	 * DB�̃A�C�R��(MAIN)
	 */
	DB_MAIN_ICON,
	/**
	 * �e�[�u���̃A�C�R��
	 */
	TABLE_ICON,
	/**
	 * �e�[�u���X�V�̃A�C�R��
	 */
	TABLE_UPDATE_ICON,
	/**
	 * �⍇���؃A�C�R��
	 */
	TREE_ICON,
	/**
	 * HAMQL�A�C�R��
	 */
	HAMQL_ICON,
	/**
	 * OPEN�A�C�R��
	 */
	OPEN_ICON,
	/**
	 * RELOAD�A�C�R��
	 */
	RELOAD_ICON,
	/**
	 * CONNECTOR�A�C�R��
	 */
	CONNECTOR_ICON,
	/**
	 * Harmonica�A�C�R��
	 */
	HARMONICA_ICON,
	/**
	 * ��яo���A�C�R��
	 */
	FLOW_ICON,
	/**
	 * Harmonica���S
	 */
	HARMONICA_LOGO,
	/**
	 * �����\���x�������o�[
	 */
	ABLE_BAR,
	/**
	 * �g�p�s��\�����鏈���\���x�������o�[
	 */
	ABLE_BAR_GRAY,
	/**
	 * �œK�����
	 */
	OPT_INFORMATION,
	PUSHED_OPT_INFORMATION,
	/**
	 * �œK��ROOT
	 */
	OPT_ROOT,
	PUSHED_OPT_ROOT,
	/**
	 * �œK��SELECTION
	 */
	OPT_SELECTION,
	PUSHED_OPT_SELECTION,
	/**
	 * �œK��PROJECTION
	 */
	OPT_PROJECTION,
	PUSHED_OPT_PROJECTION,
	/**
	 * �œK��JOIN
	 */
	OPT_JOIN,
	PUSHED_OPT_JOIN,
	/**
	 * �œK��STORE
	 */
	OPT_STORE,
	PUSHED_OPT_STORE
};
