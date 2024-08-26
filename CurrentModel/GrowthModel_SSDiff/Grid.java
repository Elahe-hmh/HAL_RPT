package GrowthModel_SSDiff;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.Grid2Ddouble;
import HAL.Rand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static HAL.Util.CircleHood;
import static HAL.Util.GenIndicesArray;


public class Grid extends AgentGrid2D<Cell> {
    public Rand rng;
    public Main main;
    public Oxygen oxygen;
    public PK PBPK;
    public int currentVesselCounts;

    public Grid2Ddouble oxygenGrid;
    public RadioBio radioBio;
    public ArrayList<double[]> DoseRateList = new ArrayList<>();
    public ArrayList<double[]> PKStateVariables = new ArrayList<>();
    public ArrayList<double[]> PopsOverTime = new ArrayList<>();
    double[] CurrentCellsPops = new double[Params.NUMBER_OF_CELL_TYPES]; // where size is the desired length of the array

    public double tempAverageRad;




    ArrayList<Integer> vesselsIndex = new ArrayList<>();

    public Grid(int xDim, int yDim, Rand rng, Main main) {
        super(xDim, yDim, Cell.class);
        this.rng = rng;
        this.main = main;
        this.oxygen = new Oxygen(this);
        this.PBPK = new PK(this);
        this.radioBio = new RadioBio(this);
        this.oxygenGrid = new Grid2Ddouble(Params.xDim, Params.yDim);
    }


    // generates tumor cells and created vessels
    void Init(int currentDay, DaVinci drawer,DataLogger logger) throws IOException {
        this.currentVesselCounts = this.GenVessels();

        //SAVE INITIAL VASCULATURE
        double r_average=ParamsPBPK.perHourUpdateParams(this);
        this.oxygen.UpdateSteadyStateOxygen(currentDay,r_average); // updates the oxygenGrid
//        drawer.gridDrawOxygen();
//        drawer.gridDrawVessel();

//        logger.saveFigureOx("Results/Vasculature"+".png",drawer);
//        logger.saveFigureVessels("Results/vessels"+".png",drawer);


        this.SeedTumor();

//        PKStateVariables.add(new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0});
        PKStateVariables.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // C_bp, C_v, C_int, C_bound, C_intern, f
//        DoseRateList.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // Healty, Normal, Hypo, Necro, Apop, Vessel
        DoseRateList.add(new double[]{0.0}); //
        PopsOverTime.add(new double[Params.NUMBER_OF_CELL_TYPES]);
    }



    public void Step(int currentDay) {
        // the following variables are mechanism that enables us to divide a whole day into smaller portions (i.e. hours)
        // and update only a fraction of whole cell population at each our, in a way that after a day we have updated all
        // the cells.
        int counter = 0;
        int hourCount = -1;
//        int perHourCellUpdateBudget = (int) (this.Pop() / (Params.T_PER_Major()-1));
        int perHourCellUpdateBudget =Math.floorDiv((int) this.Pop(),(int) (Params.T_PER_Major()-1));
        ArrayList<double[]> survivalProbLookupTable = new ArrayList<>();
        for (Cell cell : this) {
            if (counter % perHourCellUpdateBudget == 0) {
                // this block will calculate oxygen steady state, and run PBPK
                hourCount++; // one hour is passed
                Params.updateGlobalTime(currentDay, hourCount);
                // if you are here, then it means we have updated large enough number of cells that it is now time to
                // calculate the steady state of oxygen again.
                double r_average=ParamsPBPK.perHourUpdateParams(this);
                this.oxygen.UpdateSteadyStateOxygen(currentDay, r_average); // updates the oxygenGrid
                this.PBPK.DoseRateCalc_DEBUG(0,Params.T_PER_Minor(), currentDay, hourCount); // pushes new values to the doseRate arrayList
                survivalProbLookupTable = this.radioBio.SurvivalProbLookupTableCalc(currentDay, hourCount);

                //time to log
                PopsOverTime.add(copyArray(CurrentCellsPops));
            }
            cell.Step(hourCount, currentDay, survivalProbLookupTable);
            counter++;
        }
        CleanAgents();
        ShuffleAgents(this.rng);

    }


    public void SeedTumor() {
        int[] hood = CircleHood(false, Params.INITIAL_TUMOR_RAD/3);
        int seedCellCount = MapHood(hood, xDim / 2, yDim / 2);
        for (int i = 0; i < seedCellCount; i++) {
            Cell c = GetAgent(hood[i]);
            if (c == null) {
                NewAgentSQ(hood[i]).Init(Params.NORMAL_TCELL_TYPE, 0);
            }
        }
    }


