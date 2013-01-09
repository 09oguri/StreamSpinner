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

import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.*;

import java.util.*;

public class HamQLParserTest {
	public static void main(String[] args){
		try{
			String hamql1 = 
				"MASTER Turbine\n"+
				"SELECT exectime(Turbine.timestamp), avg(Turbine.value)\n"+
				"FROM Turbine[3,now]\n";

			String omake = 
				"MASTER Turbine SELECT * FROM Turbine[1,now]\n";

			String hamql2 =
				"MASTER Turbine\n"+
				"SELECT exectime(w1.timestamp), avg(w1.value),\n"+
				"       exectime(w2.timestamp), avg(w2.value)\n"+
				"FROM Turbine[3,now] AS w1, Turbine[3,now-100] AS w2";

			String hamql3 =
				"MASTER Clock_1minute\n"+
				"SELECT S1.time, dist(S1.value,S2.value)\n"+
				"FROM (SELECT exectime(Turbine.timestamp) AS time,\n"+
				"             array(Turbine.value), Turbine.id\n"+
				"      FROM Turbine[3,now]\n"+
				"      GROUP BY Turbine.id) AS S1,\n"+
				"     (SELECT array(Turbine_Table.value), Turbine_Table.id\n"+
				"      FROM Turbine_Table[3,50]\n"+
				"      GROUP BY Turbine_Table.id) AS S2\n"+
				"WHERE S1.id = S2.id";

			String hamql4 =
				"MASTER Clock_1minute\n"+
				"SELECT S1.time, dist(S1.value,S2.value)\n"+
				"FROM (SELECT exectime(Turbine.timestamp) AS time,\n"+
				"             array(Turbine.value), Turbine.id\n"+
				"      FROM Turbine[3,now]\n"+
				"      GROUP BY Turbine.id) AS S1,\n"+
				"     (SELECT array(Turbine_Table.value), Turbine_Table.id\n"+
				"      FROM Turbine_Table[3]\n"+
				"      GROUP BY Turbine_Table.id) AS S2\n"+
				"WHERE S1.id = S2.id AND dist(S1.value,S2.value) <= 10";
				
			String hamql4_1 =
				"MASTER Clock_1minute\n"+
				"SELECT S1.time AS A, dist(S1.value,S2.value) AS B\n"+
				"FROM (SELECT exectime(Turbine.timestamp) AS time,\n"+
				"             array(Turbine.value), Turbine.id\n"+
				"      FROM Turbine[3,now]\n"+
				"      GROUP BY Turbine.id) AS S1,\n"+
				"     (SELECT array(Turbine_Table.value), Turbine_Table.id\n"+
				"      FROM Turbine_Table[3]\n"+
				"      GROUP BY Turbine_Table.id) AS S2\n"+
				"WHERE S1.id = S2.id AND dist(S1.value,S2.value) <= 10";
				
			String hamql5 =
				"CREATE TABLE MyTable ( a1 Long, b1 Long)";

			String hamql6 =
				"MASTER Turbine\n"+
				"INSERT INTO MyTable\n"+
				"SELECT * \n"+
				"FROM Turbine[3,now]\n";

			String hamql7 = "DROP TABLE MyTable";

			String hamql8 = 
				"MASTER Clock_1minute SELECT dist(S1.value, S2.value) FROM ( SELECT array(Turbine.value) AS value FROM Turbine[60, now] ) AS S1, ( SELECT array(Turbine_Table.value) AS value FROM Turbine_Table[60,120] ) AS S2";

			String hamql9 = 
				"MASTER R "+
				"SELECT * "+
				"FROM R[1] UNION ALL SELECT * FROM S[1]";//, B[1],C[1] "+
				//"WHERE B.d <= R.d";
				//"WHERE dist(R.a,R.b) = 10 AND dist(R.c,R.d) = 12";
				//"WHERE R.id < 10 AND R.a = B.a AND R.b < 1000 AND R.c = B.c AND B.d = 100 AND R.c = 500 AND B.c = 111 AND B.d <= R.d";
				//"MASTER R SELECT R.* FROM R";

			HamQLParser parser = HarmonicaManager.getHamQLParser();
			HamQLQueryTree tree = parser.parse(hamql9);
			HamQLQuery query = tree.getQuery();
			System.out.println(tree);

			System.out.println
				(" [CREATE] "+query.getCreateClause());
			System.out.println
				("   [DROP] "+query.getDropClause());
			System.out.println
				(" [MASTER] "+query.getMasterClause());
			System.out.println
				(" [INSERT] "+query.getInsertClause());
			System.out.println
				(" [SELECT] "+query.getSelectClause());
			System.out.println
				("   [FROM] "+query.getFromClause());
			System.out.println
				("  [WHERE] "+query.getWhereClause());
			System.out.println
				("  [GROUP] "+query.getGroupClause());
			List<HamQLQuery> uqs = query.getUnions();
			for(HamQLQuery uq : uqs){
				System.out.println("  [UNION] x-----------x");
				System.out.println
					(" [SELECT] "+uq.getSelectClause());
				System.out.println
					("   [FROM] "+uq.getFromClause());
				System.out.println
					("  [WHERE] "+uq.getWhereClause());
				System.out.println
					("  [GROUP] "+uq.getGroupClause());
			}
	
			System.out.println("------------------");
			System.out.println(query);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
