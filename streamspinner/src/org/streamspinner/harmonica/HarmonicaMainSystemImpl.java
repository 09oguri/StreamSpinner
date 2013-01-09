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

import javax.swing.*;
import java.awt.event.*;
import java.rmi.*;
import java.rmi.registry.*;
import org.streamspinner.*;
import org.streamspinner.connection.*;
import org.streamspinner.gui.*;
import org.streamspinner.query.*;
import org.streamspinner.engine.*;
import org.streamspinner.system.*;
import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.util.*;
import org.streamspinner.harmonica.gui.*;

import java.io.*;
import java.util.*;
import org.w3c.dom.Document;

public class HarmonicaMainSystemImpl extends StreamSpinnerMainSystemImpl 
	implements RemoteStreamServer, WindowListener{

	private static Vector<Long> ids = new Vector<Long>();

	private class HarmonicaArchiveManager implements ArchiveManager {

		public void init() throws StreamSpinnerException { ; }
		public void start() throws StreamSpinnerException { ; }
		public void stop() throws StreamSpinnerException { ; }
		public void createTable(String table_name, Schema schema)
		throws StreamSpinnerException {
			try {
				archiver.createTable(table_name, schema);
			} catch(HarmonicaException he) {
				throw new StreamSpinnerException(he);
			}
		}

		public void dropTable(String table_name) 
		throws StreamSpinnerException{
			try {
				archiver.dropTable(table_name);
			} catch(HarmonicaException he) {
				throw new StreamSpinnerException(he);
			}
		}

		public void insert(String table_name, TupleSet tuples) 
		throws StreamSpinnerException {
			try {
				archiver.insert(table_name, tuples);
			} catch(HarmonicaException he) {
				throw new StreamSpinnerException(he);
			}
		}

		public void select(
				SourceSet sources, 
				PredicateSet conditions, 
				AttributeList attributes) 
		throws StreamSpinnerException {
			try {
				archiver.select(sources, conditions, attributes);
			} catch(HarmonicaException he) {
				throw new StreamSpinnerException(he);
			}
		}
	}

	private StreamArchiver archiver;
	private FeasibilityValidator validator;
	private HarmonicaWrapper wrapper;

	public HarmonicaMainSystemImpl() throws RemoteException {
		super();
		validator = HarmonicaManager.getFeasibilityValidator();
		archiver = HarmonicaManager.getStreamArchiver();

		try{
			wrapper = new HarmonicaWrapper("Harmonica");
		}catch(StreamSpinnerException sse){
			throw new RemoteException("", sse);
		}

		setArchiveManager(new HarmonicaArchiveManager());

		try {
			InformationSourceManager ism = getInformationSourceManager();
			ism.addInformationSource(wrapper);
		} catch(StreamSpinnerException sse){
			throw new RemoteException("", sse);
		}
	}

	protected DAG parseQuery(String cq) throws StreamSpinnerException {
		try {
			XMLDAGBuilder builder = new XMLDAGBuilder();
			if(!cq.startsWith("<?xml")){
				Document dom = validator.validateQuery(cq);
				if(dom == null) throw new HarmonicaException("Query is not feasible");

				return builder.createDAG(dom);
			}else{
				return builder.createDAG(cq);
			}
		} catch(HarmonicaException he){
			throw new StreamSpinnerException(he);
		}
	}

	public void shutdown() throws StreamSpinnerException {
		super.shutdown();
		HarmonicaManager.terminate();
	}

	//public static HarmonicaMainSystemImpl system = null;

	public static void main(String[] args){
		HarmonicaManager.show_experiment = false;
		try {
			String laf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(laf);
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

		try {
			Registry reg = 
				LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

			HarmonicaMainSystemImpl system = new HarmonicaMainSystemImpl();
			SystemManagerSwing manager = new SystemManagerSwing(system);
			JMenuBar jb = manager.getJMenuBar();
			manager.addWindowListener(system);
			
			JMenu harmonicaMenu = new JMenu("harmonica");
			JMenuItem monitorMenuItem = 
				new JMenuItem("Show Harmonica Monitor"){
					protected void processMouseEvent(MouseEvent e){
						super.processMouseEvent(e);

						if(e.getID() == MouseEvent.MOUSE_RELEASED){
							if(!SwingUtilities.isLeftMouseButton(e))
								return;

							HarmonicaMonitor m = 
								HarmonicaManager.getHarmonicaMonitor();
							if(!m.isVisible()){
							   	m.setVisible(true);
							}else{
								m.toFront();
							}
						}
					}
				};
			harmonicaMenu.add(monitorMenuItem);
			jb.add(harmonicaMenu);

			system.start();
			manager.setVisible(true);
			HarmonicaManager.getHarmonicaMonitor();

			system.registerQueries("experiment_queries/");

		} catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowClosing(WindowEvent e){
		try{
			for(Long id : ids){
				this.stopQuery(id.longValue());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}

	private void registerQueries(String dir) throws Exception{
		File fd = new File(dir);
		if(!fd.exists()) return;
		if(!fd.isDirectory()) return;

		File[] files = fd.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				if(name.endsWith(".cq")){
				   	return true;
				}
				System.out.println(name);
				return false;
			}	
		});

		for(File f : files){
			System.out.println(f);
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			StringBuilder buf = new StringBuilder();
			while(br.ready()){
				buf.append(br.readLine());
				buf.append("\n");
			}
			String query = buf.toString();
			br.close();
			fr.close();

			try{
				long id = this.startQuery(query, new ExConnection());
				ids.add(id);
			}catch(RemoteException e){
				e.printStackTrace();
			}
		}
	}

	private class ExConnection implements Connection {
		public ExConnection(){}

		public void start() throws RemoteException{}
		public void stop() throws RemoteException{}
		public void receiveSchema(Schema schema) throws RemoteException{}
		public void receiveCQException(CQException e){}
		public void receiveRecoveredUnits(long newseqno, DeliveryUnit[] units){}
		public long receiveDeliveryUnit(DeliveryUnit tuples){return tuples.getSequenceNumber();}
		public void reconnect(RemoteStreamServer rss) throws RemoteException {}
	}
}
