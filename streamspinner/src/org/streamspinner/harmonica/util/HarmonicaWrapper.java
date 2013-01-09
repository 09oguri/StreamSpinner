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
package org.streamspinner.harmonica.util;

import org.streamspinner.*;
import org.streamspinner.query.*;
import org.streamspinner.engine.*;
import org.streamspinner.wrapper.*;
import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.query.*;

import java.util.*;

/**
 * HarmonicaをStreamSpinnerのRDBラッパーのように見せかけるためのクラス．
 *
 * @author snic
 * @version 1.0 (2006.8.23)
 */
public class HarmonicaWrapper 
	extends Wrapper
	implements InsertDataListener,
		   SchemaListChangeListener{
	StreamArchiver arc = null;

	/**
	 * ラッパー名で初期化します．
	 * 
	 * @param name ラッパー名．null or "" の場合は"Harmonica"になります．
	 */
	public HarmonicaWrapper(String name) throws StreamSpinnerException{
		super(name);
		
		arc = HarmonicaManager.getStreamArchiver();
		arc.addInsertDataListener(this);
		arc.addSchemaListChangeListener(this);
	}

	/**
	 * 何もしません．
	 */
	public void init() throws StreamSpinnerException{}

	/**
	 * ラッパーの動作を停止します．
	 */
	public void stop() throws StreamSpinnerException{
		arc.removeInsertDataListener(this);
	}
	
	/**
	 * 何もしません．
	 */
	public void start() throws StreamSpinnerException{}

	/**
	 * テーブルの名前をすべて取得します．
	 *
	 * @return テーブル名の一覧．テーブルが一つも無いときはnull．
	 */
	public String[] getAllTableNames(){
		if(arc == null) return null;

		List<Schema> l = null;
		
		try{
			l = arc.getSchemaList();
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return null;
		}

		Vector<String> v = new Vector<String>();

		for(Schema s : l){
			v.add(s.getBaseTableNames()[0]);
		}

		return v.toArray(new String[0]);
	}

	/**
	 * 与えられたテーブル名のスキーマを取得する．
	 *
	 * @param tablename テーブル名
	 * @return テーブル名に対応するスキーマ．存在しないときはnull．
	 */
	public Schema getSchema(String tablename){
		Schema s = null;
		try{
			if(arc != null) s = arc.getSchema(tablename);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		return s;
	}

	/**
	 * タプルを取得する．
	 */
	public TupleSet getTupleSet(ORNode node) throws StreamSpinnerException {
		if(arc == null){
			HarmonicaException he = new HarmonicaException("not init.");
			HarmonicaManager.createdException(he);
		   	throw new StreamSpinnerException(he);
		}

		AttributeList select_c = node.getAttributeList();
		SourceSet from_c = node.getSources();
		PredicateSet where_c = node.getConditions();

		TupleSet ts = null;
		try{
			ts = arc.select(from_c,where_c,select_c);
		}catch(HarmonicaException e){
			throw new StreamSpinnerException(e);
		}

		return ts;
	}

	/**
	 * StreamArchiverに挿入するデータが到着したときに呼ばれるメソッド．
	 */
	public void arrivedInsertData(String table_name, TupleSet tuples){
		long time = System.currentTimeMillis();

		/*
		if(tuples == null)
			System.out.println("************************************");
		try{
		System.out.println(tuples.first());
		}catch(Exception e){}
		*/

		deliverTupleSet(time, table_name, tuples);
	}

	/**
	 * DBMSコネクタが追加された時に呼ばれるメソッド．
	 */
	public void addDBMSConnection(DBConnector con){
		try{
			List<Schema> schemas = con.getSchemaList();

			for(Schema s : schemas){
				String name = s.getBaseTableNames()[0];
				notifyTableCreation(name, s);
			}

		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	/**
	 * テーブルが追加された時に呼ばれるメソッド．
	 */
	public void addTable(Schema schema){
		notifyTableCreation(schema.getBaseTableNames()[0], schema);
	}

	/**
	 * DBMSコネクタが解除された時に呼ばれるメソッド．
	 */
	public void deleteDBMSConnection(DBConnector con){
		try{
			List<Schema> schemas = con.getSchemaList();

			for(Schema s : schemas){
				String name = s.getBaseTableNames()[0];
				notifyTableDrop(name);
			}
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	/**
	 * テーブルが削除された時に呼ばれるメソッド．
	 */
	public void deleteTable(String table_name){
		notifyTableDrop(table_name);
	}
}
