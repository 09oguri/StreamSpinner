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
package org.streamspinner.harmonica.query.hamql;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;

import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.query.*;
import org.streamspinner.query.*;
import org.streamspinner.engine.*;

/**
 * 問合せ木を作成するためのクラス．<BR>
 * <PRE>
 * 基本的な使い方．
 *
 * HamQLQueryTreeCreator creator = new HamQLQueryCreator(hamql);
 * creator.create();
 * HamQLQueryTree tree = creator().getTree();
 *
 * 以上．
 * </PRE>
 *
 * <PRE>
 * 1.4   2006.9.03 数々のバグを修正．
 * 1.3   2006.8.28 GROUPの修正．
 * 1.2   2006.8.24 RENAMEの修正．
 * 1.1.1 2006.8.3  DROP節対応．ボクシングの導入．
 * </PRE>
 * @author snic
 * @version 1.4 (2006.9.03)
 */
public class HamQLQueryTreeCreator {
	private HamQLQuery hamql = null;
	private HamQLQueryTree tree = null;
	private Document xml = null;
	private int cid = 0;
	private Map<Integer, String[]> id_map = null;
	private Map<String, Integer> source_map = null;
	private boolean doNeedOutputNode = true;
	private Vector<FunctionParameter> evaluated = null;
	private String projection_list = "*";

	private boolean has_group_clause = false;
	
	// RENAME用のMAP
	private Map<String, String> rename_map = null;
	private Vector<String> projection_attributes = null;

	/**
	 * HamQLQueryオブジェクトからHamQLQueryTreeオブジェクトを生成する．
	 */
	public HamQLQueryTreeCreator(HamQLQuery hamql){
		this.hamql = hamql;
		tree = new HamQLQueryTree(hamql);
		init();
	}

	/**
	 * 主に副問合せ用のコンストラクタ．<BR>
	 * 演算IDを任意の値から開始できる．
	 * @param cid 演算ID
	 * @param hamql 問合せ木を作成するHamQLQueryオブジェクト
	 */
	public HamQLQueryTreeCreator(int cid, HamQLQuery hamql){
		this.cid = cid;
		this.hamql = hamql;
		tree = new HamQLQueryTree(hamql);
		init();
		this.doNeedOutputNode = false;
	}

	/**
	 * 初期化
	 */
	private void init(){
		source_map = new HashMap<String, Integer>();
		id_map = new HashMap<Integer, String[]>();
		evaluated = new Vector<FunctionParameter>();
		rename_map = new TreeMap<String, String>();
		projection_attributes = new Vector<String>();
	}

	/**
	 * 問合せ木を作成する．
	 */
	public void create() throws HarmonicaException {
		Element[] nodes = null;
		Element node = null;

		xml = createXMLDocument();
		Element root = create_plan();
		xml.appendChild(root);
			
		// CREATE節
		if(hamql.getQueryType() == QueryType.CREATE){
			nodes = create_create();
			for(Element elm : nodes) root.appendChild(elm);

		// DROP節
		}else if(hamql.getQueryType() == QueryType.DROP){
			nodes = create_drop();
			for(Element elm : nodes) root.appendChild(elm);

		// 挿入 or 選択
		}else{

			// FROM節の解析
			nodes = create_sources();
			for(Element elm : nodes) root.appendChild(elm);

			// WHERE節の解析とFROM節の結合
			nodes = create_operators();
			if(nodes != null)
				for(Element elm : nodes) root.appendChild(elm);

			// GROUP節の解析
			node = create_group();
			if(node != null){
				root.appendChild(node);
				has_group_clause = true;
			}

			// 結合のし残りの直積
			nodes = create_cjoin();
			if(nodes != null)
				for(Element elm : nodes) root.appendChild(elm);

			// SELECT節の解析し，結合
			nodes = create_select();
			if(nodes != null)
				for(Element elm : nodes) root.appendChild(elm);


			// 最後に射影
			node = create_projection();
			if(node != null) root.appendChild(node);

			// その後リネーム
			node = create_rename();
			if(node != null) root.appendChild(node);

			// そしてUNION
			nodes = create_union();
			if(nodes != null)
				for(Element elm : nodes) root.appendChild(elm);
			
			node = create_insert();
			if(node != null){
				// 出力先がDBである
				root.appendChild(node);
			}
			
			// 出力先がユーザである
			node = create_root();
			root.appendChild(node);

			create_outid();
		}

		tree.setXMLDocument(xml);
	}

	private Element[] create_drop(){
		String s = hamql.getDropClause();

		Element e1 = createElement("operator");
		e1.setAttribute("id","1");
		e1.setAttribute("type","drop");

		Element e2 = createElement("parameter");
		e2.setAttribute("name","table");
		e2.setAttribute("value",s);

		Element e3 = createElement("output");
		e3.setAttribute("refid","2");

		Element e4 = createElement("operator");
		e4.setAttribute("id","2");
		e4.setAttribute("type","root");

		Element e5 = createElement("input");
		e5.setAttribute("refid","1");

		e1.appendChild(e2);
		e1.appendChild(e3);
		e4.appendChild(e5);

		Element[] elms = {e1,e4};

		return elms;
	}

