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
import SimBlock.simulator.ParallelNSGAII;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.settings.SimulationConfiguration.*;

public class MiningTask implements Task {
	private Node miningNode;
	private Block parentBlock;
	private long interval;

	public MiningTask(Node miningNode, double[] averageDifficulty) {
		this.miningNode = miningNode;
		this.parentBlock = miningNode.getBlock();

		double p = 1.0 / averageDifficulty[0];
		double u = random.nextDouble();
		this.interval = (long)(  ( Math.log(u) / Math.log(1.0-p) ) / this.miningNode.getMiningPower() );
	}

	@Override
	public double getInterval() {
		return this.interval;
	}

	@Override
	public  void run(ArrayList<Node> simulatedNodes, PriorityQueue<ScheduledTask> taskQueue, Map<Task, ScheduledTask> taskMap, ArrayList<Block> observedBlocks, ArrayList<LinkedHashMap<Integer, Double>> observedPropagations, double currentTime, long[] blockInterval, int[] difficultyInterval, double[] averageDifficulty) {
		Block createdBlock = new Block(this.parentBlock.getHeight() + 1, this.parentBlock, this.miningNode , currentTime , averageDifficulty[0]);
		this.miningNode.receiveBlock(createdBlock, taskQueue, taskMap, observedBlocks, observedPropagations, currentTime, averageDifficulty);
		if(SIMULATION_TYPE.equals("bitcoin"))
		{
			if(difficultyInterval[0] != 0)
			{
				if(runningGA == true)
				{
					if((createdBlock.getHeight()-1) % (difficultyInterval[0]) == 0) {
						setBitcoinAverageDifficulty(createdBlock,blockInterval, difficultyInterval, averageDifficulty);
					}
				}
				else
				{
					if(((createdBlock.getHeight()-1)- TOTAL_PREVIOUS_BLOCK_HEIGHT) % (difficultyInterval[0]) == 0) {
						setBitcoinAverageDifficulty(createdBlock, blockInterval, difficultyInterval, averageDifficulty);
						System.out.println("Running GA............................................");
						//runNSGAIII();
						ParallelNSGAII parallelRunGA = new ParallelNSGAII();
						ArrayList<Double> myResult = parallelRunGA.main(null);
						//myNSGAII runNSGGAII = new myNSGAII();
						//ArrayList <Double> myResult = runNSGGAII.main(null);
						runningGA = false;
						firstGARun = true;
						INTERVAL = ((new Double(myResult.get(0))).longValue() * 1000);
						TOTAL_PREVIOUS_BLOCK_HEIGHT = this.parentBlock.getHeight();
						DIFFICULTY_INTERVAL = (new Double(myResult.get(1))).intValue();
						System.out.println("GA Stopped.............................................");
						try (FileWriter fw = new FileWriter("C:\\Users\\zihau\\Desktop\\simblock\\testing-ga.csv", true);
							 BufferedWriter bw = new BufferedWriter(fw);
							 PrintWriter out = new PrintWriter(bw)) {
							out.println(INTERVAL + "," + DIFFICULTY_INTERVAL + "," + this.parentBlock.getHeight());
						} catch (IOException e) {
							//exception handling left as an exercise for the reader
						}
					}
				}
			}
		}
		else if (SIMULATION_TYPE.equals("litecoin"))
		{
			if(difficultyInterval[0] != 0 )
			{
				if((this.parentBlock.getHeight()+1) % difficultyInterval[0] == 0)
				{
					setBitcoinAverageDifficulty(createdBlock, blockInterval, difficultyInterval, averageDifficulty);
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

	public ArrayList <Double> runNSGAIII(){
		Problem<DoubleSolution> problem;
		Algorithm<List<DoubleSolution>> algorithm;
		CrossoverOperator<DoubleSolution> crossover;
		MutationOperator<DoubleSolution> mutation;
		SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

		// 定义优化问题
		problem = new BoothProblem();
		//String problemName = "org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1";

		//problem = ProblemUtils.loadProblem(problemName);
		// SBX交叉算子
		double crossoverProbability = 0.9;
		double crossoverDistributionIndex = 30.0;
		crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

		double mutationProbability = 1.0 / problem.getNumberOfVariables();
		double mutationDistributionIndex = 20.0;
		mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

		selection = new BinaryTournamentSelection<DoubleSolution>();

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

		List<DoubleSolution> population = algorithm.getResult();
		long computingTime = algorithmRunner.getComputingTime();

		new SolutionListOutput(population)
				.setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv"))
				.setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv"))
				.print();
		ArrayList <Double> myResult = new ArrayList<Double>();
		myResult.add(0, population.get(0).getVariable(0));
		myResult.add(1, population.get(0).getVariable(1));
		JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
		JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

		return myResult;
	}

}
