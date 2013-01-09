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

import org.streamspinner.harmonica.query.*;
import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.gui.*;

import org.w3c.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import java.io.*;
import java.util.*;
import java.net.URL;

/**
 * Harmonicaを集中管理するためのクラス．<BR>
 * 主に各クラスでデバッグの出力を行うかどうか管理する．<BR>
 * デフォルトはデバッグ出力を行う．<BR>
 * <BR>
 * 変更履歴：
 * <PRE>
 * 1.1 2006.8.4 各コンポーネントのインスタンスを取得できるように変更．
 * </PRE>
 * @author snic
 * @version 1.1 (2006.12.18)
 */
public class HarmonicaManager{
	/**
	 * Harmonicaのバージョン
	 */
	public static final String version = "2.9.0 (2006.12.19)";

	/**
	 * 実験用の出力を行うかどうか
	 */
	public static boolean show_experiment = false;

	/**
	 * DEBUG出力を行うかどうか
	 */
	public static boolean show_debug = false;

	/**
	 * 例外のメッセージを出力するかどうか
	 */
	public static boolean show_exception = false;

	/**
	 * スプラッシュウインドウを表示するかしないか
	 */
	public static boolean show_splush_window = true;

	/**
	 * 計測した書き込みレートを保存するかどうか
	 */
	public static boolean write_current_insertion_rate = true;

	public static Locale locale = Locale.getDefault();

	private static boolean archiver = false;
	private static boolean monitor = false;
	private static Vector<HarmonicaExceptionListener> listeners = 
		new Vector<HarmonicaExceptionListener>();

	private HarmonicaManager(){}

	/**
	 * デバッグの出力を行う．<BR>
	 * 主に各モジュールの内部で利用する．
	 *
	 * @param module_name 出力する人の名前
	 * @param target 出力したいもの
	 */
	public static void debug(String module_name, Object target){
		if(!show_debug) return;
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [");
		buffer.append(module_name);
		buffer.append("] ");
		if(target != null) buffer.append(target);
		else buffer.append("null");

		System.out.println(buffer);
	}
	
	/**
	 * Harmonicaの例外を受け取るリスナを設定する．
	 *
	 * @param l 例外を受け取るリスナ
	 */
	public static void addHarmonicaExceptionListener
		(HarmonicaExceptionListener l){
		listeners.add(l);
	}

	/**
	 * Harmonicaの例外を受け取るリスナを削除する．
	 *
	 * @param l 例外を受け取っていたリスナ
	 */
	public static void removeHarmonicaExceptionListener
		(HarmonicaExceptionListener l){
		listeners.remove(l);
	}

	/**
	 * 例外を設定する．
	 *
	 * @param e Harmonciaで発生した例外
	 */
	public static void createdException(HarmonicaException e){
		if(show_exception){
			if(show_debug){
				e.printStackTrace();
			}else{
				System.out.println(e.getMessage());
			}
		}

		for(HarmonicaExceptionListener l : listeners){
			l.threwException(e);
		}
	}

	/**
	 * XMLを出力する
	 */
	public static void printXML(Node n){
		if(!show_debug) return;
        String xml_tree = "";
        try{
            StreamSource stream = null;
			URL url = HarmonicaManager.class.getResource
				("/conf/harmonica/style.xsl");
			if(url != null)
				stream = new StreamSource(url.toString());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = null;
			if(stream != null)
				transformer = factory.newTransformer(stream);
			else
				transformer = factory.newTransformer();

            DOMSource source = new DOMSource(n);
            ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(out_stream);
            transformer.transform(source,result);

            xml_tree = out_stream.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(xml_tree);
    }

	/**
	 * StreamArchiverインスタンスを取得する．
	 * @return StreamArchiverインスタンス
	 */
	public static StreamArchiver getStreamArchiver(){
		archiver = true;
		return StreamArchiverImpl.getInstance();
	}
	
	/**
	 * HamQLParserインスタンスを取得する．
	 * @return HamQLParserインスタンス
	 */
	public static HamQLParser getHamQLParser(){
		return HamQLParser.getInstance();
	}
	
	/**
	 * FeasibilityValidatorインスタンスを取得する．
	 * @return FeasibilityValidatorインスタンス
	 */
	public static FeasibilityValidator getFeasibilityValidator(){
		return FeasibilityValidatorImpl.getInstance();
	}

	public static HarmonicaMonitor getHarmonicaMonitor(){
		monitor = true;
		return HarmonicaMonitor.getInstance();
	}

	/**
	 * Harmonicaシステムの終了処理を行う．
	 */
	public static void terminate(){
		try{
			if(archiver){
				StreamArchiverImpl.getInstance().terminate();
				archiver = false;
			}

			if(monitor){
				HarmonicaMonitor.getInstance().terminate();
				monitor = false;
			}
		}catch(HarmonicaException e){
			createdException(e);
		}
	}

	protected void finalize() throws Throwable{
		debug("HarmonicaManager","finalize");
		terminate();
		super.finalize();
	}
}
