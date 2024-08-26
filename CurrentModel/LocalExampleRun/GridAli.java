package LocalExampleRun;

//for parallelizing

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.GridsAndAgents.Grid2Ddouble;
import HAL.GridsAndAgents.PDEGrid2D;
import HAL.Interfaces.SerializableModel;
import HAL.Rand;
import HAL.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static HAL.Util.*;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

@SuppressWarnings("serial")
class Cell extends AgentSQ2Dunstackable<GridAli> implements Serializable{
    private static final long serialVersionUID = 1L;  // Example version number
    public int type; //type determines the metabolic rate of the cell

    void InitVessel(){
        type= G.VESSEL;
    }

    //return false for vessel, true for cell or empty
    boolean cellOrVessel(Cell toCheck){
        if (toCheck==null){
            return true;
        } else if (toCheck.type== G.VESSEL) {
            return false;
        } else {
            return true;
        }
    }

    //return false for occupied; true for empty
    boolean occupiedOrEmpty(Cell toCheck){
        if (toCheck==null){
            return true;
        } else {
            return false;
        }
    }

    boolean Death(){ //cell death if [O] or ATP production rate falls below critical level AND rng is below death probability
        double resValOx= G.oxygen.Get(Isq());

        //double TUMOR_GL_METABOLISM_RATE = -2*Math.pow(10,-14)*(G.glucose.Get(Isq())/(G.glucose.Get(Isq()) + 2*Math.pow(10,-14)))*(G.TIMESTEP_DIFFUSION/60.0); //units is mol/min/cell, was 10^-14 before
        double TUMOR_GL_METABOLISM_RATE = -0.5*Math.pow(10,-13)*(G.glucose.Get(Isq())/(G.glucose.Get(Isq()) + 2*Math.pow(10,-14)))*(G.TIMESTEP_DIFFUSION/60.0);
        double ATP_PRODUCTION_TUMOR = (2*TUMOR_GL_METABOLISM_RATE+27*G.TUMOR_OX_METABOLISM_RATE/5); //usually ~10^-24-10^-23 mol/min/cell
        double death_prob= ProbScale(1.0-Math.pow(resValOx/ G.DEATH_CONC, 1), G.TIMESTEP);
        double death_prob_ATP = ProbScale(1.0-Math.pow(ATP_PRODUCTION_TUMOR/ G.DEATH_ATP_PROD_RATE, 2), G.TIMESTEP);

        if (resValOx<= G.DEATH_CONC) {
            if (G.rng.Double()<death_prob) {
                return true;
            } else if (ATP_PRODUCTION_TUMOR <= G.DEATH_ATP_PROD_RATE) {
                if (G.rng.Double()<death_prob_ATP) {
                    return true;
                }
                else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (ATP_PRODUCTION_TUMOR <= G.DEATH_ATP_PROD_RATE) {
            if (G.rng.Double()<death_prob_ATP) {
                return true;
            }
            else {
                return false;
            }
        } else {
            return false;
        }

    }

    boolean Divide(){
        double resVal= G.oxygen.Get(Isq()); //effectively the probability
        double prob= resVal/G.BLOOD_VESSEL_OXYGEN;
        double divide_prob= ProbScale(prob,G.TIMESTEP);
        return ( (resVal> G.DEATH_CONC) && (G.rng.Double()< divide_prob) );
    }

    boolean Hypoxia(){
        double resVal= G.oxygen.Get(Isq());
        if ( (resVal<=G.HYPOXIA_CONC) ) {
            return true;
        } else {
            return false;
        }
    }

    boolean Normal() {
        double resVal= G.oxygen.Get(Isq());
        return resVal > G.HYPOXIA_CONC;
    }



    void MetabolismOx(){

        if (this.type==G.NORMAL){  //normal tumor cell with normal metabolism rate
            G.oxygen.Add(Isq(), G.TUMOR_OX_METABOLISM_RATE); //metabolism uses up oxygen
        } else if (this.type==G.VESSEL) {
            G.oxygen.Set(Isq(), G.BLOOD_VESSEL_OXYGEN); //oxygen concentration set to maximum
        } else if (this.type==G.HYP) {
            G.oxygen.Add(Isq(), G.HYP_OX_METABOLISM_RATE);
        }
    }

    void MetabolismGl(){

        if (this.type==G.NORMAL){  //normal tumor cell with normal metabolism rate
            double TUMOR_GL_METABOLISM_RATE = -0.5*Math.pow(10,-13)*(G.glucose.Get(Isq())/(G.glucose.Get(Isq()) + 2*Math.pow(10,-14)))*(G.TIMESTEP_DIFFUSION/60.0);
            G.glucose.Add(Isq(), TUMOR_GL_METABOLISM_RATE); //metabolism uses up glucose
        } else if (this.type==G.VESSEL) {
            G.glucose.Set(Isq(), G.BLOOD_VESSEL_GLUCOSE); //oxygen concentration set to maximum
        } else if (this.type==G.HYP) {
            double HYP_GL_METABOLISM_RATE = -1.0196*Math.pow(10,-13)*(G.glucose.Get(Isq())/(G.glucose.Get(Isq()) + 2*Math.pow(10,-14)))*(G.TIMESTEP_DIFFUSION/60.0);
            G.glucose.Add(Isq(), HYP_GL_METABOLISM_RATE);
        }
    }

    public void StepCell(){
        double moveProbVessel= 0.50;
        double killProbVessel= 1.0; //0.05; //number randomly chosen, need to find supporting info

        int currentCellType= this.type;

        if ((currentCellType==G.NORMAL) || (currentCellType==G.HYP) ){ //if we have normal/hypoxia tumor cell
            if (Death()){ //normal or hypoxic tumor cell becomes nercotic
                this.type=G.NERCOTIC;
                return;
            }
            else if (Hypoxia()){ //normal tumor cell becomes hypoxic
                this.type=G.HYP;
            }
            else { //if oxygen concentration is in the normal tumor range, hypoxic cell can return to normal cell
                this.type=G.NORMAL;
            }

            if (Divide()){ //normal or hypoxic tumor cell divides; daughter cell has same type as parent cell
                int options= MapEmptyHood(G.divHood);
                if (options>0){
                    int locCell= G.divHood[G.rng.Int(options)];
                    Cell toBeReplaced= G.GetAgent(locCell);
                    boolean isCell= cellOrVessel(toBeReplaced); //false for vessel; true for cell or empty
                    if (!isCell) { //location occupied by vessel; vessel is replaced by daughter cell
                        int optionsVessel= MapEmptyHood(G.divHood); //divHood is a moore neighbourhood
                        if (optionsVessel>0 && G.rng.Double()< moveProbVessel) { //if there are empty locations around the vessel; one is randomly selectiod and the vessel is moved to that location
                            int position;
                            while (true){
                                int movePosition= G.divHood[G.rng.Int(optionsVessel)];
                                Cell moveVesselLocationAgent= G.GetAgent(movePosition);
                                boolean identity= occupiedOrEmpty(moveVesselLocationAgent); //false for vessel; true for cell or empty
                                if (identity==true){ //location is empty
                                    position=movePosition;
                                    break;
                                }
                            }
                            toBeReplaced.MoveSQ(position);


                            /*remove the 2 lines below if vessel deleting is implemented again*/
                            G.NewAgentSQ(locCell).type= currentCellType;
                            G.countTumor= G.countTumor+1;

                        } 
                    } else if (toBeReplaced==null){ //location is empty; daughter cell created in new location
                        G.NewAgentSQ(locCell).type= currentCellType; //daughter cell has same metabolic rate as parent
                        //System.out.println("daughter cell");
                        G.countTumor= G.countTumor+1;
                    } else { //location is occupied by another cell; nothing is done
                        return;
                    }
                }
                else {
                    int occupied= MapOccupiedHood(G.divHood);
                    int locCell= G.divHood[G.rng.Int(occupied)];
                    Cell toBeReplaced= G.GetAgent(locCell);
                    boolean isCell= cellOrVessel(toBeReplaced); //false for vessel; true for cell
                    if (isCell==false) { //location occupied by vessel; vessel is replaced by daughter cell
                        int optionsVessel= MapEmptyHood(G.divHood); //divHood is a moore neighbourhood
                        if (optionsVessel>0 && G.rng.Double()< moveProbVessel) { //if there are empty locations around the vessel; one is randomly selectiod and the vessel is moved to that location
                            int position;
                            while (true){
                                int movePosition= G.divHood[G.rng.Int(optionsVessel)];
                                Cell moveVesselLocationAgent= G.GetAgent(movePosition);
                                boolean identity= occupiedOrEmpty(moveVesselLocationAgent); //false for vessel; true for cell or empty
                                if (identity==true){ //location is empty
                                    position=movePosition;
                                    break;
                                }

                            }
                            toBeReplaced.MoveSQ(position);

                            G.NewAgentSQ(locCell).type= currentCellType;
                            G.countTumor= G.countTumor+1;

                        } else if (G.rng.Double()< killProbVessel) { //if there are no empty locations around the vessel and probability is met, the vessel is killed
                            toBeReplaced.Dispose(); //vessel can be disposed
                            G.NewAgentSQ(locCell).type= currentCellType;
                            G.countTumor= G.countTumor+1;
                        }
                        //System.out.println("vessel replaced by cell");

                    } else { //all locations are occupied by other cells; nothing is done
                        //System.out.println("nothing done; all locations around occupied; seleced location was occupied by cells");
                        return;

                    }
                }
            }
        }
    }
}

@SuppressWarnings("serial")
public class GridAli extends AgentGrid2D<Cell> implements SerializableModel, Serializable{
    private static final long serialVersionUID = -3344113518820573288L;

    // static String saveFolder = "/home/jhubadmin/TUMSIM/Tumor-Growth-Modelling/test/";


    //agent types and associated colour
    public final static int NORMAL= Util.RGB(0,1,0);
    public final static int NERCOTIC= Util.RGB(0.8,0.8,0.8);
    public final static int HYP= Util.RGB(0,0,1);
    public final static int VESSEL= Util.RGB(1,0,0);

    //model constants:
    int countTumor;
    public double TIMESTEP = 1.0 / 24.0;//1 hours per timestep; cell division takes approx. 1day
    public double TIMESTEP_DIFFUSION= 0.03086; //0.03086 seconds for 10 micron diffusion distance/timestep_diffusion; 0.06049 s for 14 micron diffusion distance;
    //0.05216 s for 13 microns diffusion distance; 0.04444 s for 12 microns diffusion distance
    // 0.03735 s for 11 microns diffusion distance
    public double SPACE_STEP = 20.0;//um

    //metabolism and cell state constants
    double TUMOR_OX_METABOLISM_RATE = -4.01*Math.pow(10,-15)*(TIMESTEP_DIFFUSION/60.0);  // oxygen consumption rate; units is mol/min/cell; before random value was-0.8
    double HYP_OX_METABOLISM_RATE = -2.005*Math.pow(10,-15)*(TIMESTEP_DIFFUSION/60.0); //oxygen consumption rate; units is mol/min/cell
    double BACKGROUND_OX_METABOLISM_RATE = -2.5*Math.pow(10,-18)*(TIMESTEP_DIFFUSION/60.0); //oxygen consumption rate; units is mol/min/cell
    double BLOOD_VESSEL_OXYGEN= 5.36*Math.pow(10,-16); //oxygen concentration in blood (repleished to this concentration at the beginning of each diffusion step)
    double HYPOXIA_CONC= 8.143*Math.pow(10,-17);       // 1% O2
    double DEATH_CONC= 0.1*HYPOXIA_CONC;               // 0.1% O2
    

    double BLOOD_VESSEL_GLUCOSE= 4*Math.pow(10,-14)*0.6; //amount of oxygen provided to the cells from the blood vessles in mol/min/20 microns segment
    //x0.59 for relative diffusion of glucose across capillary membrane

    double DEATH_ATP_PROD_RATE = 2.57*Math.pow(10,-14)*(TIMESTEP_DIFFUSION/60.0);


    //diffusion constants
    double OX_DIF_RATE_VAL = 1.62*Math.pow(10,-5); //unit: cm^2/sec, realistic diffusion coefficient of oxygen
    double OX_DIFF_RATE = OX_DIF_RATE_VAL * TIMESTEP_DIFFUSION / (SPACE_STEP * SPACE_STEP)* Math.pow(10,8); //unitless diffusion rate calculation following HAL manual
    double GL_DIF_RATE_VAL = 2.7*Math.pow(10,-6); //unit: cm^2/sec, realistic diffusion coefficient of glucose
    double GL_DIFF_RATE = GL_DIF_RATE_VAL * TIMESTEP_DIFFUSION / (SPACE_STEP * SPACE_STEP)* Math.pow(10,8);


    //internal model objects
    public Rand rng; //random number generator
    public int[] divHood= MooreHood(false);
    public int[] vnHood= Util.VonNeumannHood(false);
    public int[] killVessel= MooreHood(false);
    PDEGrid2D oxygen; //PDE Grid for oxygen concentration
    PDEGrid2D glucose; //PDE Grid for glucose concentration

    public GridAli(int x, int y, Rand rng){ //grid constructor
        super(x, y, Cell.class);
        this.rng=rng;
        oxygen =new PDEGrid2D(x,y);//PDE grid for diffusion of oxygen
        glucose = new PDEGrid2D(x,y);//PDE grid for diffusion of glucose
    }

    public int TumorSize(int[] tumorNeighborhood) {
        //get a list of indices that fill a circle at the center of the grid
        int hoodSize=MapHood(tumorNeighborhood,xDim/2,yDim/2);
        return hoodSize;
    }
    public void DiffStepOx(){ //consumption and production of oxygen/glucose, and diffusion
        for (int i=0; i<800; i++){ 

            //tumor cell metabolism and vessel value setting
            for (Cell tumorCells : this) {
                tumorCells.MetabolismOx();
            }

            //non-tumor cell metabolism
            for (int j = 0; j < length; j++) {
                Cell occupant = GetAgent(j);
                if (occupant == null) {
                    oxygen.Add(j, BACKGROUND_OX_METABOLISM_RATE);
                }
            }

            //diffusion
            oxygen.Diffusion(OX_DIFF_RATE);
            oxygen.Update();
        }
    }

    public void DiffStepGl(){ //consumption and production of oxygen/glucose, and diffusion
        for (int i=0; i<1600; i++){

            //tumor cell metabolism and vessel value setting
            for (Cell tumorCells : this) {
                tumorCells.MetabolismGl();
            }

            //non-tumor cell metabolism
            for (int j = 0; j < length; j++) {
                Cell occupant = GetAgent(j);
                double BACKGROUND_GL_METABOLISM_RATE = -5*Math.pow(10,-15)*(glucose.Get(j)/(glucose.Get(j) + 2*Math.pow(10,-14)))*(TIMESTEP_DIFFUSION/60.0); //coefficient was 2 before
                if (occupant == null) {
                    glucose.Add(j, BACKGROUND_GL_METABOLISM_RATE);
                }
            }

            //diffusion
            glucose.Diffusion(GL_DIFF_RATE);
            glucose.Update();
        }
    }



    public void ModelStep(){ //executed once per time step by each Cell
        for (Cell cell: this) { //iterate over all cells on the grid
            if (cell.type != VESSEL) {
                cell.StepCell();
            }
        }
        DiffStepOx();//cell metabolism and oxygen diffusion
        DiffStepGl();//cell metabolism and glucose diffusion
    }


//    public static String newDirectory() {
//
//        System.out.println("Enter the path to save model files: ");
//        Scanner sc = new Scanner(System.in);
//        String completePath = sc.next();
//        //Creating a File object
//        File file = new File(completePath);
//        //Creating the directory
//        boolean bool = file.mkdir();
//        sc.close();
//        if(bool){
//            System.out.println("Directory created successfully in "+ completePath);
//            // System.out.println("Complete path is "+ completePath);
//        }else{
//            System.out.println("Sorry couldn’t create specified directory");
//        }
//
//        return completePath;
//
//
//    }
    public static String newDirectory() {
        // Define the path to the "Elahe" folder and its "Runs" subfolder
        String elahePath = "/home/jhubadmin/TUMSIM/Tumor-Growth-Modelling/Elaheh/Ali"; // Replace this with the actual path to the "Elahe" folder

        String completePath = elahePath;
        // Creating a File object
        File file = new File(completePath);
        // Creating the directory
        boolean bool = file.mkdir();
        //sc.close();

        //        if(bool){
        //            System.out.println("Directory created successfully in " + completePath);
        //        }else{
        //            System.out.println("Sorry couldn’t create specified directory");
        //        }

        return completePath;
    }

    public static String newOxFileName(String path) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-HH_mm_ss");

        String formattedDate = myDateObj.format(myFormatObj);
        String fileName= path+"/"+formattedDate+"oxygen";
        return fileName;
    }

    public static String newGlFileName(String path) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-HH_mm_ss");

        String formattedDate = myDateObj.format(myFormatObj);
        String fileName= path+"/"+formattedDate+"glucose";
        return fileName;
    }

