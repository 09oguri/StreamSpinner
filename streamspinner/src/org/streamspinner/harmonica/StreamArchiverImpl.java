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
import java.lang.reflect.*;
import java.io.File;
import java.sql.*;

import org.streamspinner.*;
import org.streamspinner.engine.*;
import org.streamspinner.query.*;
import org.streamspinner.harmonica.gui.*;
import org.streamspinner.harmonica.util.*;
import org.streamspinner.harmonica.query.*;

/**
 * ストリームデータを管理・蓄積するためのクラス．<BR>
 * <BR>
 * 内部でのTimestampの扱い：
 * <PRE>
 * 内部に作成されるテーブルには，必ずタイムスタンプを示す属性が付与される．
 * タイムスタンプはinsert()によって渡されるTupleオブジェクトに付与されて
 * いるタイムスタンプを利用する．
 * 
 * </PRE>
 * 変更履歴：
 * <PRE>
 * 1.1 2006.8.4 TupleのTimestampの扱い方の修正．
 * </PRE>
 * @author snic
 * @version 1.1 (2006.8.4)
 */
public class StreamArchiverImpl implements StreamArchiver {
	private List<DBConnector> connections;
	private Map<String, Map<String, String>> configures;
	private static String config_file_path = "conf/harmonica/";
	private static String config_file_name = "databases.xml";
	private DBConnector main_conn;
	private Vector<MainDBChangeListener> main_listeners;
	private Vector<SchemaListChangeListener> listeners;
	private Vector<InsertDataListener> idls;
	private String timestamp_attr = harmonica_timestamp;
	private int cid = 0;
	private Map<String, DBConnector> name_map = null;
	private static boolean initialized = false;
	public static int inserted_work = 0;
	public static int made_tuple_work = 0;

	private static StreamArchiverImpl archiver = new StreamArchiverImpl();
	private StreamArchiverImpl(){}
	/**
	 * StreamArchiverのインスタンスを取得する．
	 * @return StreamArchiverのインスタンス
	 */
	public static StreamArchiver getInstance(){
		if(!initialized){
		   	archiver.Initialize();
			initialized = true;
		}
		return archiver;
	}

	/**
	 * 初期化．
	 */
	private void Initialize(){
		Initialize(config_file_path + config_file_name);
	}
	/**
	 * 初期化と書込レートの計測
	 */
	private void Initialize(String file_path){
		name_map = new HashMap<String, DBConnector>();

		HarmonicaSplashWindow w = null;

		if(HarmonicaManager.show_splush_window){
			w = new HarmonicaSplashWindow();
			w.setVisible(true);
			if(HarmonicaManager.locale.equals(Locale.JAPAN))
				w.setText("StreamArchiverの初期化中");
			else
				w.setText("Initializing StreamArchiver...");
		}

		debug("Initializing StreamArchiver.");
		connections = new Vector<DBConnector>();
		listeners = new Vector<SchemaListChangeListener>();
		main_listeners = new Vector<MainDBChangeListener>();
		idls = new Vector<InsertDataListener>();

		// 設定の読み込み
		debug("Loading configure...");
		ConfigFileReader conf = null;
		try{
			conf = new ConfigFileReader(file_path);
		}catch(HarmonicaException e){}

		// 設定ファイルが存在しないときは特に何もしない
		if(conf == null){
			if(HarmonicaManager.show_splush_window) w.dispose();
			return;
		}

		if(HarmonicaManager.show_splush_window)
			w.setText("Calculatin insertion rate...");
			
		configures = conf.getConfig();
		Collection<Map<String, String>> values = configures.values();
		for(Map<String, String> config : values){
			Map<Integer, Double> m = null;
			
			try{
				m = extractInsertionRate(config.get("insertion_rate"));
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}

			try{
				addDB(
						config.get("type"),
						config.get("url"),
						config.get("db"),
						config.get("user"),
						config.get("password"),
						config.get("additional_option"),
						m
					);
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
				continue;
			}
		}

		if(HarmonicaManager.show_splush_window) w.dispose();
		
		JobMonitor.getInstance().start();
	}

