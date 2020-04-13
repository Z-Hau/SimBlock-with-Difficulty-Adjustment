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
import static SimBlock.simulator.Main.constructNetworkWithAllNode;
import static SimBlock.simulator.Timer.*;


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
         ArrayList<Node> simulatedNodesGA = new ArrayList<Node>();
         PriorityQueue<Timer.ScheduledTask> taskQueueGA = new PriorityQueue<Timer.ScheduledTask>();
         Map<Task, Timer.ScheduledTask> taskMapGA = new HashMap<Task, Timer.ScheduledTask>();
         ArrayList<Block> observedBlocks = new ArrayList<Block>();
        ArrayList<LinkedHashMap<Integer, Double>> observedPropagations = new ArrayList<LinkedHashMap<Integer, Double>>();
        int numberOfVariables = getNumberOfVariables();
        int numberOfObjectives = getNumberOfObjectives();
        ArrayList <Double> blocktimeSD = new ArrayList<Double>();
        ArrayList <Double> difficultySD = new ArrayList<Double>();
        double currentTime = 0L;
        long[] blockInterval = {INTERVAL};
        int[] difficultyInterval = {DIFFICULTY_INTERVAL};
        double[] averageDifficulty = {0.0};
        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];


        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;

        if (firstGARun == true)
        {
            blockInterval[0] = INTERVAL ;
            x[0] = (blockInterval[0]/1000);
            x[1] = (double) DIFFICULTY_INTERVAL;
            difficultyInterval[0] = DIFFICULTY_INTERVAL;
            firstGARun = false;
        }
        else
        {
            /**Init genes or variables */
            for (int i = 0; i < numberOfVariables; i++) {
                x[i] =  Math.round(solution.getVariable(i));
            }
            blockInterval[0] = (long) (x[0]*1000);
            difficultyInterval[0] = (int) x[1];
        }
        constructNetworkWithAllNode(NUM_OF_NODES,simulatedNodesGA, currentTime, blockInterval, averageDifficulty);
        simulatedNodesGA.get(0).genesisBlock(taskQueueGA,taskMapGA, observedBlocks, observedPropagations, currentTime, averageDifficulty);
        int j=1;
        while(getTask(taskQueueGA) != null){
            if(getTask(taskQueueGA) instanceof MiningTask){
                MiningTask task = (MiningTask) getTask(taskQueueGA);
                if(task.getParent().getHeight() == j) j++;
                if(j > ENDBLOCKHEIGHT){break;}
                //if(j%100==0 || j==2) writeGraph(j);
            }
            //mainGA newMain = new mainGA();
            //newMain.main (simulatedNodesGA,taskQueueGA,taskMapGA,observedBlocks,observedPropagations,currentTime);
            runTask(simulatedNodesGA,taskQueueGA,taskMapGA, observedBlocks, observedPropagations, currentTime, blockInterval, difficultyInterval, averageDifficulty);
        }
        Set<Block> blocks = new HashSet<Block>();
        Block block  = simulatedNodesGA.get(0).getBlock();

        int counter1 = 0;
        double oldInterval = 0;
        double newInterval = 0;
        double myInterval = 0;
        double myDifficulty = 0;

        while(block.getParent() != null){
            blocks.add(block);
            oldInterval = block.getTime();
            block = block.getParent();
            newInterval = block.getTime();
            myInterval = (oldInterval - newInterval)/1000; //convert to second
            blocktimeSD.add(myInterval);
            myDifficulty = (long) block.getDifficulty();
            difficultySD.add(myDifficulty);
            counter1 = counter1+1;
        }
        /** 1st objective: standard deviation of blocktime */
        f[0] = calculateSD(blocktimeSD);

        /** 2nd objective: standard deviation of difficulty */
        f[1] = calculateSD(difficultySD);

        /** To finalize objective and variables*/
        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, f[i]);
            solution.setVariable(i,x[i]);
        }

        System.out.println("Block time = " + x[0]);
        System.out.println("Difficulty interval = " + x[1]);
        System.out.println("Obj 1 = " + f[0]);
        System.out.println("Obj 1 = " + f[1]);
        System.out.println();

        /**
        try(FileWriter fw = new FileWriter("C:\\Users\\zihau.chin\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\GA-PARAMETERS.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(x[0] + "," + x[1] + "," + f[0] + "," + f[1]) ;

        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }*/
        observedPropagations = null;
        simulatedNodesGA = null;
        taskMapGA = null;
        taskQueueGA = null;
        observedBlocks = null;
        difficultySD = null;
        blocktimeSD = null;
        blockInterval = null;
        difficultyInterval  = null;
        averageDifficulty = null;
    }

    public  double calculateSD(ArrayList <Double> numArray)
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