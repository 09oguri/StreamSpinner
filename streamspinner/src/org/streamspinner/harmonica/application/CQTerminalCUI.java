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
package org.streamspinner.harmonica.application;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.*;

import org.streamspinner.connection.*;
import org.streamspinner.*;

public class CQTerminalCUI implements CQRowSetListener{
	private int ATTR_SIZE = 16;
	private String url = null;
	private DefaultCQRowSet conn = null;
	private String[] t_obj = null;
	private String[] t_val = null;
	private boolean first_loop = true;
	private BufferedReader reader = null;
	private InputStreamReader ir = null;
	private Pattern source_pattern = null;
	private Pattern ls_pattern = null;
	
	private int mode = 0;
	private int WAITING_MODE = 0;
	private int RUNNING_MODE = 1;

	public CQTerminalCUI(String url) {
		super();

		this.url = url;
		initialize();
	}

	private void initialize() {
		source_pattern = Pattern.compile("^(source|\\\\.)\\s(.+)");
		ls_pattern = Pattern.compile("^(ls|\\\\l)\\s(.+)");

		conn = new DefaultCQRowSet();
		conn.setUrl("//"+url+"/StreamSpinnerServer");
		conn.addCQRowSetListener(this);

		StringBuilder buf = new StringBuilder();

		buf.append("Welcome to Harmonica client. Queries end with ;.\n");
		buf.append("\nType 'help;' for help.\n");

		System.out.println(buf.toString());

		ir = new InputStreamReader(System.in);
		reader = new BufferedReader(ir);
	}

	private void fire_query(String hamql) throws CQException {
		conn.setCommand(hamql);
		conn.start();
		first_loop = true;
		mode = RUNNING_MODE;
	}

	private void cancel_query() throws CQException {
		conn.stop();
		mode = WAITING_MODE;
	}

