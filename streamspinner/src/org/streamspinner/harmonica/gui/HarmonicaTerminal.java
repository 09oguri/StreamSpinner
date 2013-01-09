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

import java.util.*;
import javax.swing.*;

/**
 * Harmonica�̃C�x���g��ߑ����ĕ\������e�L�X�g�G���A
 *
 * @author snic
 * @version 1.0 (2006.8.8)
 */
public class HarmonicaTerminal 
	extends JTextArea 
	implements HarmonicaExceptionListener {
	private StringBuffer buf = null;
	public HarmonicaTerminal(){
		super();
		HarmonicaManager.addHarmonicaExceptionListener(this);
		buf = new StringBuffer();
		String str = null;

		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			str = "[Harmonica] Harmonica (v2.0) ���J�n���܂����D";
		else
			str = "[Harmonica] Harmonica version 2.0 started.";
		
		setText(str);
	}

	/**
	 * ��O��ߑ�����D
	 *
	 * @param e Harmonica�̗�O
	 */
	public void threwException(HarmonicaException e){
		String str = "[Exception] " + e.getMessage();
		setText(str);
	}

	/**
	 * �^�[�~�i���ɕ\������e�L�X�g��ݒ肷��D
	 *
	 * @param text �\������e�L�X�g
	 */
	public void setText(String text){
		buf.append(" " + getCTime() + " " + text + "\n");
		super.setText(buf.toString());
	}

	private String getCTime(){
		StringBuilder builder = new StringBuilder();
		Calendar c = Calendar.getInstance(Locale.getDefault());
		
		builder.append(c.get(c.YEAR)+".");
		builder.append(c.get(c.MONTH)+".");
		builder.append(c.get(c.DATE)+" ");
		builder.append(c.get(c.HOUR_OF_DAY)+":");
		builder.append(c.get(c.MINUTE)+":");
		builder.append(c.get(c.SECOND));

		return builder.toString();
	}

	public void terminal(){
		if(buf == null) return;
		
		HarmonicaManager.removeHarmonicaExceptionListener(this);
		buf = null;
	}
}
