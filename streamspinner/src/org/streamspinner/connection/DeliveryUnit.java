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
package org.streamspinner.connection;

import java.io.Serializable;
import java.util.Vector;
import org.streamspinner.StreamSpinnerException;
import org.streamspinner.engine.TupleSet;
import org.streamspinner.engine.Tuple;

public class DeliveryUnit implements Serializable {

	private long timestamp;
	private long seqno;
	private Vector<Tuple> tuples;

	public DeliveryUnit(long timestamp, long seqno, TupleSet ts) throws StreamSpinnerException {
		this.timestamp = timestamp;
		this.seqno = seqno;
		tuples = new Vector<Tuple>();
		while(ts.next())
			tuples.add(ts.getTuple());
	}

	public long getSequenceNumber(){
		return seqno;
	}

	public Vector<Tuple> getTuples(){
		return tuples;
	}

	public long getTimestamp(){
		return timestamp;
	}

}
