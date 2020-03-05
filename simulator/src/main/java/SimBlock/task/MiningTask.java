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
import SimBlock.simulator.BoothProblem;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;


import java.util.*;

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
	public void run(ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Long>> observedPropagations) {
		Block createdBlock = new Block(this.parentBlock.getHeight() + 1, this.parentBlock, this.miningNode , getCurrentTime(),getAverageDifficulty());
		this.miningNode.receiveBlock(createdBlock, simulatedNodes, taskQueue, taskMap, observedBlocks, observedPropagations);
		long myDifficultyInterval = 0;
		if(runningGA == false)
		{
			myDifficultyInterval = DIFFICULTY_INTERVAL;
		}
		else
		{
			myDifficultyInterval = GA_DIFFICULTY_INTERVAL;
		}

		if(SIMULATION_TYPE.equals("bitcoin"))
		{
			if(myDifficultyInterval != 0)
			{
				if((this.parentBlock.getHeight()+1) % myDifficultyInterval == 0)
				{
					setBitcoinAverageDifficulty(simulatedNodes);
					if(runningGA == false)
					{
						runGA();
						runningGA = false;
					}
				}
			}
		}
		else if (SIMULATION_TYPE.equals("litecoin"))
		{
			if(myDifficultyInterval != 0 )
			{
				if((this.parentBlock.getHeight()+1) % myDifficultyInterval == 0)
				{
					setBitcoinAverageDifficulty(simulatedNodes);
				}
			}

		}
		else if (SIMULATION_TYPE.equals("dogecoin"))
		{
			if((this.parentBlock.getHeight()) >= 2 )
			{
				setDogecoinAverageDifficulty(simulatedNodes);
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
				for (Node node : simulatedNodes) {
					//System.out.println("Old mining power = " + node.getMiningPower());
					node.setMiningPower(randomMiningPower(node.getMiningPower()));
					//System.out.println("New mining power = " + node.getMiningPower());
					//System.out.println();
				}
				//System.out.println("Increase hash rate");
			}
		}
		/**
		if(this.parentBlock.getHeight()+1 == 1500)
		{
			AVERAGE_MINING_POWER = 659148;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());
			}

		}
		else if(this.parentBlock.getHeight()+1 == 3000)
		{
			AVERAGE_MINING_POWER = 739229;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());

			}
		}
		else if(this.parentBlock.getHeight()+1 == 4500)
		{
			AVERAGE_MINING_POWER = 883284;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 6000)
		{
			AVERAGE_MINING_POWER = 951183;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 7500)
		{
			AVERAGE_MINING_POWER = 891134;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());
			}
		}
		else if(this.parentBlock.getHeight()+1 == 9000)
		{
			AVERAGE_MINING_POWER = 933661;
			for (Node node : simulatedNodes)
			{
				node.setMiningPower(genMiningPower());
			}
		}
		 */
	}

	public Block getParent(){
		return this.parentBlock;
	}

	public void runGA(){
		Problem<IntegerSolution> problem;
		Algorithm<List<IntegerSolution>> algorithm;
		CrossoverOperator<IntegerSolution> crossover;
		MutationOperator<IntegerSolution> mutation;
		SelectionOperator<List<IntegerSolution>, IntegerSolution> selection;

		// 定义优化问题
		problem = new BoothProblem();
		//String problemName = "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1";

		//problem = ProblemUtils.loadProblem(problemName);
		// SBX交叉算子
		double crossoverProbability = 0.9;
		double crossoverDistributionIndex = 30.0;
		crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex);

		double mutationProbability = 1.0 / problem.getNumberOfVariables();
		double mutationDistributionIndex = 20.0;
		mutation = new IntegerPolynomialMutation(mutationProbability, mutationDistributionIndex);

		selection = new BinaryTournamentSelection<IntegerSolution>();

		// 注册
		algorithm =
				new NSGAIIIBuilder<>(problem)
						.setCrossoverOperator(crossover)
						.setMutationOperator(mutation)
						.setSelectionOperator(selection)
						.setMaxIterations(1)
						.setPopulationSize(1)
						.build();
		runningGA = true;
		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

		List<IntegerSolution> population = algorithm.getResult();
		long computingTime = algorithmRunner.getComputingTime();

		new SolutionListOutput(population)
				.setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv"))
				.setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv"))
				.print();

		JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
		JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
	}
}
