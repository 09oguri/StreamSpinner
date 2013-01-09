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
package org.streamspinner.harmonica.validator;

import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.query.*;
import org.streamspinner.harmonica.*;
import org.streamspinner.engine.*;

import org.w3c.dom.*;

import java.util.*;

/**
 * FeasibilityValidator�̎����N���X
 *
 * @author snic
 * @version 1.0
 */
public class FeasibilityValidatorImpl implements FeasibilityValidator {
	private static TreeMap<Integer, Double> insertion_rate = null;
	private Vector<FeasibilityValidatorListener> listeners = null;
	private HamQLParser parser = null;
	private StreamArchiver archiver = null;
	private Vector<String> insert_sources = null;
	private static boolean isInitialized = false;

	private static FeasibilityValidatorImpl validator = 
		new FeasibilityValidatorImpl();

	private FeasibilityValidatorImpl(){}

	/**
	 * ����������D
	 */
	private void Initialize(){
		debug("Initializing FeasibilityValidator.");
		archiver = HarmonicaManager.getStreamArchiver();
		parser = HarmonicaManager.getHamQLParser();

		//insertion_rate = 
		//	(TreeMap<Integer, Double>)archiver.getInsertionRate();
		listeners = new Vector<FeasibilityValidatorListener>();
		insert_sources = new Vector<String>();
	}

	/**
	 * FeasibilityValidator�C���X�^���X���擾����D
	 *
	 * @return FeasibilityValidator�C���X�^���X
	 */
	public static FeasibilityValidator getInstance(){
		if(!isInitialized){
			validator.Initialize();
			isInitialized = true;
		}
		return validator;
	}

	/**
	 * ������s���D<BR />
	 * ��������p�I�u�W�F�N�g���擾����D
	 *
	 * @param statement �⍇���L�q
	 * @return ���茋��
	 */
	public ValidatingResult getResultAfterValidate(String statement)
	throws HarmonicaException{
		return validate(statement);
	}

	/**
	 * ������s���D
	 *
	 * @param statement �⍇���L�q
	 * @return ���茋��(�����s�\�̏ꍇ��null)
	 */
	public Document validateQuery(String statement)
	throws HarmonicaException{
		ValidatingResult _result = validate(statement);

		if(_result.isFeasible()){
			return _result.getQueryTree().getXMLDocument();
		}else{
			return null;
		}
	}

