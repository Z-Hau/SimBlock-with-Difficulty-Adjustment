package SimBlock.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;

import static SimBlock.simulator.Simulator.*;
import static SimBlock.settings.SimulationConfiguration.*;

public class myThread extends Thread {

    public void run()
    {
        System.out.println("Running GA............................................");
        runningGA = true;
        ParallelNSGAII parallelRunGA = new ParallelNSGAII();
        ArrayList<Double> myResult = parallelRunGA.main(null);
        OLD_INTERVAL = INTERVAL;
        OLD_DIFFICULTY_INTERVAL = DIFFICULTY_INTERVAL;
        NEW_INTERVAL = BigDecimal.valueOf((myResult.get(0)).intValue() * 1000);
        NEW_DIFFICULTY_INTERVAL = BigDecimal.valueOf(((myResult.get(1)))) ;
        //NEW_DIFFICULTY_INTERVAL = ((myResult.get(0)).intValue());
        firstGARun = true;
        GA_END_BLOCK_HEIGHT = CURRENT_BLOCK_HEIGHT;
        GA_TRIGGERED = true;
        FIRST_TIME = true;
        runningGA = false;
        DIFFICULTY_ADJUSTED = false;
        //System.out.println("New block interval = " + INTERVAL);
        //System.out.println("New difficulty interval = " + DIFFICULTY_INTERVAL);
        System.out.println("GA Stopped.............................................");
        try (FileWriter fw = new FileWriter("C:\\Users\\zihau\\Documents\\GitHub\\SimBlock-with-Difficulty-Adjustment\\" +
                "testing-ga.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(NEW_INTERVAL + "," + NEW_DIFFICULTY_INTERVAL + "," + GA_START_BLOCK_HEIGHT + "," + CURRENT_BLOCK_HEIGHT);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
}
