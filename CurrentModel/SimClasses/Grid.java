package SimClasses;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.Grid2Ddouble;
import HAL.GridsAndAgents.PDEGrid2D;
import HAL.Interfaces.SerializableModel;
import HAL.Rand;
import HAL.Util;
import SimClasses.Cell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static HAL.Util.*;
import static HAL.Util.GenIndicesArray;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Grid extends AgentGrid2D<Cell> implements SerializableModel, Serializable {


    private static final long serialVersionUID = -3344113518820573288L;

    // static String saveFolder = "/home/jhubadmin/TUMSIM/Tumor-Growth-Modelling/test/";


    //agent types and associated colour
    public final static int NORMAL= Util.RGB(0,1,0);
    public final static int NERCOTIC= Util.RGB(0.8,0.8,0.8);
    public final static int HYP= Util.RGB(0,0,1);
    public final static int VESSEL= Util.RGB(1,0,0);

    //model constants:
    public int countTumor;
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

    public Grid(int x, int y, Rand rng){ //grid constructor
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
        String elahePath = "Data"; // Replace this with the actual path to the "Elahe" folder

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

        // The following implementation is just to make sure we can seed the appropriate number of vessels in a random way
        // The important parameter is vessel_density and we can do other implementations as well.
        int vessel_density = 20; //#vessel/mm^2
        double numVessel = Math.pow((xDim*0.02),2) * vessel_density ; // 40000 for ~100/mm^2, 30000 for 75/mm^2
        int n = (int) Math.sqrt(numVessel); // The vessels are seeded in a n*n coarse grained grid.
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
                    // This is to check if the selected random grid site is of cell type. If not, then we can safely
                    // seed a vessel agent there.
                    Cell occupant = GetAgent(x, y);
                    if (occupant != null) {
                        occupant.Dispose();
                    }

                    NewAgentSQ(x, y).InitVessel();
                    // Or equivalently we can write:
                    // Cell freeSite = NewAgentSQ(x,y);
                    // freeSite.InitVessel();
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





    @Override
    public void SetupConstructors() {
        _PassAgentConstructor(Cell.class);

    }
}

