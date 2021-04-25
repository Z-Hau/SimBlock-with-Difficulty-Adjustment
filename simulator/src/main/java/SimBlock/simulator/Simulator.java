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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import SimBlock.node.Block;
import SimBlock.node.Node;
import static SimBlock.simulator.Timer.*;
import static SimBlock.settings.SimulationConfiguration.*;


public class Simulator {
	private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	private static BigDecimal targetInterval;// = 1000*60*10;//msec
	private static BigDecimal averageDifficulty;
	
	public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static BigDecimal getAverageDifficulty(){ return averageDifficulty; }
	public static void setTargetInterval(BigDecimal interval){ targetInterval = interval; }
	
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
		BigDecimal totalInterval;
		BigDecimal currentBlockTime = currentBlock.getTime();
		BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
		BigDecimal FOUR = BigDecimal.valueOf(4);
		BigDecimal lastBlockHeight;
		BigDecimal oldnPowTargetTimespan;
		BigDecimal newnPowTargetTimespan;
		BigDecimal nPowTargetTimespan = (INTERVAL.divide(ONE_THOUSAND,2, RoundingMode.HALF_UP)).multiply(DIFFICULTY_INTERVAL);
		BigDecimal minimumDifficulty = BigDecimal.ONE;
		BigDecimal oldDifficulty = getAverageDifficulty();
		BigDecimal newDifficulty ;

		if(DIFFICULTY_INTERVAL.compareTo(BigDecimal.ONE) == 1) {
			if(GA_TRIGGERED) {
				lastBlockHeight = (BigDecimal.valueOf((currentBlock.getHeight())).subtract((DIFFICULTY_INTERVAL.subtract(BigDecimal.ONE))));
				Block lastBlock = currentBlock.getBlockWithHeight(lastBlockHeight.intValue());
				Block beforeGA = currentBlock.getBlockWithHeight((GA_END_BLOCK_HEIGHT)); //to get block before GA
				totalInterval = ((beforeGA.getTime().subtract(lastBlock.getTime())).divide(ONE_THOUSAND,2, RoundingMode.HALF_UP)); //in sec
				Block afterGA = currentBlock.getBlockWithHeight(GA_END_BLOCK_HEIGHT + 1); //to get block after GA
				totalInterval = (totalInterval.add((currentBlockTime.subtract(afterGA.getTime()).divide(ONE_THOUSAND,2, RoundingMode.HALF_UP)))); //convert to sec
				oldnPowTargetTimespan = ((OLD_INTERVAL.divide(ONE_THOUSAND,2, RoundingMode.HALF_UP))).multiply(BigDecimal.valueOf(GA_END_BLOCK_HEIGHT).subtract(lastBlockHeight.add(BigDecimal.ONE))); //in sec (before GA)
				newnPowTargetTimespan = ((INTERVAL.divide(ONE_THOUSAND,2, RoundingMode.HALF_UP))).multiply(BigDecimal.valueOf((currentBlock.getHeight() - afterGA.getHeight() + 1))); //in sec (after GA)
				if (totalInterval.compareTo((oldnPowTargetTimespan.add(newnPowTargetTimespan)).divide(FOUR,2, RoundingMode.HALF_UP))==-1){
					totalInterval = ((oldnPowTargetTimespan.add(newnPowTargetTimespan)).divide(FOUR,2, RoundingMode.HALF_UP));
				}
				if (totalInterval.compareTo((oldnPowTargetTimespan.add(newnPowTargetTimespan)).multiply(FOUR))==1){
					totalInterval = ((oldnPowTargetTimespan.add(newnPowTargetTimespan)).multiply(FOUR));
				}
				newDifficulty = oldDifficulty.multiply((oldnPowTargetTimespan.add(newnPowTargetTimespan)).divide(totalInterval,2, RoundingMode.HALF_UP));
			}
			else
			{
				lastBlockHeight = (BigDecimal.valueOf((currentBlock.getHeight())).subtract((DIFFICULTY_INTERVAL.subtract(BigDecimal.ONE))));
				Block lastBlock = currentBlock.getBlockWithHeight(lastBlockHeight.intValue());
				totalInterval = ((currentBlockTime.subtract(lastBlock.getTime())).divide(ONE_THOUSAND,2, RoundingMode.HALF_UP));
				if (totalInterval.compareTo(nPowTargetTimespan.divide(FOUR,2, RoundingMode.HALF_UP)) == -1) {
					totalInterval = nPowTargetTimespan.divide(FOUR,2, RoundingMode.HALF_UP);
				}
				if (totalInterval.compareTo(nPowTargetTimespan.multiply(FOUR)) == 1) {
					totalInterval = nPowTargetTimespan.multiply(FOUR);
				}
				newDifficulty = oldDifficulty.multiply(nPowTargetTimespan.divide(totalInterval,2, RoundingMode.HALF_UP));
			}
		}
		else
		/** when difficulty interval == 1 **/
		{
			lastBlockHeight = BigDecimal.valueOf((currentBlock.getHeight()) - 1);
			Block lastBlock = currentBlock.getBlockWithHeight(lastBlockHeight.intValue());
			totalInterval = ((currentBlockTime.subtract(lastBlock.getTime())).divide(ONE_THOUSAND,20, RoundingMode.HALF_UP));
			if (totalInterval.compareTo(nPowTargetTimespan.divide(FOUR,20, RoundingMode.HALF_UP)) == -1) {
				totalInterval = nPowTargetTimespan.divide(FOUR,20, RoundingMode.HALF_UP);
			}
			if (totalInterval.compareTo(nPowTargetTimespan.multiply(FOUR)) == 1) {
				totalInterval = nPowTargetTimespan.multiply(FOUR);
			}
			newDifficulty = oldDifficulty.multiply(nPowTargetTimespan.divide(totalInterval,20, RoundingMode.HALF_UP));
		}
		TOTAL_INTERVAL = totalInterval; // in second
		if(newDifficulty.compareTo(minimumDifficulty) == -1  )
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

