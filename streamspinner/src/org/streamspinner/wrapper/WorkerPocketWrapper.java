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
package org.streamspinner.wrapper;

import java.net.*;
import java.util.regex.Pattern;
import java.util.*;

import org.streamspinner.*;
import org.streamspinner.engine.*;
import org.streamspinner.query.*;
import org.streamspinner.connection.*;

import org.streamspinner.orinoco.application.*;
import org.streamspinner.orinoco.distributed.*;


public class WorkerPocketWrapper
	extends Wrapper
		implements WPOutputRowSetListener
{
	public static String PARAMETER_URL = "url";
	public static String PARAMETER_CLASS = "class";
	public static String PARAMETER_QUERIES = "queries";

	
	private WorkerPocket wp;
	private Vector<WorkerPocketAdapter> adapters = 
		new Vector<WorkerPocketAdapter>();
	private Vector<CQRowSet>	cqrs_set;
	
	public WorkerPocketWrapper( String name ) throws StreamSpinnerException{
		super( name );
	}
	
	// テーブル名を返す
	public String[] getAllTableNames(){
		Schema[] schemas = this.wp.getSchemata();
		
		String[] names = new String[ schemas.length ];
		
		for( int i = 0; i < schemas.length; i++ ){
			names[i] = schemas[i].getBaseTableNames()[0];
		}
		
		return names;
	}
	
	
	// 与えられたテーブル名のスキーマを返す
	public Schema getSchema( String tablename ){
		Schema[] schemas = this.wp.getSchemata();
		
		for( Schema s : schemas ){
			String name = s.getBaseTableNames()[0];
			if( name.equals( tablename ) == true ){
				return s;
			}
		}
		return null;
	}
	
	
	public TupleSet getTupleSet( ORNode node ){
		return null;
	}
	
	
	public void init(){
		this.wp = this.loadWorkerPocket();
		this.registQueries();
		
		this.wp.addWPOutputRowSetListener( this );
		this.wp.initialize();
	}
	
	// WorkerPocketクラスをロードする
	private WorkerPocket loadWorkerPocket(){
		try{
			URL url = new URL( this.getParameter( WorkerPocketWrapper.PARAMETER_URL ) );
			URL[] urls = { url };
			URLClassLoader loader = new URLClassLoader( urls );

			String class_str = this.getParameter( WorkerPocketWrapper.PARAMETER_CLASS );
      Class<?> c = loader.loadClass( class_str );
      WorkerPocket target = (WorkerPocket)c.newInstance();
      
      return target;
		}
		catch( Exception e ){
			e.printStackTrace();
			return null;
		}
	}
	
	// ローカルのStreamSpinnerに問合せを登録する
	private void registQueries(){
		String separator = WorkerPocketDispatcher.SPLIT_STRING;
		Pattern pat = Pattern.compile( separator );
		
		String q_str = 
			this.getParameter( WorkerPocketWrapper.PARAMETER_QUERIES );
		String[] queries = pat.split( q_str );
		
		int i = 0;
		this.cqrs_set = new Vector<CQRowSet>();
		for( String q : queries ){
			CQRowSet cq = new DefaultCQRowSet();
			cq.setCommand( q );
			cq.setUrl( "rmi://localhost/StreamSpinnerServer" );

			WorkerPocketAdapter ad = new WorkerPocketAdapter( this.wp, i++ );
			cq.addCQRowSetListener( ad );
			this.adapters.add( ad );

			try{
				cq.start();
				cqrs_set.add( cq );
			}
			catch( CQException e ){
				e.printStackTrace();
			}
		}
	}
	
	public void start(){
		this.wp.start();
	}
	
	public void stop(){
		this.wp.stop();
		
		for( CQRowSet cqrs : this.cqrs_set ){
			try{
				cqrs.stop();
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
	}
	
	synchronized public void dataReceived( WPOutputRowSet rowset ){
		try{
			long executiontime = System.currentTimeMillis();

			Schema schema = rowset.getSchema();
			String t_name = schema.getBaseTableNames()[0];
			
			OnMemoryTupleSet ts = new OnMemoryTupleSet(schema);
			
			Tuple[] tuples = rowset.getTuples();
			for( Tuple tuple : tuples ){
				tuple.setTimestamp( t_name, executiontime);
				ts.appendTuple( tuple );
			}
			ts.beforeFirst();

			deliverTupleSet(executiontime, schema.getBaseTableNames()[0], ts);

		} catch (Exception e){
			e.printStackTrace();
		}
	
	}
	
	
	
	class WorkerPocketAdapter
		implements CQRowSetListener
	{
		private WorkerPocket	wp;
		private int		query_id;
		
		public WorkerPocketAdapter( WorkerPocket _wp, int q_id ){
			this.wp = _wp;
			this.query_id = q_id;
		}
		
		public void dataDistributed( CQRowSetEvent event ){
			CQRowSet rs = (CQRowSet)event.getSource();
			WPInputRowSet in = new DefaultWPInputRowSet( rs );
			this.wp.dataDistributed( in, this.query_id );
		}
		
	}
}
