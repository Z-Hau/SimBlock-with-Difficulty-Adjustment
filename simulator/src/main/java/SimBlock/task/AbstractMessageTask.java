/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.task;
import static SimBlock.simulator.Network.*;

import SimBlock.node.Node;

import java.math.BigDecimal;

public abstract class AbstractMessageTask implements Task{
	private Node from;
	private Node to;

	public AbstractMessageTask(Node from, Node to){
		this.from = from;
		this.to = to;
	}
	public Node getFrom(){ return this.from; }
	public Node getTo(){ return this.to; }

	public BigDecimal getInterval(){
		long latency = getLatency(this.from.getRegion(), this.to.getRegion());
		return BigDecimal.valueOf(latency + 10);
	}

	public void run(){
		this.to.receiveMessage(this);
	}

}