	private Element[] create_create(){
		Schema s = hamql.getCreateClause();

		Element e1 = createElement("operator");
		e1.setAttribute("id","1");
		e1.setAttribute("type","create");

		Element e2 = createElement("parameter");
		e2.setAttribute("name","schema");
		e2.setAttribute("value",s.toString());

		Element e3 = createElement("output");
		e3.setAttribute("refid","2");

		Element e4 = createElement("operator");
		e4.setAttribute("id","2");
		e4.setAttribute("type","root");

		Element e5 = createElement("input");
		e5.setAttribute("refid","1");

		e1.appendChild(e2);
		e1.appendChild(e3);
		e4.appendChild(e5);

		Element[] elms = {e1,e4};

		return elms;
	}

	private Element[] create_union() throws HarmonicaException{
		List<HamQLQuery> union_queries = hamql.getUnions();

		if(union_queries.size() == 0) return null;

		Vector<Element> v = new Vector<Element>();

		// 1つ前に関係する情報源を取得する
		String[] sources = id_map.get(cid);

		for(HamQLQuery q : union_queries){
			// UNIONの元となる木を作成
			HamQLQueryTree union_tree = null;
			HamQLQueryTreeCreator union_creator = 
				new HamQLQueryTreeCreator(cid, q);
			Document union_document = null;
			Element union_root = null;
	
			union_creator.create();
			union_tree = union_creator.getTree();
			union_document = union_tree.getXMLDocument();
			union_root = union_document.getDocumentElement();

			NodeList list = union_root.getElementsByTagName("source");
			for(int i=0;i<list.getLength();i++){
				Element elm = (Element)list.item(i);
				elm = cloneElement(elm);
				v.add(elm);
			}

			int opeid = 0;
			int refid = 0;
			list = union_root.getElementsByTagName("operator");
			for(int i=0;i<list.getLength();i++){
				Element elm = (Element)list.item(i);
				elm = cloneElement(elm);
				if(!elm.getAttribute("type").equals("root")){
					v.add(elm);
				}else{
					opeid = Integer.parseInt(elm.getAttribute("id"));
					NodeList union_list = 
						elm.getElementsByTagName("input");
					for(int j=0;j<union_list.getLength();j++){
						Element union_elm = (Element)union_list.item(j);
						refid = Integer.parseInt
							(union_elm.getAttribute("refid"));
					}
				}
			}
	
			id_map.putAll(union_creator.getIDMap());
			int previous_cid = cid;
			cid = opeid-1;

			// UNIONを作成
			String[] names = null;
			String[] values = null;

			if(q.isUnionAll()){
				names = new String[]{
					"base_refid",
					"is_union_all"
				};
				values = new String[]{
					String.valueOf(previous_cid),
					"true"
				};
			}else{
				names = new String[]{
					"base_refid",
					"is_union_all"
				};
				values = new String[]{
					String.valueOf(previous_cid),
					"false"
				};
			}
			int[] rid = {previous_cid,cid};

			Element elm = createOperator(++cid,"union",names,values,rid);
			v.add(elm);

			id_map.put(cid, sources);

			for(String str : sources)
				refreshSourceMap(cid-1,str,cid);
		}

		return v.toArray(new Element[0]);
	}

	/**
	 * INSERT演算
	 */
	private Element create_insert(){
		Schema s = hamql.getInsertClause();
		if(s == null) return null;

		String table_name = s.getBaseTableNames()[0];

		String[] names = {"table"};
		String[] values = {table_name};
		int[] rid = {cid};

		Element elm = createOperator(++cid,"store",names,values,rid);
		
		String[] sources = id_map.get(cid-1);
		id_map.put(cid, sources);

		for(String str : sources)
			refreshSourceMap(cid-1,str,cid);

		return elm;
	}

	/**
	 * 結合できていないテーブルが残っている場合に直積をとる．
	 */
	private Element[] create_cjoin(){
		Vector<Element> v = new Vector<Element>();
		if(source_map.size() == 1) return null;

		Collection<Integer> c = source_map.values();
		Integer[] remain_id = c.toArray(new Integer[0]);
		
		String[] names = {"predicate"};
		String[] values = {""};
		for(int i=1;i<remain_id.length;i++){
			debug(remain_id[0] + " " + remain_id[i]);
			if(remain_id[0].intValue() == remain_id[i].intValue())
				continue;

			int[] rid = {
				remain_id[0].intValue(), 
				remain_id[i].intValue()
			};

			Element elm = createOperator(++cid,"join",names,values,rid);
			v.add(elm);

			String[] s1 = id_map.get(remain_id[0]);
			String[] s2 = id_map.get(remain_id[i]);
			String[] sources = concatStrs(s1,s2);

			id_map.put(cid,sources);

			for(String s : s1)
				refreshSourceMap(remain_id[0].intValue(),s,cid);
			for(String s : s2)
				refreshSourceMap(remain_id[i].intValue(),s,cid);

			
			// 同じIDを持っていた残りの部分も同時に更新
			for(int j = i+1; j < remain_id.length;j++){
				if(remain_id[0].intValue() == remain_id[j].intValue())
					remain_id[j] = cid;
			}
			remain_id[0] = cid;
		}

		return v.toArray(new Element[0]);
	}

	private String getBeforeName(String name){
		String oname = rename_map.get(name);

		if(oname != null) return oname;

		return name;
	}

