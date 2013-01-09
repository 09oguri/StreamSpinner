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

import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.io.*;
import java.text.*;
import java.rmi.registry.*;

import org.streamspinner.system.*;
import org.streamspinner.connection.*;
import org.streamspinner.engine.*;
import org.streamspinner.*;

public class HarmonicaServer extends SystemManagerCUI {
	public HarmonicaServer(StreamSpinnerMainSystem ssms){
		super(ssms);
	}
	
	public static void main(String[] args){
		ServerSession server = null;
		try{
			Registry reg = LocateRegistry.createRegistry
				(Registry.REGISTRY_PORT);
			HarmonicaManager.show_splush_window = false;
			HarmonicaMainSystemImpl hms = new HarmonicaMainSystemImpl();
			HarmonicaSystemManagerCUI hsm = 
				new HarmonicaSystemManagerCUI(hms);

			hms.start();
			
			server = new ServerSession(hms);
			server.start();
			
			hsm.readInputs();
			
			server.terminate();
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(server != null) server.terminate();
			System.exit(0);
		}
	}
	
}

class ServerSession extends Thread{
	private int port = 3333;
	private ExecutorService executorService = null;
	private ServerSocket serverSocket = null;
	private boolean isTerminated = false;
	private StreamSpinnerMainSystem spinner = null;
	private Logger logger = null;

	public ServerSession(StreamSpinnerMainSystem spinner, int port){
		super();
		this.port = port;
		this.spinner = spinner;
		initialize();
	}

	public ServerSession(StreamSpinnerMainSystem spinner){
		super();
		this.spinner = spinner;
		initialize();
	}

	private void initialize(){
		try{
			logger = Logger.getLogger("harmonica.log");
			FileHandler handler = new FileHandler("harmonica.log");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
			logger.setLevel(Level.ALL);
			
			executorService = Executors.newCachedThreadPool();

			serverSocket = new ServerSocket(port);
			log("Harmonicaサーバ起動 ("+port+")");
		}catch(IOException e){
			e.printStackTrace();
			isTerminated = true;
		}
	}

	private void log(String message){
		logger.log(Level.INFO, message);
	}

	public void run(){
		try{
			while(!isTerminated){
				accept();
			}
		}catch(IOException e){
		}catch(RejectedExecutionException e){
		}
	}

	public void accept() throws IOException, RejectedExecutionException{
		Socket socket = serverSocket.accept();
		ClientSession session = new ClientSession(socket, spinner, logger);
		executorService.execute(session);
	}

