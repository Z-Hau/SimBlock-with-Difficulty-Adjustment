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
package SimBlock.simulator;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Network.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.task.MiningTask;

import SimBlock.task.Task;


public class Main {
	public static Random random = new Random(10);
	public static long time1 = 0;//a value to know the simation time.
	public static double meanblockpropagationTime = 0;
	public static double totalMedian = 0;
	public static double midPropagationTime = 0;
	public static ArrayList <Double> myMedian = new ArrayList<>();
	//public static URI CONF_FILE_URI;
	//public static URI OUT_FILE_URI;

	/**
	static {
		try {
			CONF_FILE_URI = ClassLoader.getSystemResource("simulator.conf").toURI();
			OUT_FILE_URI = CONF_FILE_URI.resolve(new URI("../output/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}*/

	//public static PrintWriter OUT_JSON_FILE;
	//public static PrintWriter STATIC_JSON_FILE;

	/**
	static {
		try{
			OUT_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./output.json")))));
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	static {
		try{
			STATIC_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./static.json")))));
		} catch (IOException e){
			e.printStackTrace();
		}

	}*/

	public static void main(String[] args)  {

		ArrayList<Node> simulatedNodesGlobal = new ArrayList<Node>();
		PriorityQueue<ScheduledTask> taskQueueGlobal = new PriorityQueue<ScheduledTask>();
		Map<Task,ScheduledTask> taskMapGlobal = new HashMap<Task,ScheduledTask>();
		ArrayList<Block> observedBlocksGlobal = new ArrayList<Block>();
		ArrayList<LinkedHashMap<Integer, Double>> observedPropagationsGlobal = new ArrayList<LinkedHashMap<Integer, Double>>();
		double currentTime = 0.0;
		long[] blockInterval = {INTERVAL};
		int[] difficultyInterval = {DIFFICULTY_INTERVAL};
		double[] averageDifficulty = {0.0};

		long start = System.currentTimeMillis();
		//setTargetInterval(INTERVAL);
		//OUT_JSON_FILE.print("["); //start json format
		//OUT_JSON_FILE.flush();
		//printRegion();
		constructNetworkWithAllNode(NUM_OF_NODES,simulatedNodesGlobal,currentTime,blockInterval,averageDifficulty);
		simulatedNodesGlobal.get(0).genesisBlock(taskQueueGlobal,taskMapGlobal,observedBlocksGlobal,observedPropagationsGlobal,currentTime,averageDifficulty);

		int j=1;
		while(getTask(taskQueueGlobal) != null){
			if(getTask(taskQueueGlobal) instanceof MiningTask){
				MiningTask task = (MiningTask) getTask(taskQueueGlobal);
				if(task.getParent().getHeight() == j) j++;
				if(j > ENDBLOCKHEIGHT){break;}
				//if(j%100==0 || j==2) writeGraph(j);
			}
			runTask(simulatedNodesGlobal,taskQueueGlobal,taskMapGlobal,observedBlocksGlobal,observedPropagationsGlobal,currentTime,blockInterval,difficultyInterval,averageDifficulty);
		}
		printAllPropagation(observedBlocksGlobal,observedPropagationsGlobal);
		System.out.println();

		Set<Block> blocks = new HashSet<Block>();
		Block block  = simulatedNodesGlobal.get(0).getBlock();
		int counter1 = 1;
		double oldInterval = 0;
		double newInterval = 0;
		double myInterval = 0;
		double totalInterval = 0;
		while(block.getParent() != null){
			blocks.add(block);
			oldInterval = block.getTime();
			block = block.getParent();
			newInterval = block.getTime();
			myInterval = (oldInterval - newInterval)/1000; //convert to second
			/*
			try(FileWriter fw = new FileWriter("C:\\Users\\zihau\\Desktop\\simblock\\blockTime.csv", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw))
			{
				//out.println(myInterval);

			} catch (IOException e) {
				//exception handling left as an exercise for the reader
			}*/
			//System.out.println(oldInterval+ " - " + newInterval+ " = " + myInterval);
			totalInterval=totalInterval+myInterval;
			counter1 = counter1+1;
		}
		//System.out.println("My total average interval = " + (totalInterval/counter1));

		Set<Block> orphans = new HashSet<Block>();
		int averageOrhansSize =0;
		for(Node node :simulatedNodesGlobal){
			orphans.addAll(node.getOrphans());
			averageOrhansSize += node.getOrphans().size();
		}
		int averageorphanSize = averageOrhansSize;
		averageOrhansSize = averageOrhansSize/simulatedNodesGlobal.size();

		blocks.addAll(orphans);

		ArrayList<Block> blockList = new ArrayList<Block>();
		blockList.addAll(blocks);
		Collections.sort(blockList, new Comparator<Block>(){
			@Override
			public int compare(Block a, Block b){
				int order = (int) Math.signum(a.getTime() - b.getTime());
				if(order != 0) return order;
				order = System.identityHashCode(a) - System.identityHashCode(b);
				return order;
			}
		});
		int counter = 0;
		for(Block orphan : orphans){
			//System.out.println(orphan+ ":" +orphan.getHeight());
			counter = counter + 1;
		}
		System.out.println("Average orphan size (simblock) = " +averageOrhansSize);
		System.out.println("Number of orphan (mine) = "+counter);

		/**
		try {
			FileWriter fw = new FileWriter(new File(OUT_FILE_URI.resolve("./blockList.txt")), false);
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

			for(Block b:blockList){
				if(!orphans.contains(b)){
					pw.println("OnChain : "+b.getHeight()+" : "+b);
				}else{
					pw.println("Orphan : "+b.getHeight()+" : "+b);
				}
			}
			pw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}*/

		/**
		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"simulation-end\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + currentTime);
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("}");
		OUT_JSON_FILE.print("]"); //end json format
		OUT_JSON_FILE.close();
		 */
		long end = System.currentTimeMillis();
		time1 += end -start;
		System.out.println("Elapsed time (ms) = "+time1);
		System.out.println("Total block propagation time  = " + (meanblockpropagationTime/1000));
		Collections.sort(myMedian);
		int myLength = myMedian.size();
		totalMedian = (myMedian.get((myLength/2))) + (myMedian.get((myLength/2-1)));
		totalMedian = totalMedian/2;
		System.out.println("Median block propagation time: "+((midPropagationTime/ENDBLOCKHEIGHT)/1000));
		System.out.println("My median: " + (totalMedian/1000));
		System.out.println("Mean block propagation time: "+((meanblockpropagationTime/ENDBLOCKHEIGHT)/1000));

		try(FileWriter fw = new FileWriter("C:\\Users\\zihau.chin\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\myData.csv", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
		{
			out.println(averageorphanSize + "," + averageOrhansSize + "," + (meanblockpropagationTime/ENDBLOCKHEIGHT) + "," + (midPropagationTime/ENDBLOCKHEIGHT) + "," + totalMedian
					+ "," + counter + "," + meanblockpropagationTime + "," + (totalInterval/counter1) );


		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}


	//TODO　以下の初期生成はシナリオを読み込むようにする予定
	//ノードを参加させるタスクを作る(ノードの参加と，リンクの貼り始めるタスクは分ける)
	//シナリオファイルで上の参加タスクをTimer入れていく．

	public static ArrayList<Integer> makeRandomList(double[] distribution ,boolean facum){
		ArrayList<Integer> list = new ArrayList<Integer>();
		int index=0;

		if(facum){
			for(; index < distribution.length ; index++){
				while(list.size() <= NUM_OF_NODES * distribution[index]){
					list.add(index);
				}
			}
			while(list.size() < NUM_OF_NODES){
				list.add(index);
			}
		}else{
			double acumulative = 0.0;
			for(; index < distribution.length ; index++){
				acumulative += distribution[index];
				while(list.size() <= NUM_OF_NODES * acumulative){
					list.add(index);
				}
			}
			while(list.size() < NUM_OF_NODES){
				list.add(index);
			}
		}

		Collections.shuffle(list, random);
		return list;
	}

	public static int genMiningPower(){
		double r = random.nextGaussian();

		return  Math.max((int)(r * STDEV_OF_MINING_POWER + AVERAGE_MINING_POWER),1);
	}

	public static long randomMiningPower(long oldMiningPower) {
		//double r = random.nextGaussian();
		if ((random.nextGaussian() <= MINING_POWER_INCREASE_PERCENTAGE)) {
			return Math.round(oldMiningPower * (1 + MINING_POWER_CHANGE_RATIO));
		} else {
			return Math.round(oldMiningPower / (1 + MINING_POWER_CHANGE_RATIO));
		}
	}

	public static void constructNetworkWithAllNode(int numNodes, ArrayList<Node> simulatedNodes, double currentTime, long[] blockInterval, double[] averageDifficulty){
		//List<String> regions = new ArrayList<>(Arrays.asList("NORTH_AMERICA", "EUROPE", "SOUTH_AMERICA", "ASIA_PACIFIC", "JAPAN", "AUSTRALIA", "OTHER"));
		double[] regionDistribution = getRegionDistribution();
		List<Integer> regionList  = makeRandomList(regionDistribution,false);
		double[] degreeDistribution = getDegreeDistribution();
		List<Integer> degreeList  = makeRandomList(degreeDistribution,true);

		for(int id = 1; id <= numNodes; id++){
			Node node = new Node(id,degreeList.get(id-1)+1,regionList.get(id-1), genMiningPower(),TABLE);
			addNode(node,simulatedNodes,blockInterval,averageDifficulty);
			//OUT_JSON_FILE.print("{");
			//OUT_JSON_FILE.print(	"\"kind\":\"add-node\",");
			//OUT_JSON_FILE.print(	"\"content\":{");
			//OUT_JSON_FILE.print(		"\"timestamp\":0,");
			//OUT_JSON_FILE.print(		"\"node-id\":" + id + ",");
			//OUT_JSON_FILE.print(		"\"region-id\":" + regionList.get(id-1));
			//OUT_JSON_FILE.print(	"}");
			//OUT_JSON_FILE.print("},");
			//OUT_JSON_FILE.flush();
		}
		for(Node node: simulatedNodes){
			node.joinNetwork(simulatedNodes,currentTime);
		}

	}


	/**
	 public static void writeGraph(int j){
	 try {
	 FileWriter fw = new FileWriter(new File(OUT_FILE_URI.resolve("./graph/"+ j +".txt")), false);
	 PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

	 for(int index =1;index<=simulatedNodesGlobal.size();index++){
	 Node node = simulatedNodesGlobal.get(index-1);
	 for(int i=0;i<node.getNeighbors().size();i++){
	 Node neighter = node.getNeighbors().get(i);
	 pw.println(node.getNodeID()+" " +neighter.getNodeID());
	 }
	 }
	 pw.close();

	 } catch (IOException ex) {
	 ex.printStackTrace();
	 }

	 }
	 */
}