    public int GenVessels() throws IOException {
        this.GenerateVesselConf();
        VesselConfigConvertor convertor = new VesselConfigConvertor("Vasculature/uniform.csv");
        double[][] grid = convertor.grid;
        int vesselCount = 0;

        for (int i = 0; i < this.length; i++) {
            int x = ItoX(i);
            int y = ItoY(i);
            double r = Math.sqrt(Math.pow(x-xDim/2,2)+Math.pow(y-yDim/2,2));
            if (grid[x][y] == 1){
//                if (((y > this.yDim / 3 && y < this.yDim*2  / 3) || (x > this.yDim / 3 && x < this.yDim*2  / 3)) && this.rng.Double() < 0.5) {
//                if((r>50 && r<130) && this.rng.Double() < 0.8){
//                    NewAgentSQ(i).Init(Params.VESSEL_TYPE, 0);
//                    this.vesselsIndex.add(i);
//                    ++vesselCount;
//                }else{
//                    if (this.rng.Double() < 0.1){
                        NewAgentSQ(i).Init(Params.VESSEL_TYPE, 0);
                        this.vesselsIndex.add(i);
                        ++vesselCount;
//                    }
//                }
            }
        }

        System.out.println(vesselCount);
        return vesselCount;
    }

//    public int GenVesselsOLD() {
//        //create a Grid to store the locations that are too close for placing another vessel
//        Grid2Ddouble openSpots = new Grid2Ddouble(xDim, yDim); //planar grid; no wraparound
//        //create a neighborhood that defines all indices that are too close
//
//        // The following implementation is just to make sure we can seed the appropriate number of vessels in a random way
//        // The important parameter is vessel_density and we can do other implementations as well.
//        int vessel_density = (int) Params.VESSEL_DENSITY; //#vessel/mm^2
//        double numVessel = Math.pow((xDim * 0.02), 2) * vessel_density; // 40000 for ~100/mm^2, 30000 for 75/mm^2
//        int n = (int) Math.sqrt(numVessel); // The vessels are seeded in a n*n coarse grained grid.
//        int increment = xDim / n;
//        int initialPt = increment / 2;
//        int maxMove = increment / 2;
//        int[] vesselShiftHood = CircleHood(true, maxMove); //radius of circular neighborhood is vesselSpacingMin*/
//        int[] indicesToTry = GenIndicesArray(openSpots.length); //array of indicides
//        int vesselCount = 0; //count vessel
//
//
//        //for uniform placement with deviations
//        for (int i = initialPt; i < xDim; i += increment) {
//            for (int j = initialPt; j < yDim; j += increment) {
//                if (openSpots.Get(i) == 0) {
//                    int x = openSpots.ItoX(xDim * i + j);
//                    int y = openSpots.ItoY(yDim * i + j);
//                    int nSpots = openSpots.MapHood(vesselShiftHood, x, y);
//                    int k = vesselShiftHood[rng.Int(nSpots)];
//                    x = ItoX(k);
//                    y = ItoY(k);
//                    // This is to check if the selected random grid site is of cell type. If not, then we can safely
//                    // seed a vessel agent there.
//                    Cell occupant = GetAgent(x, y);
//                    if (occupant != null) {
//                        occupant.Dispose();
//                    }
//                    NewAgentSQ(x, y).Init(Params.VESSEL_TYPE);
//                    this.vesselsIndex.add(I(x, y));
//                    // Or equivalently we can write:
//                    // Cell freeSite = NewAgentSQ(x,y);
//                    // freeSite.InitVessel();
//                    ++vesselCount;
//                }
//            }
//        } //uniform vessel until here
///*
//        //for completely random placement
//        Random r = new Random();
//        int[] vesselIndices = r.ints((long) numVessel,0,xDim*yDim).toArray(); //1000x1000 grid
//        for (int i : vesselIndices) {
//            if (openSpots.Get(i)==0) {
//                int x = openSpots.ItoX(i);
//                int y = openSpots.ItoY(i);
//                Cell occupant = GetAgent(x, y);
//                if (occupant != null) {
//                    occupant.Dispose();
//                }
//                NewAgentSQ(x, y).InitVessel();
//                ++vesselCount;
//            }
//        } //random placement until here
//*/
//        return vesselCount;
//    }

    private void GenerateVesselConf(){
        //
        // runs the python code to generate the vessel config
        // the python code will run and generate array.csv

        // inputs of the python code: vesselDensityFunctionName, minVesselDistance, xDim
    }

    private double[] copyArray(double[] original) {
        return Arrays.copyOf(original, original.length);
    }


    // Steps over the cells.

}
