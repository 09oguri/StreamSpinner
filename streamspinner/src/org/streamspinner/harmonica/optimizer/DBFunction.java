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
package org.streamspinner.harmonica.optimizer;

import java.sql.*;
import java.util.*;

public class DBFunction{
	/**
	 * タプルサイズをDBに書いて調べる
	 * 単位は不明
	 */
	private static int num = 0;

	private static HashMap<int[], Double> map = new HashMap<int[], Double>();

	public void updateInsertionRate(ConfigureReader conf, int numOfPoint, int interval, long ms) throws Exception{
		TreeMap<Double, Double> insertion_rate = new TreeMap<Double, Double>();
		Random r = new Random();

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection
			("jdbc:mysql://localhost/harmonica", "harmonica", "harmonica");
		Statement stmt = con.createStatement();

		if(numOfPoint < 2) numOfPoint = 2;
		if(interval < 1) interval = 1;

		String table = "insertion_rate_check";
		String table_prefix = "CREATE TABLE "+table;
		String drop_prefix = "DROP TABLE "+table;
		String insert = "INSERT INTO "+table;
		String show_status = "SHOW TABLE STATUS WHERE Name='"+table;
		for(int i = 0; i < numOfPoint; i++){
			StringBuilder buf0 = new StringBuilder();
			StringBuilder buf1 = new StringBuilder();
			StringBuilder buf2 = new StringBuilder();
			StringBuilder buf3 = new StringBuilder();
			
			buf0.append(table_prefix + i + " (");
			buf1.append(drop_prefix + i);
			String insert_prefix = insert + i + " VALUES(";
			buf3.append(show_status + i + "'");

			for(int j = 0; j < i*interval+interval; j++){
				if(j != 0) buf0.append(",");
				buf0.append("i"+j+" LONG");
				buf0.append(",d"+j+" DOUBLE");
				buf0.append(",s"+j+" LONGTEXT");
			}
			buf0.append(")");
			
			try{
				// DROP TABLE
				stmt.executeUpdate(buf1.toString());
			}catch(Exception e){}

			// CREATE TABLE
			stmt.executeUpdate(buf0.toString());
			//System.out.println(buf0.toString());

			long start = System.currentTimeMillis();

			int count = 0;
			int rval = r.nextInt();
			/*
			buf2 = new StringBuilder();
			buf2.append(insert_prefix);
			for(int j = 0; j < i*interval+interval; j++){
				if(j != 0) buf2.append(",");
				buf2.append(rval-1);
			}
			buf2.append(")");
			*/
			while(true){
				buf2 = new StringBuilder();
				buf2.append(insert_prefix);
				for(int j = 0; j < i*interval+interval; j++){
					if(j != 0) buf2.append(",");
					buf2.append(r.nextLong());
					buf2.append(","+r.nextDouble());
					buf2.append(",'"+r.nextDouble()+"'");
				}
				buf2.append(")");
				if(start + ms > System.currentTimeMillis()){
					try{
					stmt.executeUpdate(buf2.toString());
					count++;
					}catch(Exception e){
						System.out.println(buf2);
						throw e;
					}
				}else{
					break;
				}
			}

			ResultSet rs = stmt.executeQuery(buf3.toString());

			double result = 0;
			while(rs.next()){
				result = rs.getDouble("Avg_row_length");
			}

			double rate = (count*1000)/(double)ms;
			insertion_rate.put(result,rate);

			System.out.println(result+ " " + count + "*1000/" + ms + "="+ rate);

			// DROP TABLE
			stmt.executeUpdate(buf1.toString());
		}

		stmt.close();
		con.close();

		conf.insertion_rate = insertion_rate;

		for(double size : insertion_rate.keySet()){
			System.out.println("writing rate="+size+":"+insertion_rate.get(size));
		}
	}

	public double tupleSize(SchemaInformation s) throws Exception{

		int[] data_num = new int[]{0,0,0,0};
		for(int n = 0; n < s.types.length; n++){
			if(s.types[n].equals("Double")) data_num[0]++;
			if(s.types[n].equals("Long")) data_num[1]++;
			if(s.types[n].equals("String")) data_num[2]++;
			if(s.types[n].equals("Object")) data_num[3]++;
		}

		int[] tmp = null;
		for(int[] str : map.keySet()){
			if(str[0] == data_num[0] && str[1] == data_num[1] &&
				str[2] == data_num[2] && str[3] == data_num[3]){
				tmp = str;
				break;
			}
		}
		if(tmp != null) return map.get(tmp);

		num++;
		StringBuilder buf0 = new StringBuilder();
		StringBuilder buf1 = new StringBuilder();
		StringBuilder buf2 = new StringBuilder();
		StringBuilder buf3 = new StringBuilder();
		String table = "tuple_size_table"+num;
		String query = "INSERT INTO "+table+" VALUES(";
		buf0.append("CREATE TABLE "+table+" (");
		buf2.append("DROP TABLE "+table);
		buf3.append("SHOW TABLE STATUS WHERE Name = '"+table+"'");

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection
			("jdbc:mysql://localhost/harmonica", "harmonica", "harmonica");
		Statement stmt = con.createStatement();

		for(int i =0; i<s.names.length;i++){
			if(i != 0){
			   	buf0.append(",");
			}
			String type = null;
			if(s.types[i].equals("Double")){
				type = "DOUBLE";
			}else  if(s.types[i].equals("Long")){
				type = "LONG";
			}else  if(s.types[i].equals("String")){
				type = "VARCHAR(255)";
			}else  if(s.types[i].equals("Object")){
				type = "BLOB";
			}
			buf0.append(s.names[i].replace('.','_')+ " " + type);
		}
		buf0.append(")");

		System.out.println("Calculating tuple size : "+buf0.toString());

		try{
			stmt.executeUpdate(buf2.toString());
		}catch(Exception e){}
		stmt.executeUpdate(buf0.toString());

		Random rand = new Random();
		for(int j=0;j<500;j++){
			String value = null;
			buf1 = new StringBuilder(query);
			for(int i = 0 ; i < s.names.length; i++){
				if(i != 0) buf1.append(",");
				if(s.types[i].equals("Double")){
					value = String.valueOf(rand.nextDouble());
				}else  if(s.types[i].equals("Long")){
					value = String.valueOf(rand.nextInt());
				}else  if(s.types[i].equals("String")){
					value = "'"+String.valueOf(rand.nextDouble()*rand.nextInt())+"'";
				}else  if(s.types[i].equals("Object")){
					value = "'"+String.valueOf(rand.nextDouble()*rand.nextInt())+"'";
				}
				buf1.append(value);
			}
			buf1.append(")");
			stmt.executeUpdate(buf1.toString());
		}



		ResultSet rs = stmt.executeQuery(buf3.toString());

		int result = 0;
		while(rs.next()){
			result = rs.getInt("Avg_row_length");
		}
		map.put(data_num,new Double(result));

		stmt.executeUpdate(buf2.toString());
		stmt.close();
		con.close();


		return result;
	}
}
