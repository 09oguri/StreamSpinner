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
 * HamQLのタイプ
 *
 * @author snic
 * @version 1.0
 */
public enum QueryType {
	/**
	 * テーブル作成要求．
	 */
	CREATE,
	/**
	 * テーブル削除要求．
	 */
	DROP,
	/**
	 * 蓄積要求．
	 */
	INSERT,
	/**
	 * 問合せ要求．
	 */
	SELECT
};
