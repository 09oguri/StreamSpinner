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
 * HamQL問合せの各要素を保持するクラス．
 *
 * <PRE>
 * 1.0.2 2006.8.3 タイプをenumを使って定義した．
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
	 * 和集合の設定
	 * @param union_hamql 和をとる集合の問合せ
	 */
	public void addUnion(HamQLQuery union_hamql){
		if(unions == null) unions = new Vector<HamQLQuery>();
		unions.add(union_hamql);
	}

	/**
	 * 和集合重複除去しないかどうか
	 *
	 * @param isUnionAll 重複除去しないときはtrue
	 */
	public void setUnionAll(boolean isUnionAll){
		is_union_all = isUnionAll;
	}

	/**
	 * 和集合の重複除去するかしないかを取得する
	 *
	 * @return 重複除去しないときはtrue
	 */
	public boolean isUnionAll(){
		return is_union_all;
	}

	/**
	 * 和集合の対象となる問合せを取得する
	 *
	 * @return 和集合となる問合せのリスト
	 */
	public List<HamQLQuery> getUnions(){
		if(unions == null) return new Vector<HamQLQuery>();

		return unions;
	}

	/**
	 * 問合せの種類を返す<BR>
	 * @return 問合せの種類
	 */
	public QueryType getQueryType(){
		return this.type;
	}
	/**
	 * DROP節を設定する．
	 *
	 * @param table_name 削除するテーブル名
	 */
	public void setDropClause(String table_name){
		this.type = QueryType.DROP;
		drop_name = table_name;
	}
	/**
	 * DROPするテーブル名を取得する．
	 *
	 * @return 削除するテーブル名
	 */
	public String getDropClause(){
		if(type != QueryType.DROP) return null;
		return drop_name;
	}
	/**
	 * INSERT節を設定する．
	 *
	 * @param schema INSERT節
	 */
	public void setInsertClause(Schema schema){
		this.type = QueryType.INSERT;
		setSchema(schema);
	}
	/**
	 * CREATE節を設定する．
	 *
	 * @param schema CREATE節
	 */
	public void setCreateClause(Schema schema){
		this.type = QueryType.CREATE;
		setSchema(schema);
	}
	/**
	 * INSERT節を取得する．
	 *
	 * @return INSERT節
	 */
	public Schema getInsertClause(){
		if(this.type != QueryType.INSERT) return null;
		return getSchema();
	}
	/**
	 * CREATE節を取得する．
	 *
	 * @return CREATE節
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
	 * 問合せIDを取得する．
	 * @return 問合せID
	 */
	public String getID(){ return id; }
	/**
	 * MASTER節を取得する．
	 * @return MASTER節
	 */
	public MasterSet getMasterClause(){ return masterc; }
	/**
	 * SELECT節を取得する．
	 * @return SELECT節
	 */
	public AttributeList getSelectClause(){ return selectc; }
	/**
	 * FROM節を取得する．
	 * @return FROM節
	 */
	public HarmonicaSourceSet getFromClause(){ return fromc; }
	/**
	 * WHERE節を取得する．
	 * @return WHERE節
	 */
	public PredicateSet getWhereClause(){ return wherec; }
	/**
	 * GROUP節を取得する．
	 * @return GROUP節
	 */
	public AttributeList getGroupClause(){ return groupc; }
	/**
	 * 問合せIDを設定する．
	 * @param id 問合せID
	 */
	public void setID(String id){ this.id = id; }
	/**
	 * MASTER節を設定する．
	 * @param m MASTER節
	 */
	public void setMasterClause(MasterSet m){ this.masterc = m; }
	/**
	 * SELECT節を設定する．
	 * @param s SELECT節
	 */
	public void setSelectClause(AttributeList s){ this.selectc = s; }
	/**
	 * FROM節を設定する．
	 * @param f FROM節
	 */
	public void setFromClause(HarmonicaSourceSet f){ this.fromc = f; }
	/**
	 * WHERE節を設定する．
	 * @param w WHERE節
	 */
	public void setWhereClause(PredicateSet w){ this.wherec = w; }
	/**
	 * GROUP節を設定する．
	 * @param g GROUP節
	 */
	public void setGroupClause(AttributeList g){ this.groupc = g; }
	/**
	 * HamQL問合せ記述を取得する．
	 * @return HqmQL問合せ記述
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
