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
//import static SimBlock.simulator.Main.writeGraph;
import static SimBlock.simulator.Main.constructNetworkWithAllNode;
import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Simulator.*;


public class BoothProblem extends AbstractDoubleProblem{

    public BoothProblem() {
        setNumberOfVariables(2);

        setNumberOfObjectives(2);
        setName("BoothProblem");

        List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Double> upperLimit = new ArrayList<>(getNumberOfVariables());
        /*for (int i=0; i<getNumberOfVariables(); i++){
            lowerLimit.add(-10.0);
            upperLimit.add(10.0);
        }*/
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
        ArrayList<LinkedHashMap<Integer, Long>> observedPropagations = new ArrayList<LinkedHashMap<Integer, Long>>();
        int numberOfVariables = getNumberOfVariables();
        int numberOfObjectives = getNumberOfObjectives();
        ArrayList <Double> blocktimeSD = new ArrayList<Double>();
        ArrayList <Double> difficultySD = new ArrayList<Double>();
        long currentTime = 0L;

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        double blockInterval = 0;
        double difficultyInterval = 0;

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;

        if (firstGARun == true)
        {
            blockInterval = (double) INTERVAL/1000;
            x[0] = blockInterval;
            difficultyInterval = (double) DIFFICULTY_INTERVAL;
            x[1] = difficultyInterval;
            firstGARun = false;
        }
        else
        {
            /**Init genes or variables */
            for (int i = 0; i < numberOfVariables; i++) {
                x[i] =  Math.round(solution.getVariable(i));
            }
            blockInterval = x[0];
            difficultyInterval = x[1];
        }
        setTargetIntervalGA((long) blockInterval*1000);
        GA_DIFFICULTY_INTERVAL = (int) difficultyInterval;
        constructNetworkWithAllNode(NUM_OF_NODES,simulatedNodesGA, currentTime);
        simulatedNodesGA.get(0).genesisBlock(simulatedNodesGA,taskQueueGA,taskMapGA, observedBlocks, observedPropagations, currentTime);

        int j=1;
        while(getTask(simulatedNodesGA,taskQueueGA,taskMapGA) != null){
            if(getTask(simulatedNodesGA, taskQueueGA, taskMapGA) instanceof MiningTask){
                MiningTask task = (MiningTask) getTask(simulatedNodesGA, taskQueueGA, taskMapGA);
                if(task.getParent().getHeight() == j) j++;
                if(j > ENDBLOCKHEIGHT){break;}
                //if(j%100==0 || j==2) writeGraph(j);
            }
            //mainGA newMain = new mainGA();
            //newMain.main (simulatedNodesGA,taskQueueGA,taskMapGA,observedBlocks,observedPropagations,currentTime);
            runTask(simulatedNodesGA,taskQueueGA,taskMapGA, observedBlocks, observedPropagations, currentTime);
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

        try(FileWriter fw = new FileWriter("C:\\Users\\zihau\\Desktop\\simblock\\GA-PARAMETERS.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(x[0] + "," + x[1] + "," + f[0] + "," + f[1]) ;


        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }

    public synchronized double calculateSD(ArrayList <Double> numArray)
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