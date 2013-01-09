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

import org.streamspinner.harmonica.*;
import org.w3c.dom.Document;

/**
 * 問合せ処理が永続化可能かどうかを判定する判定器のインタフェース．
 *
 * @author snic
 * @version 1.0 (2006.8.4)
 */
public interface FeasibilityValidator {
	/**
	 * 問合せ記述から永続化可能かを判定し，判定結果を返す．
	 * @param statement 問合せ記述
	 * @return 判定結果 
	 */
	public ValidatingResult getResultAfterValidate(String statement)
	throws HarmonicaException;

	/**
	 * 問合せ記述から永続化可能かを判定し，判定結果を返す．
	 * @param statement 問合せ記述
	 * @return DOM(処理可能でない場合はnull) 
	 */
	public Document validateQuery(String statement) 
	throws HarmonicaException;
	
	/**
	 * 判定器のモニタリング用のリスナを登録する．
	 * @param listener モニタリングを行うリスナ
	 */
	public void addFeasibilityValidatorListener
	(FeasibilityValidatorListener listener);

	/**
	 * 判定器のモニタリングを行っているリスナの登録を解除する．
	 *
	 * @param listener モニタリングを行っているリスナ
	 */
	public void removeFeasibilityValidatorListener
	(FeasibilityValidatorListener listener);

	/*
	 * 判定済みの問合せをメディエータに登録する．
	 *
	 * @param query 判定済みの問合せ
	 */
	//public String startQuery(ValidatingResult query);

	/*
	 * メディエータに登録されている問合せをキャンセルする．
	 */
	//public void stopQuery(String qid);
}
