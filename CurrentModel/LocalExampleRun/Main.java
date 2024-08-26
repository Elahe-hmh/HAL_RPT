package LocalExampleRun;
import HAL.Rand;
import HAL.Util;

import SimClasses.Grid;
import SimClasses.Cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("Start");
        long startTime = System.nanoTime();

        // assuming tumour cell diameter of 20 microns
        // as there is one cell per grid location
        int x = 20, y = 20, msPause = 5; //future try 2000x2000
        double tumorRadCm = 0.01; //future try 1.00, for now try 0.5
        // int winScale = 1; // used to be 5
        double convFactor = 500; // 1 unit=sqrt(0.0004)= 0.02 mm= 20 micron= 0.002 cm, 1 cm= 500 units
        // where units is the distance between grid locations
        // double tumorRad= convFactor*tumorRadScaled; //radius of tumor in terms of
        // unit given;
        double tumorRad = convFactor * tumorRadCm;

        int[] tumorNeighborhood = Util.CircleHood(true, tumorRad);

        Grid model = new Grid(x, y, new Rand());


        String filePath = model.newDirectory(); // needed for saving all forms

        // generating blood vessels
        model.GenVessels();
        // Diffuse to steady state
        System.out.println("Initializing Blood Vessels");

        // TODO: Why is this happening? Oxygen has higher diffusion rate than Glucose.
        for (int i=0; i<16; i++) {
            model.DiffStepOx();
        }
        for (int i=0; i<5; i++) {
            model.DiffStepGl();
        }


        System.out.println("Blood Vessel Initialization Complete");

        int neighbourHoodSize = model.TumorSize(tumorNeighborhood); // total number of cells of the given tumour size

        //dispose vessel if it occupies the middle unit
        Cell occupantMid = model.GetAgent(model.xDim / 2, model.yDim / 2);
        if (occupantMid != null) {
            occupantMid.Dispose();
        }
        // initialize model (plant a normal tumor cell)
        model.NewAgentSQ(model.xDim / 2, model.yDim / 2).type = model.NORMAL;
        model.countTumor = 1;



        // main run loop begins
        int tick = 0;// number of cell steps taken
        int previousPop = model.countTumor;
        int currentPop = 0;
        int countNoGrow = 0;
        List<Integer> trackSize = new ArrayList<Integer>();
        System.out.println("Cell Initialization Complete");
        System.out.println("Number of Tumor Cells Needed: " + neighbourHoodSize);


        while(true) {

            //breaks loop when there is no tumor cell
            if (model.countTumor<=0){
                System.out.println("No Tumor Cell; Model Ended");
                break;
            }


            //loops breaks if the tumor cell population doesn't change for 40 loops
            currentPop= model.countTumor;
            if (currentPop==previousPop){
                System.out.println("Previous Pop: "+ previousPop+" Current Pop: "+currentPop);
                countNoGrow++;
                if (countNoGrow>=50) {
                    System.out.println("Tumor Has Stopped Growing Before Reaching Desired Size.");
                    int diff=  neighbourHoodSize-currentPop;
                    System.out.println("Number of Desired Tumor Cells: "+ neighbourHoodSize +" With a Difference of "+ diff+ " cells. ");
                    long stopTime = System.nanoTime();
                    long totalTime=   (stopTime - startTime); //time took to run model in nanoseconds

                    System.out.println("Time for Code to Run in Nanosecond: "+ totalTime); //prints out time it takes code to run in nanoseconds
                    System.out.println("Number of Steps: "+ tick);
                    System.out.println("Done");
                    break;
                }
            } else {
                countNoGrow = 0;
            }
            previousPop=currentPop;

            model.ModelStep();

            /*To generate TIFF files every hour
            model.WriteTiff(model.WriteBufferedAgent(), newAgentFileName(filePath));
            model.WriteTiff(model.WriteBufferedOxygen(), newOxFileName(filePath));
            model.WriteTiff(model.WriteBufferedGlucose(), newGlFileName(filePath));*/

            //loop breaks when tumor meets the desired size
            if (model.countTumor>=neighbourHoodSize){ //if (model.countTumor>=neighbourHoodSize) for stopping based on tumor size or (tick>=numSteps) for stopping based on number of time steps
                long stopTime = System.nanoTime();
                long totalTime=   (stopTime - startTime);
                System.out.println("Complete");
                System.out.println("Number of Tumor Cells "+ model.countTumor);


                model.WriteTiff(model.WriteBufferedOxygen(), model.newOxFileName(filePath));
                model.WriteTiff(model.WriteBufferedGlucose(), model.newGlFileName(filePath));
                System.out.println(totalTime); //prints out time it takes code to run in nanoseconds
                System.out.println(tick);
                System.out.println("Done");
                System.out.println(trackSize);
                //model.SaveFinalState("/home/jhubadmin/TUMSIM/Tumor-Growth-Modelling/Elaheh/Ali/Ali_NextModel10.ser");
                break;
            }

            tick=tick+1;

            trackSize.add(currentPop);


            //update progress on terminal every 10 steps
            if (tick % 10==0){
                System.out.println(model.countTumor);
                model.WriteTiff(model.WriteBufferedAgent(), model.newAgentFileName(filePath));
                //model.WriteTiff(model.WriteBufferedOxygen(), newOxFileName(filePath));

            }
        }
    }
}


// Things to ask from Arman
// 1. About the initial steps for oxygen and Glucose diffusion --> IMPORTANT: THIS CONTRADICTS THE FACT THAT THE
// NUMBER OF LOOPS FOR THE DIFFUSIN FOR OXYGEN AND GLUCOSE ARE ALMOST THE OTHER WAY AROUND.
// 2. About the vessel move probability in the first line of the StepCell funciton.
// 3. About the reason for the 800 number of loops for DiffStepOx