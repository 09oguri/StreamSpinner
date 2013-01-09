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
package org.streamspinner.harmonica.optimizer;

import java.util.*;

public class PaintNode {
	public Node node = null;
	public int x = -1;
	public int y = -1;

	public Vector<PaintNode> next;
	public Vector<PaintNode> prev;

	public int level;

	public PaintNode(Node node){
		this.node = node;
		next = new Vector<PaintNode>();
		prev = new Vector<PaintNode>();
	}

	public String toString(){
		return node.id;
	}
}
