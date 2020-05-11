package SimBlock.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.task.MiningTask;
import SimBlock.task.Task;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.simulator.Simulator.getAverageDifficulty;
import static SimBlock.simulator.Simulator.getSimulatedNodes;
import static SimBlock.simulator.Timer.*;
import SimBlock.simulator.Simulator;


public class BoothProblem extends AbstractDoubleProblem{

    public BoothProblem() {
        setNumberOfVariables(2);
        setNumberOfObjectives(2);
        setName("BoothProblem");
        List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Double> upperLimit = new ArrayList<>(getNumberOfVariables());
        lowerLimit.add(1.0);
        upperLimit.add(600.0);
        lowerLimit.add(1.0);
        upperLimit.add(4032.0);
        setVariableBounds(lowerLimit,upperLimit);
    }


    @Override
    public void evaluate(DoubleSolution solution) {
        Random myRandom = new Random(10);
        int numberOfVariables = getNumberOfVariables();
        int numberOfObjectives = getNumberOfObjectives();
        long blockInterval = INTERVAL;
        int difficultyInterval = DIFFICULTY_INTERVAL;
        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];
        double myAverageDifficulty = AVERAGE_DIFFICULTY;

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;
        if (firstGARun == true)
        {
            blockInterval = INTERVAL ;
            x[0] = (blockInterval/1000);
            x[1] = (double) DIFFICULTY_INTERVAL;
            difficultyInterval = DIFFICULTY_INTERVAL;
            firstGARun = false;
        }
        else
        {
            /**Init genes or variables */
            for (int i = 0; i < numberOfVariables; i++) {
                x[i] =  Math.round(solution.getVariable(i));
            }
            blockInterval = (long) (x[0]*1000);
            difficultyInterval = (int) x[1];
        }
        ArrayList<Double> blockTime = new ArrayList<Double>();
        ArrayList<Double> difficultyHistory = new ArrayList<Double>();
        double actualTimeTaken = 0.0;
        int counter = 1;
        for (int i = 1; i <= ENDBLOCKHEIGHT; i ++)
        {
            for (Node node : getSimulatedNodes())
            {
                double p = 1.0 / myAverageDifficulty;
                double u = myRandom.nextDouble();
                if(counter == 1)
                {
                    actualTimeTaken = (double) ( Math.log(u) / Math.log(1.0-p) ) / node.getMiningPower();
                }
                else
                {
                    double newTimeTaken = (double) ( Math.log(u) / Math.log(1.0-p) ) / node.getMiningPower();
                    if(newTimeTaken < actualTimeTaken)
                    {
                        actualTimeTaken = newTimeTaken;
                    }
                }
                counter++;
            }
            counter = 1;

            long nPowTargetTimespan = (blockInterval/1000) * difficultyInterval; // in second
            int minDifficulty = 1;
            //actualTimeTaken = (long) (myAverageDifficulty * Math.pow(2.0,32.0) / miningPower);
            blockTime.add((actualTimeTaken/1000));
            difficultyHistory.add(myAverageDifficulty);
            /**difficulty adjustment*/
            if((i) % (difficultyInterval) == 0)
            {
                int previousBlockHeight = i - (difficultyInterval-1);
                double totalInterval = 0.0;

                if(difficultyInterval != 1)
                {
                    for (int a = previousBlockHeight; a <= i; a++ )
                    {
                        totalInterval = totalInterval + (blockTime.get(a-1)); //convert to sec?
                    }
                }
                else
                {
                    totalInterval = blockTime.get(i-1);
                }

                if(totalInterval < (nPowTargetTimespan/4))
                {
                    totalInterval = nPowTargetTimespan / 4;
                }
                if(totalInterval > nPowTargetTimespan * 4)
                {
                    totalInterval = nPowTargetTimespan * 4;
                }
                double newDifficulty = myAverageDifficulty * nPowTargetTimespan/totalInterval;
                if(newDifficulty < minDifficulty)
                {
                    newDifficulty = minDifficulty;
                }
                myAverageDifficulty = newDifficulty;
            }
        }
        /** 1st objective: standard deviation of blocktime */
        f[0] = calculateSDDouble(blockTime);

        /** 2nd objective: standard deviation of difficulty */
        f[1] = calculateSDDouble(difficultyHistory);

        /** To finalize objective and variables*/
        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, f[i]);
            solution.setVariable(i,x[i]);
        }
        /**
        System.out.println("Block time = " + x[0]);
        System.out.println("Difficulty interval = " + x[1]);
        System.out.println("Obj 1 = " + f[0]);
        System.out.println("Obj 2 = " + f[1]);
        System.out.println();

         try(FileWriter fw = new FileWriter("C:\\Users\\zihau\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\GA-PARAMETERS.csv", true);
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw))
         {
            out.println(x[0] + "," + x[1] + "," + f[0] + "," + f[1]) ;
         } catch (IOException e) {
         //exception handling left as an exercise for the reader
         }
         */
    }

    public  double calculateSDDouble(ArrayList <Double> numArray)
    {
        long sum = 0;
        double standardDeviation = 0;
        int length = numArray.size();
        for(double num : numArray) {
            sum += num;
        }
        double mean = sum/length;
        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.round(Math.sqrt(standardDeviation/length));
    }
}