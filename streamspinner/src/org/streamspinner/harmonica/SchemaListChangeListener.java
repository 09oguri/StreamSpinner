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
 * データベースに対してテーブルの追加・削除の通知を受けるリスナー．
 *
 * @author snic
 * @version 1.0 (2006.8.2)
 */
public interface SchemaListChangeListener extends EventListener {
	/**
	 * テーブルが追加されたことを通知するイベント．
	 */
	public void addTable(Schema schema);
	
	/**
	 * テーブルが削除されたことを通知するイベント．
	 */
	public void deleteTable(String table_name);

	/**
	 * DBとの接続が追加されたことを通知するイベント．
	 */
	public void addDBMSConnection(DBConnector con);

	/**
	 * DBとの接続が解除されたことを通知するイベント．
	 */
	public void deleteDBMSConnection(DBConnector con);
}