	public void terminate(){
		if(isTerminated) return;

		isTerminated = true;
		if(executorService != null) executorService.shutdownNow();
		log("Harmonicaサーバ終了");
		try{
			serverSocket.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}

class ClientSession implements Runnable, CQRowSetListener, ArrivalTupleListener, MetaDataUpdateListener{
	private Socket socket = null;
	private String client_ip = null;
	private String hamql = null;
	private DefaultCQRowSet conn = null;
	private boolean isTerminated = false;
	private PrintWriter client_out = null;
	private BufferedReader client_in = null;
	private Logger logger = null;
	private StreamSpinnerMainSystem spinner = null;

	static final int SOURCE_MODE = 1;
	static final int QUIT_MODE = 2;
	static final int HELP_MODE = 3;
	static final int QUERY_MODE = 4;
	static final int SCHEMA_MODE = 5;
	static final int EVENT_MODE = 6;

	ClientSession(Socket socket, StreamSpinnerMainSystem spinner, Logger logger){
		super();

		this.socket = socket;
		this.spinner = spinner;
		this.logger = logger;
		initialize();
	}

	private void initialize(){
		client_ip = socket.getInetAddress().getHostAddress();
		log(client_ip + " が接続");

		conn = new DefaultCQRowSet();
		conn.setUrl("//localhost/StreamSpinnerServer");
		conn.addCQRowSetListener(this);
		
		try{
			client_out = new PrintWriter(socket.getOutputStream(), true);
			client_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(IOException e){
			isTerminated = true;
		}
	}

	private void terminate(){
		isTerminated = true;

		try{
			conn.stop();
			conn.removeCQRowSetListener(this);
		}catch(CQException e){
		}

		try{
			client_in.close();
			client_out.close();
			socket.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void log(String message){
		logger.log(Level.INFO, message);
	}

	public void run(){
		if(isTerminated) return;

		try{

			client_out.println("Harmonica > TYPE 'HELP' SHOW HELP");

			StringBuilder buf = null;
			String line = null;
			while((line = client_in.readLine()) != null){
				int command = getCommand(line);
				boolean isTerminated = false;

				switch(command){
					case SOURCE_MODE:
						showAllTables();
						isTerminated = true;
						break;
					case QUERY_MODE:
						registerQuery();
						isTerminated = true;
						break;
					case HELP_MODE:
						showHelp();
						isTerminated = true;
						break;
					case SCHEMA_MODE:
						showSchema();
						isTerminated = true;
						break;
					case EVENT_MODE:
						keepEvent();
						isTerminated = true;
						break;
					case QUIT_MODE:
					default:
						isTerminated = true;
				}

				if(isTerminated) break;
			}

			terminate();
		}catch(IOException e){
			e.printStackTrace();
		}

		log(client_ip + " が切断");
	}

	private int getCommand(String line){
		if(line.equals("HELP")) return HELP_MODE;
		if(line.equals("QUERY")) return QUERY_MODE;
		if(line.equals("SOURCES")) return SOURCE_MODE;
		if(line.equals("SCHEMA")) return SCHEMA_MODE;
		if(line.equals("EVENT")) return EVENT_MODE;
		return QUIT_MODE;
	}

	private void keepEvent(){
		log("keep event");
		InformationSourceManager imanager = spinner.getInformationSourceManager();
		InformationSource[] sources = imanager.getAllInformationSources();
		for(InformationSource s : sources){
			s.addArrivalTupleListener(this);
			s.addMetaDataUpdateListener(this);
		}

		try{
			String line = null;
			while((line = client_in.readLine()) != null){
				if(line.length() == 0) break;
			}
		}catch(IOException e){
			client_out.println("EXCEPTION "+e.getMessage());
		}

		for(InformationSource s : sources){
			s.removeArrivalTupleListener(this);
			s.removeMetaDataUpdateListener(this);
		}
		log("end keep event");
	}

	public void receiveTupleSet(long executiontime, String source, TupleSet ts){
		client_out.println("ARRIVAL_TUPLE_EVENT:"+source);
	}
	public void tableCreated(String wrappername, String tablename, Schema schema){
		client_out.println("TABLE_CREATE_EVENT:"+tablename);
	}
	public void tableDropped(String wrappername, String tablename){
		client_out.println("TABLE_DROP_EVENT:"+tablename);
	}

	private void showAllTables(){
		log("get all tables");
		InformationSourceManager imanager = spinner.getInformationSourceManager();
		InformationSource[] sources = imanager.getAllInformationSources();

		StringBuilder buf = new StringBuilder();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buf.append("<result>");
		for(InformationSource source : sources){
			String names[] = source.getAllTableNames();
			for(String s : names){
				buf.append("<table>");
				buf.append(s);
				buf.append("</table>");
			}
		}
		buf.append("</result>");

		client_out.println(buf.toString());
	}

	private void showSchema() throws IOException {
		try{
			log("get schema");
			String name = client_in.readLine();
			InformationSourceManager imanager = spinner.getInformationSourceManager();
			InformationSource source = imanager.getSourceFromTableName(name);
			Schema schema = source.getSchema(name);

			StringBuilder buf = new StringBuilder();
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			buf.append("<result>");
			buf.append("<schema>");
			for(int i=0; i<schema.size(); i++){
				buf.append("<column>");
				buf.append("<name>"+schema.getAttributeName(i)+"</name>");
				buf.append("<type>"+schema.getType(i)+"</type>");
				buf.append("</column>");
			}
			buf.append("</schema>");
			buf.append("</result>");

			client_out.println(buf.toString());
		}catch(StreamSpinnerException e){
			client_out.println("EXCEPTION: " + e.getMessage());		
			log(e.getMessage());
		}
	}

	private void registerQuery() throws IOException {
		StringBuilder buf = new StringBuilder();
		boolean first_loop = true;
		String line = null;
		while((line = client_in.readLine()) != null){
			if(line.length() != 0){
				if(!first_loop) buf.append(" ");
				buf.append(line);
				first_loop = false;
			}else{
				break;
			}
		}
		try{
			hamql = buf.toString();
			conn.setCommand(hamql);
			conn.start();
							
			log("exec query");
			
			while((line = client_in.readLine()) != null){
				if(line.length() == 0) break;
			}

			log("canceled query");

			conn.stop();
		}catch(CQException e){
			client_out.println("EXCEPTION: " + e.getMessage());
		}
	}

	private void showHelp(){
		StringBuilder buf = new StringBuilder();
		buf.append("QUERY	- REGIST QUERY (query_statement \\n\\n \\n)\n");
		buf.append("SOURCES	- SHOW ALL SOURCES\n");
		buf.append("SCHEMA	- SHOW SCHEMA (schema_name \\n)\n");
		buf.append("EVENT	- EVENT MONITOR MODE\n");
		buf.append("EXIT	- CLOSE CONNECTION\n");
		buf.append("HELP	- SHOW THIS MESSAGE\n");
		client_out.println(buf.toString());
	}

	public void dataDistributed(CQRowSetEvent e){
		try{			
			CQRowSet rs = (CQRowSet)e.getSource();			
			CQRowSetMetaData meta = rs.getMetaData();									
			
			String[] t_obj = new String[meta.getColumnCount()];
			String[] t_typ = new String[meta.getColumnCount()];
			String[] t_val = new String[meta.getColumnCount()];
																	
			rs.beforeFirst();			
			while(rs.next()){							
				for(int i=1;i<=meta.getColumnCount();i++){
					t_obj[i-1] = meta.getColumnName(i);
					t_typ[i-1] = meta.getColumnTypeName(i);
					if(meta.getColumnTypeName(i).equals(DataTypes.STRING)){
						t_val[i-1] = rs.getString(i);
						continue;
					}else if(meta.getColumnTypeName(i).equals(DataTypes.OBJECT)){
						t_val[i-1] = rs.getObject(i).toString();
						continue;
					}else if(meta.getColumnTypeName(i).equals(DataTypes.LONG)){
						long lval = rs.getLong(i);
						t_val[i-1] = String.valueOf(lval);
						continue;
					}else if(meta.getColumnTypeName(i).equals(DataTypes.DOUBLE)){
						double val = rs.getDouble(i);
						t_val[i-1] = String.valueOf(val);
						continue;
					}else{
						t_val[i-1] = rs.getString(i);
						continue;
					}
				}
				
				String xml_data = convertToXML(t_obj, t_typ, t_val);
				client_out.println(xml_data);
			}

		}catch(CQException ex){
			client_out.println("EXCEPTION: " + ex.getMessage());		
		}
	}

	private String convertToXML(String[] names, String[] types, String[] tuple){
		StringBuilder buf = new StringBuilder();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buf.append("<result>");
		buf.append("<schema>");
		for(int i=0; i<names.length;i++){
			buf.append("<column>");
			buf.append("<name>"+names[i]+"</name>");
			buf.append("<type>"+types[i]+"</type>");
			buf.append("</column>");
		}
		buf.append("</schema>");
		buf.append("<tuple>");
		for(int i=0; i<tuple.length; i++){
			buf.append("<data>"+tuple[i]+"</data>");
		}
		buf.append("</tuple>");
		buf.append("</result>");

		return buf.toString();
	}
}
