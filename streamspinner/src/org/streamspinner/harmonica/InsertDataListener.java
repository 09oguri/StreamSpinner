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

import org.streamspinner.engine.*;

/**
 * 蓄積データがArchiverに到着するのを監視するためのリスナ
 *
 * @author snic
 * @version 1.0 (2006.8.8)
 */
public interface InsertDataListener extends EventListener {
	/**
	 * 蓄積データが到着した．
	 *
	 * @param table_name データを蓄積するためのテーブル
	 * @param tuples 蓄積するデータ
	 */
	public void arrivedInsertData(String table_name, TupleSet tuples);
}