    public static String newAgentFileName(String path) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-HH_mm_ss");

        String formattedDate = myDateObj.format(myFormatObj);
        String fileName= path+"/"+formattedDate+"agent";
        return fileName;
    }

    public BufferedImage WriteBufferedOxygen() {
        BufferedImage forTiff = new BufferedImage(xDim, yDim, TYPE_INT_ARGB);
        Grid2Ddouble Spot=new Grid2Ddouble(xDim,yDim);
        for (int i = 0; i < length; i++) {
            double resVal = oxygen.Get(i)/BLOOD_VESSEL_OXYGEN;
            forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(resVal, 0 ,0));
        }
        return forTiff;
    }

    public BufferedImage WriteBufferedGlucose() {
        BufferedImage forTiff = new BufferedImage(xDim, yDim, TYPE_INT_ARGB);
        Grid2Ddouble Spot=new Grid2Ddouble(xDim,yDim);
        for (int i = 0; i < length; i++) {
            double resVal = glucose.Get(i)/BLOOD_VESSEL_GLUCOSE;
            forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(resVal, 0 ,0));
        }
        return forTiff;
    }

    public BufferedImage WriteBufferedAgent() {
        BufferedImage forTiff = new BufferedImage(xDim, yDim, TYPE_INT_ARGB);
        Grid2Ddouble Spot=new Grid2Ddouble(xDim,yDim);
        int agentType;

        for (int i = 0; i < length; i++) { // saving agent types
            Cell currentAgent = GetAgent(i);
            if (currentAgent != null) {
                agentType = currentAgent.type;
                if (agentType == VESSEL) {
                    forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(1, 0, 0));
                } else if (agentType == NORMAL) {
                    forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(0, 1, 0));
                } else if (agentType == HYP) {
                    forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(0, 0, 1));
                } else if (agentType == NERCOTIC) {
                    forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), Util.RGB(0.8, 0.8, 0.8));
                }

            }else {
                forTiff.setRGB(Spot.ItoX(i), Spot.ItoY(i), BLACK);
            }
        }
        return forTiff;
    }

    public void WriteTiff(BufferedImage bufferedImage, String filename) throws IOException {
        File file = new File(filename+".tif");
        ImageIO.write(bufferedImage, "TIFF", file);
    }
    public void SaveFinalState(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this); // Serialize the entire Grid15 object
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int GenVessels(){
        //create a Grid to store the locations that are too close for placing another vessel
        Grid2Ddouble openSpots=new Grid2Ddouble(xDim,yDim); //planar grid; no wraparound
        //create a neighborhood that defines all indices that are too close
        int vessel_density = 20; //#vessel/mm^2
        double numVessel = Math.pow((xDim*0.02),2) * vessel_density ; // 40000 for ~100/mm^2, 30000 for 75/mm^2
        int n = (int) Math.sqrt(numVessel);
        int increment = xDim/n;
        int initialPt = increment/2;
        int maxMove = increment/2;
        int[] vesselShiftHood = CircleHood(true,maxMove); //radius of circular neighborhood is vesselSpacingMin*/
        int[] indicesToTry=GenIndicesArray(openSpots.length); //array of indicides
        int vesselCount = 0; //count vessel


        //for uniform placement with deviations
        for (int i = initialPt; i < xDim; i += increment) {
            for (int j = initialPt; j < yDim; j += increment){
                if (openSpots.Get(i)==0) {
                    int x = openSpots.ItoX(xDim * i + j);
                    int y = openSpots.ItoY(yDim * i + j);
                    int nSpots = openSpots.MapHood(vesselShiftHood, x, y);
                    int k = vesselShiftHood[rng.Int(nSpots)];
                    x = ItoX(k);
                    y = ItoY(k);

                    Cell occupant = GetAgent(x, y);
                    if (occupant != null) {
                        occupant.Dispose();
                    }
                    NewAgentSQ(x, y).InitVessel();
                    ++vesselCount;
                }
            }
        } //uniform vessel until here


/*
        //for completely random placement
        Random r = new Random();
        int[] vesselIndices = r.ints((long) numVessel,0,xDim*yDim).toArray(); //1000x1000 grid
        for (int i : vesselIndices) {
            if (openSpots.Get(i)==0) {
                int x = openSpots.ItoX(i);
                int y = openSpots.ItoY(i);
                Cell occupant = GetAgent(x, y);
                if (occupant != null) {
                    occupant.Dispose();
                }
                NewAgentSQ(x, y).InitVessel();
                ++vesselCount;
            }
        } //random placement until here

*/

        return vesselCount;
    }


    public static void main(String[] args) throws IOException {

        System.out.println("Start");
        long startTime = System.nanoTime();

        // assuming tumour cell diameter of 20 microns
        // as there is one cell per grid location
        int x = 1000, y = 1000, msPause = 5; //future try 2000x2000
        double tumorRadCm = 0.5; //future try 1.00, for now try 0.5
        // int winScale = 1; // used to be 5
        double convFactor = 500; // 1 unit=sqrt(0.0004)= 0.02 mm= 20 micron= 0.002 cm, 1 cm= 500 units
        // where units is the distance between grid locations
        // double tumorRad= convFactor*tumorRadScaled; //radius of tumor in terms of
        // unit given;
        double tumorRad = convFactor * tumorRadCm;

        int[] tumorNeighborhood = Util.CircleHood(true, tumorRad);

        GridAli model = new GridAli(x, y, new Rand());


        String filePath = newDirectory(); // needed for saving all forms

        // generating blood vessels
        model.GenVessels();
        // Diffuse to steady state
        System.out.println("Initializing Blood Vessels");
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
        model.NewAgentSQ(model.xDim / 2, model.yDim / 2).type = NORMAL;
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


                model.WriteTiff(model.WriteBufferedOxygen(), newOxFileName(filePath));
                model.WriteTiff(model.WriteBufferedGlucose(), newGlFileName(filePath));
                System.out.println(totalTime); //prints out time it takes code to run in nanoseconds
                System.out.println(tick);
                System.out.println("Done");
                System.out.println(trackSize);
                model.SaveFinalState("/home/jhubadmin/TUMSIM/Tumor-Growth-Modelling/Elaheh/Ali/Ali_NextModel10.ser");
                break;
            }

            tick=tick+1;

            trackSize.add(currentPop);


            //update progress on terminal every 10 steps
            if (tick % 10==0){
                System.out.println(model.countTumor);
                model.WriteTiff(model.WriteBufferedAgent(), newAgentFileName(filePath));
                //model.WriteTiff(model.WriteBufferedOxygen(), newOxFileName(filePath));

            }
        }
}

    @Override
    public void SetupConstructors() {
        _PassAgentConstructor(Cell.class);

    }


}