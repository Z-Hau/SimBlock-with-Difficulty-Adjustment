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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import SimBlock.node.Block;
import SimBlock.node.Node;

import static SimBlock.settings.SimulationConfiguration.*;


public class Simulator {
    //private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	//private static long targetInterval;// = 1000*60*10;//msec
	//private static long targetIntervalGA;
	//public static double averageDifficulty;
	//public static double averageDifficultyGA;


	//public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	/**
	public static double getAverageDifficulty()
	{
		if(runningGA == false)
		{
			return averageDifficulty;
		}
		else
		{
			return averageDifficultyGA;
		}
		return 0;

	}*/
	//public static double getAverageDifficultyGA(){return averageDifficultyGA;}
	//public static void setTargetIntervalGA(long interval) {targetIntervalGA = interval;}
	//public static void setTargetInterval(long interval){ targetInterval = interval; }
	
	public static void addNode(Node node, ArrayList<Node> simulatedNodes, long[] blockInterval, double[] averageDifficulty){
		simulatedNodes.add(node);
		//setAverageDifficulty();
		setInitialDifficulty(simulatedNodes,blockInterval,averageDifficulty);
	}

	/**
	public static void removeNode(Node node){
		simulatedNodes.remove(node);
		//setAverageDifficulty();
		setInitialDifficulty(simulatedNodes);
	}*/

	/**
	public static void addNodeWithConnection(Node node){
		node.joinNetwork();
		addNode(node, simulatedNodes);
		for(Node existingNode: simulatedNodes){
			existingNode.addNeighbor(node);
		}
	}
	 * @param blockInterval
	 * @param difficultyInterval
	 * @param averageDifficulty   */

	public static void setBitcoinAverageDifficulty(Block simulatedBlock, long[] blockInterval, int[] difficultyInterval, double[] averageDifficulty){

		int myCounter = difficultyInterval[0];
		Block currentBlock  = simulatedBlock;
		double currentBlockTime = currentBlock.getTime();
		Block lastBlock = currentBlock.getBlockWithHeight((currentBlock.getHeight() - (difficultyInterval[0]-1)));
		double lastBlockTime = lastBlock.getTime();
		double totalInterval = 0;
		double minimumDifficulty = 1;
		double oldDifficulty = averageDifficulty[0];
		double nPowTargetTimespan = 0;
		nPowTargetTimespan	= (blockInterval[0]) * myCounter;
		totalInterval = (currentBlockTime - lastBlockTime)/1000;
		if(totalInterval < (nPowTargetTimespan/4))
		{
			totalInterval = nPowTargetTimespan / 4;
		}
		if(totalInterval > nPowTargetTimespan * 4)
		{
			totalInterval = nPowTargetTimespan * 4;
		}
		if(runningGA == false)
		{
			TOTAL_INTERVAL = totalInterval;
		}
		//System.out.println("total interval = " + totalInterval);
		//System.out.println("Old average difficulty = " + averageDifficulty[0]);
		double newDifficulty = oldDifficulty * nPowTargetTimespan/totalInterval;
		if(newDifficulty <= minimumDifficulty )
		{
			newDifficulty = minimumDifficulty;
		}
		averageDifficulty[0] = newDifficulty;
		//System.out.println("Updated new difficulty = " + averageDifficulty[0]);
		//System.out.println();
	}