	/**
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

	}**/

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
	}**/

	private static void setInitialDifficulty(){
		BigDecimal totalMiningPower = BigDecimal.ZERO;

		for(Node node : simulatedNodes){
			totalMiningPower = totalMiningPower.add(BigDecimal.valueOf(node.getMiningPower())) ;
		}
		//System.out.println("My total mining power = " +totalMiningPower);
		if(totalMiningPower.compareTo(BigDecimal.ZERO) == 1 ){
			averageDifficulty =  totalMiningPower.multiply(targetInterval) ;
		}
	}

	//
	// Record block propagation time
	// For saving memory, Record only the latest 10 Blocks
	//
	private static ArrayList<Block> observedBlocks = new ArrayList<Block>();
	private static ArrayList<LinkedHashMap<Integer, BigDecimal>> observedPropagations = new ArrayList<>();
	
	public static void arriveBlock(Block block,Node node){
		if(observedBlocks.contains(block)){
			LinkedHashMap<Integer, BigDecimal> Propagation = observedPropagations.get(observedBlocks.indexOf(block));
			Propagation.put(node.getNodeID(), getCurrentTime().subtract(block.getTime()));
		}else{
			if(observedBlocks.size() > 10){
				printPropagation(observedBlocks.get(0),observedPropagations.get(0));
				observedBlocks.remove(0);
				observedPropagations.remove(0);
			}
			LinkedHashMap<Integer, BigDecimal> propagation = new LinkedHashMap<Integer, BigDecimal>();
			propagation.put(node.getNodeID(), getCurrentTime().subtract(block.getTime()));
			observedBlocks.add(block);
			observedPropagations.add(propagation);
		}
	}
	
	public static void printPropagation(Block block, LinkedHashMap<Integer, BigDecimal> propagation){
		System.out.println(block + ":" + block.getHeight());
		int printCounter = 0;
		for(Map.Entry<Integer, BigDecimal> timeEntry : propagation.entrySet()){
			printCounter = printCounter + 1;
			if(printCounter == (NUM_OF_NODES/2) ) {
				Main.midPropagationTime = Main.midPropagationTime.add(timeEntry.getValue());
			}
			if(printCounter%propagation.size() == 0) {
				Main.myMedian.add(timeEntry.getValue());
				Main.meanblockpropagationTime = Main.meanblockpropagationTime.add(timeEntry.getValue());
				System.out.println("node id = " + timeEntry.getKey() + ", " + "propagation time = " + timeEntry.getValue().intValue());
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