	private Element create_projection() throws HarmonicaException{
		AttributeList list = hamql.getSelectClause();
		Vector<String> v = new Vector<String>();
		Vector<String> v1 = new Vector<String>();

		for(int i=0;i<list.size();i++){
			if(list.isAttribute(i)){ // Attribute
				Attribute ha = list.getAttribute(i);
				String rename = ((HarmonicaAttribute)ha).getRename();
				if(rename != null){
					String[] renames = rename.split("\\.");
					if(renames.length == 1){
						v.add(ha.getSourceName() + "." + rename);
						v1.add(getBeforeName(ha.getSourceName()+"."+rename));

					}else if(renames.length == 2){
						v.add(renames[0] + "." + renames[1]);
						v1.add(getBeforeName(renames[0]+"."+renames[1]));
					}else{
						throw new HarmonicaException
							("Invalid : "+ha.toString());
					}
				}else{
					v.add(ha.getSourceName() + "." + ha.getColumnName());
					v1.add(getBeforeName(
								ha.getSourceName()+"."+ha.getColumnName()));
				}
			}else if(list.isFunction(i)){ // Function
				FunctionParameter fp = list.getFunction(i);
				String rename = ((HarmonicaFunctionParameter)fp).getRename();
					
				String table_name = "";
				String[] cnames = id_map.get(cid-1);
				if(cnames.length >= 2) table_name += "[";
				int j=0;
				for(String s : cnames){
					if(j != 0) table_name += ",";
					table_name += s;
					j++;
				}
				if(cnames.length >= 2) table_name += "]";

				if(rename != null){
					v.add(table_name + "." + rename);
					v1.add(getBeforeName(table_name+"."+rename));
				}else{
					v.add(fp.toString());
					v1.add(getBeforeName(fp.toString()));
				}
			}else{ // * <ASTERISK>
				return null;
			}
		}
		
		projection_attributes = v;

		String value = "";
		int i = 0;
		for(String s : v1){
			if(i != 0) value += ",";
			value += s;
			i++;
		}

		projection_list = value;

		String[] names = {"attribute"};
		String[] values = {value};
		int[] rid = {cid};

		Element elm = createOperator(++cid,"projection",names,values,rid);

		String[] sources = id_map.get(cid-1);
		id_map.put(cid,sources);
		for(String s : sources){
			refreshSourceMap(cid-1,s,cid);
		}

		return elm;
	}

	/**
	 * outidを作成する．
	 */
	private void create_outid(){
		if(!doNeedOutputNode) return;
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		for(int i=cid;i>1;i--){
			Element elm = getElement(i);
			int[] refid = getRID(elm);
			for(int j=0;j<refid.length;j++){
				m.put(refid[j],i);
			}
			Integer tmp = m.get(i);
			if(tmp != null){
				if(!elm.getNodeName().equals("source"))
				elm.appendChild(createOutputElement(tmp.intValue()));
			}
		}
	}

	private Element createOutputElement(int id){
		Element el = createElement("output");
		el.setAttribute("refid",String.valueOf(id));

		return el;
	}

	private Element getElement(int id){
		Element root = xml.getDocumentElement();
		NodeList nodes = root.getChildNodes();

		for(int i=0;i<nodes.getLength();i++){
			Element el = (Element)(nodes.item(i));
			if(Integer.parseInt(el.getAttribute("id")) == id ){
				return el;
			}
		}
		return null;
	}

	private int[] getRID(Element el){
		Vector<String> v = new Vector<String>();
		NodeList nodes = el.getChildNodes();

		for(int i=0;i<nodes.getLength();i++){
			Element el2 = (Element)(nodes.item(i));
			String nname = el2.getNodeName();
			if(!nname.equals("input")) continue;

			v.add(el2.getAttribute("refid"));
		}

		int[] out = new int[v.size()];
		int i=0;
		for(String s : v){
			out[i] = Integer.parseInt(s);
			i++;
		}

		return out;
	}

	/**
	 * SELECTを作る．
	 */
	private Element[] create_select() throws HarmonicaException{
		Vector<Element> v = new Vector<Element>();
		AttributeList sels = hamql.getSelectClause();
		
		create_arguments(sels,v);

		return v.toArray(new Element[0]);
	}

