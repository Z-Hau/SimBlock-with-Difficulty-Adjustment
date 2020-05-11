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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import SimBlock.node.Block;
import SimBlock.node.Node;
import static SimBlock.simulator.Timer.*;
import static SimBlock.settings.SimulationConfiguration.*;


public class Simulator {
	private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	private static long targetInterval;// = 1000*60*10;//msec
	private static double averageDifficulty;
	
	public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static double getAverageDifficulty(){ return averageDifficulty; }
	public static void setTargetInterval(long interval){ targetInterval = interval; }
	
	public static void addNode(Node node){
		simulatedNodes.add(node);
		//setAverageDifficulty();
		setInitialDifficulty();
	}
	
	public static void removeNode(Node node){
		simulatedNodes.remove(node);
		//setAverageDifficulty();
		setInitialDifficulty();
	}
	
	public static void addNodeWithConnection(Node node){
		node.joinNetwork();
		addNode(node);
		for(Node existingNode: simulatedNodes){
			existingNode.addNeighbor(node);
		}
	}

	public static void setBitcoinAverageDifficulty(Block createdBlock){
		Block currentBlock  = createdBlock;
		double totalInterval = 0;
		double currentBlockTime = currentBlock.getTime();
		int lastBlockHeight = 0;
		double oldnPowTargetTimespan = 0.0;
		double newnPowTargetTimespan = 0.0;
		double nPowTargetTimespan = (INTERVAL/1000) * DIFFICULTY_INTERVAL;
		double minimumDifficulty = 1;
		double oldDifficulty = getAverageDifficulty();
		double newDifficulty = 0.0;

		if(DIFFICULTY_INTERVAL != 1) {
			if(GA_TRIGGERED) {
				lastBlockHeight = ((currentBlock.getHeight()) - (DIFFICULTY_INTERVAL - 1));
				Block lastBlock = currentBlock.getBlockWithHeight(lastBlockHeight);
				Block beforeGA = currentBlock.getBlockWithHeight((GA_END_BLOCK_HEIGHT)); //to get block before GA
				totalInterval = (beforeGA.getTime() - (lastBlock.getTime())) / 1000; //in sec
				Block afterGA = currentBlock.getBlockWithHeight(GA_END_BLOCK_HEIGHT + 1); //to get block after GA
				totalInterval = totalInterval + ((currentBlockTime - (afterGA.getTime())) / 1000); //convert to sec
				oldnPowTargetTimespan = (OLD_INTERVAL / 1000) * (GA_END_BLOCK_HEIGHT - lastBlockHeight + 1); //in sec (before GA)
				newnPowTargetTimespan = (INTERVAL / 1000) * (currentBlock.getHeight() - afterGA.getHeight() + 1); //in sec (after GA)
				if (totalInterval < ((oldnPowTargetTimespan + newnPowTargetTimespan) / 4)) {
					totalInterval = (oldnPowTargetTimespan + newnPowTargetTimespan) / 4;
				}
				if (totalInterval > ((oldnPowTargetTimespan + newnPowTargetTimespan) * 4)) {
					totalInterval = (oldnPowTargetTimespan + newnPowTargetTimespan) * 4;
				}
				newDifficulty = oldDifficulty * (oldnPowTargetTimespan + newnPowTargetTimespan) / totalInterval;
			}
			else
			{
				lastBlockHeight = ((currentBlock.getHeight()) - (DIFFICULTY_INTERVAL - 1));
				Block lastBlock = currentBlock.getBlockWithHeight(lastBlockHeight);
				totalInterval = (currentBlockTime - lastBlock.getTime())/1000;
				if (totalInterval < nPowTargetTimespan/4) {
					totalInterval = nPowTargetTimespan / 4;
				}
				if (totalInterval > nPowTargetTimespan * 4) {
					totalInterval = nPowTargetTimespan * 4;
				}
				newDifficulty = oldDifficulty *  nPowTargetTimespan/totalInterval;
			}
		}
		else
		/** when difficulty interval == 1 */
		{
			totalInterval = currentBlockTime/1000;
			if(totalInterval < nPowTargetTimespan /4 )
			{
				totalInterval =  nPowTargetTimespan  / 4;
			}
			if(totalInterval > nPowTargetTimespan * 4 )
			{
				totalInterval = nPowTargetTimespan * 4;
			}
			newDifficulty = oldDifficulty *  nPowTargetTimespan/totalInterval;
		}
		TOTAL_INTERVAL = totalInterval; // in second
		if(newDifficulty <= minimumDifficulty )
		{
			newDifficulty = minimumDifficulty;
		}
		averageDifficulty = newDifficulty;
		try (FileWriter fw = new FileWriter("C:\\Users\\zihau\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\difficulty.csv", true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(oldDifficulty + "," + newDifficulty + "," + createdBlock.getHeight());
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

	public static void setDogecoinAverageDifficulty(){
		long totalMiningPower = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();
		}
		System.out.println("total mining power = " + totalMiningPower);
		Block myBlock  = getSimulatedNodes().get(0).getBlock();
		double totalInterval = 0;
		double oldDifficulty = getAverageDifficulty();
		double lastBlockTime = myBlock.getTime();
		double minimumDifficulty = 0.00024414;
		double nPowTargetTimespan = 0;
		nPowTargetTimespan	= (INTERVAL/1000);
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
	
	// calculate averageDifficulty from totalMiningPower
	private static void setAverageDifficulty(){
		long totalMiningPower = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();
		}

		if(totalMiningPower != 0){
			averageDifficulty =  totalMiningPower * targetInterval;
		}
	}

	private static void setInitialDifficulty(){
		long totalMiningPower = 0;

		for(Node node : simulatedNodes){
			totalMiningPower += (long) node.getMiningPower();
		}
		//System.out.println("My total mining power = " +totalMiningPower);
		if(totalMiningPower != 0){
			averageDifficulty =  totalMiningPower * targetInterval;
		}
	}

	public static void updateDifficulty(double newDifficulty) {
		System.out.println("GA old difficulty = " + averageDifficulty);
		averageDifficulty = newDifficulty;
		System.out.println("GA updated difficulty = " + averageDifficulty);
	}



	//
	// Record block propagation time
	// For saving memory, Record only the latest 10 Blocks
	//
	private static ArrayList<Block> observedBlocks = new ArrayList<Block>();
	private static ArrayList<LinkedHashMap<Integer, Double>> observedPropagations = new ArrayList<>();
	
	public static void arriveBlock(Block block,Node node){
		if(observedBlocks.contains(block)){
			LinkedHashMap<Integer, Double> Propagation = observedPropagations.get(observedBlocks.indexOf(block));
			Propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
		}else{
			if(observedBlocks.size() > 10){
				printPropagation(observedBlocks.get(0),observedPropagations.get(0));
				observedBlocks.remove(0);
				observedPropagations.remove(0);
			}
			LinkedHashMap<Integer, Double> propagation = new LinkedHashMap<Integer, Double>();
			propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
			observedBlocks.add(block);
			observedPropagations.add(propagation);
		}
	}
	
	public static void printPropagation(Block block,LinkedHashMap<Integer, Double> propagation){
		System.out.println(block + ":" + block.getHeight());
		int printCounter = 0;
		for(Map.Entry<Integer, Double> timeEntry : propagation.entrySet()){
			printCounter = printCounter + 1;
			if(printCounter == (NUM_OF_NODES/2) ) {
				Main.midPropagationTime = Main.midPropagationTime + timeEntry.getValue();
			}
			if(printCounter%propagation.size() == 0) {
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