	public void terminate(){
		try{
			reader.close();
			ir.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		if(conn != null){
			try{
				if(mode == RUNNING_MODE) conn.stop();
			}catch(CQException e){
			}
		}
	}

	public void dataDistributed(CQRowSetEvent e){
		try{
			CQRowSet rs = (CQRowSet)e.getSource();
			CQRowSetMetaData meta = rs.getMetaData();

			if(first_loop)
				t_obj = new String[meta.getColumnCount()];

			t_val = new String[meta.getColumnCount()];

			rs.beforeFirst();
			while(rs.next()){
				for(int i=1;i<=meta.getColumnCount();i++){
					if(first_loop)
						t_obj[i-1] = meta.getColumnName(i);
						
					if(meta.getColumnTypeName(i).equals(DataTypes.STRING)){
						t_val[i-1] = rs.getString(i);
						continue;
					}else if(meta.getColumnTypeName(i).equals
							(DataTypes.OBJECT)){
						t_val[i-1] = rs.getObject(i).toString();
						continue;
					}else if(meta.getColumnTypeName(i).equals
							(DataTypes.LONG)){
						long lval = rs.getLong(i);
						t_val[i-1] = String.valueOf(lval);
					}else if(meta.getColumnTypeName(i).equals
							(DataTypes.DOUBLE)){
						double val = rs.getDouble(i);
						t_val[i-1] = String.valueOf(val);
					}else{
						t_val[i-1] = rs.getString(i);
						continue;
					}
				}
				if(first_loop) print_header(t_obj);

				print_data(t_val);
				first_loop = false;
			}
		}catch(CQException ce){
			ce.printStackTrace();
		}
	}

	private void print_line(int num){
		for(int i=0; i<num; i++){
			System.out.print("+");
			for(int j=0;j<ATTR_SIZE;j++) System.out.print("-");
		}
		System.out.println("+");
	}

	private void print_header(String[] header){
		System.out.println("\nPush \"Enter\" to stop this query.");
		print_line(header.length);
		print_data(header);
		print_line(header.length);
	}

	private void print_data(String[] data){
		for(int i=0; i<data.length; i++){
			System.out.print("| ");
			
			int len = (ATTR_SIZE - 2 - data[i].length()) / 2;
			if(len >= 0){
				for(int j=0;j<len;j++) System.out.print(" ");
				System.out.print(data[i]);
				for(int j=len+data[i].length();j<ATTR_SIZE-2;j++)
					System.out.print(" ");
			}else{
				System.out.print(data[i].substring(0,ATTR_SIZE-2));
			}

			System.out.print(" ");
		}
		System.out.println("|");
	}

	private void print_pronpt(){
		System.out.print("Harmonica> ");
	}

	private void pwd(){
		System.out.println(new File(".").getAbsoluteFile().getParent());
	}

	private void ls(String path){
		File dir = new File(path);
	   	File[] files = dir.listFiles();
		StringBuilder buf = new StringBuilder();

		int max_size = 0;
		int max_line = 80;
		int current_line = 0;

		for(File file : files){
			String fname = file.getName();
			int length = fname.length();
			if(file.isDirectory()) length += 1;
			if(max_size < length) max_size = length;
		}

		for(File file : files){
			String fname = file.getName();
			int length = fname.length();

			if(current_line + max_size + 2 > max_line){
				buf.append("\n");
				current_line = 0;
			}else{
				if(current_line != 0) buf.append("  ");
			}

			current_line += max_size + 2;

			buf.append(fname);

			if(file.isDirectory()){
				buf.append("/");
				length += 1;
			}

			for(int i = max_size - length; i > 0; i--){
				buf.append(" ");
			}
		}
		buf.append("\n");

		System.out.print(buf.toString());
	}

	public boolean read_cmd() throws IOException{
		String line = "";

		while(line.equals("")){
			print_pronpt();
			line = reader.readLine();
		}
			
		if(line.equals("\\h") || line.equals("help;")){
			print_usage();
			return true;
		}else if(line.equals("\\q") || 
				line.equals("quit;") || line.equals("exit;")){
			return false;
		}else if(line.equals("\\p") || line.equals("pwd")){
			pwd();
			return true;
		}else{
			Matcher matcher = ls_pattern.matcher(line);
			if(matcher.matches()){
				String path = matcher.group(2);
				ls(path);
				return true;
			}else if(line.equals("\\l") || line.equals("ls")){
				ls(".");
				return true;
			}
		}

		Matcher matcher = source_pattern.matcher(line);
		StringBuilder buf = new StringBuilder();
		if(matcher.matches()){
			String file_name = matcher.group(2);
			try{
				buf.append(read_file(file_name));
				System.out.println(buf.toString());
			}catch(IOException e){
				System.out.println(e.getMessage());
				return true;
			}
		}else{
			while(true){
				if(line.endsWith(";")){
					buf.append(line.substring(0,line.length()-1));
					break;
				}

				buf.append(line);
				buf.append(" ");

				System.out.print("        -> ");
				line = reader.readLine();
			}
		}

		try{
			fire_query(buf.toString());
			reader.readLine();
			cancel_query();
		}catch(CQException e){
			System.out.println(e.getMessage());
		}

		return true;
	}

	private String read_file(String fname) throws IOException{
		StringBuilder buf = new StringBuilder();
		FileReader fr = new FileReader(fname);
		BufferedReader reader = new BufferedReader(fr);

		String line = reader.readLine();
		while(line != null){
			buf.append(line+" ");
			line = reader.readLine();
		}
		reader.close();
		fr.close();

		return buf.toString();
	}

	private void print_usage(){
		StringBuilder buf = new StringBuilder();
		buf.append("For information about Harmonica services, visit:\n");
		buf.append("   http://www.streamspinner.org/harmonica/\n\n");
		buf.append("List of all Harmonica commands:\n");
		buf.append("Note that queries must be end with ';'\n");
		buf.append("exit\t(\\q) Exit harmonica. Same as quit.\n");
		buf.append("help\t(\\h) Display this help.\n");
		buf.append("quit\t(\\q) Quit harmonica.\n");
		buf.append("source\t(\\.) Execute an HamQL query file.");
	   	buf.append(" Takes a file name as an argument.\n");
		buf.append("\n");
		buf.append("ls\t(\\l) Display file list.");
	   	buf.append(" Takes a file name as an argument.\n");
		buf.append("pwd\t(\\p) Display current directory path.\n");

		System.out.println(buf);
	}

	public static void main(String[] args){
		CQTerminalCUI terminal = null;
		try{
			String ss_url = "localhost";
			if(args.length > 0) ss_url = args[0];

			terminal = new CQTerminalCUI(ss_url);

			boolean flag = true;
			while(flag) flag = terminal.read_cmd();

			System.out.println("Bye!");

		}catch(Exception e){
			System.out.println(e.getMessage());
		}finally{
			terminal.terminate();
			System.exit(0);
		}
	}

}
