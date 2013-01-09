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

import org.streamspinner.*;
import org.streamspinner.engine.*;
import org.streamspinner.query.*;
import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.query.*;
import java.util.*;

public class StreamArchiverTest {
	public static void main(String[] args){
		try{
			StreamArchiver archiver = HarmonicaManager.getStreamArchiver();

			String table_name = "A_test";
			String[] names = {"A_test.timestamp","A_test.b","A_test.c"};
			String[] types = {DataTypes.LONG, DataTypes.LONG, DataTypes.LONG};

			Schema schema = new Schema(table_name, names, types);

			archiver.createTable(table_name, schema);
			Schema s2 = archiver.getSchema(table_name);
			System.out.println(s2);

			OnMemoryTupleSet ts = new OnMemoryTupleSet(schema);
			Tuple t = new Tuple(3);
			t.setLong(0,1);
			t.setLong(1,2);
			t.setLong(2,3);
			t.setTimestamp(table_name,10);
			ts.appendTuple(t);

			archiver.insert(table_name,ts);


			Map m = archiver.getInsertionRate();
			System.out.println(m);

			Schema[] schemas = {schema};
			//m = archiver.updateInsertionRate(schemas);
			//System.out.println(m);

			List l = archiver.getSchemaList();
			System.out.println(l);

			HarmonicaSource hs = new HarmonicaSource("A_test",10,14);
			HarmonicaSourceSet ss = new HarmonicaSourceSet();
			ss.add(hs);

			Predicate p = new Predicate("A_test.timestamp",Predicate.NE,"3");
			PredicateSet ps = new PredicateSet(p);

			String[] attrs = {"A_test.timestamp","A_test.c"};
			AttributeList at = new AttributeList(attrs);

			archiver.select(ss,ps,at);

			archiver.dropTable(table_name);
			HarmonicaManager.terminate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
