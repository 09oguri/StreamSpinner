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

import java.util.*;

import org.streamspinner.*;
import org.streamspinner.engine.*;
import org.streamspinner.query.*;
import org.streamspinner.harmonica.query.*;

/**
 * �X�g���[���f�[�^���Ǘ��E�~�ς��邽�߂̃C���^�t�F�[�X
 *
 * @author snic
 * @version 1.0 (2006.7.24)
 */
public interface StreamArchiver{
	public static final String harmonica_timestamp = "harmonica_ts";

	/**
	 * �e�[�u�����쐬����D
	 * @param table_name �쐬����e�[�u����
	 * @param schema �쐬����e�[�u���̃X�L�[�}
	 */
	public void createTable(String table_name, Schema schema) 
		throws HarmonicaException;

	/**
	 * ���[�U�Ǝ��̃e�[�u�����폜����D
	 * @param table_name �폜����e�[�u����
	 */
	public void dropTable(String table_name) throws HarmonicaException;

	/**
	 * ���[�U�Ǝ��̃e�[�u���ɒl��ǉ�����D
	 * @param table_name �e�[�u����
	 * @param tuples �ǉ�����^�v���W��
	 */
	public void insert(String table_name, TupleSet tuples)
	   	throws HarmonicaException;

	/**
	 * �X�g���[���f�[�^���^�v���W���Ƃ��Ď擾����D
	 * �E�C���h�E�̃T�C�Y��0�ȉ��̂Ƃ��̓T�C�Y������i�S�^�v���j�D
	 *
	 * @param sources ��񌹂̃��X�g(�E�C���h�E�t��)
	 * @param conditions �擾����^�v���̏���(null����)
	 * @param attributes ������ˉe���Z�p(null����)
	 */
	public TupleSet select
		(HarmonicaSourceSet sources, 
		 PredicateSet conditions, 
		 AttributeList attributes)
	   	throws HarmonicaException;

	/**
	 * �X�g���[���f�[�^���^�v���W���Ƃ��Ď擾����D
	 * �E�C���h�E�̋N�_�� now �ɃZ�b�g�����D
	 * �E�C���h�E�̃T�C�Y��0�ȉ��̂Ƃ��̓T�C�Y������i�S�^�v���j�D
	 *
	 * @param sources ��񌹂̃��X�g(�E�C���h�E�t��)
	 * @param conditions �擾����^�v���̏���(null����)
	 * @param attributes ������ˉe���Z�p(null����)
	 */
	public TupleSet select
		(SourceSet sources, 
		 PredicateSet conditions, 
		 AttributeList attributes)
	   	throws HarmonicaException;

	/**
	 * �������[�g���擾����D
	 * �܂��C�������[�g���v�Z���Ă��Ȃ��ꍇ�́C�����œK���ȃX�L�[�}���쐬���C
	 * updateInsertionRate()�ɂ�葪����s���D
	 *
	 * @return �f�[�^�ʂƏ������[�g��Map
	 */
	public Map<Integer, Double> getInsertionRate();

	/**
	 * �������[�g�𑪒肷��D
	 * @param schemas �������[�g���v�Z����X�L�[�}�̔z��
	 * @return �f�[�^�ʂƏ������[�g��Map
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas);

	/**
	 * �������[�g�𑪒肷��D���Ԃ��w��ł���D
	 * @param schemas �������[�g���v�Z����X�L�[�}�̔z��
	 * @param time ���肷�鎞��
	 * @return �f�[�^�ʂƏ������[�g��Map
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas, long time);

	/**
	 * �e�[�u���ꗗ���擾����D
	 * @return �e�[�u���ꗗ
	 */
	public List<Schema> getSchemaList() throws HarmonicaException;

	/**
	 * �e�[�u���̃X�L�[�}���擾����D
	 * @param table_name �擾����X�L�[�}�̃e�[�u����
	 * @return �e�[�u���̃X�L�[�}
	 */
	public Schema getSchema(String table_name) throws HarmonicaException;

