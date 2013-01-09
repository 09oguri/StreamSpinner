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
package org.streamspinner.harmonica.query.hamql;

import org.streamspinner.engine.*;
import org.streamspinner.query.*;
import org.streamspinner.harmonica.query.*;
import org.streamspinner.harmonica.*;


import java.util.*;
import java.io.*;

/**
 * HamQL�⍇���̊e�v�f��ێ�����N���X�D
 *
 * <PRE>
 * 1.0.2 2006.8.3 �^�C�v��enum���g���Ē�`�����D
 * </PRE>
 * @author snic
 * @version 1.0.2 (2006.8.3)
 */
public class HamQLQuery implements Serializable {
	private MasterSet masterc = null;
	private AttributeList selectc = null;
	private HarmonicaSourceSet fromc = null;
	private PredicateSet wherec = null;
	private AttributeList groupc = null;
	private String id = null;
	private Schema schema = null;
	private String drop_name = null;
	private QueryType type = QueryType.SELECT;
	private boolean is_union_all = true;
	private Vector<HamQLQuery> unions = null;

	/**
	 * @param m MASTER
	 * @param s SELECT
	 * @param f FROM
	 * @param w WHERE
	 * @param g GROUP
	 */
	public HamQLQuery(
			MasterSet m, 
			AttributeList s, 
			HarmonicaSourceSet f,
			PredicateSet w,
			AttributeList g
	){
		this.masterc = m;
		this.selectc = s;
		this.fromc = f;
		this.wherec = w;
		this.groupc = g;
	}

	public HamQLQuery(){
	}

	/**
	 * �a�W���̐ݒ�
	 * @param union_hamql �a���Ƃ�W���̖⍇��
	 */
	public void addUnion(HamQLQuery union_hamql){
		if(unions == null) unions = new Vector<HamQLQuery>();
		unions.add(union_hamql);
	}

	/**
	 * �a�W���d���������Ȃ����ǂ���
	 *
	 * @param isUnionAll �d���������Ȃ��Ƃ���true
	 */
	public void setUnionAll(boolean isUnionAll){
		is_union_all = isUnionAll;
	}

	/**
	 * �a�W���̏d���������邩���Ȃ������擾����
	 *
	 * @return �d���������Ȃ��Ƃ���true
	 */
	public boolean isUnionAll(){
		return is_union_all;
	}

	/**
	 * �a�W���̑ΏۂƂȂ�⍇�����擾����
	 *
	 * @return �a�W���ƂȂ�⍇���̃��X�g
	 */
	public List<HamQLQuery> getUnions(){
		if(unions == null) return new Vector<HamQLQuery>();

		return unions;
	}

