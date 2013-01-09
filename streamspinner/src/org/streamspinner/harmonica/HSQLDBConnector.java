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

import java.util.concurrent.*;
import java.util.*;
import java.sql.*;

import org.streamspinner.*;
import org.streamspinner.query.*;
import org.streamspinner.engine.*;

/**
 * HSQLDBと接続するためのクラス．<BR>
 * <BR>
 * 変更履歴：
 * <PRE>
 * </PRE>
 * @author snic
 * @version 1.0 (2006.8.13)
 */
public class HSQLDBConnector implements DBConnector {
	private int SCHEMA_HEADER_SIZE = 4; // 4 byte(仮)
	private long PROCESSING_TIME_FOR_RATE_ESTIMATE = 1000; // 1 秒(仮)

	private Connection conn = null;
	private Statement stmt = null;
	private String driver, url, db, user, password, option;
	private Map<Integer, Double> insertion_rate = null;
	private String name = null;
	private String version = null;
	private String id = null;
	private ExecutorService worker_pool = null;

	public HSQLDBConnector(){
		init();
	}

	/**
	 * コネクタの初期化
	 */
	private void init(){
		driver = "org.hsqldb.jdbcDriver";
		worker_pool = Executors.newCachedThreadPool();
	}
	
	/**
	 * HSQLDBと接続する．
	 *
	 * @param url データベースの場所
	 * @param db データベース名
	 * @param user ユーザ名
	 * @param password パスワード
	 * @param option オプション
	 */
	public void connect(
			String url, 
			String db, 
			String user, 
			String password, 
			String option) 
	throws HarmonicaException{
		this.url = url;
		this.db = db;
		this.user = user;
		this.password = password;
		this.option = option;

		if(!option.equals("")) option = "?"+option;

		String jdbc_url = "jdbc:hsqldb:file:"+db+option;
		debug(jdbc_url);
			
		try{
			Class.forName(driver);
		}catch(ClassNotFoundException e){
			throw new HarmonicaException("can't load "+driver+".");
		}
		
		try{
			conn = DriverManager.getConnection(jdbc_url, user, password);
			stmt = conn.createStatement();
			debug("Connect to HSQLDB!");

			DatabaseMetaData meta = conn.getMetaData();
			name = meta.getDatabaseProductName();
			version = meta.getDatabaseProductVersion();
			
		}catch(Exception e){
			throw new HarmonicaException("can't connect HSQLDB.");
		}
	}

	public void setInsertionRate(Map<Integer, Double> insertion_rate){
		this.insertion_rate = insertion_rate;
	}
	public String getName(){ return name; }
	public String getVersion(){ return version; }
	public String getHost(){ return url; }
	public String getID(){ return id; }
	public String[] getInitializeInformation(){
		String[] init_conf = {
			"org.streamspinner.harmonica.HSQLDBConnector",
		   	url, 
			db, 
			user, 
			password, 
			option};

		return init_conf;
	}
	public void setID(String id){ this.id = id; }
	public String toString(){
		StringBuilder buf = new StringBuilder(15);
		buf.append(name);
		buf.append("@");
		buf.append(url);
		buf.append("#");
		buf.append(id);

		return buf.toString();
	}

	/**
	 * HSQLDBとの再接続を試みる．
	 */
	public void reconnect() throws HarmonicaException {
		connect(url,db,user,password,option);
	}

