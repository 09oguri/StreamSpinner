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
package org.streamspinner;

import org.streamspinner.query.Query;
import org.streamspinner.query.OperatorGroup;
import org.streamspinner.engine.TupleSet;
import org.streamspinner.engine.Schema;
import java.util.Set;

public interface SystemManager {

	public void queryRegistered(Query q);

	public void queryDeleted(Query q);

	public void dataDistributedTo(long timestamp, Set queryids, TupleSet ts);

	public void dataReceived(long timestamp, String source, TupleSet ts);

	public void executionPerformed(long executiontime, String master, long duration, long delay);


	public void startCacheConsumer(long timestamp, OperatorGroup og, double ratio);

	public void endCacheConsumer(long timestamp, OperatorGroup og, double ratio);

	public void informationSourceAdded(InformationSource is);

	public void informationSourceDeleted(InformationSource is);

	public void tableCreated(String wrappername, String tablename, Schema schema);

	public void tableDropped(String wrappername, String tablename);

}
