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
import java.sql.*;

import org.streamspinner.query.*;
import org.streamspinner.engine.*;

/**
 * DBと接続するためのインタフェース．
 * 
 * @author snic
 * @version 1.0 (2006.7.23)
 */
public interface DBConnector {
	public void connect(String url, String db, String user, String password, String option) throws HarmonicaException;
	public void reconnect() throws HarmonicaException;
	public ResultSet executeQuery(String query) throws HarmonicaException;
	public TupleSet makeTupleSet(ResultSet rs) throws HarmonicaException;
	public void executeUpdate(String query) throws HarmonicaException;
	public void executeUpdateNoQueue(String query) throws HarmonicaException;
	public Map<Integer, Double> getInsertionRate();
	public Map<Integer, Double> updateInsertionRate
		(Schema[] schemas);
	public Map<Integer, Double> updateInsertionRate
		(Schema[] schemas, long time);
	public String[] getInitializeInformation();
	public void setInsertionRate(Map<Integer, Double> insertion_rate);
	public List<Schema> getSchemaList() throws HarmonicaException;
	public Schema getSchema(String table_name) throws HarmonicaException;
	public int getNumberOfTuples(String table_name);
	public int getNumberOfTuples
		(String table_name, long window_size, long original_point);
	public void disconnect() throws HarmonicaException;
	public int getDataSize(Schema schema);
	public String convertDataType(String ss_type);
	public String getName();
	public String getVersion();
	public String getHost();
	public String getID();
	public void setID(String id);
	public String toString();
	public DBConnector getSelfObject();
}
