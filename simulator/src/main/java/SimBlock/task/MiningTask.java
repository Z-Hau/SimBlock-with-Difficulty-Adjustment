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
import java.util.ArrayList;

import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.settings.SimulationConfiguration.*;

public class MiningTask implements Task {
	private Node miningNode;
	private Block parentBlock;
	private double interval;

	public MiningTask(Node miningNode) {
		this.miningNode = miningNode;
		this.parentBlock = miningNode.getBlock();
	
		double p = 1.0 / getAverageDifficulty();
		double u = random.nextDouble();
		if(p <= Math.pow(2.0,-53.0))
		{
			this.interval = 0;
		}
		else
		{
			this.interval =  Math.round((double)Math.log(u) / Math.log(1.0-p) / this.miningNode.getMiningPower());
		}
		int wtf =0;
	}
	
	@Override
	public double getInterval() {
		return this.interval;
	}

	@Override
	public void run() {
		Block createdBlock = new Block(this.parentBlock.getHeight() + 1, this.parentBlock, this.miningNode ,getCurrentTime());
		this.miningNode.receiveBlock(createdBlock);
		CURRENT_BLOCK_HEIGHT = createdBlock.getHeight();
		if(SIMULATION_TYPE.equals("bitcoin")) {
			//Run GA if actual time taken is less than or more than X minutes.
			if(createdBlock.getHeight() > 6) {
				Block previousBlocks = createdBlock.getBlockWithHeight((createdBlock.getHeight()-6));
				if(((createdBlock.getTime() - previousBlocks.getTime())/6 <= 450000) || ( (createdBlock.getTime() - previousBlocks.getTime())/6 >= 750000))  {
					if(!runningGA && DIFFICULTY_ADJUSTED) {
						GA_START_BLOCK_HEIGHT = createdBlock.getHeight();
						AVERAGE_DIFFICULTY = getAverageDifficulty();
						myThread testingThread = new myThread();
						testingThread.start();
					}
				}
			}
			/** Readjust difficulty */
			if(DIFFICULTY_INTERVAL != 0) {
				if((((createdBlock.getHeight())-(TOTAL_PREVIOUS_BLOCK_HEIGHT)) % DIFFICULTY_INTERVAL == 0) && (createdBlock.getHeight() != PREVIOUS_DIFFICULTY_READJUST_HEIGHT)) {
					PREVIOUS_DIFFICULTY_READJUST_HEIGHT = createdBlock.getHeight();
					setBitcoinAverageDifficulty(createdBlock);
					GA_TRIGGERED = 	false;
					TOTAL_PREVIOUS_BLOCK_HEIGHT = createdBlock.getHeight();
					DIFFICULTY_ADJUSTED = true;
				}
			}
		}
		else if (SIMULATION_TYPE.equals("litecoin")) {
			if(DIFFICULTY_INTERVAL != 0 ) {
				if((this.parentBlock.getHeight()+1) % DIFFICULTY_INTERVAL == 0) {
					setBitcoinAverageDifficulty(createdBlock);
				}
			}
		}
		else if (SIMULATION_TYPE.equals("dogecoin")) {
			if((this.parentBlock.getHeight()) >= 2 ) {
				setDogecoinAverageDifficulty();
			}
		}
		else {
			System.out.println("Incorrect SIMULATION_TYPE. Please try again.");
		}
		if(CHANGE_MINING_POWER_INTERVAL != 0 ) {
			if((createdBlock.getHeight()+1) % CHANGE_MINING_POWER_INTERVAL == 0) //allow user to set when to change the mining power
			{
				double totalMiningPower = 0.0;
				for (Node node : getSimulatedNodes()) {
					node.setMiningPower(randomMiningPower(node.getMiningPower()));
					totalMiningPower = totalMiningPower + node.getMiningPower();
				}
				try (FileWriter fw = new FileWriter("C:\\Users\\zihau\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\mining-power.csv", true);
					 BufferedWriter bw = new BufferedWriter(fw);
					 PrintWriter out = new PrintWriter(bw)) {
					out.println(totalMiningPower);
				} catch (IOException e) {
					//exception handling left as an exercise for the reader
				}
			}
		}
		if(createdBlock.getHeight()+1 == 6) {
			AVERAGE_MINING_POWER = 659148;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);

		}
		else if(createdBlock.getHeight()+1 == 3000)
		{
			AVERAGE_MINING_POWER = 759229;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);
		}
		else if(createdBlock.getHeight()+1 == 4500)
		{
			AVERAGE_MINING_POWER = 883284;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);
		}
		else if(createdBlock.getHeight()+1 == 6000)
		{
			AVERAGE_MINING_POWER = 951183;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);
		}
		else if(createdBlock.getHeight()+1 == 7500)
		{
			AVERAGE_MINING_POWER = 1091134;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);
		}
		else if(createdBlock.getHeight()+1 == 9000)
		{
			AVERAGE_MINING_POWER = 1293366;
			double totalMiningPower = 0.0;
			for (Node node : getSimulatedNodes()) {
				node.setMiningPower(genMiningPower());
				totalMiningPower = totalMiningPower + node.getMiningPower();
			}
			printMiningPower(totalMiningPower);
		}
		difficultySD.add(getAverageDifficulty());
		if(GA_TRIGGERED && FIRST_TIME ) {
			INTERVAL = NEW_INTERVAL;
			setTargetInterval(INTERVAL);
			DIFFICULTY_INTERVAL = NEW_DIFFICULTY_INTERVAL;
			FIRST_TIME = false;
		}
	}

	public Block getParent(){
		return this.parentBlock;
	}

	public void printMiningPower(double totalMiningPower){
		try (FileWriter fw = new FileWriter("C:\\Users\\zihau\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\mining-power.csv", true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(totalMiningPower);
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}


}
