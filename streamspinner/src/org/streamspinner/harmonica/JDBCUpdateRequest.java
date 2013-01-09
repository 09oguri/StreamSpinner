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

import java.sql.*;

/**
 * Wokerに行わせる仕事．(JDBC版)
 *
 * @author snic
 * @version 1.0 (2006.8.3)
 */
public class JDBCUpdateRequest implements HarmonicaRequest{
	public JobMonitor monitor = null;
	private Statement stmt = null;
	private String sql = null;
	public int id = 0;
	public JDBCUpdateRequest(Statement stmt, String sql){
		this.stmt = stmt;
		this.sql = sql;

		monitor = JobMonitor.getInstance();
		monitor.create();
	}
	/**
	 * 更新要求を実行
	 */
	public void run(){
		try{
			stmt.executeUpdate(sql);
			debug(sql);
			monitor.done();
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
	}
	private void debug(Object o){
		HarmonicaManager.debug("JDBCRequest",o);
	}
}
