package SimBlock.simulator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;


import SimBlock.node.Node;
import com.compomics.util.math.BigFunctions;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.random;
import static SimBlock.simulator.Simulator.getAverageDifficulty;
import static SimBlock.simulator.Simulator.getSimulatedNodes;


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
        int blockInterval = 0;
        int difficultyInterval = 0;
        //BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];
        double myAverageDifficulty = AVERAGE_DIFFICULTY.doubleValue();
        //BigDecimal FOUR = BigDecimal.valueOf(4);

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;
        if (firstGARun == true)
        {
            blockInterval = INTERVAL.intValue() ;
            x[0] = (blockInterval/1000);
            x[1] = DIFFICULTY_INTERVAL.intValue();
            difficultyInterval =  (int) x[1];
            firstGARun = false;
        }
        else
        {
            /**Init genes or variables */
            for (int i = 0; i < numberOfVariables; i++) {
                x[i] = Math.round(solution.getVariable(i));
            }
            blockInterval = (int) x[0];
            difficultyInterval = (int) x[1];
        }
        ArrayList<Double> blockTime = new ArrayList<>();
        ArrayList<Double> difficultyHistory = new ArrayList<>();
        double actualTimeTaken = 0.0;
        int counter = 1;
        double newTimeTaken = 0.0;
        for (int i = 1; i <= ENDBLOCKHEIGHT; i ++)
        {
            /**mining**/
            for (Node node : getSimulatedNodes())
            {
                double p = 1.0 / myAverageDifficulty;
                double u = myRandom.nextDouble();
                if(counter == 1)
                {
                    actualTimeTaken = ( Math.log(u) / Math.log(1.0-p) ) / node.getMiningPower();
                }
                else
                {
                    newTimeTaken =  ( Math.log(u) / Math.log(1.0-p) ) / node.getMiningPower();
                    if(newTimeTaken < actualTimeTaken)
                    {
                        actualTimeTaken = newTimeTaken;
                    }
                }
                counter++;
            }
            counter = 1;
            double nPowTargetTimespan = blockInterval*difficultyInterval;
            int minDifficulty = 1;
            blockTime.add((actualTimeTaken/1000));
            difficultyHistory.add(myAverageDifficulty);
            /**difficulty adjustment*/
            if((i % difficultyInterval) == 0)
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
        f[0] = calculateSD(blockTime);

        /** 2nd objective: standard deviation of difficulty */
        f[1] = calculateSD(difficultyHistory);

        /** To finalize objective and variables*/
        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, f[i]);
            solution.setVariable(i, x[i]);
        }
        //solution.setVariable(0,x[0].doubleValue());


    }

    public static double calculateSD(List<Double> numArray)
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