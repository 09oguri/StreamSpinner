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

import java.util.*;

/**
 * 問合せ判定器の状態を監視するリスナ
 *
 * @author snic
 * @version 1.0 (2006.8.4)
 */
public interface FeasibilityValidatorListener extends EventListener {
	/**
	 * 問合せの判定結果が生成された時に呼び出されるメソッド
	 *
	 * @param result 生成された判定結果
	 */
	public void generatedResult(ValidatingResult result);

	/*
	 * 問合せをメディエータに登録した時に呼び出されるメソッド
	 *
	 * @param qid 登録された問合せのID
	 */
	//public void startedQuery(String qid);

	/*
	 * 問合せをメディエータから削除した時に呼び出されるメソッド
	 *
	 * @param qid 削除された問合せのID
	 */
	//public void stoppedQuery(String qid);
}
