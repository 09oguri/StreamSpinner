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

import org.streamspinner.system.*;
import org.streamspinner.*;
import java.rmi.registry.*;

public class HarmonicaSystemManagerCUI extends SystemManagerCUI {
	public HarmonicaSystemManagerCUI(StreamSpinnerMainSystem ssms){
		super(ssms);
	}
	public static void main(String[] args){
		try{
			Registry reg = LocateRegistry.createRegistry
				(Registry.REGISTRY_PORT);

			HarmonicaManager.show_splush_window = false;
			HarmonicaMainSystemImpl hms = new HarmonicaMainSystemImpl();
			HarmonicaSystemManagerCUI hsm = 
				new HarmonicaSystemManagerCUI(hms);

			hms.start();
			hsm.readInputs();

		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.exit(0);
		}
	}
}
