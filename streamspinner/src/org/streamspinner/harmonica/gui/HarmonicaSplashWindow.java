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

import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Locale;

import org.streamspinner.harmonica.*;

/**
 * �X�v���b�V���E�C���h�E��\�����邽�߂̃N���X�D<BR>
 * 
 * <PRE>
 * </PRE>
 *
 * @author snic
 * @version 1.0 (2006.8.4)
 */
public class HarmonicaSplashWindow extends JWindow {
	BufferedImage img = null;
	private JProgressBar bar = null;
	private JLabel label = null;
	private int current = 0;

	/**
	 * �������D
	 */
	public HarmonicaSplashWindow(){
		try{
			img = (BufferedImage)ImageFactory.getInstance().getImage
				(ImageType.HARMONICA_LOGO);
		}catch(Exception e){
			HarmonicaManager.createdException(new HarmonicaException(e));
		}
		
		super.setContentPane(new Container(){
			public void paint(Graphics g){
				if(img != null) g.drawImage(img,0,0,null);
				super.paint(g);
			}
		});

		getContentPane().setLayout(null);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height;

		int gw = 400;
		int gh = 300;
		
		if(img != null){
			gw = img.getWidth();
			gh = img.getHeight();
		}

		// �����\��
		super.setBounds((w-gw)/2,(h-gh)/2,gw,gh+50);
		super.setBackground(new Color(200,200,240));

		// �v���O���X�o�[
		bar = new JProgressBar();
		bar.setIndeterminate(true);
		int w2 = gw - 80;
		int h2 = 20;
		bar.setBounds(40,gh+20,w2,h2);
		getContentPane().add(bar);

		// ���x��
		label = new JLabel();
		if(HarmonicaManager.locale.equals(Locale.JAPAN))
			label.setText
				("Harmonica�̏�������");
		else
			label.setText("Initializing Harmonica System. Please wait ...");
		label.setBounds(40,gh,w2,20);
		getContentPane().add(label);
	}

	/**
	 * �X�v���b�V���E�C���h�E�̕\�������邩�ǂ��������߂�D
	 *
	 * @param isshown �\�����邩�ǂ���
	 */
	public void setVisible(boolean isshown){
		super.setVisible(isshown);
	}
	/**
	 * �\�����镶�����ݒ肷��D
	 *
	 * @param text �\�����镶����
	 */
	public void setText(String text){
		label.setText(text);
		repaint();
	}
	/**
	 * �v���O���X�o�[�̐i����ݒ肷��D
	 *
	 * @param n �i��(%)
	 */
	public synchronized void setValue(int n){
		bar.setIndeterminate(false);
		bar.setStringPainted(true);
		setValue(current, n);
		current = n;
	}
	private void setValue(int s, int e){
		if(s<=e){ bar.setValue(s); }else{ return; }
		
		try{
			Thread.sleep(10);
		}catch(Exception ex){
			HarmonicaManager.createdException(new HarmonicaException(ex));	
		}
		
		setValue(++s,e);
		repaint();
	}
}
