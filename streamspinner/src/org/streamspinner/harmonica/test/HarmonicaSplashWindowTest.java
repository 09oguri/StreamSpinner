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

import org.streamspinner.harmonica.gui.*;

import java.util.*;

public class HarmonicaSplashWindowTest{
	public static void main(String[] args){
		HarmonicaSplashWindow w = new HarmonicaSplashWindow();
		w.setVisible(true);

		ThreadA a = new ThreadA(w);
		a.start();
	}
}
class ThreadA extends Thread {
	private HarmonicaSplashWindow w = null;
	public ThreadA(HarmonicaSplashWindow w){
		this.w = w;
	}
	public void run(){
		try{
			sleep(5000);
			w.setText("Calcurating Insertion rate...");
			sleep(3000);
			w.setValue(25);
			w.setText("Initializing components...");
			sleep(1000);
			w.setValue(68);
			w.setText("Starting Harmonica System...");
			sleep(1000);
			w.setText("Running...");
			w.setValue(100);
			sleep(2000);
			w.dispose();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
