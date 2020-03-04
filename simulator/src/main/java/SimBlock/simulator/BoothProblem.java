package SimBlock.simulator;

import java.util.*;


import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.task.MiningTask;
import SimBlock.task.Task;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.writeGraph;
import static SimBlock.simulator.Main.constructNetworkWithAllNode;
import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;


public class BoothProblem extends AbstractIntegerProblem {


    public BoothProblem() {
        setNumberOfVariables(2);

        setNumberOfObjectives(2);
        setName("BoothProblem");

        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
        /*for (int i=0; i<getNumberOfVariables(); i++){
            lowerLimit.add(-10.0);
            upperLimit.add(10.0);
        }*/
        lowerLimit.add(1);
        upperLimit.add(600);
        lowerLimit.add(1);
        upperLimit.add(4032);
        setVariableBounds(lowerLimit,upperLimit);
    }


    @Override
    public void evaluate(IntegerSolution integerSolution) {
         ArrayList<Node> simulatedNodesGA = new ArrayList<Node>();
         PriorityQueue<Timer.ScheduledTask> taskQueueGA = new PriorityQueue<Timer.ScheduledTask>();
         Map<Task, Timer.ScheduledTask> taskMapGA = new HashMap<Task, Timer.ScheduledTask>();
        int numberOfVariables = getNumberOfVariables();
        int numberOfObjectives = getNumberOfObjectives();
        ArrayList <Long> blocktimeSD = new ArrayList<Long>();
        ArrayList <Long> difficultySD = new ArrayList<Long>();

        Integer[] f = new Integer[numberOfObjectives];
        Integer[] x = new Integer[numberOfVariables];

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;

        /**Init genes or variables */
        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = integerSolution.getVariable(i);
        }
        int blockInterval = x[0];
        int difficultyInterval = x[1];

        setTargetIntervalGA(blockInterval);
        GA_DIFFICULTY_INTERVAL = difficultyInterval;


        constructNetworkWithAllNode(NUM_OF_NODES,simulatedNodesGA);
        simulatedNodesGA.get(0).genesisBlock(simulatedNodesGA,taskQueueGA,taskMapGA);

        int j=1;
        while(getTask(simulatedNodesGA,taskQueueGA,taskMapGA) != null){
            if(getTask(simulatedNodesGA, taskQueueGA, taskMapGA) instanceof MiningTask){
                MiningTask task = (MiningTask) getTask(simulatedNodesGA, taskQueueGA, taskMapGA);
                if(task.getParent().getHeight() == j) j++;
                if(j > ENDBLOCKHEIGHT){break;}
                if(j%100==0 || j==2) writeGraph(j);
            }
            runTask(simulatedNodesGA,taskQueueGA,taskMapGA);
        }

        Set<Block> blocks = new HashSet<Block>();
        Block block  = simulatedNodesGA.get(0).getBlock();

        int counter1 = 0;
        long oldInterval = 0;
        long newInterval = 0;
        long myInterval = 0;
        long myDifficulty = 0;


        while(block.getParent() != null){
            blocks.add(block);
            oldInterval = block.getTime();
            block = block.getParent();
            newInterval = block.getTime();
            myInterval = (oldInterval - newInterval)/1000; //convert to second
            blocktimeSD.add(myInterval);
            myDifficulty =  (long) getAverageDifficulty();
            difficultySD.add(myDifficulty);
            counter1 = counter1+1;
        }

        /** 1st objective: standard deviation of blocktime */
        f[0] = calculateSD(blocktimeSD);

        /** 2nd objective: standard deviation of difficulty */
        f[1] = calculateSD(difficultySD);

        /** To finalize objective */
        for (int i = 0; i < numberOfObjectives; i++) {
            integerSolution.setObjective(i, f[i]);
        }


    }

    public int calculateSD(ArrayList <Long> numArray)
    {
        long sum = 0;
        double standardDeviation = 0;
        int length = numArray.size();

        for(long num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(long num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        int wtf = (int) Math.round(Math.sqrt(standardDeviation/length));
        return wtf;
    }


}