package SimBlock.simulator;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.jmetal5version.NSGAIIBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.ArrayList;
import java.util.List;

import static SimBlock.settings.SimulationConfiguration.runningGA;

public class myNSGAII extends AbstractAlgorithmRunner {
    public   static  ArrayList<Double>  main(String[] args) {
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

        selection = new BinaryTournamentSelection<DoubleSolution>(
                new RankingAndCrowdingDistanceComparator<DoubleSolution>());

        int populationSize = 200;
        // 注册
        algorithm = new NSGAIIBuilder<DoubleSolution>(problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(100)
                .build();
        runningGA = true;
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
                .execute();

        List<DoubleSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime() ;

        printFinalSolutionSet(population);

        ArrayList <Double> myResult = new ArrayList<Double>();
        myResult.add(0, population.get(0).getVariable(0));
        myResult.add(1, population.get(0).getVariable(1));
        //myResult.add(2, population.get(1).getVariable(0));
        //myResult.add(3, population.get(1).getVariable(1));
        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

        return myResult;
    }
}