	private ValidatingResult validate(String statement) 
	throws HarmonicaException{
		ValidatingResult result = new ValidatingResult();
		Map<String, OperatorCost> costs = 
			new HashMap<String, OperatorCost>();

		// �⍇�����
		HamQLQueryTree tree = null;
		tree = parser.parse(statement);
		result.setQueryTree(tree);

		if(tree.getQuery().getMasterClause() == null){
			result.setFeasible(true);
			return result;
		}

		Document doc = (Document)tree.getXMLDocument().cloneNode(true);
		Element root = (Element)doc.getFirstChild();

		// ID��Element��Map�𐶐�����
		Map<String, Element> id_map = new HashMap<String, Element>();
		NodeList list = root.getChildNodes();
		Element root_node = null;
		Element insert_node = null;
		int sliding = 1;
		for(int i=0;i<list.getLength();i++){
			Element elm = (Element)list.item(i);
			String id = elm.getAttribute("id");

			id_map.put(id,elm);

			if(elm.getAttribute("type").equals("root"))
				root_node = elm;

			if(elm.getAttribute("type").equals("store")){
				insert_node = elm;
				NodeList ch_list = elm.getChildNodes();
				for(int j=0;j<ch_list.getLength();j++){
					Element ch = (Element)ch_list.item(j);
					if(!ch.getNodeName().equals("parameter"))
						continue;
					if(!ch.getAttribute("name").equals("table"))
						continue;
					insert_sources.add(ch.getAttribute("value"));
					ch = (Element)elm.getFirstChild();
				}
			}

			if(!elm.getNodeName().equals("source")) continue;
			if(!elm.getAttribute("window_at").equals("")) continue;

			// AT�������ꍇ�X���C�f�B���O���l������
			String name = elm.getAttribute("name");
			Schema s = null;
			try{
				s = archiver.getSchema(name);
			}catch(HarmonicaException e){
			}
			if(s != null){
				sliding *= archiver.getNumberOfTuples(name);
				continue;
			}else{
				/* 
				 * StreamSpinner����name�̃^�v�������擾������
				 * ���͓K���ɒl������
				 */
				sliding *= 100;
				continue;
			}
		}

		// �⍇���؂ɓ��̓��[�g��R�X�g�Ȃǂ�t�^����
		root = create_cost(root);

		// root�m�[�h����ċA�I�ɂ��ǂ�
		try{
			calcurate_costs(root_node, costs, id_map);
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
		result.setCost(costs);

		// ����
		double processing_time = calcurate_processingtime(costs, sliding);
		debug(processing_time);
		result.setProcessingTime(processing_time);
		debug(insert_node);
		if(insert_node != null){
			double output_rate = calcurate_queryrate
				(insert_node, costs, sliding);
			double irate = 
				calcurate_insertionrate(tree.getQuery());
			result.setInsertionRate(irate);
			result.setOutputRate(output_rate);
		}

		// ���X�i�ɒʒm
		for(FeasibilityValidatorListener l : listeners)
			l.generatedResult(result);

		return result;
	}

	private double calcurate_processingtime
		(Map<String, OperatorCost> costs, int sliding)
	{
		double processing_times = 0.0;

		for(OperatorCost cost : costs.values())
			processing_times += cost.getProcessingTime();

		return processing_times * sliding;
	}
	private double calcurate_queryrate
		(Element insert_node, Map<String, OperatorCost> costs, int sliding)
	{
		String[] ids = extractIDs(insert_node);
		OperatorCost cost = costs.get(ids[1]);

		return cost.getOutputRate() * sliding;
	}
	private double calcurate_insertionrate(HamQLQuery hamql){
		int x;
		String table_name = hamql.getInsertClause().getBaseTableNames()[0];
		try{
			x = archiver.getDataSize(table_name);
			debug("datasize="+x);
			if(x == 0)
				throw new HarmonicaException
					("can't find table : ("+table_name+")");
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
			return 0.0;
		}

		if(x <= 0){
			HarmonicaManager.createdException(new HarmonicaException(
				("No Table (" + table_name +") in Harmonica DB.")));
			return 0.0;
		}

		if(insertion_rate == null){
			insertion_rate = 
				(TreeMap<Integer, Double>)archiver.getInsertionRate();
		}

		if(insertion_rate == null){
			HarmonicaManager.createdException(
					new HarmonicaException("can't find database"));
			return 0.0;
		}


		Double rate = insertion_rate.get(x);
		debug("rate : " + rate + " size : "+x);
		if(rate != null) return rate.doubleValue();

		// �ȉ�����
		int[] xx = {-1, -1, -1, -1};
		double y1, y2;
		int x1, x2;
		double y;

		/*
		 * ��Ɉȉ��̊֌W�������Ă�����
		 * ----+-----+----+------+------+----
		 *   xx[0] xx[1]  x    xx[2]  xx[3]
		 */
		for(int i : insertion_rate.keySet()){
			debug(i+ " " + x);
			if(i < x){ // i��x��菬����
				if(xx[1] < 0){ // ���Ȃ疳�����ő��
				   	xx[1] = i;
					continue;
				}

				if(xx[1] < i){ // xx[1]���ł����Ȃ�X�V
				   	xx[0] = xx[1];
					xx[1] = i;
					continue;
				}
				
				if(xx[0] < 0){ // ���Ȃ疳�����ő��
					xx[0] = i;
					continue;
				}

				if(xx[0] < i){ // xx[0]���ł����Ȃ�X�V
					xx[0] = i;
					continue;
				}

			}else{ // i��x���傫��
				if(xx[2] < 0){
					xx[2] = i;
					continue;
				}

				if(i < xx[2]){
					xx[3] = xx[2];
					xx[2] = i;
					continue;
				}

				if(xx[3] < 0){
					xx[3] = i;
					continue;
				}
				   	
				if(i < xx[3]){
				   	xx[3] = i;
					continue;
				}
			}
		}

		debug(xx[0]+" "+xx[1]+" "+xx[2]+" "+xx[3]);

		if(xx[1] < 0){
			x1 = xx[2];
			x2 = xx[3];
		}else if(xx[2] < 0){
			x1 = xx[0];
			x2 = xx[1];
		}else{
			x1 = xx[1];
			x2 = xx[2];
		}

		y1 = insertion_rate.get(x1);
		y2 = insertion_rate.get(x2);

		if(y1 < y2){
			y = y1;
		}else{
			y = ((y1-y2)*x+(x1*y2-x2*y1))/(x1-x2);
		}

		insertion_rate.put(x,y);
		update_insertion_rate(insertion_rate);
		insertion_rate = null;
		
		return y;
	}

	private void update_insertion_rate(Map<Integer, Double> irate){
		List<DBConnector> l = archiver.getConnectors();
		String main_db = archiver.getMainDB();

		for(DBConnector c : l){
			if(c.toString().equals(main_db)){
				c.setInsertionRate(irate);
				break;
			}
		}
	}

	private void calcurate_costs
		(Element elm, 
		 Map<String, OperatorCost> costs, 
		 Map<String, Element> id_map) throws Exception
	{
		String[] ids = extractIDs(elm);
		//debug(ids[0]+" "+ids[1]+" "+ids[2]+" "+ids[3]);

		if(ids[1] == null){ // source
			String id = elm.getAttribute("id");
			double window = Double.parseDouble(elm.getAttribute("window"));
			double rate = Double.parseDouble(elm.getAttribute("rate"));

			if(rate > 0) window = window * rate;
			OperatorCost cost = new OperatorCost(id, rate, window);
			cost.setText(elm.getAttribute("name"));
			costs.put(id,cost);
		}else if(ids[2] == null){ // 1-input op including root op
			calcurate_costs(id_map.get(ids[1]), costs, id_map);
			calcurate_cost(elm, costs);
		}else{ // 2-input operators
			calcurate_costs(id_map.get(ids[1]), costs, id_map);
			calcurate_costs(id_map.get(ids[2]), costs, id_map);
			calcurate_cost(elm, costs);
		}
	}

	/**
	 * Element���玩ID�Ɠ��o��ID���擾����D
	 */
	private String[] extractIDs(Element elm){
		// [current_id, input1_id, input2_id, output_id]
		String[] ids = {null,null,null,null};
		ids[0] = elm.getAttribute("id");
		NodeList list = elm.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Element ch = (Element)list.item(i);
			if(ch.getNodeName().equals("input")){
				String refid = ch.getAttribute("refid");
				if(ids[1] == null) ids[1] = refid;
				else ids[2] = refid;
			}else if(ch.getNodeName().equals("output")){
				ids[3] = ch.getAttribute("refid");
			}
		}
		
		return ids;
	}
	/**
	 * �⍇���؂Ɋe���Z�̃R�X�g�ƑI�𗦂�t�^����D<BR>
	 * �܂��C��񌹂̓������[�g��t�^����D<BR>
	 */
	private Element create_cost(Element root){
		/*
		 * ������XML�̓��͂���StreamSpinner�ɂ���Ă��炢�����E�E�E
		 *
		 * ��񌹂̏ꍇ: 
		 * => 1.�������[�g�̒ǉ�
		 * <source id="1" name="a" window="10" winwow_at="0" rate="100" />
		 * => 2.window�����̒ǉ��i�Ȃ���΁j
		 * 		Harmonica�̃����[�V��������񌹂̏ꍇ:
		 * 		=> ���݂����̏�񌹂�INSERT���閽�߂�����΁C�������傫���l
		 * 		=> �Ȃ����StreamArchiver.getNumberOfTuples(��񌹖�)�̕Ԃ�l
		 * 		����RDB����񌹂̏ꍇ:
		 * 		=> JDBC���b�p�[����^�v�������擾
		 * 		�X�g���[���^��񌹂̏ꍇ:
		 * 		=> �������傫���l
		 *
		 * EVAL,RENAME���Z�ȊO�̏ꍇ:
		 * => �����R�X�g�ƑI�𗦂̒ǉ�
		 * <operator id="2" type="selection" cost="10" selectivity="0.3">
		 *  ...
		 * </operator>
		 *
		 * ���́C���̃��\�b�h���œK���ɍs��
		 */

		String max_window_size = String.valueOf(Long.MAX_VALUE);
		NodeList list = root.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Element elm = (Element)list.item(i);

			String node_name = elm.getNodeName();
			if(node_name.equals("source")){ // ���
				String source = elm.getAttribute("name");
				String window = elm.getAttribute("window");
				String at = elm.getAttribute("window_at");
				double rate = 0;
				debug(source + " " + window + " " + at);
				try{
					if(archiver.getSchema(source) != null){ // Harmonica
						if(window.equals("")){ // ��Ȃ�S�^�v��
							window = String.valueOf
								(archiver.getNumberOfTuples(source));

							elm.setAttribute("window",window);
						}else{ // �󂶂�Ȃ��Ȃ�C�^�v�������v�Z
							int tuple_size = archiver.getNumberOfTuples
								(source, 
								 Long.parseLong(window), 
								 Long.parseLong(at));

							window = String.valueOf(tuple_size);
							elm.setAttribute("window",window);
						}
					}else{  // Harmonica�ȊO
						rate = 1;
						if(window.equals("")){
							elm.setAttribute("window",max_window_size);
						}else{
							double dw = Long.parseLong(window)/1000;
							elm.setAttribute
								("window",String.valueOf(dw*rate));
						}
					}
					elm.setAttribute("rate",String.valueOf(rate));
				}catch(HarmonicaException e){
					HarmonicaManager.createdException(e);
					elm.setAttribute("window",max_window_size);
				}
				//Random r = new Random();
				//elm.setAttribute("rate",String.valueOf(r.nextInt(10)));
			}else{ // ���Z
				String node_type = elm.getAttribute("type");
				if(node_type.equals("eval") || 
						node_type.equals("rename") ||
						node_type.equals("store") ||
						node_type.equals("root"))
					continue;
				elm.setAttribute("selectivity","1.0");
				if(node_type.equals("selection") || 
						node_type.equals("projection")){
					elm.setAttribute("cost","0.0001");	// 0.1 ms
				}else if(node_type.equals("join")){
					elm.setAttribute("cost","0.0005");  // 0.5 ms
				}else if(node_type.equals("group")){
					elm.setAttribute("cost","0.0001");  // 0.1 ms
				}else if(node_type.equals("union")){
					elm.setAttribute("cost","0.0001");  // 0.1 ms
				}
			}
		}