	private int create_arguments(
			AttributeList sels, 
			Vector<Element> v) 
		throws HarmonicaException
	{
		boolean contain_functions = false;
		int return_val = -1;

		if(sels == null) throw new HarmonicaException("no attribute");
	
		// 属性がFunctionの場合
		for(int i=0; i<sels.size();i++){
			if(sels.isFunction(i)){
				contain_functions = true;
				FunctionParameter fp = sels.getFunction(i);
				AttributeList args = fp.getArguments();
				
				int arg_id = create_arguments(args, v);
				
				// 集約関数かつGROUPが無い場合は，GROUP演算を挿入
				String fname = fp.getFunctionName();
				if(isAggregationFunction(fname) && !has_group_clause){
					Element elm = 
						create_group_for_aggregate_function(arg_id);
					v.add(elm);
					has_group_clause = true;
					arg_id = cid;
				}

				// 関数評価
				String[] fparts = fp.toString().split(" AS ");
				if(!is_evaluated(fp)){
					String[] names = {"function"};
					String[] values = {fparts[0]};
					int[] rid = {arg_id};
					Element elm = null;
					String[] sources = null;

					elm = createOperator(++cid,"eval",names,values,rid);
					v.add(elm);
					evaluated.add(fp);
	
					sources = id_map.get(arg_id);
					id_map.put(cid,sources);
					for(int x=0;x<sources.length;x++){
						refreshSourceMap(rid[0],sources[x],cid);
					}
					return_val = cid;
				}

				// RENAME
				if(fp instanceof HarmonicaFunctionParameter){
					HarmonicaFunctionParameter hfp = 
						(HarmonicaFunctionParameter)fp;
					
					String rename = hfp.getRename();
					if(rename == null) continue;
				
					String table_name = "";
					String[] sources = id_map.get(cid);
					if(sources.length == 1){
						table_name = sources[0];
					}else{
						table_name = "[";
						for(int x=0;x<sources.length;x++){
							if(x!=0) table_name+=",";
							table_name += sources[x];
						}
						table_name += "]";
					}
				
					rename_map.put(table_name+"."+rename,fparts[0]);
				}
			}
		}

		// 属性がAttributeの場合
		Vector<Integer> its = new Vector<Integer>();
		for(int i=0;i<sels.size();i++){
			if(sels.isAttribute(i)){ // Attribute
				Attribute a = sels.getAttribute(i);
	
				Integer iv = new Integer(searchID(a.getSourceName()));
				if(!its.contains(iv)) its.add(iv);
			}
		}

	
		// 関数と関数に含まれていない属性の結合を考慮する
		if(its.size() >= 1 && contain_functions){
			String[] names = {"predicate"};
			String[] values = {""};
	
			Integer[] ids = its.toArray(new Integer[0]);
			String[] sources = id_map.get(ids[0]);
			String[] target = null; 
	
			int[] rid = {ids[0].intValue(),cid};
	
			for(int j=0;j<ids.length;j++){
				target = id_map.get(ids[j]);
	
				if(!hasJoined(sources, target)){
					Element elm = 
						createOperator(++cid,"join",names,values,rid);
					v.add(elm);
	
					id_map.put(cid,sources);
					for(int i=0;i<sources.length;i++)
						refreshSourceMap(rid[0],sources[i],cid);

					return_val = cid;
				}
			}

		// 関数が無くて，属性が2つ以上ある場合は結合を考慮する
		}else if(its.size() >= 2){
			String[] names = {"predicate"};
			String[] values = {""};
			int[] rid = null;
				
			Integer[] ids = its.toArray(new Integer[0]);
	
			for(int j=1;j<ids.length;j++){
				rid = new int[]{ids[0].intValue(), ids[j].intValue()};
	
				Element elm = 
					createOperator(++cid,"join",names,values,rid);
				v.add(elm);
				String[] s1 = id_map.get(rid[0]);
				String[] s2 = id_map.get(rid[1]);
				String[] sources = concatStrs(s1, s2);
					
				id_map.put(cid,sources);
				for(int k=0;k<s1.length;k++)
					refreshSourceMap(rid[0], s1[k], cid);
				for(int k=0;k<s2.length;k++)
					refreshSourceMap(rid[1], s2[k], cid);

				return_val = cid;
			}

		// 属性１つのみの場合
		}else if(its.size() == 1){
			return_val = its.get(0);
		}
	
		//RENAME
		for(int i=0;i<sels.size();i++){
			if(sels.isAttribute(i)){ 
				Attribute a = sels.getAttribute(i);
				if(a instanceof HarmonicaAttribute){
					String rename = ((HarmonicaAttribute)a).getRename();
					String source = "";
					String column = "";

					if(rename == null) continue;

					source = a.getSourceName();
					column = a.getColumnName();

					rename_map.put(source+"."+rename,source+"."+column);
				}
			}
		}
		
		return return_val;
	}

	/**
	 * 関数が集約関数か，ただの関数かを判定する．
	 *
	 * @param fname 関数名
	 * @return 判定結果
	 */
	private boolean isAggregationFunction(String fname){
		return Function.isAggregateFunction(fname);
	}

	private Element create_rename(){
		if(rename_map.size()<=0) //RENAMEは無し
			return null;

		StringBuilder buf = new StringBuilder();
		int i = 0;
		for(String s : projection_attributes){
			if(i!=0) buf.append(",");
			
			buf.append(s);
			i++;
		}

		projection_list = buf.toString();

		String[] names = {"attribute"};
		String[] values = {buf.toString()};
		int[] rid = new int[1];
		rid[0] = cid;

		Element elm = 
			createOperator(++cid,"rename",names,values,rid);
	
		String[] sources = id_map.get(cid-1);
		id_map.put(cid, sources);
		for(String s : sources){
			refreshSourceMap(cid-1,s,cid);
		}
		
		return elm;
	}

	private String[] concatStrs(String[] a, String[] b){
		Vector<String> v = new Vector<String>();

		for(String ae : a) v.add(ae);
		for(String be : b) v.add(be);

		return v.toArray(new String[0]);
	}
	private boolean hasJoined(String[] a, String[] b){
		for(String a_s : a){
			for(String b_s : b){
				if(a_s.equals(b_s)) return true;
			}
		}

		return false;
	}
	/**
	 * GROUPを作る．
	 */
	private Element create_group() throws HarmonicaException {
		AttributeList group = hamql.getGroupClause();

		if(group == null) return null;

		String[] names = {"attribute"};
		String[] values = {group.toString()};
		int[] rid = new int[1];


		TreeSet<String> ts = new TreeSet<String>();
		for(int i=0;i<group.size();i++){
			ts.add(group.getAttribute(i).getSourceName());
		}
		
		if(ts.size() != 1) 
			throw new HarmonicaException("Invalid : " + group.toString());

		String[] sources = {ts.first()};
		rid[0] = searchID(sources[0]);
		Element elm = createOperator(++cid,"group",names,values,rid);

		id_map.put(cid,sources);
		refreshSourceMap(rid[0],sources[0],cid);

		return elm;
	}

