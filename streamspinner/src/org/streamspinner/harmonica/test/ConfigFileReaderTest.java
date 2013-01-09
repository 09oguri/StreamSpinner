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
package org.streamspinner.harmonica.test;

import java.util.*;

import org.streamspinner.harmonica.util.*;
import org.streamspinner.harmonica.*;

public class ConfigFileReaderTest {
	public static void main(String[] args){
		try{
			// 絶対パス
			ConfigFileReader cfr = new ConfigFileReader("C:\\cygwin\\home\\snic\\harmonica\\conf\\harmonica\\harmonica.conf");
			Map v = cfr.getConfig();
			System.out.println(v);

			// 相対パス
			ConfigFileReader cfr2 = new ConfigFileReader("conf\\harmonica\\harmonica.conf");
			Map v2 = cfr2.getConfig();
			System.out.println(v2);

			// 存在しない
			ConfigFileReader cfr3 = new ConfigFileReader("hoge.conf");
			Map v3 = cfr3.getConfig();
			System.out.println(v3);

		}catch(HarmonicaException e){
			e.printStackTrace();
		}
	}
}
