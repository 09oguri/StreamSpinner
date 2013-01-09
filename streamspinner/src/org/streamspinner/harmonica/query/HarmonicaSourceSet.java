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

import org.streamspinner.harmonica.*;
import org.streamspinner.query.*;

import java.util.*;
import java.io.*;

/**
 * Harmonicaで扱う情報源を管理するためのクラス．<br>
 * 内部には，HarmonicaSourceオブジェクトを格納している．
 *
 * <PRE>
 * 変更履歴：
 * 1.1 2006.8.28 SourceSet用のコンストラクタ追加
 * </PRE>
 *
 * @author snic
 * @version 1.0 (2006.8.28)
 */
public class HarmonicaSourceSet implements Serializable {
	private Set<HarmonicaSource> sources;

	/**
	 * オブジェクトの初期化
	 */
	public HarmonicaSourceSet(){
		init();
	}

	/**
	 * StreamSpinnerのSourceSetで初期化
	 * 起点は全て now にセットされる．
	 */
	public HarmonicaSourceSet(SourceSet source){
		init();

		Iterator it = source.iterator();
		while(it.hasNext()){
			String source_name = (String)it.next();
			long window_size = source.getWindowsize(source_name);
			long window_origin = source.getWindowOrigin(source_name);

			HarmonicaSource harmonica_source = 
				new HarmonicaSource(source_name,window_size,window_origin);

			add(harmonica_source);
		}
	}

	private void init(){
		sources = new LinkedHashSet<HarmonicaSource>();
	}

	/**
	 * 情報源の追加を行う．
	 *
	 * @param source HarmonicaSourceオブジェクト
	 */
	public void add(HarmonicaSource source){
		sources.add(source);
	}

	/**
	 * 情報源のセットを取得する．<br>
	 *
	 * @return HarmonicaSourceオブジェクトのセット
	 */
	public Set<HarmonicaSource> getSources(){
		return sources;
	}

	/**
	 * HarmonicaSourceオブジェクトの内部表現を返す．<br>
	 * (例) Turbine[30], Sensor[10,now], (SELECT ...),
	 *
	 * @return FROM節
	 */
	public String toString(){
		String str = "";
		int i=0;
		for(HarmonicaSource source : sources){
			if(i != 0) str += ",";
			i++;
			str += source.toString();
		}
		return str;
	}
}
