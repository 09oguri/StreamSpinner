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
 * 描画する画像のタイプ
 *
 * @author snic
 * @version 1.0
 */
public enum ImageType {
	/**
	 * ストリーム処理可能である．
	 */
	PROCESSABLE,
	/**
	 * ストリーム処理不可能である．
	 */
	UNPROCESSABLE,
	/**
	 * 蓄積可能である．
	 */
	INSERTABLE,
	/**
	 * 蓄積可能かどうかの判定は必要ない．
	 */
	INSERTABLE_GRAY,
	/**
	 * 蓄積不可能である．
	 */
	UNINSERTABLE,
	/**
	 * 問合せが登録中である．
	 */
	QUERY_RUNNING,
	/**
	 * 問合せがキャンセルされた．
	 */
	QUERY_CANCELED,
	/**
	 * 問合せの行方を待っている最中である．
	 */
	QUERY_WAITING,
	/**
	 * DBがいっぱいのアイコン
	 */
	DBS_ICON,
	/**
	 * DBのアイコン
	 */
	DB_ICON,
	/**
	 * DBのアイコン(MAIN)
	 */
	DB_MAIN_ICON,
	/**
	 * テーブルのアイコン
	 */
	TABLE_ICON,
	/**
	 * テーブル更新のアイコン
	 */
	TABLE_UPDATE_ICON,
	/**
	 * 問合せ木アイコン
	 */
	TREE_ICON,
	/**
	 * HAMQLアイコン
	 */
	HAMQL_ICON,
	/**
	 * OPENアイコン
	 */
	OPEN_ICON,
	/**
	 * RELOADアイコン
	 */
	RELOAD_ICON,
	/**
	 * CONNECTORアイコン
	 */
	CONNECTOR_ICON,
	/**
	 * Harmonicaアイコン
	 */
	HARMONICA_ICON,
	/**
	 * 飛び出すアイコン
	 */
	FLOW_ICON,
	/**
	 * Harmonicaロゴ
	 */
	HARMONICA_LOGO,
	/**
	 * 処理可能程度を示すバー
	 */
	ABLE_BAR,
	/**
	 * 使用不可を表現する処理可能程度を示すバー
	 */
	ABLE_BAR_GRAY,
	/**
	 * 最適化情報源
	 */
	OPT_INFORMATION,
	PUSHED_OPT_INFORMATION,
	/**
	 * 最適化ROOT
	 */
	OPT_ROOT,
	PUSHED_OPT_ROOT,
	/**
	 * 最適化SELECTION
	 */
	OPT_SELECTION,
	PUSHED_OPT_SELECTION,
	/**
	 * 最適化PROJECTION
	 */
	OPT_PROJECTION,
	PUSHED_OPT_PROJECTION,
	/**
	 * 最適化JOIN
	 */
	OPT_JOIN,
	PUSHED_OPT_JOIN,
	/**
	 * 最適化STORE
	 */
	OPT_STORE,
	PUSHED_OPT_STORE
};
