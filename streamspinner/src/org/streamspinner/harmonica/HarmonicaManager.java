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

import org.streamspinner.harmonica.query.*;
import org.streamspinner.harmonica.query.hamql.*;
import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.gui.*;

import org.w3c.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import java.io.*;
import java.util.*;
import java.net.URL;

/**
 * Harmonica���W���Ǘ����邽�߂̃N���X�D<BR>
 * ��Ɋe�N���X�Ńf�o�b�O�̏o�͂��s�����ǂ����Ǘ�����D<BR>
 * �f�t�H���g�̓f�o�b�O�o�͂��s���D<BR>
 * <BR>
 * �ύX�����F
 * <PRE>
 * 1.1 2006.8.4 �e�R���|�[�l���g�̃C���X�^���X���擾�ł���悤�ɕύX�D
 * </PRE>
 * @author snic
 * @version 1.1 (2006.12.18)
 */
public class HarmonicaManager{
	/**
	 * Harmonica�̃o�[�W����
	 */
	public static final String version = "2.9.0 (2006.12.19)";

	/**
	 * �����p�̏o�͂��s�����ǂ���
	 */
	public static boolean show_experiment = false;

	/**
	 * DEBUG�o�͂��s�����ǂ���
	 */
	public static boolean show_debug = false;

	/**
	 * ��O�̃��b�Z�[�W���o�͂��邩�ǂ���
	 */
	public static boolean show_exception = false;

	/**
	 * �X�v���b�V���E�C���h�E��\�����邩���Ȃ���
	 */
	public static boolean show_splush_window = true;

	/**
	 * �v�������������݃��[�g��ۑ����邩�ǂ���
	 */
	public static boolean write_current_insertion_rate = true;

	public static Locale locale = Locale.getDefault();

	private static boolean archiver = false;
	private static boolean monitor = false;
	private static Vector<HarmonicaExceptionListener> listeners = 
		new Vector<HarmonicaExceptionListener>();

	private HarmonicaManager(){}

	/**
	 * �f�o�b�O�̏o�͂��s���D<BR>
	 * ��Ɋe���W���[���̓����ŗ��p����D
	 *
	 * @param module_name �o�͂���l�̖��O
	 * @param target �o�͂���������
	 */
	public static void debug(String module_name, Object target){
		if(!show_debug) return;
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [");
		buffer.append(module_name);
		buffer.append("] ");
		if(target != null) buffer.append(target);
		else buffer.append("null");

		System.out.println(buffer);
	}
	
	/**
	 * Harmonica�̗�O���󂯎�郊�X�i��ݒ肷��D
	 *
	 * @param l ��O���󂯎�郊�X�i
	 */
	public static void addHarmonicaExceptionListener
		(HarmonicaExceptionListener l){
		listeners.add(l);
	}

	/**
	 * Harmonica�̗�O���󂯎�郊�X�i���폜����D
	 *
	 * @param l ��O���󂯎���Ă������X�i
	 */
	public static void removeHarmonicaExceptionListener
		(HarmonicaExceptionListener l){
		listeners.remove(l);
	}

	/**
	 * ��O��ݒ肷��D
	 *
	 * @param e Harmoncia�Ŕ���������O
	 */
	public static void createdException(HarmonicaException e){
		if(show_exception){
			if(show_debug){
				e.printStackTrace();
			}else{
				System.out.println(e.getMessage());
			}
		}

		for(HarmonicaExceptionListener l : listeners){
			l.threwException(e);
		}
	}

	/**
	 * XML���o�͂���
	 */
	public static void printXML(Node n){
		if(!show_debug) return;
        String xml_tree = "";
        try{
            StreamSource stream = null;
			URL url = HarmonicaManager.class.getResource
				("/conf/harmonica/style.xsl");
			if(url != null)
				stream = new StreamSource(url.toString());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = null;
			if(stream != null)
				transformer = factory.newTransformer(stream);
			else
				transformer = factory.newTransformer();

            DOMSource source = new DOMSource(n);
            ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(out_stream);
            transformer.transform(source,result);

            xml_tree = out_stream.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(xml_tree);
    }

	/**
	 * StreamArchiver�C���X�^���X���擾����D
	 * @return StreamArchiver�C���X�^���X
	 */
	public static StreamArchiver getStreamArchiver(){
		archiver = true;
		return StreamArchiverImpl.getInstance();
	}
	
	/**
	 * HamQLParser�C���X�^���X���擾����D
	 * @return HamQLParser�C���X�^���X
	 */
	public static HamQLParser getHamQLParser(){
		return HamQLParser.getInstance();
	}
	
	/**
	 * FeasibilityValidator�C���X�^���X���擾����D
	 * @return FeasibilityValidator�C���X�^���X
	 */
	public static FeasibilityValidator getFeasibilityValidator(){
		return FeasibilityValidatorImpl.getInstance();
	}

	public static HarmonicaMonitor getHarmonicaMonitor(){
		monitor = true;
		return HarmonicaMonitor.getInstance();
	}

	/**
	 * Harmonica�V�X�e���̏I���������s���D
	 */
	public static void terminate(){
		try{
			if(archiver){
				StreamArchiverImpl.getInstance().terminate();
				archiver = false;
			}

			if(monitor){
				HarmonicaMonitor.getInstance().terminate();
				monitor = false;
			}
		}catch(HarmonicaException e){
			createdException(e);
		}
	}

	protected void finalize() throws Throwable{
		debug("HarmonicaManager","finalize");
		terminate();
		super.finalize();
	}
}
