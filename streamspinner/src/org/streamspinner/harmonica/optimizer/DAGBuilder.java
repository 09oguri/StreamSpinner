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
package org.streamspinner.harmonica.optimizer;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;

public class DAGBuilder {
	private Map<String, Node> nodes = new TreeMap<String, Node>();
	private Map<Node, Vector<String>> prev_node_ids = new HashMap<Node, Vector<String>>();
	private ConfigureReader conf = null;
	private String master = null;
	
	public DAG dag = null;

	public DAGBuilder(String xml, ConfigureReader conf) throws XMLStreamException, IOException{
		getDataFromFile(xml);

		dag = new DAG(conf, master);
		for(Node n : prev_node_ids.keySet()){
			Vector<String> v = prev_node_ids.get(n);
			if(v != null)
			for(String s : v){
				dag.addNode(s,n);
			}
		}

		dag.completeDAG();
	}

	public void printNextNodes(){
		System.out.println("*** Prev Node Info ***");
		for(Node n : prev_node_ids.keySet()){
			System.out.println(prev_node_ids.get(n) + "=>" + n);
		}
		System.out.println();
	}

	public void printNodes(){
		System.out.println("*** Node Info ***");
		for(String s : nodes.keySet()){
			System.out.println("id="+s + " node=" + nodes.get(s));
		}
		System.out.println();
	}

	private void getDataFromFile(String xml) throws XMLStreamException, IOException{
		/*		
		BufferedInputStream stream = 
			new BufferedInputStream(new FileInputStream(fname));
		*/
		BufferedReader stream = new BufferedReader(new StringReader(xml));
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLStreamReader reader = f.createXMLStreamReader(stream);

		while(reader.hasNext()){
			int type = reader.getEventType();

			if(type == XMLStreamConstants.START_ELEMENT){
				String tag = reader.getLocalName();

				if(tag.equals("plan")){
					for(int i=0;i<reader.getAttributeCount(); i++){
						if(reader.getAttributeLocalName(i).equals("master")){
							master = reader.getAttributeValue(i);
							break;
						}
					}
				// source
				}else if(tag.equals("source")){
					Node n = new Node();
					n.type = "source";
					Vector<String> sv = new Vector<String>();
					sv.add("-1");
					prev_node_ids.put(n,sv);

					int scount = reader.getAttributeCount();
					for(int i=0;i<scount;i++){
						String name = reader.getAttributeLocalName(i);
						String val = reader.getAttributeValue(i);
						if(name.equals("id")){ 
							nodes.put(val,n);
							n.id = val;
						}
						else if(name.equals("name")){ n.value = val; }
						else if(name.equals("window")){ n.window = val; }
					}

					reader.next();

					while(reader.hasNext()){
						int stype = reader.getEventType();
	
						if(stype == XMLStreamConstants.END_ELEMENT)
							if(reader.getLocalName().equals("source")){
							   	break;
							}

						reader.next();
					}

				// operator
				}else if(tag.equals("operator")){
					Node n = new Node();

					// operator-attribute
					int ocount = reader.getAttributeCount();
					for(int i=0;i<ocount;i++){
						String name = reader.getAttributeLocalName(i);
						String val = reader.getAttributeValue(i);
						if(name.equals("id")){ 
							nodes.put(val,n);
							n.id = val;
						}else if(name.equals("type")){ n.type = val; }
					}

					// operator-child
					reader.next();
					while(reader.hasNext()){
						int ctype = reader.getEventType();

						if(ctype == XMLStreamConstants.START_ELEMENT){
							String ctag = reader.getLocalName();

							if(ctag.equals("parameter")){
								boolean isPredicate = false;
								boolean isSelectivity = false;
								int occount = reader.getAttributeCount();
								for(int i=0;i<occount;i++){
									String name = reader.getAttributeLocalName(i);
									String val = reader.getAttributeValue(i);
									if(name.equals("name")){
										if(val.equals("predicate")) isPredicate = true;
										if(val.equals("selectivity")) isSelectivity = true;
									}else if(name.equals("value")){
										if(isSelectivity){
											n.selectivity = Double.parseDouble(val);
											isSelectivity = false;
											continue;
										}
										n.value = val;
									}
								}

								if(!isPredicate) 
									n.attribute = n.value;
								else 
									n.predicate = n.value;
	
							}else if(ctag.equals("input")){
								int occount = reader.getAttributeCount();
								for(int i=0;i<occount;i++){
									String val = reader.getAttributeValue(i);
									if(!prev_node_ids.containsKey(n)){
										Vector<String> v = new Vector<String>();
										v.add(val);
										prev_node_ids.put(n,v);
									}else{
										Vector<String> v = prev_node_ids.get(n);
										v.add(val);
									}
								}
							}
						}else if(ctype == XMLStreamConstants.END_ELEMENT){
							if(reader.getLocalName().equals("operator")) break;
						}

						reader.next();
					}
				}

			}

			reader.next();
		}

		reader.close();
		stream.close();
	}
}
