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
import java.io.*;
import java.net.URL;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * �摜�����x���ǂݍ��܂Ȃ��悤�ɁC�����摜���ė��p���邽�߂̃N���X
 *
 * <PRE>
 * 1.1 - jar����摜���\�[�X��ǂݍ��߂�悤�ɕύX
 * </PRE>
 *
 * @author snic
 * @version 1.1 (2006.8.30)
 */
public class ImageFactory {
	private static ImageFactory imgf = new ImageFactory();
	private static Map<NodeType, Image> node_map = null;
	private static Map<OperatorType, Image> operator_map = null;
	private static Map<ImageType, Image> able_map = null;
	private static final String path = "/conf/harmonica/images/";
	private static final String file_path = "conf//harmonica//images//";

	private ImageFactory(){}

	/**
	 * �C���X�^���X���擾����D
	 *
	 * @return ImageFactory�C���X�^���X
	 */
	public static ImageFactory getInstance(){
		if(node_map == null) imgf.Initialize();
		return imgf;
	}

	private void Initialize(){
		node_map = new HashMap<NodeType, Image>();
		operator_map = new HashMap<OperatorType, Image>();
		able_map = new HashMap<ImageType, Image>();
	}

	/**
	 * ���Z�̃^�C�v�ɍ������摜���擾����D
	 *
	 * @param type ���Z�̃^�C�v
	 * @return �^�C�v�ɍ������摜
	 */
	public Image getImage(OperatorType type) throws HarmonicaException{
		try{
			Image img = operator_map.get(type);

			if(img != null) return img;

			String file = null;
			switch(type){
				case SELECTION:
					file = "selection.png";
					break;
				case PROJECTION:
					file = "projection.png";
					break;
				case CARTESIAN_PRODUCT:
					file = "cartesian_product.png";
					break;
				case JOIN:
					file = "join.png";
					break;
				case EVAL:
					file = "eval.png";
					break;
				case RENAME:
					file = "rename.png";
					break;
				case INSERTION:
					file = "insertion.png";
					break;
				case GROUP:
					file = "group.png";
					break;
				case UNION:
					file = "union.png";
					break;
				case ROOT:
					file = "root.png";
					break;
			}

			URL url = getClass().getResource(path+file);
			if(url != null){
				// ���\�[�X���擾
				img = ImageIO.read(url);
			}else{
				// ���\�[�X���擾�ł��Ȃ������Ƃ��́C���[�J����T��
				img = ImageIO.read(new File(file_path+file));
			}

			operator_map.put(type, img);

			return img;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}

	/**
	 * �m�[�h�̃^�C�v�ɍ������摜���擾����D
	 *
	 * @param type �m�[�h�̃^�C�v
	 * @return �^�C�v�ɍ������摜
	 */
	public Image getImage(NodeType type) throws HarmonicaException{
		try{
			Image img = node_map.get(type);
			
			if(img != null) return img;

			String file = null;
			switch(type){
				case SOURCE:
					file = "source.png";
					break;
				case RDB:
					file = "rdb.png";
					break;
				case HARMONICA:
					file = "harmonica_source.png";
					break;
			}

			// ���\�[�X���擾
			URL url = getClass().getResource(path+file);
			if(url != null){
				// ���\�[�X���擾
				img = ImageIO.read(url);
			}else{
				// ���\�[�X���擾�ł��Ȃ������Ƃ��́C���[�J����T��
				img = ImageIO.read(new File(file_path+file));
			}

			if(img != null) node_map.put(type, img);

			return img;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}

	/**
	 * �擾����摜�̃^�C�v�ɍ������摜���擾����D
	 *
	 * @param type �摜�̃^�C�v
	 * @return �^�C�v�ɍ������摜
	 */
	public Image getImage(ImageType type) throws HarmonicaException{
		try{
			Image img = able_map.get(type);

			if(img != null) return img;

			String file = null;
			switch(type){
				case PROCESSABLE:
					file = "processable.png";
					break;
				case UNPROCESSABLE:
					file = "unprocessable.png";
					break;
				case INSERTABLE:
					file = "insertable.png";
					break;
				case INSERTABLE_GRAY:
					file = "insertable_gray.png";
					break;
				case UNINSERTABLE:
					file = "uninsertable.png";
					break;
				case QUERY_WAITING:
					file = "waiting_query.png";
					break;
				case QUERY_RUNNING:
					file = "running_query.png";
					break;
				case QUERY_CANCELED:
					file = "canceled_query.png";
					break;
				case DBS_ICON:
					file = "dbs.png";
					break;
				case DB_ICON:
					file = "db.png";
					break;
				case DB_MAIN_ICON:
					file = "db_main.png";
					break;
				case TABLE_ICON:
					file = "table.png";
					break;
				case TABLE_UPDATE_ICON:
					file = "table_update.png";
					break;
				case TREE_ICON:
					file = "tree_icon.png";
					break;
				case HAMQL_ICON:
					file = "create.png";
					break;
				case OPEN_ICON:
					file = "open.png";
					break;
				case RELOAD_ICON:
					file = "reload.png";
					break;
				case CONNECTOR_ICON:
					file = "connector.png";
					break;
				case FLOW_ICON:
					file = "flow.png";
					break;
				case HARMONICA_ICON:
					file = "harmonica-icon.png";
					break;
				case HARMONICA_LOGO:
					file = "harmonica.png";
					break;
				case ABLE_BAR:
					file = "able_bar.png";
					break;
				case ABLE_BAR_GRAY:
					file = "able_bar_gray.png";
					break;
				case OPT_INFORMATION:
					file = "opt-source.png";
					break;
				case OPT_ROOT:
					file = "opt-root.png";
					break;
				case OPT_SELECTION:
					file = "opt-selection.png";
					break;
				case OPT_PROJECTION:
					file = "opt-projection.png";
					break;
				case OPT_JOIN:
					file = "opt-join.png";
					break;
				case OPT_STORE:
					file = "opt-store.png";
					break;
				case PUSHED_OPT_INFORMATION:
					file = "pushed_source.png";
					break;
				case PUSHED_OPT_ROOT:
					file = "pushed_root.png";
					break;
				case PUSHED_OPT_SELECTION:
					file = "pushed_selection.png";
					break;
				case PUSHED_OPT_PROJECTION:
					file = "pushed_-projection.png";
					break;
				case PUSHED_OPT_JOIN:
					file = "pushed_join.png";
					break;
				case PUSHED_OPT_STORE:
					file = "pushed_store.png";
					break;
			}

			URL url = getClass().getResource(path+file);
			if(url != null){
				// ���\�[�X���擾
				img = ImageIO.read(url);
			}else{
				// ���\�[�X���擾�ł��Ȃ������Ƃ��́C���[�J����T��
				img = ImageIO.read(new File(file_path+file));
			}

			if(img != null) able_map.put(type, img);

			return img;
		}catch(Exception e){
			throw new HarmonicaException(e);
		}
	}
}