	private Element create_group_for_aggregate_function(int arg_id){
		String[] names = {"attribute"};
		String[] values = {""};
		int[] rid = {arg_id};

		Element elm = createOperator(++cid,"group",names,values,rid);

		String[] sources = id_map.get(arg_id);

		id_map.put(cid,sources);

		for(String s : sources){
			refreshSourceMap(rid[0],s,cid);
		}

		return elm;
	}

	/**
	 * ROOTを最後に付ける
	 */
	private Element create_root() throws HarmonicaException {
		Collection<Integer> ids = source_map.values();
		Integer[] id = ids.toArray(new Integer[0]);

		int tmp = id[0].intValue();
		for(Integer i : id){
			if(tmp != i.intValue()) 
				throw new HarmonicaException("tree error.");
		}

		int[] inputs = {id[0].intValue()};

		Element elm = createOperator(++cid,"root",null,null,inputs);

		return elm;
	}

	private Element create_join_op(String cond, int rid1, int rid2){
		Element elm = null;
		String[] names = {"predicate"};
		String[] values = {cond};
		int[] rid = {rid1,rid2};

		// join演算ノードを作成
		elm = createOperator(++cid,"join",names,values,rid);

		// それぞれの1つ前のIDとその時に関係する情報源を取得
		String[] s1 = id_map.get(rid[0]);
		String[] s2 = id_map.get(rid[1]);

		// s1とs2から重複を除去した1つの配列を作成
		String[] sources = concatStrs(s1,s2);

		// 作成した演算のIDとそれに依存する情報源を登録
		id_map.put(cid,sources);

		// 情報源の最新の更新を行った演算のIDを更新
		for(String s : s1) refreshSourceMap(rid[0], s, cid);
		for(String s : s2) refreshSourceMap(rid[1], s, cid);

		return elm;
	}

	private Element create_selection_op(String cond, int rid1){
		Element elm = null;
		String[] names = {"predicate"};
		String[] values = {cond};
		int[] rid = {rid1};

		// selection演算ノードを作成
		elm = createOperator(++cid,"selection",names,values,rid);

		// 1つ前のIDとその時に関係する情報源を取得
		String[] sources = id_map.get(rid[0]);

		// 作成した演算のIDとそれに依存する情報源を登録
		id_map.put(cid,sources);

		// 情報源の最新の更新を行った演算のIDを更新
		for(String s : sources) refreshSourceMap(rid[0], s, cid);

		return elm;
	}

	private boolean isFirstSelection
		(Vector<String> selected, String src){
			debug(selected + " " + src);
		if(selected.contains(src)) return false;

		return true;
	}

	private void add_selection_operator_of_where_clause
		(Vector<Element> v, 
		 Vector<String> selected,
		 String cond, 
		 String src) throws HarmonicaException{


		if(isFirstSelection(selected, src)){ // 初選択演算
			int rid = searchID(src);
			String[] sources = id_map.get(rid);
			v.add(create_selection_op(cond,rid));
			for(String s : sources){
				selected.add(s);
			}

		}else{ // すでに選択した演算が存在
			// 前回の選択演算にマージ
			concatSelection(v,src,cond);
		}
	}
	
	private void add_join_operator_of_where_clause
		(Vector<Element> v, String cond, String src1, String src2)
		throws HarmonicaException{
		int[] rid = {searchID(src1),searchID(src2)};

		if(rid[0] != rid[1]){ // 初結合演算
			v.add(create_join_op(cond,rid[0],rid[1]));

		}else{ // すでに結合した演算が存在 => 前回の結合演算にマージ
			concatJoin(v,String.valueOf(rid[0]),cond);
		}
	}

	/**
	 * WHERE節のEVAL演算をいろいろな条件を加味して作成する．
	 */
	private void add_eval_operator_of_where_clause
		(Vector<Element> v, FunctionParameter fp)
		throws HarmonicaException{
		if(is_evaluated(fp)) return;

		AttributeList args = fp.getArguments();
		TreeSet<String> ts = new TreeSet<String>();

		if(args.size() > 2) // F(A, B, C)
			throw new HarmonicaException
				("Wrong syntax (3 more tables) : "+fp);

		// 引数のテーブル名を取得
		for(int i=0;i<args.size();i++){
			if(!args.isAttribute(i) && !args.isFunction(i))
				throw new HarmonicaException
					("Can't apply constant value to Functions : "+fp);

			ts.add(args.getAttribute(i).getSourceName());
		}

		if(ts.size() == 2){ // F(A, B) = 10
			// 最初に直積が必要
			String[] ss = ts.toArray(new String[0]);
			add_join_operator_of_where_clause(v, "", ss[0], ss[1]);
		}			
		
		// EVAL演算の追加
		int rid = -1;

		if(ts.size() > 0) rid = searchID(ts.first());
		else rid = cid;
		
		v.add(create_eval_op(fp.toString(),rid));
		evaluated.add(fp);
	}

