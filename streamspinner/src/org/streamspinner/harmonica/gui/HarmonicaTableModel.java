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
package org.streamspinner.harmonica.gui;

import org.streamspinner.harmonica.*;

import javax.swing.table.*;
import java.util.*;

public class HarmonicaTableModel extends DefaultTableModel {
	public HarmonicaTableModel(String[][] rows, String[] cols){
		super(rows, cols);
	}

	public Class getColumnClass(int columnindex){
		Class c = null;
		try{
			if(columnindex == 2 || columnindex == 3){
				c = Class.forName("java.lang.Boolean");
			}else{
				c =  Class.forName("java.lang.String");
			}

			return c;
		}catch(Exception e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}

		return getValueAt(0, columnindex).getClass();
	}

	public void update(int row, int col){
		Object o = super.getValueAt(row, col);

		if(((Boolean)o).booleanValue()){
			super.setValueAt(new Boolean(false), row, col);
		}else{
			super.setValueAt(new Boolean(true), row, col);
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex){
		return false;
	}
}
