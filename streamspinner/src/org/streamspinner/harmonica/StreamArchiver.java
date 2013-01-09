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
 * ストリームデータを管理・蓄積するためのインタフェース
 *
 * @author snic
 * @version 1.0 (2006.7.24)
 */
public interface StreamArchiver{
	public static final String harmonica_timestamp = "harmonica_ts";

	/**
	 * テーブルを作成する．
	 * @param table_name 作成するテーブル名
	 * @param schema 作成するテーブルのスキーマ
	 */
	public void createTable(String table_name, Schema schema) 
		throws HarmonicaException;

	/**
	 * ユーザ独自のテーブルを削除する．
	 * @param table_name 削除するテーブル名
	 */
	public void dropTable(String table_name) throws HarmonicaException;

	/**
	 * ユーザ独自のテーブルに値を追加する．
	 * @param table_name テーブル名
	 * @param tuples 追加するタプル集合
	 */
	public void insert(String table_name, TupleSet tuples)
	   	throws HarmonicaException;

	/**
	 * ストリームデータをタプル集合として取得する．
	 * ウインドウのサイズが0以下のときはサイズ無限大（全タプル）．
	 *
	 * @param sources 情報源のリスト(ウインドウ付き)
	 * @param conditions 取得するタプルの条件(nullも可)
	 * @param attributes いわゆる射影演算用(nullも可)
	 */
	public TupleSet select
		(HarmonicaSourceSet sources, 
		 PredicateSet conditions, 
		 AttributeList attributes)
	   	throws HarmonicaException;

	/**
	 * ストリームデータをタプル集合として取得する．
	 * ウインドウの起点は now にセットされる．
	 * ウインドウのサイズが0以下のときはサイズ無限大（全タプル）．
	 *
	 * @param sources 情報源のリスト(ウインドウ付き)
	 * @param conditions 取得するタプルの条件(nullも可)
	 * @param attributes いわゆる射影演算用(nullも可)
	 */
	public TupleSet select
		(SourceSet sources, 
		 PredicateSet conditions, 
		 AttributeList attributes)
	   	throws HarmonicaException;

	/**
	 * 書込レートを取得する．
	 * まだ，書込レートを計算していない場合は，内部で適当なスキーマを作成し，
	 * updateInsertionRate()により測定を行う．
	 *
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> getInsertionRate();

	/**
	 * 書込レートを測定する．
	 * @param schemas 書込レートを計算するスキーマの配列
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas);

	/**
	 * 書込レートを測定する．時間を指定できる．
	 * @param schemas 書込レートを計算するスキーマの配列
	 * @param time 測定する時間
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas, long time);

	/**
	 * テーブル一覧を取得する．
	 * @return テーブル一覧
	 */
	public List<Schema> getSchemaList() throws HarmonicaException;

	/**
	 * テーブルのスキーマを取得する．
	 * @param table_name 取得するスキーマのテーブル名
	 * @return テーブルのスキーマ
	 */
	public Schema getSchema(String table_name) throws HarmonicaException;

	/**
	 * テーブル名から，1タプル辺りのデータ量を取得する．
	 *
	 * @param table_name テーブル名
	 * @return データ量
	 */
	public int getDataSize(String table_name) throws HarmonicaException;

	/**
	 * テーブルのタプル数を取得する．
	 * @param table_name タプル数を取得したいテーブル名
	 * @return タプル数
	 */
	public int getNumberOfTuples(String table_name);

	/**
	 * テーブルのタプル数を取得する．
	 * @param table_name タプル数を取得したいテーブル名
	 * @param window_size タプル数を取得するウインドウ幅
	 * @param original_point タプル数を取得したい範囲の起点
	 * @return タプル数
	 */
	public int getNumberOfTuples(
			String table_name, 
			long window_size, 
			long original_point);

	/**
	 * 終了処理を行う．
	 * DBとの接続の解放に必要．
	 */
	public void terminate() throws HarmonicaException;

	/**
	 * データベースに対するテーブルの増加・減少の通知を受け取る
	 * リスナーを登録する．
	 */
	public void addSchemaListChangeListener
	(SchemaListChangeListener listener);

	/**
	 * 登録されたリスナーを削除する．
	 */
	public void removeSchemaListChangeListener
	(SchemaListChangeListener listener);

	/**
	 * Archiverが内部で主として利用しているDBが変化したことを通知する
	 * リスナーを登録する．
	 */
	public void addMainDBChangeListener
	(MainDBChangeListener listener);

	/**
	 * 登録されたリスナーを削除する．
	 */
	public void removeMainDBChangeListener
	(MainDBChangeListener listener);

	/**
	 * 蓄積処理が発生したことを知らせるリスナを登録する．
	 *
	 * @param l 登録するリスナ
	 */
	public void addInsertDataListener(InsertDataListener l);

	/**
	 * 蓄積処理が発生したことを知らせるリスナを削除する．
	 *
	 * @param l 削除するリスナ
	 */
	public void removeInsertDataListener(InsertDataListener l);

	/**
	 * DBコネクタの一覧を取得する．
	 *
	 * @return DBコネクタのリスト
	 */
	public List<DBConnector> getConnectors();

	/**
	 * メインDBに設定する．
	 *
	 * @param db_id メインとするDBのID
	 */
	public void setMainDB(String db_id);

	/**
	 * メインとなっているDBを取得する．
	 *
	 * @return メインとなっているDBのID
	 */
	public String getMainDB();

	/**
	 * DBの接続の追加
	 *
	 * @param conn 追加するDBのコネクタのクラス
	 * @param url DBの場所
	 * @param name DB名
	 * @param user DB接続用のユーザ名
	 * @param password DB接続用のユーザのパスワード
	 * @param options その他，オプション
	 * @param rate 書き込みレート(null可)
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
	 * DBの接続の削除
	 *
	 * @param db_id 削除するDBコネクタのID
	 */
	public void deleteDB(String db_id) throws HarmonicaException;

	/**
	 * テーブルがHarmonica用のテーブルかどうかを判定する
	 *
	 * @param table_name Harmonica用のテーブルのときはtrue
	 */
	public boolean isHarmonicaTable(String table_name);
}