	/**
	 * �e�[�u��������C1�^�v���ӂ�̃f�[�^�ʂ��擾����D
	 *
	 * @param table_name �e�[�u����
	 * @return �f�[�^��
	 */
	public int getDataSize(String table_name) throws HarmonicaException;

	/**
	 * �e�[�u���̃^�v�������擾����D
	 * @param table_name �^�v�������擾�������e�[�u����
	 * @return �^�v����
	 */
	public int getNumberOfTuples(String table_name);

	/**
	 * �e�[�u���̃^�v�������擾����D
	 * @param table_name �^�v�������擾�������e�[�u����
	 * @param window_size �^�v�������擾����E�C���h�E��
	 * @param original_point �^�v�������擾�������͈͂̋N�_
	 * @return �^�v����
	 */
	public int getNumberOfTuples(
			String table_name, 
			long window_size, 
			long original_point);

	/**
	 * �I���������s���D
	 * DB�Ƃ̐ڑ��̉���ɕK�v�D
	 */
	public void terminate() throws HarmonicaException;

	/**
	 * �f�[�^�x�[�X�ɑ΂���e�[�u���̑����E�����̒ʒm���󂯎��
	 * ���X�i�[��o�^����D
	 */
	public void addSchemaListChangeListener
	(SchemaListChangeListener listener);

	/**
	 * �o�^���ꂽ���X�i�[���폜����D
	 */
	public void removeSchemaListChangeListener
	(SchemaListChangeListener listener);

	/**
	 * Archiver�������Ŏ�Ƃ��ė��p���Ă���DB���ω��������Ƃ�ʒm����
	 * ���X�i�[��o�^����D
	 */
	public void addMainDBChangeListener
	(MainDBChangeListener listener);

	/**
	 * �o�^���ꂽ���X�i�[���폜����D
	 */
	public void removeMainDBChangeListener
	(MainDBChangeListener listener);

	/**
	 * �~�Ϗ����������������Ƃ�m�点�郊�X�i��o�^����D
	 *
	 * @param l �o�^���郊�X�i
	 */
	public void addInsertDataListener(InsertDataListener l);

	/**
	 * �~�Ϗ����������������Ƃ�m�点�郊�X�i���폜����D
	 *
	 * @param l �폜���郊�X�i
	 */
	public void removeInsertDataListener(InsertDataListener l);

	/**
	 * DB�R�l�N�^�̈ꗗ���擾����D
	 *
	 * @return DB�R�l�N�^�̃��X�g
	 */
	public List<DBConnector> getConnectors();

	/**
	 * ���C��DB�ɐݒ肷��D
	 *
	 * @param db_id ���C���Ƃ���DB��ID
	 */
	public void setMainDB(String db_id);

	/**
	 * ���C���ƂȂ��Ă���DB���擾����D
	 *
	 * @return ���C���ƂȂ��Ă���DB��ID
	 */
	public String getMainDB();

	/**
	 * DB�̐ڑ��̒ǉ�
	 *
	 * @param conn �ǉ�����DB�̃R�l�N�^�̃N���X
	 * @param url DB�̏ꏊ
	 * @param name DB��
	 * @param user DB�ڑ��p�̃��[�U��
	 * @param password DB�ڑ��p�̃��[�U�̃p�X���[�h
	 * @param options ���̑��C�I�v�V����
	 * @param rate �������݃��[�g(null��)
	 */
	public void addDB(
			String conn, 
			String url, 
			String name, 
			String user,
			String password,
			String options,
			Map<Integer, Double> rate) throws HarmonicaException;

	/**
	 * DB�̐ڑ��̍폜
	 *
	 * @param db_id �폜����DB�R�l�N�^��ID
	 */
	public void deleteDB(String db_id) throws HarmonicaException;

	/**
	 * �e�[�u����Harmonica�p�̃e�[�u�����ǂ����𔻒肷��
	 *
	 * @param table_name Harmonica�p�̃e�[�u���̂Ƃ���true
	 */
	public boolean isHarmonicaTable(String table_name);
}
