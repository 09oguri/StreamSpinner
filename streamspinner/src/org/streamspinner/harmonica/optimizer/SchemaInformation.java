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

public class SchemaInformation {
	public String[] names = null;
	public String[] types = null;
	public String[] table_name;
	public double tuple_size = 0;

	public SchemaInformation(String[] table_name, String[] names, String[] types){
		this.names = names;
		this.types = types;
		this.table_name = table_name;
	}

	public SchemaInformation clone(){
		SchemaInformation s = new SchemaInformation(table_name.clone(),names.clone(),types.clone());
		s.tuple_size = tuple_size;

		return s;
	}

	public SchemaInformation convertTableName(String name){
		String[] ntnames = new String[]{name};
		String[] nnames = new String[names.length];
		String[] ntypes = new String[types.length];

		for(int i = 0; i < names.length; i++){
			nnames[i] = name + "." + names[i].replace(".","_");
			ntypes[i] = types[i];
		}

		return new SchemaInformation(ntnames, nnames, ntypes);
	}

	public String toString(){
		StringBuilder buf = new StringBuilder();

		buf.append(table_name[0] + "(");
		for(int i = 0; i < names.length; i++){
			if(i != 0) buf.append(",");
			buf.append(names[i]+" "+types[i]);
		}
		buf.append(")");
		return buf.toString();
	}

	public String toShortString(){
		StringBuilder buf = new StringBuilder();

		buf.append(table_name[0] + "(");
		for(int i = 0; i < names.length; i++){
			if(i != 0) buf.append(",");
			buf.append(names[i].split("\\.")[1]);
		}
		buf.append(")");
		return buf.toString();
	}

	public SchemaInformation join(SchemaInformation s){
		String[] tmp_names = new String[s.names.length + names.length];
		String[] tmp_types = new String[s.types.length + types.length];
		String[] tmp_table_names = new String[s.table_name.length + table_name.length];

		int i=0;
		for(;i<names.length;i++){
			tmp_names[i] = names[i];
			tmp_types[i] = types[i];
		}
		for(int j=0;j<s.names.length;j++){
			tmp_names[i+j] = s.names[j];
			tmp_types[i+j] = s.types[j];
		}

		i = 0;
		for(;i<table_name.length;i++){
			tmp_table_names[i] = table_name[i];
		}
		for(int j=0;j<s.table_name.length;j++){
			tmp_table_names[i+j] = table_name[j];
		}

		return new SchemaInformation(tmp_table_names,tmp_names,tmp_types);
	}

	public SchemaInformation merge(SchemaInformation s){
		ArrayList<String> n = new ArrayList<String>();
		ArrayList<String> t = new ArrayList<String>();

		for(int i=0; i < names.length; i++){
			n.add(names[i]);
			t.add(types[i]);
		}

		for(int i=0; i< s.names.length; i++){
			if(n.contains(s.names[i])) continue;
			n.add(s.names[i]);
			t.add(s.types[i]);
		}

		SchemaInformation new_s = new SchemaInformation
			(table_name, n.toArray(new String[0]), t.toArray(new String[0]));

		return new_s;
	}

	public boolean equalsAllElements(SchemaInformation s){
		if(table_name.length != s.table_name.length) return false;
		if(names.length != s.names.length) return false;

		ArrayList<String> n = new ArrayList<String>();
		ArrayList<String> t = new ArrayList<String>();
		for(String str : names){
			n.add(str);
		}
		for(String str : types){
			t.add(str);
		}

		for(int i=0 ; i<s.names.length; i++){
			int index = n.indexOf(s.names[i]);
			if(index == -1) return false;
			if(!s.types[i].equals(t.get(index))) return false;
		}

		return true;
	}

	public SchemaInformation projection(String[] attrs){
		TreeSet<String> tables = new TreeSet<String>();
		String[] tmp_names = new String[attrs.length];
		String[] tmp_types = new String[attrs.length];

		HashMap<String, String> name_type_map = new HashMap<String, String>();
		for(int i=0;i<names.length;i++){
			name_type_map.put(names[i],types[i]);
		}

		for(int i=0;i<attrs.length;i++){
			tmp_names[i] = attrs[i];
			tmp_types[i] = name_type_map.get(attrs[i]);
			String[] stmp = attrs[i].split("\\.");
			tables.add(stmp[0]);
		}

		return new SchemaInformation(tables.toArray(new String[0]),tmp_names,tmp_types);
	}
}