	/**
	 * DBの接続の追加
	 */
	public void addDB(
			String cls, 
			String url, 
			String name, 
			String user, 
			String password, 
			String options,
			Map<Integer, Double> rate) throws HarmonicaException
	{
		DBConnector conn = null;

		try{
			conn = (DBConnector)Class.forName(cls).newInstance();
		}catch(Exception e){
			throw new HarmonicaException("No Class ("+cls+")");
		}

		conn.connect(url,name,user,password,options);
		conn.setID(String.valueOf(++cid));

		// 書き込みレートの設定
		if(rate != null) conn.setInsertionRate(rate);

		connections.add(conn);
		if(main_conn == null){
		   	main_conn = conn;
		}

		notifyDBAdded(conn);
		
		// スキーママップの作成
		List<Schema> ss = conn.getSchemaList();
		for(Schema s : ss){
			name_map.put(s.getBaseTableNames()[0].toLowerCase(), conn);
		}
			
		// 書込レートの計測
		Map rate_map = conn.getInsertionRate();
		debug(rate_map);
	}

	/**
	 * DBの接続の削除
	 */
	public void deleteDB(String db_id) throws HarmonicaException{
		DBConnector target = null;
		for(DBConnector conn : connections){
			if(conn.toString().equals(db_id)){
				target = conn;
				break;
			}
		}

		if(target == null) 
			throw new HarmonicaException("no connection ("+db_id+")");
		
		connections.remove(target);
		 
		// スキーママップから削除
		List<Schema> ss = target.getSchemaList();
		for(Schema s : ss){
			name_map.remove(s.getBaseTableNames()[0]);
		}

		notifyDBClosed(target);

		target.disconnect();

		// メインDBの変更
		if(target == main_conn){
			if(connections.size() == 0){
			   	main_conn = null;
			}else{
				main_conn = connections.get(0);
			}

			notifyMainDBChanged(target, main_conn);
		}
	}

	private Map<Integer, Double> extractInsertionRate(String rate) 
		throws HarmonicaException{
		Map<Integer, Double> m = new TreeMap<Integer, Double>();

		String[] rate_pair = rate.split(",");

		for(String s : rate_pair){
			s = s.replaceAll("\\[|\\]","");
			String[] rates = s.split(":");
			if(rates.length != 2) 
				throw new HarmonicaException("wrong insertion rate.");
			m.put(new Integer(rates[0]), new Double(rates[1]));
		}

		return m;
	}

	private void outputConfigureToFile(){
		Map<String , Map<String, String>> m = 
			new TreeMap<String, Map<String, String>>();
		int id=0;

		for(DBConnector c : connections){
			Map<String, String> mm = new TreeMap<String, String>();
			String[] init_conf = c.getInitializeInformation();
			mm.put("type",init_conf[0]);
			mm.put("url",init_conf[1]);
			mm.put("db",init_conf[2]);
			mm.put("user",init_conf[3]);
			mm.put("password",init_conf[4]);
			mm.put("additional_option",init_conf[5]);

			Map<Integer, Double> ir = c.getInsertionRate();
			StringBuilder buf = null;
			for(int i : ir.keySet()){
				if(buf == null) buf = new StringBuilder();
				else buf.append(",");

				buf.append("["+i+":"+ir.get(i)+"]");
			}
			mm.put("insertion_rate",buf.toString());

			m.put(String.valueOf(++id),mm);
		}
		
		ConfigFileWriter w = new ConfigFileWriter(m);
		try{
			File f = new File(config_file_path);
			f.mkdirs();

			w.write(config_file_path+config_file_name);
			debug("Write insertion rate to " + config_file_path);
		}catch(HarmonicaException e){
			e.printStackTrace();
			HarmonicaManager.createdException(
				new HarmonicaException
					("Can't output current insertion rate.",e));
		}
	}

