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

import java.util.*;
import java.io.*;
import javax.xml.stream.*;

public class ConfigureReader {
	public HashMap<String, Double> cost = new HashMap<String, Double>();
	public HashMap<String, Double> input_rate = new HashMap<String, Double>();
	public TreeMap<Double, Double> insertion_rate = new TreeMap<Double, Double>();
	public HashMap<String, SchemaInformation> schema = new HashMap<String, SchemaInformation>();
	public boolean isShort = true;
	
	private static final String path = "conf/harmonica/experiment/optimizer.xml";

	public ConfigureReader() throws IOException, XMLStreamException{
		XMLInputFactory f = XMLInputFactory.newInstance();

		BufferedInputStream s = 
			new BufferedInputStream(new FileInputStream(path));
		XMLStreamReader r = f.createXMLStreamReader(s);

		while(r.hasNext()){
			int type = r.getEventType();

			if(type == XMLStreamConstants.START_ELEMENT){
				if(r.getLocalName().equals("cost")){
					int size = r.getAttributeCount();
					String name = null;
					double c = -1;
					for(int i =0; i<size ;i++){
						if(r.getAttributeLocalName(i).equals("name")){
							name = r.getAttributeValue(i);
						}else if(r.getAttributeLocalName(i).equals("value")){
							c = Double.parseDouble(r.getAttributeValue(i));
						}
					}
					cost.put(name,c);
				}else if(r.getLocalName().equals("input_rate")){
					int size = r.getAttributeCount();
					String name = null;
					double c = -1;
					for(int i =0; i<size ;i++){
						if(r.getAttributeLocalName(i).equals("name")){
							name = r.getAttributeValue(i);
						}else if(r.getAttributeLocalName(i).equals("value")){
							c = Double.parseDouble(r.getAttributeValue(i));
						}
					}
					input_rate.put(name,c);
				}else if(r.getLocalName().equals("insertion_rate")){
					int size = r.getAttributeCount();
					double[] c1 = null, c2 = null;
					for(int i =0; i<size ;i++){
						if(r.getAttributeLocalName(i).equals("size")){
							String[] tmp = r.getAttributeValue(i).split(",");
							c1 = new double[tmp.length];
							for(int j=0;j<c1.length;j++){
								c1[j] = Double.parseDouble(tmp[j]);
							}
						}else if(r.getAttributeLocalName(i).equals("value")){
							String[] tmp = r.getAttributeValue(i).split(",");
							c2 = new double[tmp.length];
							for(int j=0;j<c1.length;j++){
								c2[j] = Double.parseDouble(tmp[j]);
							}
						}
					}
					if(c1 != null && c2 != null && c1.length == c2.length)
					for(int i=0;i<c1.length;i++){
						insertion_rate.put(c1[i],c2[i]);
					}
				}else if(r.getLocalName().equals("schema")){
					int size = r.getAttributeCount();
					String table_name = null;
					String[] names = null;
					String[] types = null;
					for(int i = 0; i < size; i++){
						if(r.getAttributeLocalName(i).equals("name")){
							table_name = r.getAttributeValue(i);
						}else if(r.getAttributeLocalName(i).equals("value")){
							String[] tmp1 = r.getAttributeValue(i).split(",");
							names = new String[tmp1.length];
							types = new String[tmp1.length];
							for(int j=0;j<tmp1.length;j++){
								String[] tmp2 = tmp1[j].split(":");
								names[j] = tmp2[0];
								types[j] = tmp2[1];
							}
						}
					}
					SchemaInformation sc = new SchemaInformation(new String[]{table_name}, names, types);
					schema.put(table_name,sc);
				}else if(r.getLocalName().equals("isShort")){
					if(r.getAttributeValue(0).equals("false")){
						isShort = false;
					}
				}
			}

			r.next();
		}
		r.close();
		s.close();
	}

	public SchemaInformation schema(String name){
		return schema.get(name);
	}

	public double cost(String name){
		return cost.get(name);
	}
	public double input_rate(String name){
		// “ü—ÍƒŒ[ƒg‚ª–³‚¢ê‡‚Í0 <= DB‚È‚Ç‚Ì‚Æ‚«
		if(!input_rate.containsKey(name)) return 0;
		return input_rate.get(name);
	}
	public double insertion_rate(double v){
		if(insertion_rate.containsKey(v)) return insertion_rate.get(v);

		/*
		for(Double d : insertion_rate.keySet()){
			System.out.println(d);
		}
		*/

		double[] sn = new double[2];
		double[] vn = new double[2];
		double result = -1;

		if(v < insertion_rate.firstKey()){
			int i = 0;
			for(double d : insertion_rate.keySet()){
				if(i >= 2) break;
				sn[i] = d;
				vn[i] = insertion_rate.get(d);
				i++;
			}
			result =  calcurate_insertion_rate(v, sn[0] ,vn[0], sn[1], vn[1]);
		}else if(v > insertion_rate.lastKey()){
			int i = 0, j = 0;
			for(double d : insertion_rate.keySet()){
				if(i < insertion_rate.size() - 2){
					i++;
				   	continue;
				}
				//System.out.println(d);
				sn[j] = d;
				vn[j] = insertion_rate.get(d);
				j++;
			}
			result = calcurate_insertion_rate(v, sn[1] ,vn[1], sn[0], vn[0]);
		}else{
			SortedMap<Double, Double> head = insertion_rate.headMap(v);
			SortedMap<Double, Double> tail = insertion_rate.tailMap(v);

			result = calcurate_insertion_rate
				(v, head.lastKey(),head.get(head.lastKey()), 
			 	tail.firstKey(), tail.get(tail.firstKey()));
		}

		insertion_rate.put(v,result);
		System.out.println("Tuple_size="+v+" Insertion_rate="+result);
		return result;
	}

	private double calcurate_insertion_rate
		(double target, double x1, double y1, double x2, double y2){

			//System.out.println(target + " " + x1 + " " + y1 + " " + x2 + " " + y2);

		double a = (y1-y2)/(x1-x2);
		double b = (x1*y2-x2*y1)/(x1-x2);

		return a*target+b;
	}
}
