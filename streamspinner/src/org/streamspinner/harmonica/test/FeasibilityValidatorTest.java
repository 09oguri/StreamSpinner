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

import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.gui.*;
import org.streamspinner.harmonica.validator.*;

import java.awt.event.*;
import javax.swing.*;

public class FeasibilityValidatorTest {
	public static void main(String[] args){
		ValidatingResult result = null;
		try{
			HarmonicaManager.show_splush_window = false;
			FeasibilityValidator validator = 
				HarmonicaManager.getFeasibilityValidator();

			String query =
				//"MASTER Clock_1minute"+
				//" SELECT dist(S1.value, S2.value)"+
				//" FROM ( SELECT array(Turbine.value) AS value FROM Turbine[60, now] ) AS S1,"+
            	//"      ( SELECT array(Turbine_Table.value) AS value FROM Turbine_Table[60,120] ) AS S2";
				//"MASTER Turbine SELECT * FROM Turbine[10,now]";
				//"MASTER Turbine SELECT * FROM Turbine[10,now], Turbine_Table[10, now-5]";
				//"MASTER Turbine SELECT * FROM Turbine[10,now] WHERE Turbine.id = 1 AND Turbine.value > 10";
				//"MASTER Turbine SELECT exectime(Turbine.timestamp), avg(Turbine.value) FROM Turbine[10,now]";
				//"MASTER Turbine SELECT exectime(w1.timestamp), avg(w1.value), excetime(w2.timestamp), avg(w2.value) FROM Turbine[3,now] AS w1, Turbine_Table[3, now-100] AS w2";
				//"MASTER Clock_1minute SELECT S1.time, dist(S1.value, S2.value) FROM ( SELECT exectime(Turbine.timestamp) AS time, array(Turbine.value), Turbine.id FROM Turbine[3,now] GROUP BY Turbine.id ) AS S1, ( SELECT array(Turbine_Table.value), Turbine_Table.id FROM Turbine_Table[3,now-100] GROUP BY Turbine_Table.id ) AS S2 WHERE S1.id = S2.id ";
				//"MASTER Clock_1minute SELECT S1.time, dist(S1.value, S2.value) FROM ( SELECT exectime(Turbine.timestamp) AS time, array(Turbine.value) AS value, Turbine.id FROM Turbine[3,now] GROUP BY Turbine.id ) AS S1, ( SELECT array(Turbine_Table.value) AS value, Turbine_Table.id FROM Turbine_Table[3] GROUP BY Turbine_Table.id ) AS S2 WHERE S1.id = S2.id AND dist(S1.value, S2.value) <= 10";
				//"CREATE TABLE MyTable (MyTable.id Long, MyTable.value Long)";
				//"MASTER Turbine INSERT INTO MyTable SELECT * FROM Turbine[3,now]";
				//"DROP TABLE MyTable";
				//"MASTER Turbine SELECT A.id, B.id, C.id FROM A, B, C WHERE A.id = B.id";
				"MASTER R "+
				"SELECT dist(R.a,C.a) "+
				"FROM R[1],B[1],C[1] "+
				"WHERE dist(R.a,B.a) = dist(R.c,C.c) ";
			result = validator.getResultAfterValidate(query);

		}catch(HarmonicaException e){
			e.printStackTrace();
			System.exit(1);
		}

			UIManager.LookAndFeelInfo[] info = 
				UIManager.getInstalledLookAndFeels();

			boolean b = false;
			String laf = 
				"com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			try{
				for(UIManager.LookAndFeelInfo f : info){
					if(f.getClassName().equals(laf)){
						UIManager.setLookAndFeel(f.getClassName());
					}
				}
			}catch(Exception e){}

			JFrame frame = new JFrame("TEST"){
				protected void processWindowEvent(WindowEvent e) {
					super.processWindowEvent(e);
					if(e.getID() == WindowEvent.WINDOW_CLOSING) {
						System.exit(0);
					}
				}
			};
			QueryTreePanel panel = new QueryTreePanel(80, 60, 20);
			panel.setValidatingResult(result);
			panel.setVisible(true);
			frame.getContentPane().add(panel);
			frame.setSize(panel.getWidth()+4,panel.getHeight()+50);
			frame.setVisible(true);

		HarmonicaManager.terminate();
	}
}
