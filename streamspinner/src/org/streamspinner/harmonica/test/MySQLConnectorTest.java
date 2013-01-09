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
package org.streamspinner.harmonica.test;

import java.util.*;

import org.streamspinner.harmonica.util.*;
import org.streamspinner.harmonica.*;
import org.streamspinner.engine.*;

public class MySQLConnectorTest {
	public static void main(String[] args){
		try{
			MySQLConnector conn = new MySQLConnector();
			conn.connect(
					"localhost",
					"harmonica",
					"harmonica",
					"harmonica",
					"useUnicode=true&characterEncoding=SJIS"
			);
			conn.executeUpdate
				("create table test (id integer, name text)");
			conn.executeUpdate
				("create table test2 (ids double, name text)");
			conn.executeUpdate
				("insert into test values (1,'snic1')");
			conn.executeUpdate
				("insert into test values (2,'snic2')");
			List list = conn.getSchemaList();
			System.out.println(list);
			TupleSet ts = conn.makeTupleSet(conn.executeQuery
				("select * from test"));
			System.out.println(ts);
			ts = conn.makeTupleSet(conn.executeQuery
				("select name,id,name from test where id=2"));
			System.out.println(ts);
			conn.executeUpdate("drop table test");
			conn.executeUpdate("drop table test2");

			/*
			String[] names = {"a","b","c"};
			String[] types = {Schema.LONG, Schema.DOUBLE, Schema.STRING};
			String[] types2 = {Schema.LONG, Schema.LONG, Schema.LONG};

			Schema s = new Schema("hoge",names,types);
			Schema s2 = new Schema("hoge2",names,types2);

			System.out.println(conn.getDataSize(s));
			System.out.println(conn.getDataSize(s2));

			System.out.println("Calculating insertion rate...");

			Schema[] ss = {s,s2,s};

			Map m = conn.updateInsertionRate(ss);
			System.out.println(m);
			*/
			System.out.println(conn.getInsertionRate());

			conn.disconnect();
		}catch(HarmonicaException e){
			e.printStackTrace();
		}
	}
}
