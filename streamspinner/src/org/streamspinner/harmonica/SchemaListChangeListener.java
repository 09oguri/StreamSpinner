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

import java.util.*;
import org.streamspinner.engine.*;

/**
 * �f�[�^�x�[�X�ɑ΂��ăe�[�u���̒ǉ��E�폜�̒ʒm���󂯂郊�X�i�[�D
 *
 * @author snic
 * @version 1.0 (2006.8.2)
 */
public interface SchemaListChangeListener extends EventListener {
	/**
	 * �e�[�u�����ǉ����ꂽ���Ƃ�ʒm����C�x���g�D
	 */
	public void addTable(Schema schema);
	
	/**
	 * �e�[�u�����폜���ꂽ���Ƃ�ʒm����C�x���g�D
	 */
	public void deleteTable(String table_name);

	/**
	 * DB�Ƃ̐ڑ����ǉ����ꂽ���Ƃ�ʒm����C�x���g�D
	 */
	public void addDBMSConnection(DBConnector con);

	/**
	 * DB�Ƃ̐ڑ����������ꂽ���Ƃ�ʒm����C�x���g�D
	 */
	public void deleteDBMSConnection(DBConnector con);
}