	public static void setDogecoinAverageDifficulty(ArrayList<Node> simulatedNodes){
		/**
		long totalMiningPower = 0;
		int myCounter = 0;
		int counter = 0;
		long myTargetInterval = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();

		}
		System.out.println("total mining power = " + totalMiningPower);
		Block myBlock  = simulatedNodes.get(0).getBlock();
		double totalInterval = 0;
		if(runningGA == false)
		{
			myCounter = DIFFICULTY_INTERVAL;
			counter = myCounter;
			myTargetInterval = targetInterval;
		}
		else
		{
			//myCounter = DIFFICULTY_INTERVAL;
			counter = myCounter;
			myTargetInterval = targetIntervalGA;
		}

		double oldDifficulty = getAverageDifficulty();
		double lastBlockTime = myBlock.getTime();
		double minimumDifficulty = 0.00024414;
		double nPowTargetTimespan = 0;
		nPowTargetTimespan	= (myTargetInterval/1000);
		myBlock = myBlock.getParent();
		totalInterval = (lastBlockTime - (myBlock.getTime()))/1000 ; //convert to sec

		if(totalInterval < (nPowTargetTimespan/4))
		{
			totalInterval = nPowTargetTimespan / 4;
		}
		if(totalInterval > nPowTargetTimespan * 4)
		{
			totalInterval = nPowTargetTimespan * 4;
		}
		System.out.println("total interval = " + totalInterval);
		System.out.println("Old average difficulty = " + oldDifficulty);
		double newDifficulty = oldDifficulty * nPowTargetTimespan/totalInterval;
		if(newDifficulty <= minimumDifficulty )
		{
			newDifficulty = minimumDifficulty;
		}
		System.out.println("New Average difficulty = " + newDifficulty);
		//averageDifficulty = newDifficulty;
		//System.out.println("Updated new difficulty = " + //averageDifficulty);
		System.out.println();
		*/
	}

	/**
	// calculate averageDifficulty from totalMiningPower
	private static void setAverageDifficulty(){
		long totalMiningPower = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();
		}

		if(totalMiningPower != 0){
			averageDifficulty =  totalMiningPower * targetInterval;
		}
	}*/

	public static void setInitialDifficulty(ArrayList<Node> simulatedNodes, long[] blockInterval, double[] averageDifficulty){
		long totalMiningPower = 0;
		double myDifficulty = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();
		}
		//System.out.println("My total mining power = " +totalMiningPower);
		if(totalMiningPower != 0){
			myDifficulty =  totalMiningPower * blockInterval[0];
		}
		averageDifficulty[0] = myDifficulty;
	}

	//
	// Record block propagation time
	// For saving memory, Record only the latest 10 Blocks
	//
	public   static void arriveBlock(Block block, Node node, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Double>> observedPropagations, double currentTime){
		if(observedBlocks.contains(block)){
			LinkedHashMap<Integer, Double> Propagation = observedPropagations.get(observedBlocks.indexOf(block));
			Propagation.put(node.getNodeID(), currentTime - block.getTime());
		}else{
			if(observedBlocks.size() > 10){
				if(runningGA == false)
				{
					printPropagation(observedBlocks.get(0),observedPropagations.get(0));
				}
				observedBlocks.remove(0);
				observedPropagations.remove(0);
			}
			LinkedHashMap<Integer, Double> propagation = new LinkedHashMap<Integer, Double>();
			propagation.put(node.getNodeID(), currentTime - block.getTime());
			observedBlocks.add(block);
			observedPropagations.add(propagation);
		}
	}
	
	public   static void printPropagation(Block block, LinkedHashMap<Integer, Double> propagation){
		//System.out.println(block + ":" + block.getHeight());
		int printCounter = 0;
		for(Map.Entry<Integer, Double> timeEntry : propagation.entrySet()){
			printCounter = printCounter + 1;
			if(printCounter == (NUM_OF_NODES/2) )
			{
				Main.midPropagationTime = Main.midPropagationTime + timeEntry.getValue();
			}
			if(printCounter%propagation.size() == 0)
			{
				Main.myMedian.add(timeEntry.getValue());
				Main.meanblockpropagationTime = Main.meanblockpropagationTime + timeEntry.getValue();
				//System.out.println("node id = " + timeEntry.getKey() + ", " + "propagation time = " + timeEntry.getValue());
			}

		}
		//System.out.println();
	}
	
	public   static void printAllPropagation(ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Double>> observedPropagations){
		for(int i=0;i < observedBlocks.size();i++){
			printPropagation(observedBlocks.get(i), observedPropagations.get(i));
		}
	}
	
}