	/**
	 * �⍇���̎�ނ�Ԃ�<BR>
	 * @return �⍇���̎��
	 */
	public QueryType getQueryType(){
		return this.type;
	}
	/**
	 * DROP�߂�ݒ肷��D
	 *
	 * @param table_name �폜����e�[�u����
	 */
	public void setDropClause(String table_name){
		this.type = QueryType.DROP;
		drop_name = table_name;
	}
	/**
	 * DROP����e�[�u�������擾����D
	 *
	 * @return �폜����e�[�u����
	 */
	public String getDropClause(){
		if(type != QueryType.DROP) return null;
		return drop_name;
	}
	/**
	 * INSERT�߂�ݒ肷��D
	 *
	 * @param schema INSERT��
	 */
	public void setInsertClause(Schema schema){
		this.type = QueryType.INSERT;
		setSchema(schema);
	}
	/**
	 * CREATE�߂�ݒ肷��D
	 *
	 * @param schema CREATE��
	 */
	public void setCreateClause(Schema schema){
		this.type = QueryType.CREATE;
		setSchema(schema);
	}
	/**
	 * INSERT�߂��擾����D
	 *
	 * @return INSERT��
	 */
	public Schema getInsertClause(){
		if(this.type != QueryType.INSERT) return null;
		return getSchema();
	}
	/**
	 * CREATE�߂��擾����D
	 *
	 * @return CREATE��
	 */
	public Schema getCreateClause(){
		if(this.type != QueryType.CREATE) return null;
		return getSchema();
	}
	private void setSchema(Schema schema){
		this.schema = schema;
	}
	private Schema getSchema(){
		return this.schema;
	}
	/**
	 * �⍇��ID���擾����D
	 * @return �⍇��ID
	 */
	public String getID(){ return id; }
	/**
	 * MASTER�߂��擾����D
	 * @return MASTER��
	 */
	public MasterSet getMasterClause(){ return masterc; }
	/**
	 * SELECT�߂��擾����D
	 * @return SELECT��
	 */
	public AttributeList getSelectClause(){ return selectc; }
	/**
	 * FROM�߂��擾����D
	 * @return FROM��
	 */
	public HarmonicaSourceSet getFromClause(){ return fromc; }
	/**
	 * WHERE�߂��擾����D
	 * @return WHERE��
	 */
	public PredicateSet getWhereClause(){ return wherec; }
	/**
	 * GROUP�߂��擾����D
	 * @return GROUP��
	 */
	public AttributeList getGroupClause(){ return groupc; }
	/**
	 * �⍇��ID��ݒ肷��D
	 * @param id �⍇��ID
	 */
	public void setID(String id){ this.id = id; }
	/**
	 * MASTER�߂�ݒ肷��D
	 * @param m MASTER��
	 */
	public void setMasterClause(MasterSet m){ this.masterc = m; }
	/**
	 * SELECT�߂�ݒ肷��D
	 * @param s SELECT��
	 */
	public void setSelectClause(AttributeList s){ this.selectc = s; }
	/**
	 * FROM�߂�ݒ肷��D
	 * @param f FROM��
	 */
	public void setFromClause(HarmonicaSourceSet f){ this.fromc = f; }
	/**
	 * WHERE�߂�ݒ肷��D
	 * @param w WHERE��
	 */
	public void setWhereClause(PredicateSet w){ this.wherec = w; }
	/**
	 * GROUP�߂�ݒ肷��D
	 * @param g GROUP��
	 */
	public void setGroupClause(AttributeList g){ this.groupc = g; }
	/**
	 * HamQL�⍇���L�q���擾����D
	 * @return HqmQL�⍇���L�q
	 */
	public String toString(){
		StringBuilder strb = new StringBuilder();
		if(this.type == QueryType.CREATE){
			String[] table_name = this.schema.getBaseTableNames();
			if(table_name.length != 0){
				strb.append("CREATE TABLE ");
			   	strb.append(table_name[0]);
			   	strb.append(" (");
				for(int i=0;i<schema.size();i++){
					if(i != 0) strb.append(", ");
					strb.append(schema.getAttributeName(i));
				   	strb.append(" ");
					strb.append(schema.getType(i));
				}
				strb.append(")");
			}
			return strb.toString();
		}

		if(this.type == QueryType.DROP){
			strb.append("DROP TABLE ");
		   	strb.append(drop_name);
			return strb.toString();
		}

		if(masterc != null){
			strb.append("MASTER ");
	   		strb.append(this.masterc.toString());
		}

		if(this.type == QueryType.INSERT){
			String[] table_name = this.schema.getBaseTableNames();
			if(table_name.length != 0){
				strb.append(" INSERT INTO ");
			   	strb.append(table_name[0]);
			   	strb.append(" ");
			}
		}
	   	
		if(selectc != null){
			strb.append(" SELECT ");
	   		strb.append(this.selectc.toString());
		}

		if(fromc != null){
			strb.append(" FROM ");
		   	strb.append(this.fromc.toString());
		}

	   	if(wherec != null){
			strb.append(" WHERE ");
		   	strb.append(this.wherec.toString());
		}

	   	if(groupc != null){
			strb.append(" GROUP BY ");
		   	strb.append(this.groupc.toString());
		}

		if(unions != null)
		for(HamQLQuery q : unions){
			strb.append(" UNION");
			if(q.isUnionAll()) strb.append(" ALL");
			strb.append(q.toString());
		}

		return strb.toString(); 
	}
}