	/**
	 * 単純にEVAL演算用のノードを生成する．
	 */
	private Element create_eval_op(String cond, int rid1){
		Element elm = null;
		String[] names = {"function"};
		String[] values = {cond};
		int[] rid = {rid1};

		elm = createOperator(++cid,"eval",names,values,rid);

		String[] sources = id_map.get(rid[0]);
		id_map.put(cid,sources);

		for(String s : sources)
			refreshSourceMap(rid[0], s, cid);

		return elm;
	}

	/**
	 * WHERE節による各演算を生成する．
	 */
	private Element[] create_operators() throws HarmonicaException{
		Vector<String> selected = new Vector<String>();
		Vector<Element> v = new Vector<Element>();
		PredicateSet ps = hamql.getWhereClause();
		if(ps == null) return null;

		Iterator it = ps.iterator();
		
		while(it.hasNext()){
			Predicate p = (Predicate)it.next();

			if(p.isConstant(p.LEFT) && p.isConstant(p.RIGHT))
				throw new HarmonicaException
					("Can't find attributes : "+p);

			if(p.isConstant(p.LEFT) && !p.isConstant(p.RIGHT)) 
				p = p.reverse();

			if(p.isAttribute(p.LEFT)){ // 関数無し
				if(p.isAttribute(p.RIGHT)){ // X = Y

					String[] n1 = p.getLeftString().split("\\.");
					String[] n2 = p.getRightString().split("\\.");

					if(n1[0].equals(n2[0])){ // A.x = A.y

						// SELECTION演算の作成
						add_selection_operator_of_where_clause
							(v,selected,p.toString(),n1[0]);

					}else{ // A.x = B.y

						// JOIN演算の作成
						add_join_operator_of_where_clause
							(v,p.toString(),n1[0],n2[0]);
					}
				
				}else if(p.isConstant(p.RIGHT)){ // X = 10
					String[] n1 = p.getLeftString().split("\\.");

					// SELECTION演算の作成
					add_selection_operator_of_where_clause
						(v,selected,p.toString(),n1[0]);
				}

			}else if(p.isFunction(p.LEFT)){ // 関数有り
				FunctionParameter[] fps = p.extractFunctions();
				if(p.isConstant(p.RIGHT)){ // F = 10
					if(fps[0].getArguments().size() == 0)
						throw new HarmonicaException
							("Can't find attributes : "+p);

					// 左側の関数に対するEVAL演算の作成
					add_eval_operator_of_where_clause(v, fps[0]);

					String src = fps[0].getArguments().getAttribute(0).
						getSourceName();

					// 選択演算の追加
					Vector<String> selected2 = new Vector<String>();
					add_selection_operator_of_where_clause
						(v,selected2,p.toString(),src);

					for(String s :selected2)
						if(!selected.contains(s)) selected.add(s);
						
				}else if(p.isAttribute(p.RIGHT)){ // F = X
					// 左型の関数に対するEVAL演算の作成
					add_eval_operator_of_where_clause(v, fps[0]);

					String s1 = fps[0].getArguments().getAttribute(0).
						getSourceName();
					String s2 = p.getRightString().split("\\.")[0];

					if(searchID(s1) == searchID(s2)){
						// 結合済み => 選択演算
						Vector<String> selected2 = new Vector<String>();
						add_selection_operator_of_where_clause
							(v,selected2,p.toString(),s1);

						for(String s :selected2)
							if(!selected.contains(s)) selected.add(s);
					}else{
						// 未結合 => 結合演算
						add_join_operator_of_where_clause
							(v,p.toString(),s1,s2);
					}

				}else if(p.isFunction(p.RIGHT)){ // F1 = F2
					if(fps[0].getArguments().size() == 0 && 
							fps[1].getArguments().size() == 0)
						throw new HarmonicaException
							("Can't find attributes : "+p);

					// 左側の関数に対するEVAL演算の作成
					add_eval_operator_of_where_clause(v,fps[0]);

					// 右側の関数に対するEVAL演算の作成
					add_eval_operator_of_where_clause(v,fps[1]);

					String s1 = fps[0].getArguments().getAttribute(0).
						getSourceName();
					String s2 = fps[1].getArguments().getAttribute(0).
						getSourceName();

					if(searchID(s1) == searchID(s2)){
						// 選択演算
						Vector<String> selected2 = new Vector<String>();
						add_selection_operator_of_where_clause
							(v,selected2,p.toString(),s1);

						for(String s :selected2)
							if(!selected.contains(s)) selected.add(s);
					}else{
						// 結合演算
						add_join_operator_of_where_clause
							(v,p.toString(),s1,s2);
					}
				}else{
					throw new HarmonicaException("Something is wrong :"+p);
				}
			}else{ // 11 = 11
				throw new HarmonicaException
					("Can't find attribute : "+p);
			}
		}

		return v.toArray(new Element[0]);
	}

