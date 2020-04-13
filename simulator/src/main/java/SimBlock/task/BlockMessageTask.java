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
import static SimBlock.simulator.Timer.*;

import SimBlock.node.Block;
import SimBlock.node.Node;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class BlockMessageTask extends AbstractMessageTask {

	private Block block;
	private double interval;

	public BlockMessageTask(Node from, Node to, Block block , double delay) {
		super(from, to);
		this.block = block;
		this.interval = getLatency(this.getFrom().getRegion(), this.getTo().getRegion()) + delay;
	}

	public double getInterval(){
		return this.interval;
	}

	public void run(ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Double>> observedPropagations, double[] currentTime, long[] blockInterval, int[] difficultyInterval, double[] averageDifficulty){
		this.getFrom().sendNextBlockMessage(taskQueue,taskMap,currentTime);

		//OUT_JSON_FILE.print("{");
		//OUT_JSON_FILE.print(	"\"kind\":\"flow-block\",");
		//OUT_JSON_FILE.print(	"\"content\":{");
		//OUT_JSON_FILE.print(		"\"transmission-timestamp\":" + (currentTime - this.interval) + ",");
		//OUT_JSON_FILE.print(		"\"reception-timestamp\":" + currentTime + ",");
		//OUT_JSON_FILE.print(		"\"begin-node-id\":" + getFrom().getNodeID() + ",");
		//OUT_JSON_FILE.print(		"\"end-node-id\":" + getTo().getNodeID() + ",");
		//OUT_JSON_FILE.print(		"\"block-id\":" + block.getId());
		//OUT_JSON_FILE.print(	"}");
		//OUT_JSON_FILE.print("},");
		//OUT_JSON_FILE.flush();
		super.run(simulatedNodes, taskQueue, taskMap, observedBlocks, observedPropagations, currentTime, blockInterval, difficultyInterval, averageDifficulty);
	}

	public Block getBlock(){
		return this.block;
	}


}
