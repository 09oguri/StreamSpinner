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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
import org.streamspinner.engine.Schema;

public interface Connection extends Remote {

	public long receiveDeliveryUnit(DeliveryUnit tuples) throws RemoteException;

	public void receiveSchema(Schema s) throws RemoteException;

	public void receiveCQException(CQException e) throws RemoteException;

	public void receiveRecoveredUnits(long newseqno, DeliveryUnit[] units) throws RemoteException ;

	public void start() throws RemoteException;

	public void stop() throws RemoteException;

	public void reconnect(RemoteStreamServer rss) throws RemoteException ;
}