	private boolean is_evaluated(FunctionParameter f1){
		for(FunctionParameter f2 : evaluated){
			if(f1.equals(f2)) return true;
		}

		return false;
	}
	private boolean is_same_attribute(AttributeList a1, AttributeList a2){
		if(a1.size() != a2.size()) return false;
		
		return true;
	}
	private void concatSelection(Vector<Element> v, String n, String exp){
		for(Element elm : v){
			if(!elm.getAttribute("type").equals("selection")) continue;

			NodeList nodes = elm.getChildNodes();
			for(int i=0;i<nodes.getLength();i++){
				Element el = (Element)(nodes.item(i));
				String nname = el.getNodeName();
				if(!nname.equals("parameter")) continue;

				String name = el.getAttribute("name");
				if(!name.equals("predicate")) continue;

				String value = el.getAttribute("value");
				if(!value.contains(n)) continue;

				el.setAttribute("value",value + " AND " + exp);
			}
		}
	}
	private boolean concatJoin(Vector<Element> v, String rid, String exp){
		Element elm = null;
		for(Element el : v){
			if(el.getAttribute("id").equals(rid)){
				elm = el;
				break;
			}
		}
		if(elm == null) return false;

		String type = elm.getAttribute("type");
		if(!type.equals("join")){
			NodeList nodes = elm.getChildNodes();
			boolean b = false;
			for(int i=0;i<nodes.getLength();i++){
				Element el = (Element)(nodes.item(i));

				if(!el.getNodeName().equals("input")) continue;
				String refid = el.getAttribute("refid");
				b = concatJoin(v, refid, exp);
				if(b) break;
			}
		   	return b;
		}

		NodeList nodes = elm.getChildNodes();
		for(int i=0;i<nodes.getLength();i++){
			Element el = (Element)(nodes.item(i));
			if(!el.getNodeName().equals("parameter")) continue;
			if(!el.getAttribute("name").equals("predicate")) continue;
			el.setAttribute("value",
					el.getAttribute("value")+" AND "+exp);
			return true;
		}

		return false;
	}

	private int searchID(String source) throws HarmonicaException{
		Integer id = source_map.get(source);

		if(id == null) 
			throw new HarmonicaException("No source (" + source + ").");

		return id.intValue();
	}
	private Element cloneElement(Element oe){
		String nname = oe.getNodeName();
		String id = oe.getAttribute("id");
		if(nname.equals("source")){
			Element elm = createElement("source");
			String name = oe.getAttribute("name");
			String window = oe.getAttribute("window");
			String window_at = oe.getAttribute("window_at");

			elm.setAttribute("id",id);
			elm.setAttribute("name",name);
			if(window != "") elm.setAttribute("window",window);
			if(window_at != "") elm.setAttribute("window_at",window_at);

			return elm;
		}

		if(nname.equals("operator")){
			String type = oe.getAttribute("type");
			Vector<String> v_names = new Vector<String>();
			Vector<String> v_values = new Vector<String>();
			Vector<String> v_rid = new Vector<String>();
			int out_id = -1;

			NodeList nodes = oe.getChildNodes();
			for(int i=0;i<nodes.getLength();i++){
				Element elm = (Element)nodes.item(i);
				String cnname = elm.getNodeName();
				if(cnname.equals("parameter")){
					v_names.add(elm.getAttribute("name"));
					v_values.add(elm.getAttribute("value"));
				}else if(cnname.equals("input")){
					v_rid.add(elm.getAttribute("refid"));
				}else if(cnname.equals("output")){
					out_id = Integer.parseInt(elm.getAttribute("refid"));
				}
			}
			
			String[] names = v_names.toArray(new String[0]);
			String[] values = v_values.toArray(new String[0]);
			int[] rid = new int[v_rid.size()];
			int i = 0;
			for(String s : v_rid){
				rid[i] = Integer.parseInt(s);
				i++;
			}

			Element elm = createOperator
				(Integer.parseInt(id),type,names,values,rid);
			
			if(out_id > 0){
				Element o_elm = createOutputElement(out_id);
				elm.appendChild(o_elm);
			}

			return elm;
		}

		return oe;
	}
	/**
	 * sourceノードを生成する．
	 */
	private Element[] create_sources() throws HarmonicaException{
		Vector<Element> v = new Vector<Element>();
		Element source = null;
		
		HarmonicaSourceSet h_ss = hamql.getFromClause();

		try{
			// SUB_QUERY処理
			for(HarmonicaSource h_s : h_ss.getSources()){
				if(h_s.getType() == SourceType.SUB_QUERY){
					HamQLQuery sub_hamql = h_s.getQuery();
					HamQLQueryTree sub_tree = null;
					HamQLQueryTreeCreator sub_creator = 
						new HamQLQueryTreeCreator(cid, sub_hamql);
					Document sub_document = null;
					Element sub_root = null;
	
					sub_creator.create();
					sub_tree = sub_creator.getTree();
					sub_document = sub_tree.getXMLDocument();
					sub_root = sub_document.getDocumentElement();

					NodeList list = sub_root.getElementsByTagName("source");
					for(int i=0;i<list.getLength();i++){
						Element elm = (Element)list.item(i);
						elm = cloneElement(elm);
						v.add(elm);
					}

					int opeid = 0;
					int refid = 0;
					list = sub_root.getElementsByTagName("operator");
					for(int i=0;i<list.getLength();i++){
						Element elm = (Element)list.item(i);
						elm = cloneElement(elm);
						if(!elm.getAttribute("type").equals("root")){
							v.add(elm);
						}else{
							opeid = Integer.parseInt(elm.getAttribute("id"));
							NodeList sub_list = 
								elm.getElementsByTagName("input");
							for(int j=0;j<sub_list.getLength();j++){
								Element sub_elm = (Element)sub_list.item(j);
								refid = Integer.parseInt
									(sub_elm.getAttribute("refid"));
							}
						}
					}
	
					id_map.putAll(sub_creator.getIDMap());
					cid = opeid-1;
					source_map.putAll(sub_creator.getSourceMap());

					// RENAME 処理
					String rename_value = h_s.getRename();
					if(rename_value != null){
						cid++;
						String[] pn = {"attribute"};
						String[] pv = {rename_value+".*"};
						int[] iid = {cid-1};
						Element rename = 
							createOperator(cid,"rename",pn,pv,iid);
						
						v.add(rename);

						String[] srcs = {rename_value};
						id_map.put(cid,srcs);
						refreshSourceMap(cid-1,rename_value,cid);
					}
				}
			}
		}catch(Exception e){
			throw new HarmonicaException(e);
		}

		// SOURCE処理
		for(HarmonicaSource h_s : h_ss.getSources()){
			if(h_s.getType() == SourceType.SOURCE){
				source = createElement("source");
				cid++;
				Integer id = new Integer(cid);

				source.setAttribute("id",id.toString());
				String[] sources = {h_s.getSource()};
				String w = String.valueOf(h_s.getWindowSize());
				String at = String.valueOf(h_s.getOriginalTime());
				
				id_map.put(cid, sources);

				debug("window : " + w + " orig : " + at);
				
				StreamArchiver arc =  HarmonicaManager.getStreamArchiver();

				if(Long.parseLong(w) < 0) w = "";

				source.setAttribute("name",h_s.getSource());
				source.setAttribute("window", w);
				source.setAttribute("window_at", at);

				v.add(source);
				source_map.put(h_s.getSource(),id);

				// RENAME処理
				int refid = cid;
				String rename_value = h_s.getRename();
				if(rename_value != null){
					cid++;
					String[] pn = {"attribute"};
					String[] pv = {rename_value+".*"};
					int[] iid = {cid-1};
					Element rename = 
						createOperator(cid,"rename",pn,pv,iid);

					v.add(rename);
					String[] sources2 = {rename_value};
					id_map.put(cid, sources2);
					refreshSourceMap(cid-1,rename_value,cid);
				}
			}
		}

		return v.toArray(new Element[0]);
	}
	private void refreshSourceMap(int old_id, String new_name, int new_id){
		String[] old_names = id_map.get(old_id);

		for(String old_name : old_names){
			Integer id = source_map.get(old_name);


			if(id != null && id.intValue()!=new_id) 
				source_map.remove(old_name);
		}
		source_map.put(new_name, new_id);
	}
	/**
	 * 主に副問合せ用．<BR>
	 * 情報源とそれに対応する最新の演算IDを取得する．
	 *
	 * @return 情報源MAP
	 */
	public Map<String, Integer> getSourceMap(){
		return source_map;
	}