	public List<DBConnector> getConnectors(){ 
		Vector<DBConnector> v = new Vector<DBConnector>();
		for(DBConnector c : connections) v.add(c);
		return v;
	}

	/**
	 * テーブルを作成する．
	 * @param table_name 作成するテーブル名
	 * @param schema 作成するテーブルのスキーマ
	 */
	public void createTable(String table_name, Schema schema) 
	throws HarmonicaException {
		if(main_conn == null) 
			throw new HarmonicaException(
					"no database connection.");

		StringBuilder sqlb = new StringBuilder(100);
		sqlb.append("CREATE TABLE ");
	   	sqlb.append(table_name);
	   	sqlb.append(" (");
		sqlb.append(timestamp_attr);
		sqlb.append(" ");
		sqlb.append(main_conn.convertDataType(DataTypes.LONG));

		for(int i=0;i<schema.size();i++){
			sqlb.append(",");
			sqlb.append(extractAttributeName(schema.getAttributeName(i)));
			sqlb.append(" "); 
			sqlb.append(main_conn.convertDataType(schema.getType(i)));
		}
		sqlb.append(")");

		StringBuilder index = new StringBuilder(100);
		index.append("CREATE INDEX ");
		index.append(table_name);
	   	index.append("_INDEX ON ");
	   	index.append(table_name);
		index.append(" ( ");
		index.append(timestamp_attr);
		index.append(" )");

		try{
			main_conn.executeUpdateNoQueue(sqlb.toString());
			name_map.put(table_name.toLowerCase(),main_conn);
		}catch(HarmonicaException e){
			String s = "Can't create " + table_name + " table";
			HarmonicaManager.createdException(e);
			throw new HarmonicaException(s);
		}

		/*
		try{
			main_conn.executeUpdateNoQueue(index.toString());

		}catch(HarmonicaException e){
			StringBuilder s = new StringBuilder();
			s.append("Can't create index against ");
			s.append(timestamp_attr);
			s.append(" attribute on ");
			s.append(table_name);
			s.append(" table.");
			throw new HarmonicaException(s.toString());
		}
		*/

		notifyCreateTable(table_name);
	}
	private String extractAttributeName(String attribute){
		String[] strs = attribute.split("\\.");
	 	if(strs.length == 1) return strs[0];
		if(strs.length >= 2) return strs[strs.length-1];
		return attribute;
	}
	/**
	 * ユーザ独自のテーブルを削除する．
	 * @param table_name 削除するテーブル名
	 */
	public void dropTable(String table_name) throws HarmonicaException{
		DBConnector con = name_map.get(table_name.toLowerCase());
		
		if(con == null) 
			throw new HarmonicaException("No Table : "+table_name);

		String sql = "DROP TABLE " + table_name;

		con.executeUpdateNoQueue(sql);
		name_map.remove(table_name.toLowerCase());
		notifyDropTable(table_name);
	}

	private class InsertedWorker extends Thread{
		public String table_name = null;
		public Vector<Tuple> tuples = null;
		public Schema schema = null;

		public InsertedWorker(String table_name, Vector<Tuple> tuples, Schema tuples_schema){
			this.table_name = table_name;
			this.tuples = tuples;
			this.schema = tuples_schema;
		}