		HarmonicaManager.printXML(root);
		return root;
	}

	/**
	 * �e���Z��Element����OperatorCost�I�u�W�F�N�g�𐶐�����D
	 */
	private void calcurate_cost
		(Element elm, Map<String, OperatorCost> costs) throws Exception{
		if(!elm.getNodeName().equals("operator")) return;

		String type = elm.getAttribute("type");

		OperatorType ot = null;
		if(type.equals("selection")) ot = OperatorType.SELECTION;
		else if(type.equals("projection")) ot = OperatorType.PROJECTION;
		else if(type.equals("join")) ot = OperatorType.JOIN;
		else if(type.equals("eval")) ot = OperatorType.EVAL;
		else if(type.equals("group")) ot = OperatorType.GROUP;
		else if(type.equals("rename")) ot = OperatorType.RENAME;
		else if(type.equals("root")) ot = OperatorType.ROOT;
		else if(type.equals("store")) ot = OperatorType.INSERTION;
		else if(type.equals("union")) ot = OperatorType.UNION;
		
		String id = elm.getAttribute("id");
		String cost_str = elm.getAttribute("cost");
		String sel_str = elm.getAttribute("selectivity");
		double cost = -1.0;
		double selectivity = -1.0;

		if(!cost_str.equals("")){ // not [EVAL,ROOT,INSERTION] operator
			cost = Double.parseDouble(cost_str);
			selectivity = Double.parseDouble(sel_str);
		}

		// ���Z�̓��͌���ID�𒊏o
		String[] refid = new String[2];
		NodeList list = elm.getChildNodes();
		String fname = null;
		String condition = null;
		for(int i=0;i<list.getLength();i++){
			Element celm = (Element)list.item(i);
			String tag_name = celm.getNodeName();
			if(!tag_name.equals("input")){

				// �������Z�����ω��Z�������ꍇ
				if(ot == OperatorType.JOIN && 
					celm.getAttribute("name").equals("predicate"))
				{
					condition = celm.getAttribute("value");
					if(condition.equals("")){
						ot = OperatorType.CARTESIAN_PRODUCT;
						condition = null;
					}
				}
				
				// �֐����̒��o
				else if(ot == OperatorType.EVAL &&
					celm.getAttribute("name").equals("function"))
				{
					fname = celm.getAttribute("value");
				}

				// ���l�[�����擾
				else if(ot == OperatorType.RENAME &&
					celm.getAttribute("name").equals("attribute"))
				{
					fname = celm.getAttribute("value");
				}

				// �}������e�[�u�������擾
				else if(ot == OperatorType.INSERTION &&
					celm.getAttribute("name").equals("table"))
				{
					fname = celm.getAttribute("value");
				}

				else if(ot == OperatorType.SELECTION){
					if(celm.getAttribute("name").equals("predicate"))
						condition = celm.getAttribute("value");
				}

				else if(ot == OperatorType.GROUP || 
						ot == OperatorType.PROJECTION){
					if(celm.getAttribute("name").equals("attribute"))
						condition = celm.getAttribute("value");
				}

				else if(ot == OperatorType.UNION){
					if(celm.getAttribute("name").equals("attribute"))
						condition = celm.getAttribute("value");
					if(celm.getAttribute("name").equals("base_rid"))
						fname = celm.getAttribute("value");
				}

				continue;
			}
			
			String input_id = celm.getAttribute("refid");
			if(refid[0] == null) refid[0] = input_id;
			else refid[1] = input_id;
		}

		OperatorCost oc = null;
		if(refid[1] == null){ // ���Z�̓��͂�1�̂Ƃ�
			OperatorCost oc1 = costs.get(refid[0]);

			// EVAL���Z�̂Ƃ��́CEVAL���Z�p�̃R�X�g�v�Z���s��
			if(ot == OperatorType.EVAL){
				double[] intensitys = {oc1.getOutputIntensity()};
				cost = evaluate_function_cost(fname, intensitys);
			}

			oc = new OperatorCost(
						ot,
						id,
						oc1.getOutputRate(),
						oc1.getOutputWindow(),
						oc1.getOutputIntensity(),
						selectivity,
						cost
					);

		}else{ // ���Z�̓��͂�2�̂Ƃ�
			OperatorCost oc1 = costs.get(refid[0]);
			OperatorCost oc2 = costs.get(refid[1]);

			if(ot == OperatorType.EVAL){
				double[] intensitys = {
						oc1.getOutputIntensity(),
						oc2.getOutputIntensity()
					};
				cost = evaluate_function_cost(fname, intensitys);
			}

			oc = new OperatorCost(
						ot,
						id,
						oc1.getOutputRate(),
						oc1.getOutputWindow(),
						oc1.getOutputIntensity(),
						oc2.getOutputRate(),
						oc2.getOutputWindow(),
						oc2.getOutputIntensity(),
						selectivity,
						cost
					);
		}

		switch(ot){
			case EVAL:
			case RENAME:
			case INSERTION:
				oc.setText(fname);
				break;
			case JOIN:
			case PROJECTION:
			case SELECTION:
			case GROUP:
				oc.setCondition(condition);
				break;
			case UNION:
				oc.setText(fname);
				oc.setCondition(condition);
				break;
		}


		costs.put(id, oc);
	}

	private double evaluate_function_cost(String f, double[] intensitys){
		/*
		 * �֐����ƏW��x(�z��̗v�f��)����StreamSpinner��
		 * ����Ă��炢�������ƁE�E�E
		 *
		 * �֐��̃R�X�g�̌v�Z
		 *
		 * ���́C�����̃��\�b�h�œK���ɒl��t����
		 */

		if(f.equals("array")) return 0.0;
		if(f.equals("max")) return intensitys[0] * 0.0001;
		if(f.equals("dist")) return intensitys[0] * 0.01;

		return intensitys[0] * 0.0001;
	}
	/**
	 * ���X�i�̓o�^�D
	 * @param listener �o�^���郊�X�i
	 */
	public void addFeasibilityValidatorListener
	(FeasibilityValidatorListener listener){
		listeners.add(listener);
	}
	/**
	 * ���X�i�̍폜�D
	 * @param listener �폜���郊�X�i
	 */
	public void removeFeasibilityValidatorListener
	(FeasibilityValidatorListener listener){
		listeners.remove(listener);
	}

	private void debug(Object o){
		HarmonicaManager.debug("Validator",o);
	}
}
