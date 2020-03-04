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
import static SimBlock.simulator.Timer.*;
import static SimBlock.settings.SimulationConfiguration.*;


public class Simulator {
    //private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	private static long targetInterval;// = 1000*60*10;//msec
	private static long targetIntervalGA;
	private static double averageDifficulty;
	private static double averageDifficultyGA;
	//public Timer myTime = new Timer();

	//public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static double getAverageDifficulty(){ return averageDifficulty; }
	public static double getAverageDifficultyGA(){return averageDifficultyGA;}
	public static void setTargetIntervalGA(long interval) {targetIntervalGA = interval;}
	public static void setTargetInterval(long interval){ targetInterval = interval; }
	
	public static void addNode(Node node, ArrayList<Node> simulatedNodes){
		simulatedNodes.add(node);
		//setAverageDifficulty();
		setInitialDifficulty(simulatedNodes);
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
	 * @param simulatedNodes*/

	public static void setBitcoinAverageDifficulty(ArrayList<Node> simulatedNodes){

		int myCounter = 0;
		int counter = 0;
		long myTargetInterval = 0;
		long totalMiningPower = 0;
		for(Node node : simulatedNodes){
			totalMiningPower +=  node.getMiningPower();

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
			 myCounter = GA_DIFFICULTY_INTERVAL;
			 counter = myCounter;
			 myTargetInterval = targetIntervalGA;
		}

		double minimumDifficulty = 1;
		double oldDifficulty = 0;
		if(runningGA == false)
		{
			oldDifficulty = getAverageDifficulty();
		}
		else
		{
			oldDifficulty = getAverageDifficultyGA();
		}
		double lastBlockTime = myBlock.getTime();
		double nPowTargetTimespan = 0;
		nPowTargetTimespan	= (myTargetInterval/1000) * myCounter;
		for (int i = 1; i < (counter - 1) ; i ++)
		{
			myBlock = myBlock.getParent();
		}
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
		//oldDifficulty = 999999999999999999L;
		//long testing = (long)oldDifficulty * (long)(nPowTargetTimespan/totalInterval);
		//System.out.println("Testing = " + testing);
		System.out.println("New Average difficulty = " + newDifficulty);
		averageDifficulty = newDifficulty;
		System.out.println("Updated new difficulty = " + averageDifficulty);
		System.out.println();
	}

	public static void setDogecoinAverageDifficulty(ArrayList<Node> simulatedNodes){
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
			myCounter = GA_DIFFICULTY_INTERVAL;
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
		averageDifficulty = newDifficulty;
		System.out.println("Updated new difficulty = " + averageDifficulty);
		System.out.println();

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

	private static void setInitialDifficulty(ArrayList<Node> simulatedNodes){
		long totalMiningPower = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();

		}
		//System.out.println("My total mining power = " +totalMiningPower);
		if(totalMiningPower != 0){
			if(runningGA == false) {
				averageDifficulty =  totalMiningPower * targetInterval;
			}
			else {
				averageDifficultyGA = totalMiningPower * targetIntervalGA;
			}
		}
	}
	
	//
	// Record block propagation time
	// For saving memory, Record only the latest 10 Blocks
	//
	private static ArrayList<Block> observedBlocks = new ArrayList<Block>();
	private static ArrayList<LinkedHashMap<Integer, Long>> observedPropagations = new ArrayList<LinkedHashMap<Integer, Long>>();
	
	public static void arriveBlock(Block block,Node node){
		if(observedBlocks.contains(block)){
			LinkedHashMap<Integer, Long> Propagation = observedPropagations.get(observedBlocks.indexOf(block));
			Propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
		}else{
			if(observedBlocks.size() > 10){
				printPropagation(observedBlocks.get(0),observedPropagations.get(0));
				observedBlocks.remove(0);
				observedPropagations.remove(0);
			}
			LinkedHashMap<Integer, Long> propagation = new LinkedHashMap<Integer, Long>();
			propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
			observedBlocks.add(block);
			observedPropagations.add(propagation);
		}
	}
	
	public static void printPropagation(Block block, LinkedHashMap<Integer, Long> propagation){
		System.out.println(block + ":" + block.getHeight());
		int printCounter = 0;
		for(Map.Entry<Integer, Long> timeEntry : propagation.entrySet()){
			printCounter = printCounter + 1;
			if(printCounter == (NUM_OF_NODES/2) )
			{

				Main.midPropagationTime = Main.midPropagationTime + timeEntry.getValue();
			}
			if(printCounter%propagation.size() == 0)
			{
				Main.myMedian.add(timeEntry.getValue());
				Main.meanblockpropagationTime = Main.meanblockpropagationTime + timeEntry.getValue();
				System.out.println("node id = " + timeEntry.getKey() + ", " + "propagation time = " + timeEntry.getValue());
			}

		}
		System.out.println();
	}
	
	public static void printAllPropagation(){
		for(int i=0;i < observedBlocks.size();i++){
			printPropagation(observedBlocks.get(i), observedPropagations.get(i));
		}
	}
	
}
