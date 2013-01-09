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
import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.*;
import org.streamspinner.connection.*;

import java.awt.event.*;
import javax.swing.*;

public class HarmonicaMonitorTest {
	public static void main(String[] args){
		HarmonicaManager.show_debug = true;
		try{
			UIManager.setLookAndFeel
				("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		//		("smooth.windows.SmoothLookAndFeel");
		//		("com.birosoft.liquid.LiquidLookAndFeel"); 
		}catch(Exception e){}

		HarmonicaMonitor m = HarmonicaManager.getHarmonicaMonitor();
		m.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				HarmonicaManager.terminate();
				System.exit(0);
			}
		});

		m.setVisible(true);

		FeasibilityValidator validator = 
		HarmonicaManager.getFeasibilityValidator();

		String query = 
				"MASTER Turbine SELECT A.id, B.id, C.id FROM A, B, C WHERE A.id = B.id";

		String query2 = 
				"MASTER Turbine SELECT test_t.value, B.id, C.id FROM test_t, B, C WHERE dist(C.value,B.value) >= 30";
		String query3 ="MASTER Clock_1minute SELECT S1.time, dist(S1.value, S2.value) FROM ( SELECT exectime(Turbine.timestamp) AS time, array(Turbine.value), Turbine.id FROM Turbine[3,now] GROUP BY Turbine.id ) AS S1, ( SELECT array(Turbine_Table.value), Turbine_Table.id FROM Turbine_Table[3,now-100] GROUP BY Turbine_Table.id ) AS S2 WHERE S1.id = S2.id";
		try{
			ValidatingResult result = validator.getResultAfterValidate(query);
			ValidatingResult result2 = validator.getResultAfterValidate(query2);
			ValidatingResult result3 = validator.getResultAfterValidate(query3);

			Thread a = new AThread(validator, result);
			a.start();

		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
	}

	private static class AThread extends Thread{
		FeasibilityValidator v;
		ValidatingResult r;
		AThread(FeasibilityValidator v1, ValidatingResult r1){
			v = v1;
			r = r1;
		}
		public void run(){
			try{
				Thread.sleep(5000);

				HarmonicaDBViewer vi = null;
				for(int i = 0; i<10; i++){
					Thread.sleep(2000);
					vi = (HarmonicaDBViewer)(HarmonicaManager.getHarmonicaMonitor().getDBViewer());
					vi.arrivedInsertData("turbine_table",null);
				}

			}catch(Exception e){}
		}
	}
}

