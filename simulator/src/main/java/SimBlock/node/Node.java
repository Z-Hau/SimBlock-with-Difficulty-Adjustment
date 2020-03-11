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
package SimBlock.node;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.simulator.Network.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;

import java.util.*;

import SimBlock.node.routingTable.AbstractRoutingTable;
import SimBlock.task.AbstractMessageTask;
import SimBlock.task.BlockMessageTask;
import SimBlock.task.InvMessageTask;
import SimBlock.task.MiningTask;
import SimBlock.task.RecMessageTask;
import SimBlock.task.Task;

public class Node {
	private volatile int region;
	private volatile int nodeID;
	private volatile long miningPower;
	private volatile AbstractRoutingTable routingTable;
	private volatile Block block;
	private volatile Set<Block> orphans = new HashSet<Block>();

	private volatile Task executingTask = null;

	private volatile boolean sendingBlock = false;
	private volatile ArrayList<RecMessageTask> messageQue = new ArrayList<RecMessageTask>();
	private volatile Set<Block> downloadingBlocks = new HashSet<Block>();

	private volatile long processingTime = 2;


	public Node(int nodeID,int nConnection ,int region, long miningPower, String routingTableName){
		this.nodeID = nodeID;
		this.region = region;
		this.miningPower = miningPower;
		try {
			this.routingTable = (AbstractRoutingTable) Class.forName(routingTableName).getConstructor(Node.class).newInstance(this);
			this.setnConnection(nConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getNodeID(){ return this.nodeID; }
	public Block getBlock(){ return this.block; }
	public long getMiningPower(){ return this.miningPower; }
	public void setMiningPower(long miningPower) { this.miningPower = miningPower;}
	public Set<Block> getOrphans(){ return this.orphans; }
	public void setRegion(int region){ this.region = region; }
	public int getRegion(){ return this.region; }

	public boolean addNeighbor(Node node){ return this.routingTable.addNeighbor(node); }
	public boolean removeNeighbor(Node node){ return this.routingTable.removeNeighbor(node); }
	public ArrayList<Node> getNeighbors(){ return this.routingTable.getNeighbors(); }
	public AbstractRoutingTable getRoutingTable(){ return this.routingTable; }
	public void setnConnection(int nConnection){ this.routingTable.setnConnection(nConnection); }
	public int getnConnection(){ return this.routingTable.getnConnection(); }


	public void joinNetwork(ArrayList<Node> simulatedNodes, long currentTime){
		this.routingTable.initTable(simulatedNodes,currentTime);
	}

	public void genesisBlock(ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Long>> observedPropagations, long currentTime){
		Block genesis = new Block(1, null, this, 0,1);
		this.receiveBlock(genesis,simulatedNodes,taskQueue,taskMap,observedBlocks,observedPropagations,currentTime);
	}

	public synchronized void addToChain(Block newBlock, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Long>> observedPropagations, long currentTime) {
		if(this.executingTask != null){
			removeTask(this.executingTask,taskQueue,taskMap);
			this.executingTask = null;
		}
		this.block = newBlock;
		printAddBlock(newBlock,currentTime);
		arriveBlock(newBlock, this,observedBlocks,observedPropagations,currentTime);
	}

	private void printAddBlock(Block newBlock, long currentTime){
		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"add-block\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + currentTime + ",");
		OUT_JSON_FILE.print(		"\"node-id\":" + this.getNodeID() + ",");
		OUT_JSON_FILE.print(		"\"block-id\":" + newBlock.getId());
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("},");
		OUT_JSON_FILE.flush();
	}

	public void addOrphans(Block newBlock, Block correctBlock){
		if(newBlock != correctBlock){
			this.orphans.add(newBlock);
			this.orphans.remove(correctBlock);
			if(newBlock.getParent() != null && correctBlock.getParent() != null){
				this.addOrphans(newBlock.getParent(),correctBlock.getParent());
			}
		}
	}

	public synchronized void mining(ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, long currentTime){
		Task task = new MiningTask(this);
		this.executingTask = task;
		putTask(task,taskQueue,taskMap,currentTime);
	}

	public synchronized  void sendInv(Block block, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, long currentTime){
		for(Node to : this.routingTable.getNeighbors()){
			AbstractMessageTask task = new InvMessageTask(this,to,block);
			putTask(task, taskQueue, taskMap, currentTime);
		}
	}

	public synchronized void receiveBlock(Block receivedBlock, ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Long>> observedPropagations, long currentTime){
		Block sameHeightBlock;

		if(this.block == null){
			this.addToChain(receivedBlock,taskQueue,taskMap,observedBlocks,observedPropagations,currentTime);
			this.mining(simulatedNodes,taskQueue,taskMap,currentTime);
			this.sendInv(receivedBlock,taskQueue,taskMap,currentTime);

		}else if(receivedBlock.getHeight() > this.block.getHeight()){
			sameHeightBlock = receivedBlock.getBlockWithHeight(this.block.getHeight());
			if(sameHeightBlock != this.block){
				this.addOrphans(this.block, sameHeightBlock);
			}
			this.addToChain(receivedBlock, taskQueue, taskMap, observedBlocks, observedPropagations, currentTime);
			this.mining(simulatedNodes, taskQueue, taskMap, currentTime);
			this.sendInv(receivedBlock, taskQueue, taskMap, currentTime);

		}else if(receivedBlock.getHeight() <= this.block.getHeight()){
			sameHeightBlock = this.block.getBlockWithHeight(receivedBlock.getHeight());
			if(!this.orphans.contains(receivedBlock) && receivedBlock != sameHeightBlock){
				this.addOrphans(receivedBlock, sameHeightBlock);
				arriveBlock(receivedBlock, this, observedBlocks, observedPropagations, currentTime);
			}
		}

	}

	public synchronized  void receiveMessage(AbstractMessageTask message, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Node> simulatedNodes, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Long>> observedPropagations, long currentTime){
		Node from = message.getFrom();

		if(message instanceof InvMessageTask){
			Block block = ((InvMessageTask) message).getBlock();
			if(!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)){
				if(this.block == null || block.getHeight() > this.block.getHeight()){
					AbstractMessageTask task = new RecMessageTask(this,from,block);
					putTask(task, taskQueue, taskMap, currentTime);
					downloadingBlocks.add(block);
				}else{

					// get orphan block
					if(block != this.block.getBlockWithHeight(block.getHeight())){
						AbstractMessageTask task = new RecMessageTask(this,from,block);
						putTask(task, taskQueue, taskMap, currentTime);
						downloadingBlocks.add(block);
					}
				}
			}
		}

		if(message instanceof RecMessageTask){
			this.messageQue.add((RecMessageTask) message);
			if(!sendingBlock){
				this.sendNextBlockMessage(taskQueue, taskMap, currentTime);
			}
		}

		if(message instanceof BlockMessageTask){
			Block block = ((BlockMessageTask) message).getBlock();
			downloadingBlocks.remove(block);
			this.receiveBlock(block, simulatedNodes, taskQueue, taskMap, observedBlocks, observedPropagations, currentTime);
		}
	}

	// send a block to the sender of the next queued recMessage
	public synchronized  void sendNextBlockMessage(PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, long currentTime){
		if(this.messageQue.size() > 0){

			sendingBlock = true;

			Node to = this.messageQue.get(0).getFrom();
			Block block = this.messageQue.get(0).getBlock();
			this.messageQue.remove(0);
			long blockSize = BLOCKSIZE;
			long bandwidth = getBandwidth(this.getRegion(),to.getRegion());
			long delay = blockSize * 8 / (bandwidth/1000) + processingTime;
			BlockMessageTask messageTask = new BlockMessageTask(this, to, block, delay);

			putTask(messageTask, taskQueue, taskMap, currentTime);
		}else{
			sendingBlock = false;
		}
	}

}