	/**
	 * 演算ノードを生成する
	 */
	private Element createOperator(
			int opeid, 
			String type, 
			String[] parameter_names,
			String[] parameter_values,
			int[] input_id
	){
		Element elm = createElement("operator");
		elm.setAttribute("id",String.valueOf(opeid));
		elm.setAttribute("type",type);

		if(parameter_names != null){
			for(int i=0;i<parameter_names.length;i++){
				Element child_elm = createElement("parameter");
				child_elm.setAttribute("name",parameter_names[i]);
				child_elm.setAttribute("value",parameter_values[i]);
				elm.appendChild(child_elm);
			}
		}

		for(int i=0;i<input_id.length;i++){
			Element child_elm = createElement("input");
			child_elm.setAttribute("refid",String.valueOf(input_id[i]));
			elm.appendChild(child_elm);
		}

		return elm;
	}
	/**
	 * 主に副問合せ用．<BR>
	 * 演算IDとそれに対応する情報源を取得する．
	 *
	 * @return 演算IDMAP
	 */
	public Map<Integer, String[]> getIDMap(){
		return id_map;
	}
	/**
	 * planノードを生成する．
	 */
	private Element create_plan(){
		MasterSet m = hamql.getMasterClause();
		Element plan = createElement("plan");

		if(m != null){
			String masters = "";
			
			int i=0;
			Iterator it = m.iterator();
			while(it.hasNext()){
				if(i != 0) masters += ",";
				i++;
				masters += (String)it.next();
			}

			plan.setAttribute("master",masters);
		}

		return plan;
	}
	/**
	 * Elementを作成する．
	 */
	private Element createElement(String name){
		return xml.createElement(name);
	}
	/**
	 * xml文書の初期化を行う．
	 */
	private Document createXMLDocument() throws HarmonicaException{
		try{
			DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xml = builder.newDocument();
			
			return xml;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
	/**
	 * 問合せ木を取得する．
	 *
	 * @return 問合せ木
	 */
	public HamQLQueryTree getTree(){ return tree; }
	/**
	 * DEBUG用（その１）．
	 */
	private void debug(Object o){
		HarmonicaManager.debug("TreeCreator",o);
	}
	/**
	 * DEBUG用（その２）．
	 */
	private void xml_debug(org.w3c.dom.Node node){
		if(!HarmonicaManager.show_debug) return;
		try{
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(node);
			ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(System.out);
			transformer.transform(source,result);
		}catch(Exception e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
	}
}
