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

import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.simulator.ParallelNSGAII;
import SimBlock.simulator.myThread;
import it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap;
import weka.Run;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import SimBlock.simulator.BigMath;
import com.compomics.util.math.BigFunctions;

import static SimBlock.simulator.Simulator.getAverageDifficulty;
import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.settings.SimulationConfiguration.*;

public class MiningTask implements Task {
	private Node miningNode;
	private Block parentBlock;
	private BigDecimal interval;

	public MiningTask(Node miningNode) {
		this.miningNode = miningNode;
		this.parentBlock = miningNode.getBlock();
		BigDecimal p = BigDecimal.ONE.divide(getAverageDifficulty(),30, RoundingMode.HALF_UP);
		double u = random.nextDouble();
		u = Math.log(u);
		BigDecimal o = BigDecimal.ONE.subtract(p);
		MathContext mc = new MathContext(30);
		BigDecimal i = BigFunctions.ln(o,mc);;
		BigDecimal newInterval =  BigDecimal.valueOf(u).divide(i,30,RoundingMode.HALF_UP);
		this.interval = newInterval.divide(BigDecimal.valueOf(this.miningNode.getMiningPower()),30,RoundingMode.HALF_UP);
	}
	
	@Override
	public BigDecimal getInterval() {
		return this.interval;
	}

	@Override
	public void run() {
		Block createdBlock = new Block(this.parentBlock.getHeight() + 1, this.parentBlock, this.miningNode ,getCurrentTime());
		this.miningNode.receiveBlock(createdBlock);
		CURRENT_BLOCK_HEIGHT = createdBlock.getHeight();
		BigDecimal testing = BigDecimal.valueOf(createdBlock.getHeight()).remainder(DIFFICULTY_INTERVAL);
		if(SIMULATION_TYPE.equals("bitcoin")) {
			//Run GA if actual time taken is less than or more than X minutes.
			/*
			if(createdBlock.getHeight() > 6) {
				Block previousBlocks = createdBlock.getBlockWithHeight((createdBlock.getHeight()-6));
				if(((createdBlock.getTime().subtract(previousBlocks.getTime())).divide(BigDecimal.valueOf(6),10,RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(45000)) == -1) || ( (createdBlock.getTime().subtract(previousBlocks.getTime())).divide(BigDecimal.valueOf(6),10,RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(75000)) == 1))  {
					if(!runningGA && DIFFICULTY_ADJUSTED) {
						GA_START_BLOCK_HEIGHT = createdBlock.getHeight();
						AVERAGE_DIFFICULTY = getAverageDifficulty();
						myThread testingThread = new myThread();
						testingThread.start();
					}
				}
			}*/
			/** Readjust difficulty */
			if(DIFFICULTY_INTERVAL.compareTo(BigDecimal.ZERO) == 1) {
				if((((BigDecimal.valueOf(createdBlock.getHeight()).subtract(TOTAL_PREVIOUS_BLOCK_HEIGHT)).remainder(DIFFICULTY_INTERVAL)).compareTo(BigDecimal.ZERO) == 0) &&  ((createdBlock.getHeight()) != PREVIOUS_DIFFICULTY_READJUST_HEIGHT)){
				//if((((createdBlock.getHeight())-(TOTAL_PREVIOUS_BLOCK_HEIGHT)) % DIFFICULTY_INTERVAL == 0) && (createdBlock.getHeight() != PREVIOUS_DIFFICULTY_READJUST_HEIGHT)) {
					PREVIOUS_DIFFICULTY_READJUST_HEIGHT = (createdBlock.getHeight());
					setBitcoinAverageDifficulty(createdBlock);
					GA_TRIGGERED = 	false;
					TOTAL_PREVIOUS_BLOCK_HEIGHT = BigDecimal.valueOf(createdBlock.getHeight()) ;
					DIFFICULTY_ADJUSTED = true;
				}
			}
		}
		else if (SIMULATION_TYPE.equals("litecoin")) {
			if(DIFFICULTY_INTERVAL.compareTo(BigDecimal.ZERO) == 1) {
				if(BigDecimal.valueOf(this.parentBlock.getHeight()+1).remainder(DIFFICULTY_INTERVAL).compareTo(BigDecimal.ZERO) == 0) {
					setBitcoinAverageDifficulty(createdBlock);
				}
			}
		}
		else if (SIMULATION_TYPE.equals("dogecoin")) {
			if((this.parentBlock.getHeight()) >= 2 ) {
				//setDogecoinAverageDifficulty();
			}
		}
		else {
			System.out.println("Incorrect SIMULATION_TYPE. Please try again.");
		}
		if(CHANGE_MINING_POWER_INTERVAL != 0 ) {
			if((createdBlock.getHeight()+1) % CHANGE_MINING_POWER_INTERVAL == 0) //allow user to set when to change the mining power
			{
				for (Node node : getSimulatedNodes()) {
					node.setMiningPower(randomMiningPower(node.getMiningPower()));
				}
			}
		}
		if(createdBlock.getHeight()+1 == 6) {
			AVERAGE_MINING_POWER = 659148;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}

		}
		else if(createdBlock.getHeight()+1 == 3000)
		{
			AVERAGE_MINING_POWER = 759229;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}
		}
		else if(createdBlock.getHeight()+1 == 4500)
		{
			AVERAGE_MINING_POWER = 883284;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}
		}
		else if(createdBlock.getHeight()+1 == 6000)
		{
			AVERAGE_MINING_POWER = 951183;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}
		}
		else if(createdBlock.getHeight()+1 == 7500)
		{
			AVERAGE_MINING_POWER = 1091134;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}
		}
		else if(createdBlock.getHeight()+1 == 9000)
		{
			AVERAGE_MINING_POWER = 1293366;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
			}
		}
		difficultySD.add(getAverageDifficulty());

		if(GA_TRIGGERED && FIRST_TIME ) {
			//INTERVAL = NEW_INTERVAL;
			//setTargetInterval(INTERVAL);
			//DIFFICULTY_INTERVAL = NEW_DIFFICULTY_INTERVAL;
			//FIRST_TIME = false;
		}

	}

	public Block getParent(){
		return this.parentBlock;
	}


}
