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
package org.streamspinner.harmonica.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.streamspinner.harmonica.*;

/**
 * <pre>
 * 設定ファイルからHarmonica用の設定を読み込み，
 * 内部オブジェクトに変換するクラス．
 *
 * 設定ファイルの記述例
 *
 * &lt;xml version="1.0" encoding="Shift_JIS" ?&gt;
 * &lt;harmonica&gt;
 *   &lt;database id="mysql(データベースの任意のid)"&gt;
 *     &lt;conf name="type" value="mysql(使用するDB)"/&gt;
 *     &lt;conf name="url" value="localhost(DBのURL)"/&gt;
 *     &lt;conf name="db" value="harmonica(DB名)"/&gt;
 *     &lt;conf name="user" value="snic(ユーザ名)"/&gt;
 *     &lt;conf name="password" value="hogehoge(DBのパスワード)"/&gt;
 *     &lt;conf name="additional_option" value="useUnicode=true&amp;charsetEncoding=SJIS(オプション)"/&gt;
 *   &lt;database/&gt;
 *   &lt;database id="..."&gt; (複数のDBも可)
 *     ...
 *   &lt;database/&gt;
 * &lt;harmonica/&gt;
 * </pre>
 *
 * @author snic@kde.cs.tsukuba.ac.jp
 * @version 1.0 (2006.7.23)
 */
public class ConfigFileReader{
	private String file_name;
	private Map<String, Map<String, String>> config;
	
	public ConfigFileReader(String path) throws HarmonicaException{
		file_name = path;
		config = new TreeMap<String, Map<String, String>>();
		init();
	}
	private void init() throws HarmonicaException{
		try{
			Map<String,String> database_config_map;
			
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			Document doc = builder.parse(new File(file_name));
			Element root = doc.getDocumentElement();

			NodeList database = root.getElementsByTagName("database");
			for(int i=0;i<database.getLength();i++){
				Element database_element = (Element)database.item(i);
				String id = database_element.getAttribute("id");
				NodeList conf = database_element.getElementsByTagName("conf");
				database_config_map = new HashMap<String,String>();
				for(int j=0;j<conf.getLength();j++){
					Element conf_element = (Element)conf.item(j);
					database_config_map.put(
						conf_element.getAttribute("name"),
						conf_element.getAttribute("value")
					);
				}
				config.put(id,database_config_map);
			}
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
	public Map<String, Map<String, String>> getConfig(){
		return config;
	}
}