		public void run(){
			DBConnector con = name_map.get(table_name.toLowerCase());

			try{
				if(con == null)
					throw new HarmonicaException("No Table : "+ table_name);

				StringBuilder prefix_sqlb = new StringBuilder(50);
				StringBuilder sqlb = new StringBuilder(50);
				prefix_sqlb.append("INSERT INTO " + table_name + " VALUES (");

				//Schema schema = tuples.getSchema();
				//Schema table_schema = getSchema(table_name);

				//System.out.println("tuples="+tuples);
				//System.out.println("schema="+schema);
				//System.out.println("table="+table_schema);

				// サイズチェック
				//if(schema.size() != table_schema.size())
				//	throw new HarmonicaException("Invalid schema size.");

				// 型チェック
				//for(int i=0;i<schema.size();i++){
				//	boolean b = schema.getType(i).equals(table_schema.getType(i));
				//	if(!b) throw new HarmonicaException("Invalid schema type.");
				//}

				//Vector<String> v = new Vector<String>();
				//Vector<Tuple> ts = new Vector<Tuple>();

				//tuples.beforeFirst();
				//while(tuples.next()){
				for(Tuple t : tuples){
					//Tuple t = tuples.getTuple();

					sqlb.append(prefix_sqlb);
				
					boolean b = true;//isHarmonicaTable(table_name);
					if(b) sqlb.append(t.getMinTimestamp());

					for(int i=0;i<schema.size();i++){
						if(b || i != 0) sqlb.append(",");
						if(schema.getType(i).equals(DataTypes.LONG)){
							sqlb.append(t.getLong(i));
						}else if(schema.getType(i).equals(DataTypes.DOUBLE)){
							sqlb.append(t.getDouble(i));
						}else if(schema.getType(i).equals(DataTypes.STRING)){
							sqlb.append("'"+t.getString(i)+"'");
						}else{
							sqlb.append("'"+t.getObject(i)+"'");
						}
					}
					sqlb.append(")");

					made_tuple_work++;

					//v.add(sqlb.toString());
					con.executeUpdate(sqlb.toString());
					//debug(sqlb);

					//ts.add(t);

					sqlb = new StringBuilder(50);
				}

				//tuples = new OnMemoryTupleSet(tuples.getSchema(),ts);

				//made_tuple_work++;

				//for(String s : v) con.executeUpdate(s);

				OnMemoryTupleSet ts = new OnMemoryTupleSet(schema, tuples);
				for(InsertDataListener l : idls)
					l.arrivedInsertData(table_name, ts);

			}catch(StreamSpinnerException e){
				HarmonicaManager.createdException(new HarmonicaException(e));
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}
		}
	}
	/**
	 * ユーザ独自のテーブルに値を追加する．
	 * @param table_name テーブル名
	 * @param tuples 追加するタプル集合
	 */
	public void insert(String table_name, TupleSet tuples)
	throws HarmonicaException{
		StreamArchiverImpl.inserted_work++;

		try{
			Vector<Tuple> tset = new Vector<Tuple>();
			tuples.beforeFirst();
			while(tuples.next()){
				tset.add(tuples.getTuple());
			}
		
			InsertedWorker worker = new InsertedWorker(table_name, tset, tuples.getSchema());
			worker.start();
		}catch(StreamSpinnerException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
		

		/*
		DBConnector con = name_map.get(table_name.toLowerCase());

		if(con == null)
			throw new HarmonicaException("No Table : "+ table_name);

		StringBuilder prefix_sqlb = new StringBuilder(50);
		StringBuilder sqlb = new StringBuilder(50);
		prefix_sqlb.append("INSERT INTO " + table_name + " VALUES (");

		try{
			Schema schema = tuples.getSchema();
			Schema table_schema = getSchema(table_name);

			// サイズチェック
			if(schema.size() != table_schema.size())
				throw new HarmonicaException("Invalid schema size.");

			// 型チェック
			for(int i=0;i<schema.size();i++){
				boolean b = schema.getType(i).equals
					(table_schema.getType(i));
				if(!b)
					throw new HarmonicaException("Invalid schema type.");
			}

			Vector<String> v = new Vector<String>();
			Vector<Tuple> ts = new Vector<Tuple>();

			tuples.beforeFirst();
			while(tuples.next()){
				Tuple t = tuples.getTuple();

				sqlb.append(prefix_sqlb);
				
				boolean b = isHarmonicaTable(table_name);
				if(b) sqlb.append(t.getMinTimestamp());

				for(int i=0;i<schema.size();i++){
					if(b || i != 0) sqlb.append(",");
					if(schema.getType(i).equals(DataTypes.LONG)){
						sqlb.append(t.getLong(i));
					}else if(schema.getType(i).equals(DataTypes.DOUBLE)){
						sqlb.append(t.getDouble(i));
					}else if(schema.getType(i).equals(DataTypes.STRING)){
						sqlb.append("'"+t.getString(i)+"'");
					}else{
						sqlb.append("'"+t.getObject(i)+"'");
					}
				}
				sqlb.append(")");

				v.add(sqlb.toString());
				debug(sqlb);

				ts.add(t);
			}

			tuples = new OnMemoryTupleSet(tuples.getSchema(),ts);

			for(String s : v) con.executeUpdate(s);

			for(InsertDataListener l : idls)
				l.arrivedInsertData(table_name, tuples);

		}catch(StreamSpinnerException e){
			throw new HarmonicaException(e);
		}
		*/
	}

	/**
	 * Tupleに付属するTimestampを属性として付加したTupleとして返す．
	 */
	private Tuple extractTimestampToAttribute(Tuple t) 
	throws StreamSpinnerException{
		long timestamp = t.getMinTimestamp();
		Tuple timestamp_t = new Tuple(1);
		timestamp_t.setLong(0,timestamp);

		Tuple new_t = new Tuple(timestamp_t,t);

		return new_t;
	}

	public void setMainDB(String db_id){
		debug(db_id);
		for(DBConnector con : connections){
			if(con.toString().equals(db_id)){
				debug("changing main db");
				DBConnector old = main_conn;
				main_conn = con;
				notifyMainDBChanged(old, main_conn);
				return;
			}
		}
	}

	public String getMainDB(){
		if(main_conn == null) return null;
		return main_conn.toString();
	}

	/**
	 * ストリームデータを他プル集合として取得する．
	 * ただし，起点は now．
	 *
	 * @param sources 情報源のリスト(ウインドウ付き)
	 * @param conditions 取得するタプルの条件(nullも可)
	 * @param attributes いわゆる射影演算用(nullも可)
	 */
	public TupleSet select(
			SourceSet sources,
			PredicateSet conditions,
			AttributeList attributes) throws HarmonicaException{
		
		HarmonicaSourceSet harmonica_sources = 
			new HarmonicaSourceSet(sources);

		return select(harmonica_sources, conditions, attributes);
	}

	/**
	 * ストリームデータをタプル集合として取得する．
	 *
	 * @param sources 情報源のリスト(ウインドウ付き)
	 * @param conditions 取得するタプルの条件(nullも可)
	 * @param attributes いわゆる射影演算用(nullも可)
	 */
	public TupleSet select(
			HarmonicaSourceSet sources, 
	 	 	PredicateSet conditions, 
	 	 	AttributeList attributes)
			throws HarmonicaException{

		DBConnector con = null;

		StringBuilder sqlb = new StringBuilder(100);
		StringBuilder windowb = new StringBuilder(50);
		Vector<String> harmonica_sources = null;

		sqlb.append("SELECT ");

		// SELECT節
		if(attributes != null){
			if(!attributes.isAsterisk()){
				for(int i=0;i<attributes.size();i++){
					if(i != 0) sqlb.append(", ");
					sqlb.append(attributes.getAttribute(i).toString());
				}
			}else{
				int num = 0;
				for(HarmonicaSource s : sources.getSources()){
					String source = s.getSource();
					Schema schema = getSchema(source);
					if(isHarmonicaTable(source)){
						if(harmonica_sources == null) harmonica_sources = new Vector<String>();
						harmonica_sources.add(source);
						num++;
						sqlb.append(source+"."+timestamp_attr);
					}
					for(int i=0;i<schema.size();i++){
						if(num != 0) sqlb.append(", ");
						sqlb.append(schema.getAttributeName(i));
						num++;
					}
				}
			}
		}else{
			sqlb.append("*");
		}
			
		// FROM節
		sqlb.append(" FROM ");
		int source_size=0;
		String source = null;
		for(HarmonicaSource s : sources.getSources()){
			if(source_size!=0) sqlb.append(", ");
			source_size++;
			source = s.getSource();
			if(con == null) con = name_map.get(source.toLowerCase());
			sqlb.append(source);
			long orig = s.getOriginalTime();
			long widt = s.getWindowSize();
			if(isHarmonicaTable(source)){
				if(widt > 0){
					if(!windowb.toString().equals("")){
					   	windowb.append(" AND "); 
					}

					if(orig == 0){
						orig = System.currentTimeMillis();
					}else if(orig < 0){
						orig = System.currentTimeMillis() + orig;
					}
					windowb.append(source+"."+timestamp_attr+" >= ");
					windowb.append(orig-widt);
					windowb.append(" AND "+source+"."+timestamp_attr+" <= ");
		   			windowb.append(orig);
				}
			}
		}

		if(con == null)
			throw new HarmonicaException("Not found some relations.");

		// WHERE節
		if(!windowb.toString().equals("") || 
				(conditions != null && !conditions.toString().equals("")))
			sqlb.append(" WHERE ");
		int where_i = 0;
		if(conditions != null){
			Iterator it = conditions.iterator();
			while(it.hasNext()){
				Predicate cond = (Predicate)it.next();
				if(where_i != 0) sqlb.append(" AND ");
				where_i++;
				sqlb.append(cond.getLeft());
				String op = cond.getOperator();

				if(op.equals(cond.EQ)){ op = "=";       }
				else if(op.equals(cond.GE)){ op = ">="; }
				else if(op.equals(cond.GT)){ op = ">";  }
				else if(op.equals(cond.LE)){ op = "<="; }
				else if(op.equals(cond.LT)){ op = "<";  }
				else{ op = "<>";                        }
					
				sqlb.append(" "+op+" "+cond.getRight());
			}
		}

		if(!windowb.toString().equals("")){
			if(where_i != 0) sqlb.append(" AND ");
			sqlb.append(windowb);
		}

		try{
			debug(sqlb);
			ResultSet rs = con.executeQuery(sqlb.toString());
			if(harmonica_sources == null){
				TupleSet ts = con.makeTupleSet(rs);
				rs.close();
				return ts;
			}

			Vector<Integer> attr_indeies = new Vector<Integer>();
			ResultSetMetaData meta = rs.getMetaData();
			
			Schema schema = null;
			for(int i=1; i <= meta.getColumnCount(); i++){
				String tname = meta.getTableName(i);
				String cname = meta.getColumnName(i);
				debug(tname + " " + cname);
				if(cname.toLowerCase().equals
						(harmonica_timestamp.toLowerCase())){
					attr_indeies.add(i);
				}else{
					String[] n1 = {tname+"."+cname};
					String[] t1 = 
						{DataTypes.convertSQLType(meta.getColumnType(i))};
					
					if(schema == null){
						schema = new Schema(tname, n1, t1);
					}else{
						Schema schema2 = new Schema(tname, n1, t1);
						schema = schema.concat(schema2);
					}
				}
			}

			debug(schema);
			debug(attr_indeies);

			if(schema == null) 
				throw new HarmonicaException("Schema is null.");
			
			OnMemoryTupleSet ts = new OnMemoryTupleSet(schema);

			while(rs.next()){
				Tuple t = new Tuple(schema.size());
				for(int i=1,j=0; i <= meta.getColumnCount(); i++){
					if(attr_indeies.contains(i)){
						t.setTimestamp(meta.getColumnName(i),rs.getLong(i));
						continue;
					}
					String type = schema.getType(j);
					if(type.equals(DataTypes.LONG)) 
						t.setLong(j,rs.getLong(i));
					else if(type.equals(DataTypes.DOUBLE)) 
						t.setDouble(j,rs.getDouble(i));
					else if(type.equals(DataTypes.STRING)) 
						t.setString(j,rs.getString(i));
					else 
						t.setObject(j,rs.getObject(i));
					j++;
				}
				ts.appendTuple(t);
			}

			rs.close();
			
			return ts;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}

	public boolean isHarmonicaTable(String table_name){
		Schema s = null;
		DBConnector con = name_map.get(table_name.toLowerCase());

		try{
			if(con == null)
				throw new HarmonicaException("No relation (1): "+table_name);
			s = con.getSchema(table_name);
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return false;
		}

		if(s == null) return false;

		for(int i=0;i<s.size();i++){
			String attr = s.getAttributeName(i);
			String[] names = attr.split("\\.");

			if(names.length != 2){
				continue;
			}

			if(names[1].toLowerCase().equals(timestamp_attr.toLowerCase())) 
				return true;
		}

		return false;
	}

	/**
	 * 書込レートを取得する．
	 * まだ，書込レートを計算していない場合は，内部で適当なスキーマを作成し，
	 * updateInsertionRate()により測定を行う．
	 *
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> getInsertionRate(){
		if(main_conn == null) return null;
		return main_conn.getInsertionRate();
	}

	/**
	 * 書込レートを測定する．測定する時間．
	 * @param schemas 書込レートを計算するスキーマの配列
	 * @param time 測定する時間
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas, long time){
		if(main_conn == null) return null;
		debug("recalculating insertion rate...");
		return main_conn.updateInsertionRate(schemas, time);
	}

	/**
	 * 書込レートを測定する．
	 * @param schemas 書込レートを計算するスキーマの配列
	 * @return データ量と書込レートのMap
	 */
	public Map<Integer, Double> 
	updateInsertionRate(Schema[] schemas){
		if(main_conn == null) return null;
		debug("recalculating insertion rate...");
		return main_conn.updateInsertionRate(schemas);
	}
	
	/**
	 * テーブル一覧を取得する．
	 * @return テーブル一覧
	 */
	public List<Schema> getSchemaList() throws HarmonicaException{
		Vector<Schema> v = new Vector<Schema>();
		Vector<Schema> v2 = new Vector<Schema>();

		
		for(DBConnector con : connections){
			v.addAll(con.getSchemaList());
		}

		for(Schema s : v){
			if(!isHarmonicaTable(s.getBaseTableNames()[0])){
				v2.add(s);
			}else{
				v2.add(removeTimestampAttr(s));
			}
		}

		return v2;
	}
	
	/**
	 * テーブルのスキーマを取得する．
	 * @param table_name 取得するスキーマのテーブル名
	 * @return テーブルのスキーマ
	 */
	public Schema getSchema(String table_name) throws HarmonicaException{
		DBConnector con = name_map.get(table_name.toLowerCase());

		if(con == null) return null;
		
		Schema s = con.getSchema(table_name);

		if(s == null) return null;

		if(!isHarmonicaTable(table_name)){
			s.setTableType(s.RDB);
		   	return s;
		}

		// Harmonicaテーブルのときは、timestamp_attrを削除
		return removeTimestampAttr(s);
	}

	private Schema removeTimestampAttr(Schema s){
		String[] names = new String[s.size()-1];
		String[] types = new String[s.size()-1];

		for(int i=0, j=0;i<s.size();i++){
			String attr = s.getAttributeName(i);
			String type = s.getType(i);
			if(!attr.toLowerCase().equals(
				(s.getBaseTableNames()[0]+
				 "."+
				 timestamp_attr).toLowerCase()
				)){

				names[j] = attr;
				types[j] = type;
				j++;
			}
		}

		s = new Schema(s.getBaseTableNames()[0],names,types);
		s.setTableType(s.HISTORY);

		return s;
	}

	/**
	 * テーブル1タプル辺りのデータサイズを取得する．
	 *
	 * @param table_name テーブル名
	 */
	public int getDataSize(String table_name)
	throws HarmonicaException{
		DBConnector con = name_map.get(table_name.toLowerCase());

		if(con == null)
			throw new HarmonicaException("No relation (2): "+table_name);

		Schema s = con.getSchema(table_name);
		if(s == null) return 0;

		return con.getDataSize(s);
	}

	/**
	 * テーブルのタプル数を取得する．
	 * @param table_name タプル数を取得したいテーブル名
	 * @return タプル数
	 */
	public int getNumberOfTuples(String table_name){
		DBConnector con = name_map.get(table_name.toLowerCase());

		if(con == null){
			HarmonicaManager.createdException
				(new HarmonicaException("No relation (3): "+table_name));   
			return 0;
		}

		return con.getNumberOfTuples(table_name);
	}

	/**
	 * テーブルのタプル数を取得する．
	 * @param table_name タプル数を取得したいテーブル名
	 * @return タプル数
	 */
	public int getNumberOfTuples
		(String table_name, long window_size, long original_point){

		DBConnector con = name_map.get(table_name.toLowerCase());

		if(con == null){
			HarmonicaManager.createdException
				(new HarmonicaException("No relation (3.1): "+table_name)); 
			return 0;
		}

		return con.getNumberOfTuples(table_name,window_size,original_point);
	}

	/**
	 * 終了処理を行う．
	 * DBとの接続の解放に必要．
	 */
	public void terminate() throws HarmonicaException{
		if(HarmonicaManager.write_current_insertion_rate)
			outputConfigureToFile();

		for(DBConnector conn : connections) conn.disconnect();

		JobMonitor.getInstance().start();
	}

	/**
	 * 追加されたテーブルの通知．
	 */
	private void notifyCreateTable(String table_name){
		try{
			Schema schema = getSchema(table_name);

			for(SchemaListChangeListener l : listeners){
				l.addTable(schema);
			}

		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	/**
	 * 削除されたテーブルの通知．
	 */
	private void notifyDropTable(String table_name){
		for(SchemaListChangeListener l : listeners){
			l.deleteTable(table_name);
		}
	}

	/**
	 * MainDBの変更の通知
	 */
	private void notifyMainDBChanged(DBConnector odb, DBConnector ndb){
		for(MainDBChangeListener l : main_listeners){
			l.dbChanged(odb,ndb);
		}
	}

	private void notifyDBAdded(DBConnector con){
		for(SchemaListChangeListener l : listeners)
			l.addDBMSConnection(con);
	}

	private void notifyDBClosed(DBConnector con){
		for(SchemaListChangeListener l : listeners)
			l.deleteDBMSConnection(con);
	}

	/**
	 * リスナーの登録．
	 */
	public void addSchemaListChangeListener
	(SchemaListChangeListener listener){
		this.listeners.add(listener);
	}

	/**
	 * リスナーの削除．
	 */
	public void removeSchemaListChangeListener
	(SchemaListChangeListener listener){
		this.listeners.remove(listener);
	}

	/**
	 * リスナーの登録．
	 */
	public void addMainDBChangeListener
	(MainDBChangeListener listener){
		this.main_listeners.add(listener);
	}

	/**
	 * リスナーの削除．
	 */
	public void removeMainDBChangeListener
	(MainDBChangeListener listener){
		this.main_listeners.remove(listener);
	}

	/**
	 * リスナーの登録．
	 */
	public void addInsertDataListener(InsertDataListener l){
		this.idls.add(l);
	}

	/**
	 * リスナーの削除．
	 */
	public void removeInsertDataListener(InsertDataListener l){
		this.idls.remove(l);
	}

	/**
	 * DEBUG出力用．
	 */
	private void debug(Object o){
		HarmonicaManager.debug("Archiver",o);
	}
}
