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
package org.streamspinner.harmonica.query;

import java.io.*;

import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.query.hamql.*;

/**
 * Harmonicaで扱う各情報源を管理するクラス．
 *
 * <PRE>
 * 1.0.1 2006.8.3 定数をenumを使って定義するようにした．
 * </PRE>
 * @author snic
 * @version 1.0.1 (2006.8.3)
 */
public class HarmonicaSource implements Serializable {
	private String source = null;
	private String rename = null;
	private long window = -1;
	private long original_time = 0;
	private boolean sliding = false;
	private HamQLQuery sub_query = null;
	private SourceType type = SourceType.SOURCE;

	/**
	 * 初期化．特に何もやってなし．
	 */
	public HarmonicaSource(){}

	/**
	 * 副問合せで初期化．<br>
	 * #HarmonicaSource.getType() ⇒ SourceType.SUB_QUERY に自動設定．
	 *
	 * @param sub_query HamQL問合せ記述による副問合せ．
	 */
	public HarmonicaSource(HamQLQuery sub_query){
		setQuery(sub_query);
	}
	/**
	 * 情報源の情報で初期化．<br>
	 * ちなみに，起点(original_time)の扱い方に注意が必要．<br>
	 * original_timeの記述の仕方によって，StreamArchiverでは以下のように扱う．
	 * <pre>
	 *	original_time = 0 ⇒ nowがその都度セットされる．
	 *	original_time &lt; 0 ⇒ now+original_timeにセットされる．
	 *	original_time &gt; 0 ⇒ original_timeがそのままセットされる．
	 * </pre>
	 *
	 * @param source 情報源の名前
	 * @param window 時間ウインドウの幅
	 * @param original_time 起点となる時刻
	 */
	public HarmonicaSource(String source, long window, long original_time){
		setSource(source);
		setWindowSize(window);
		setOriginalTime(original_time);
	}

	/**
	 * 情報源か副問合せかを判定するための変数をセットする．<br>
	 * SourceType.SOURCE ⇒ 情報源<br>
	 * SourceType.SUB_QUERY ⇒ 副問合せ<br>
	 *
	 * @param type タイプ
	 */
	public void setType(SourceType type){
		this.type = type;
	}
	/**
	 * タイプを取得する．
	 *
	 * @return タイプ
	 */
	public SourceType getType(){
		return this.type;
	}
	/**
	 * ASによるリネームを設定する．
	 *
	 * @param rename リネーム
	 */
	public void setRename(String rename){
		this.rename = rename;
	}
	/**
	 * ASによって指定された別名を取得する．
	 *
	 * @return 別名(設定されていないときはnull)
	 */
	public String getRename(){
		return rename;
	}
	/**
	 * 情報源をセットする．<br>
	 * 自動的にTYPE_SOURCEが設定される．
	 *
	 * @param source 情報源の名前
	 */
	public void setSource(String source){
		this.source = source;
		this.type = SourceType.SOURCE;
	}
	/**
	 * 時間ウインドウの大きさをセットする．
	 *
	 * @param window 時間ウインドウの大きさ
	 */
	public void setWindowSize(long window){
		this.window = window;
	}
	/**
	 * 起点の時刻をセットする．
	 *
	 * @param original_time 起点の時刻
	 */
	public void setOriginalTime(long original_time){
		this.original_time = original_time;
	}
	/**
	 * 副問合せをセットする．<br>
	 * 自動的にTYPE_SUB_QUERYが設定される．
	 *
	 * @param sub_query 副問合せ
	 */
	public void setQuery(HamQLQuery sub_query){
		this.sub_query = sub_query;
		this.type = SourceType.SUB_QUERY;
	}
	/**
	 * 1度の処理でウインドウを全タプルスライディングさせるかどうかを指定する．
	 *
	 * @param sliding スライディングさせたいときはtrue
	 */
	public void setSliding(boolean sliding){ this.sliding = sliding; }
	/**
	 * スライディングの設定を取得する．
	 *
	 * @return スライディングするときはtrue
	 */
	public boolean getSliding(){ return this.sliding; }
	/**
	 * 情報源の名前を取得する．
	 *
	 * @return 情報源の名前;
	 */
	public String getSource(){ return source; }
	/**
	 * 時間ウインドウの大きさを取得する．
	 *
	 * @return 時間ウインドウの大きさ
	 */
	public long getWindowSize(){ return window; }
	/**
	 * 起点となる時刻を取得する．
	 *
	 * @return 起点となる時刻
	 */
	public long getOriginalTime(){ return original_time; }
	/**
	 * 副問合せを取得する．
	 *
	 * @return 副問合せオブジェクト
	 */
	public HamQLQuery getQuery(){ return sub_query; }
	/**
	 * 情報源を文字列に変換する．
	 *
	 * @return 情報源のHamQL的表現
	 */
	public String toString(){
		String str = "";
		if(type == SourceType.SOURCE){
			str = source+"["+window;
			if(!sliding){
			   	str += ",";
				if(original_time == 0) str += "now";
				else if(original_time < 0) str += "now" + original_time;
				else str += original_time;
			}
			str += "]";
		}else if(type == SourceType.SUB_QUERY){
			str = "(" + sub_query + ")";
		}
		if(rename != null) str += " AS " + rename;
		return str;
	}
}
