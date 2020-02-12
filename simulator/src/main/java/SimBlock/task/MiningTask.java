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
import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.settings.SimulationConfiguration.*;

public class MiningTask implements Task {
	private Node miningNode;
	private Block parentBlock;
	private long interval;

	public MiningTask(Node miningNode) {
		this.miningNode = miningNode;
		this.parentBlock = miningNode.getBlock();
	
		double p = 1.0 / getAverageDifficulty();
		double u = random.nextDouble();
		this.interval = (long)(  ( Math.log(u) / Math.log(1.0-p) ) / this.miningNode.getMiningPower() );
	}
	
	@Override
	public long getInterval() {
		return this.interval;
	}

	@Override
	public void run() {
		Block createdBlock = new Block(this.parentBlock.getHeight() + 1, this.parentBlock, this.miningNode ,getCurrentTime());
		this.miningNode.receiveBlock(createdBlock);


		if(SIMULATION_TYPE.equals("bitcoin"))
		{
			if(DIFFICULTY_INTERVAL != 0)
			{
				if((this.parentBlock.getHeight()+1) % DIFFICULTY_INTERVAL == 0)
				{
					setBitcoinAverageDifficulty();
				}
			}
		}
		else if (SIMULATION_TYPE.equals("litecoin"))
		{
			if(DIFFICULTY_INTERVAL != 0 )
			{
				if((this.parentBlock.getHeight()+1) % DIFFICULTY_INTERVAL == 0)
				{
					setBitcoinAverageDifficulty();
				}
			}

		}
		else if (SIMULATION_TYPE.equals("dogecoin"))
		{
			if((this.parentBlock.getHeight()) >= 2 )
			{
				setDogecoinAverageDifficulty();
			}
		}
		else
		{
			System.out.println("Incorrect SIMULATION_TYPE. Please try again.");
		}


		if(CHANGE_MINING_POWER_INTERVAL != 0 )
		{
			if((this.parentBlock.getHeight()+1) % CHANGE_MINING_POWER_INTERVAL == 0) //allow user to set when to change the mining power
			{
				for (Node node : getSimulatedNodes()) {
					//System.out.println("Old mining power = " + node.getMiningPower());
					node.setMiningPower(randomMiningPower(node.getMiningPower()));
					//System.out.println("New mining power = " + node.getMiningPower());
					//System.out.println();
				}
				//System.out.println("Increase hash rate");
			}
		}

		if(this.parentBlock.getHeight()+1 == 1500)
		{
			AVERAGE_MINING_POWER = 659148;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 3000)
		{
			AVERAGE_MINING_POWER = 739229;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());

			}
		}
		else if(this.parentBlock.getHeight()+1 == 4500)
		{
			AVERAGE_MINING_POWER = 883284;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 6000)
		{
			AVERAGE_MINING_POWER = 951183;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 7500)
		{
			AVERAGE_MINING_POWER = 891134;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 9000)
		{
			AVERAGE_MINING_POWER = 933661;
			for (Node node : getSimulatedNodes())
			{
				node.setMiningPower(genMiningPower());
			}
		}
	}

	public Block getParent(){
		return this.parentBlock;
	}
}
