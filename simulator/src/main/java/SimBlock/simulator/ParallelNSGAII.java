package SimBlock.simulator;


import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.component.evaluation.impl.MultithreadedEvaluation;
import org.uma.jmetal.component.termination.Termination;
import org.uma.jmetal.component.termination.impl.TerminationByEvaluations;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

import static SimBlock.settings.SimulationConfiguration.*;

/**
public class ParallelNSGAII extends AbstractAlgorithmRunner {
    public static  ArrayList<Double>  main(String[] args) {
        DoubleProblem problem;
        Algorithm<List<DoubleSolution>> algorithm;
        CrossoverOperator<DoubleSolution> crossover;
        MutationOperator<DoubleSolution> mutation;
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

        String referenceParetoFront = "";

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

        SolutionListEvaluator<DoubleSolution> evaluator =
                new MultithreadedSolutionListEvaluator<DoubleSolution>(2);

        // 注册
        int populationSize = 8;
        NSGAIIBuilder<DoubleSolution> builder =
                new NSGAIIBuilder<DoubleSolution>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(1)
                        .setSolutionListEvaluator(evaluator);
        runningGA = true;
        algorithm = builder.build();
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        builder.getSolutionListEvaluator().shutdown();
        runningGA = false;

        List<DoubleSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime();

        evaluator.shutdown();
        ArrayList<Double> myResult = new ArrayList<Double>();
        myResult.add(0, population.get(0).getVariable(0));
        myResult.add(1, population.get(0).getVariable(1));
        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

        printFinalSolutionSet(population);
        return myResult;

    }
}
 */


public class ParallelNSGAII extends AbstractAlgorithmRunner {
    public  static  ArrayList<Double>  main (String[] args) throws JMetalException {
        Problem<DoubleSolution> problem;
        NSGAII<DoubleSolution> algorithm;
        CrossoverOperator<DoubleSolution> crossover;
        MutationOperator<DoubleSolution> mutation;
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

        problem = new BoothProblem();
        JMetalRandom.getInstance().setSeed((long) TOTAL_INTERVAL);
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        //selection = new BinaryTournamentSelection<DoubleSolution>(
               // new RankingAndCrowdingDistanceComparator<DoubleSolution>());

        int populationSize = 200;
        int offspringPopulationSize = populationSize;

        //int numberOfCores = 0;
        Termination termination = new TerminationByEvaluations(50);

        algorithm =
                new NSGAII<>(
                        problem, populationSize, offspringPopulationSize, crossover, mutation, termination)
                        .setEvaluation(new MultithreadedEvaluation<>(28));
        runningGA = true;
        algorithm.run();

        /**
        GeneticAlgorithmBuilder<DoubleSolution> builder =
                new GeneticAlgorithmBuilder<DoubleSolution>(problem, crossover, mutation)
                        .setPopulationSize(10)
                        .setMaxEvaluations(1)
                        .setSelectionOperator(selection)
                        .setSolutionListEvaluator(
                                new MultithreadedSolutionListEvaluator<DoubleSolution>(numberOfCores));

        algorithm = builder.build();
        runningGA = true;
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        builder.getEvaluator().shutdown();
        DoubleSolution solution = algorithm.getResult();
        List<DoubleSolution> population = new ArrayList<>(1);
        population.add(solution);

        long computingTime = algorithmRunner.getComputingTime();

        new SolutionListOutput(population)
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
                .print();

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
        */

        List<DoubleSolution> population = algorithm.getResult();
        JMetalLogger.logger.info("Total execution time : " + algorithm.getTotalComputingTime() + "ms");
        JMetalLogger.logger.info("Number of evaluations: " + algorithm.getEvaluations());

        new SolutionListOutput(population)
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
                .print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

        ArrayList<Double> myResult = new ArrayList<Double>();
        myResult.add(0, population.get(0).getVariable(0));
        myResult.add(1, population.get(0).getVariable(1));
        return myResult;
    }
}