	/**
	 * 問合せを実行する．
	 *
	 * @param query 更新を要求しない問合せ(select ... )．
	 * @return 問合せによって得られたタプル
	 */
	public ResultSet executeQuery(String query) throws HarmonicaException{
		ResultSet rs = null;

		try{
			rs = stmt.executeQuery(query);

			return rs;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}

	/**
	 * JDBCのResultSetからStreamSpinner用のタプルに変換する．
	 *
	 * @param rs JDBCのResultSet
	 * @return StreamSpinnerの内部形式(TupleSet)
	 */
	public TupleSet makeTupleSet(ResultSet rs) throws HarmonicaException{
		try{
			ResultSetMetaData info = rs.getMetaData();
			Schema schema = new Schema(info);

			OnMemoryTupleSet tuples = new OnMemoryTupleSet(schema);

			while(rs.next()){
				Tuple t = new Tuple(schema.size());
				for(int i=0;i<schema.size();i++){
					String type = schema.getType(i);
					if(type.equals(DataTypes.LONG)) 
						t.setLong(i,rs.getLong(i+1));
					else if(type.equals(DataTypes.DOUBLE)) 
						t.setDouble(i,rs.getDouble(i+1));
					else if(type.equals(DataTypes.STRING)) 
						t.setString(i,rs.getString(i+1));
					else 
						t.setObject(i,rs.getObject(i+1));
				}
				tuples.appendTuple(t);
			}

			return tuples;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
	
	/**
	 * 更新要求を非同期に(Queueに溜めて)実行する．<BR>
	 * 更新要求の完了を待たずしてリターンする．
	 *
	 * @param query 更新要求(create or insert or delete or drop)
	 */
	public void executeUpdate(String query) throws HarmonicaException {
		try{

			HarmonicaRequest request = new JDBCUpdateRequest(stmt,query);
			worker_pool.execute(request);
		}catch(RejectedExecutionException e){
			throw new HarmonicaException(e);
		}
	}
	/**
	 * 更新要求を同期して(Queueに溜めないで)実行する．<BR>
	 * 更新要求の完了を待ってリターンする．
	 *
	 * @param query 更新要求
	 */
	public void executeUpdateNoQueue(String query)
	throws HarmonicaException {
		try{
			stmt.executeUpdate(query);
		}catch(SQLException e){
			throw new HarmonicaException(e);
		}
	}
	/**
	 * 書込レートを取得する．
	 * まだ，1度も測定していない場合には，
	 * 任意のスキーマを使って自動的に測定を行う．
	 */
	public Map<Integer, Double> getInsertionRate(){
		if(insertion_rate != null) return insertion_rate;

		Schema[] schemas = new Schema[2];
		for(int i=0;i<schemas.length;i++){
			String table_name = "auto_created_table";
			String[] names = new String[i*30+1];
			String[] types = new String[i*30+1];
			for(int j=0;j<names.length;j++){
				names[j] = "a" + j;
				types[j] = DataTypes.LONG;
			}
			schemas[i] = new Schema(table_name,names,types);
		}

		return updateInsertionRate(schemas);
	}

	/**
	 * 書込レートの測定を行う．
	 *
	 * @param schemas スキーマの配列
	 * @return {key=バイト数，value=書込レート}が格納されたMap
	 */
	public Map<Integer, Double> updateInsertionRate(Schema[] schemas){
		return updateInsertionRate(schemas, PROCESSING_TIME_FOR_RATE_ESTIMATE);
	}

	/**
	 * 書込レートの測定を行う．
	 * 1スキーマ辺りの見積り時間を設定可能．
	 *
	 * @param schemas スキーマの配列
	 * @param time 1スキーマ辺りの見積り時間
	 * @return {key=バイト数，value=書込レート}が格納されたMap
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas, long time){
		Map<Integer, Double> rates = new TreeMap<Integer, Double>();

		for(int i=0;i<schemas.length;i++){
			String[] names = {"harmonica_rate_estimate.timstamp"};
			String[] types = {DataTypes.LONG};
			Schema s = new Schema("harmonica_rate_estimate",names,types);

			schemas[i] = s.concat(schemas[i]);
		}

		for(Schema schema : schemas){
			String table_name = "harmonica_rate_estimate";
			String timestamp_attr = "timestamp";
			StringBuilder create_tableb = new StringBuilder(100);
			StringBuilder insert_intob = new StringBuilder(100);
			String drop_table = "DROP TABLE " + table_name;

			StringBuilder insert_intoa = new StringBuilder(50);

			create_tableb.append("CREATE TABLE " + table_name + " (");
			insert_intob.append("INSERT INTO " + table_name + " VALUES (");

			create_tableb.append(timestamp_attr+" ");
			create_tableb.append(convertDataType(DataTypes.LONG));

			for(int i=0;i<schema.size();i++){
				create_tableb.append(",");
				insert_intoa.append(",");
				create_tableb.append("a"+i+" "); 
				create_tableb.append(convertDataType(schema.getType(i)));
				if(schema.getType(i).equals(DataTypes.LONG)){
					insert_intoa.append("555");
				}else if(schema.getType(i).equals(DataTypes.DOUBLE)){
					insert_intoa.append("55.5");
				}else{
					// 30 * 1000 = 30000 byte
					insert_intoa.append("'");
					for(int j=0;j<1000;j++){
						insert_intoa.append("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
					}
					insert_intoa.append("'");
				}
			}
			create_tableb.append(")");
			insert_intoa.append(")");

			long start_time;
			int count = -1;
			double rate;
			int data;

			data = getDataSize(schema);
			StringBuilder index = new StringBuilder();
			index.append("CREATE INDEX rate_INDEX ON ");
			index.append(table_name);
			index.append(" ( " + timestamp_attr + " )");
			try{
				executeUpdateNoQueue(create_tableb.toString());
				executeUpdateNoQueue(index.toString());
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
			
			start_time = System.currentTimeMillis();
			
			try{
				while(System.currentTimeMillis() - start_time < time){
					count++;
					StringBuilder buf = new StringBuilder();
					buf.append(insert_intob);
					buf.append(count);
					buf.append(insert_intoa);
					executeUpdateNoQueue(buf.toString());
				}
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
				
			rate = ( 1000 * count ) / time;
			rates.put(new Integer(data), new Double(rate));
	
			try{
				executeUpdateNoQueue(drop_table);
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
		}

		this.insertion_rate = rates;
		return this.insertion_rate;
	}

	/**
	 * DB内に存在するスキーマの一覧を取得する．
	 *
	 * @return スキーマの一覧が格納されたリスト
	 */
	public List<Schema> getSchemaList() throws HarmonicaException{
		try{
			List<Schema> schemas = new Vector<Schema>();
			
			DatabaseMetaData meta = conn.getMetaData();
			String[] types = {"TABLE","VIEW"};
			ResultSet rs = meta.getTables(null,null,null,types);
			
			while(rs.next()){
				String table_name = rs.getString(3);
				ResultSet rsc = meta.getColumns(null,null,table_name,null);
				List<String> column_names = new Vector<String>(); 
				List<String> column_types = new Vector<String>(); 
				while(rsc.next()){
					column_names.add(table_name+"."+rsc.getString(4));
					column_types.add
						(DataTypes.convertSQLType(rsc.getInt(5)));
				}
				rsc.close();
				String[] attr_names = column_names.toArray(new String[0]);
				String[] attr_types = column_types.toArray(new String[0]);

				Schema schema = new Schema(table_name,attr_names,attr_types);
				schema.setTableType(schema.HISTORY);
				schemas.add(schema);
			}
			rs.close();
			
			return schemas;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
	/**
	 * テーブルのスキーマを取得する．
	 *
	 * @param tname テーブル名
	 * @return テーブルに対応するスキーマ(テーブルが無いときはnull)
	 */
	public Schema getSchema(String tname) throws HarmonicaException{
		try{
			String table_name = tname.toUpperCase();
			Schema schema = null;
			DatabaseMetaData meta = conn.getMetaData();
			String[] types = {"TABLE", "VIEW"};
			ResultSet rs = meta.getTables(null,null,table_name,types);

			while(rs.next()){
				String name = rs.getString(3);
				ResultSet rsc = meta.getColumns(null,null,table_name,null);
				List<String> column_names = new Vector<String>(); 
				List<String> column_types = new Vector<String>(); 
				while(rsc.next()){
					column_names.add(table_name+"."+rsc.getString(4));
					column_types.add
						(DataTypes.convertSQLType(rsc.getInt(5)));
				}
				rsc.close();
				String[] attr_names = column_names.toArray(new String[0]);
				String[] attr_types = column_types.toArray(new String[0]);

				schema = new Schema(table_name,attr_names,attr_types);
				schema.setTableType(schema.HISTORY);
			}
			rs.close();
				
			return schema;
		}catch(Exception e){
			e.printStackTrace();
			throw new HarmonicaException(e);
		}
	}
	/**
	 * テーブルのタプル数をカウントする．
	 *
	 * @return テーブルのタプル数 
	 */
	public int getNumberOfTuples(String table_name){
		ResultSet rs = null;
		try{
			rs = stmt.executeQuery("SELECT count(*) FROM " + table_name);
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return 0;
		}

		int num = 0;
		try{
			while(rs.next()) num = rs.getInt(1);
			rs.close();
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return 0;
		}

		return num;
	}

	/**
	 * テーブルのタプル数をカウントする．
	 *
	 * @return テーブルのタプル数 
	 */
	public int getNumberOfTuples
		(String table_name, long window_size, long original_point){
		ResultSet rs = null;

		String tm = HarmonicaManager.getStreamArchiver().harmonica_timestamp;

		long end_time = original_point;
		if(end_time <= 0)
			end_time += System.currentTimeMillis();

		long start_time = end_time - window_size;

		try{
			rs = stmt.executeQuery(
				"SELECT count(*) FROM " + table_name +
				" WHERE "+tm+" >= " + start_time + 
				" AND "+tm+" <= " + end_time
				);
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return 0;
		}

		int num = 0;
		try{
			while(rs.next()) num = rs.getInt(1);
			rs.close();
		}catch(SQLException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			return 0;
		}

		return num;
	}


	/**
	 * HSQLDBとの接続を切断する．
	 */
	public void disconnect() throws HarmonicaException{
		try{
			if(stmt != null){
				worker_pool.shutdown();

				ResultSet rs = stmt.executeQuery("SHUTDOWN");
				rs.close();

				stmt.close();
				conn.close();
			}
			debug("Disconnect to HSQLDB!");
		}catch(Exception e){
			throw new HarmonicaException("Disconnect error.",e);
		}
	}

	public DBConnector getSelfObject(){
		return this;
	}
	
	/**
	 * Schemaからタプルのバイト量を計算する．
	 *
	 * @param schema スキーマ
	 * @return バイト量
	 */
	public int getDataSize(Schema schema){
		int data_size = SCHEMA_HEADER_SIZE;

		for(int i=0;i<schema.size();i++){
			if(schema.getType(i).equals("Long")){
				data_size += 8; // BIGINT 8 byte
			}else if(schema.getType(i).equals("Double")){
				data_size += 8; // Double 8 byte
			}else if(schema.getType(i).equals("String")){
				data_size += 65535; // TEXT 65535 byte (max)
			}else{
				data_size += 65535; // BLOB 65535 byte
			}
		}

		return data_size;
	}

	/**
	 * StreamSpinner用のデータ型をPostgreSQL用のデータ型に変換する．
	 *
	 * @param ss_type StreamSpinner用のデータ型
	 * @return PostgreSQL用のデータ型
	 */
	 public String convertDataType(String ss_type){
		if(ss_type.equals(DataTypes.STRING)) return "VARCHAR";
		if(ss_type.equals(DataTypes.LONG)) return "BIGINT";
		if(ss_type.equals(DataTypes.DOUBLE)) return "DOUBLE";
		return "BLOB";
	}

	/**
	 * DEBUG用．
	 */
	private void debug(Object o){
		HarmonicaManager.debug("HSQLDB",o);
	}
}
