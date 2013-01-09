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

import java.util.*;

/**
 * INSERT待ち行列の長さをモニターするクラス(実験用)
 *
 * @author snic
 * @version 1.0
 */
public class JobMonitor{
	public static int remained_job = 0;
	public static int inserted_job = 0;
	public Timer timer = new Timer();
	private static JobMonitor monitor = null;

	private JobMonitor(){
	}

	public static JobMonitor getInstance(){
		if(monitor != null) return monitor;

		monitor = new JobMonitor();
		return monitor;
	}

	public void start(){
		timer = new Timer();
		timer.scheduleAtFixedRate(new MyTask(), 0, 1000);
	}

	public void stop(){
		timer.cancel();
	}

	public static synchronized void create(){
		JobMonitor.remained_job++;
		return;
	}
	public static synchronized void done(){
		JobMonitor.remained_job--;
		JobMonitor.inserted_job++;
		return;
	}

	private class MyTask extends TimerTask{
		long original_time = 0;
		public MyTask(){
			super();
			original_time = System.currentTimeMillis();
		}

		public void run(){
			if(!HarmonicaManager.show_experiment) return;
			long current_time = System.currentTimeMillis();
			System.out.println
				("(Time:Queue:Inserted:Transformed:Require),"+
				 (current_time - original_time) + 
				 ","+JobMonitor.remained_job + 
				 ","+JobMonitor.inserted_job + 
				 ","+StreamArchiverImpl.made_tuple_work +
				 ","+StreamArchiverImpl.inserted_work);
		}
	}

}
