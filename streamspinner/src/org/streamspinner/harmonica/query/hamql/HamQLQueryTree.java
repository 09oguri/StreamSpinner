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

import org.streamspinner.query.*;
import org.streamspinner.harmonica.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * HamQLQuery�I�u�W�F�N�g��⍇�������؂ɕϊ�����N���X�D
 * <BR><BR>
 * �����؂̋L�q��
 * <PRE>
 * &lt;?xml version="1.0" encoding="iso-8859-1" ?&gt;
 * &lt;plan master="Clock"&gt;
 *   &lt;source id="1" name="News" window="1000" /&gt;
 *   &lt;operator id="2" type="selection"&gt;
 *     &lt;parameter name="predicate" value="News.category='IT'" /&gt;
 *     &lt;input refid="1" /&gt;
 *     &lt;output refid="3" /&gt;
 *   &lt;/operator&gt;
 *   &lt;operator id="3" type="root"&gt;
 *     &lt;input refid="2" /&gt;
 *   &lt;/operator&gt;
 * &lt;/plan&gt;
 * </PRE>
 * <PRE>
 * ���Ȃ݂ɁC
 *      plan�̑����Fmaster
 *    source�̑����Fid name window window_at
 *  operator�̑����Fid type
 * parameter�̑����Fname value
 *     input�̑����Frefid
 *    output�̑����Frefid
 * ������C
 *  operator#type�l�Fselection projection join eval insertion 
 *                   create group rename root
 * parameter#name�l�Fpredicate attribute function target as table schema
 * ������D
 * </PRE>
 *
 * @author snic
 * @version 1.0 (2006.8.1)
 */
public class HamQLQueryTree {
	private HamQLQuery hamql = null;
	private Document xml = null;
	/**
	 * �������D���ɉ�������Ă��Ȃ��D
	 */
	public HamQLQueryTree(HamQLQuery hamql){
		this.hamql = hamql;
	}

	/**
	 * �⍇���؂�ݒ肷��D
	 *
	 * @param xml �⍇����
	 */
	public void setXMLDocument(Document xml){
		this.xml = xml;
	}
	/**
	 * �⍇���؂��擾����D
	 *
	 * @return �⍇����(XML)
	 */
	public Document getXMLDocument(){
		return xml;
	}
	/**
	 * �⍇���I�u�W�F�N�g���擾����D
	 *
	 * @return HamQLQuery�I�u�W�F�N�g
	 */
	public HamQLQuery getQuery(){
		return hamql;
	}
	private String convertToString() throws HarmonicaException{
		String xml_tree = "";
		try{
			StreamSource stream = null;
			URL url = getClass().getResource("/conf/harmonica/style.xsl");
			
			if(url != null){
				stream = new StreamSource(url.toString());
			}

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = null;
		
			if(stream != null){
				transformer = factory.newTransformer(stream);
			}else{
				transformer = factory.newTransformer();
			}

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(xml);
			ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(out_stream);
			transformer.transform(source,result);

			xml_tree = out_stream.toString();
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
		return xml_tree;
	}
	
	/**
	 * ������XML�̕�����ɕϊ�����D
	 *
	 * @return XML
	 */
	public String toString(){
		try{
			return convertToString();
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return "";
		}
	}
}
