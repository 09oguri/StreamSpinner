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
package org.streamspinner.harmonica.query.hamql;

import org.streamspinner.harmonica.*;

import java.net.*;

/**
 * HamQL問合せを解析して，HamQLQueryオブジェクトを生成する．<BR>
 * ID = {シーケンスID}@{ローカルIPアドレス}#{インスタンス生成時間}
 *
 * <PRE>
 * 1.1.0 2006.8.3 シングルトンにして，問合せに一意のIDを設定するように変更．
 * </PRE>
 * @author snic
 * @version 1.0.2 (2006.8.1)
 */
public class HamQLParser {
	private static HamQLParser hamql_parser = new HamQLParser();
	private int current_number = 0;
	private String address = null;
	private long start_time = -1;
	private HamQLParser(){
		Initialize();
	}
	private void Initialize(){
		debug("Initializing HamQLParser.");
		try{
			address = InetAddress.getLocalHost().getHostAddress();
			start_time = System.currentTimeMillis();
		}catch(UnknownHostException e){
			HarmonicaManager.createdException(new HarmonicaException(e));
			address = "127.0.0.1";
		}
	}
	/**
	 * インスタンスを取得する．
	 *
	 * @return HamQLParserインスタンス
	 */
 	public static HamQLParser getInstance(){ return hamql_parser; }
	/**
	 * 入力されたHamQL問合せ記述からHamQLQueryTreeオブジェクトを生成する．
	 *
	 * @param hamql_statement HamQL問合せ記述
	 * @return HamQLQueryTreeオブジェクト
	 */
	public HamQLQueryTree parse(String hamql_statement) 
	throws HarmonicaException{
		HamQLQuery hamql = parseHamQL(hamql_statement);
		hamql.setID(getNextID());
		HamQLQueryTree tree = createTree(hamql);

		return tree;
	}

	private String getNextID(){
		String str = ++current_number + "@" + address + "#" + start_time;
		debug(str);
		return str;
	}
	
	private HamQLQueryTree createTree(HamQLQuery hamql)
	throws HarmonicaException{
		HamQLQueryTreeCreator creator = new HamQLQueryTreeCreator(hamql);
		creator.create();

		return creator.getTree();
	}
	
	private HamQLQuery parseHamQL(String hamql_statement)
	throws HarmonicaException{
		try{
			InnerParser parser = new InnerParser(hamql_statement);
			parser.parse();
			return parser.getQuery();
		}catch(Exception e){
			//throw new HarmonicaException("parse error.");
			throw new HarmonicaException(e);
		}
	}
	
	private void debug(Object o){
		HarmonicaManager.debug("HamQLParser",o);
	}
}
