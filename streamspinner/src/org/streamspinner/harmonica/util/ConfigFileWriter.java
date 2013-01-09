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
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

import org.streamspinner.harmonica.*;

/**
 * <pre>
 * 内部オブジェクトからHarmonica用の設定に書き出すクラス．
 *
 * 書き出される設定ファイルの例
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
 * @version 1.0 (2006.8.11)
 */
public class ConfigFileWriter{
	private Map<String, Map<String, String>> config;
	
	public ConfigFileWriter(Map<String, Map<String, String>> config){
		this.config = config;
	}

	public void write(String file_name) throws HarmonicaException{
		Document d = createDocument();

		createXml(d);

		writeXml(d, file_name);
	}

	private void writeXml(Document d, String file) 
		throws HarmonicaException{
		try{
			StreamSource stream = null;
			URL url = getClass().getResource("/conf/harmonica/style.xsl");
			if(url != null){
				stream = new StreamSource(url.toString());
			}

			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = null;
			if(stream != null){
			   	tf = tff.newTransformer(stream);
			}else{
			   	tf = tff.newTransformer();
			}
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.transform(new DOMSource(d), new StreamResult(file));
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
	
	private void createXml(Document d){
		Element root = d.createElement("harmonica");
		d.appendChild(root);

		for(String s : config.keySet()){
			createDBElement(d, root, s, config.get(s));
		}
	}

	private void createDBElement
		(Document d, Element root, String id, Map<String, String> m){
		Element db = d.createElement("database");
		db.setAttribute("id", id);
		root.appendChild(db);
		for(String s : m.keySet()){
			Element elm = d.createElement("conf");
			elm.setAttribute("name", s);
			elm.setAttribute("value", m.get(s));
			db.appendChild(elm);
		}
	}

	private Document createDocument() throws HarmonicaException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try{
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.newDocument();
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}

	public Map<String, Map<String, String>> getConfig(){
		return config;
	}
}
