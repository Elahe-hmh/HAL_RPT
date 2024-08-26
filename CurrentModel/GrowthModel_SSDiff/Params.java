package GrowthModel_SSDiff;

import HAL.Util;

import static HAL.Util.*;

public class Params {


//    ███████ ██ ███    ███     ██████   █████  ██████   █████  ███    ███
//    ██      ██ ████  ████     ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//    ███████ ██ ██ ████ ██     ██████  ███████ ██████  ███████ ██ ████ ██
//         ██ ██ ██  ██  ██     ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//    ███████ ██ ██      ██     ██      ██   ██ ██   ██ ██   ██ ██      ██
    // The following parameters takes care of the strict removal rule. If a cell (necro or aopop) is not removed
    // after the corresponding average time, then after strictRemovalCoeff*Average removal time we will force and
    // remove the cell.
    public static double strictRemovalCoeff = 2.0;
    public static int globalTime = 0;
    public static double markovTimeStep = 1 * T_PER_Minor() * MinorLoop_PER_MajorLoop(); // T
    // The unit of number "1" in the equation above is MajorLoop. We update each cell, once in the MajorLoop.
    // In other words, we hardcoded the markovTimeStep to be exactly 1 MajorLoop duration
    public static double dtStart=0.01; // T
    public static double errorTolerance = 1E-5;
    public static int r_max = 40;

    // TODO: write this in the unti of T not Major
    public static int maxLookupAge = 20 ; // Major
    // (5) Major (Day!)
    // the lookup table will contain the Survival probabilities for different ages of cells, ranging from
    // 1 day upto maxLookupAge.


//    ██████   ██████  ███    ███  █████  ██ ███    ██     ██████   █████  ██████   █████  ███    ███
//    ██   ██ ██    ██ ████  ████ ██   ██ ██ ████   ██     ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//    ██   ██ ██    ██ ██ ████ ██ ███████ ██ ██ ██  ██     ██████  ███████ ██████  ███████ ██ ████ ██
//    ██   ██ ██    ██ ██  ██  ██ ██   ██ ██ ██  ██ ██     ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//    ██████   ██████  ██      ██ ██   ██ ██ ██   ████     ██      ██   ██ ██   ██ ██   ██ ██      ██


    public static int xDim = (int) (RealWorldParams.domainSize * L_PER_um());
    public static int yDim = xDim;
    public static int zDim = xDim;


//
//      ██████ ███████ ██      ██          ██████   █████  ██████   █████  ███    ███
//     ██      ██      ██      ██          ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//     ██      █████   ██      ██          ██████  ███████ ██████  ███████ ██ ████ ██
//     ██      ██      ██      ██          ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//      ██████ ███████ ███████ ███████     ██      ██   ██ ██   ██ ██   ██ ██      ██
//


    public static double D_O2 = RealWorldParams.D_O2 * Math.pow(L_PER_um(),2) / T_PER_second();
    public static int INITIAL_TUMOR_RAD = (int) (RealWorldParams.initialTumorRad*L_PER_um() )  ;
    public static double VESSEL_OXYGEN_CONCENTRATION = RealWorldParams.P_O2_Vessel * RealWorldParams.O2_HenryConstant; // nmol/um^3
    public static double Vein_OXYGEN_CONCENTRATION = RealWorldParams.P_O2_Vein * RealWorldParams.O2_HenryConstant; // nmol/um^3
    public static double NECROTIC_THRESHOLD = RealWorldParams.NecroticThreshold  * RealWorldParams.O2_HenryConstant;
    public static double HYPOXIC_THRESHOLD = RealWorldParams.HypoxicThreshold * RealWorldParams.O2_HenryConstant;
    // TODO: take care of the following parameter
    public static double[] CELLS_CONSUMPTION_RATE_LIST = RealWorldParams.CELLS_CONSUMPTION_RATE_LIST; // this contains the consumption rate of each cell type.

    // transition probability = rate * markovTimeStep
    public static double NecroRemovalTime = RealWorldParams.NecroRemovalTime * T_PER_second() ;
    public static double ApopRemovalTime = RealWorldParams.ApopRemovealTime * T_PER_second() ;
    public static double CellCycleDuration = RealWorldParams.cellCycleDuration * T_PER_second();
    public static double NECROTIC_REMOVAL_PROB = (1/NecroRemovalTime) * markovTimeStep;
    public static double APOP_REMOVAL_PROB = (1/ApopRemovalTime) * markovTimeStep;
    public static double DIVISON_PROB_MAX = (1/CellCycleDuration) * markovTimeStep;


    public static double repairRate = RealWorldParams.repairRate; // per hour
    public static double[] alphaValues = RealWorldParams.alphaValues; // First one for normal and the second one for the hypo
    public static double[] betaValues = RealWorldParams.betaValues; // First one for normal and the second one for the hypo



//    ██ ███    ██ ██████  ███████ ██   ██
//    ██ ████   ██ ██   ██ ██       ██ ██
//    ██ ██ ██  ██ ██   ██ █████     ███
//    ██ ██  ██ ██ ██   ██ ██       ██ ██
//    ██ ██   ████ ██████  ███████ ██   ██
//

    public static int HEALTHY_CELL_TYPE = 0;
    public static int NORMAL_TCELL_TYPE = 1;
    public static int HYPO_TCELL_TYPE = 2;
    public static int NECRO_TCELL_TYPE = 3;
    public static int APOP_TCELL_TYPE = 4;
    public static int VESSEL_TYPE = 5;
//    public static int[] COLORLIST = {BLACK, GREEN, Util.RGB(0.075, 0.549,0), Util.RGB(0.5,0.5,0.5), YELLOW, Util.RGB(0.1,0.77,0.77)};
    public static int[] COLORLIST = {BLACK, Util.RGB(1,0.345,0.831), Util.RGB(0.69,0,0.667), Util.RGB(0.5,0.5,0.5), YELLOW, Util.RGB(0.1,0.77,0.77)};
    public static int NUMBER_OF_CELL_TYPES = COLORLIST.length; // this is the number of cell types that we have in the simulation
    public static int[] divHood = MooreHood(false);



//
//    ███████ ██    ██ ███    ██  ██████ ████████ ██  ██████  ███    ██ ███████
//    ██      ██    ██ ████   ██ ██         ██    ██ ██    ██ ████   ██ ██
//    █████   ██    ██ ██ ██  ██ ██         ██    ██ ██    ██ ██ ██  ██ ███████
//    ██      ██    ██ ██  ██ ██ ██         ██    ██ ██    ██ ██  ██ ██      ██
//    ██       ██████  ██   ████  ██████    ██    ██  ██████  ██   ████ ███████
//


    public static void updateGlobalTime(int currentDay, int currentHour){
        int globalTime = (int) (currentDay * Params.T_PER_Major() + currentHour);
        Params.globalTime = globalTime;
    }


    public static void LoadClass(){

    }

    // The followings are our definition of the timescale T
    // MajorLoop = g * cellCycle ||| g is determined in a way that to make sure the cell cycle
    // is appropriately in sync with the dynamics of the system
    // MinorLoop = f * MajorLoop ||| f is determined by the fact that due to the cell dynamics
    // in the simulation, there has been enough changes in the cellMap that we need to update the fields
    // T = 1 MinorLoop ||| This means that T is actually just a renaming of the parameter MinorLoop. We want
    // to make the MinorLoop as T since it is the smallest timescale in the simulation
    // Manor


//      ██████  ██████  ███    ██ ██    ██ ███████ ██████  ███████ ██  ██████  ███    ██
//     ██      ██    ██ ████   ██ ██    ██ ██      ██   ██ ██      ██ ██    ██ ████   ██
//     ██      ██    ██ ██ ██  ██ ██    ██ █████   ██████  ███████ ██ ██    ██ ██ ██  ██
//     ██      ██    ██ ██  ██ ██  ██  ██  ██      ██   ██      ██ ██ ██    ██ ██  ██ ██
//      ██████  ██████  ██   ████   ████   ███████ ██   ██ ███████ ██  ██████  ██   ████


    ////////////// Definition ////////////////////////
    public static double MajorLoop_PER_CellCycle(){ return 1.0; }
    public static double MinorLoop_PER_MajorLoop(){ return 24.0; }
    public static double T_PER_Minor(){ return 1.0; }
    public static double Volume_PER_Cell() { return 1.0; }

    // 1T = 1 MinorLoop (1 hour)
    // 1L = 1 cellLen
    // 1M = 1 nmol

    //////////// Auxiliary function //////////////
    public static double CellCycle_PER_second() { return 1/RealWorldParams.cellCycleDuration; }

    public static double T_PER_Major(){return (Params.MinorLoop_PER_MajorLoop() * Params.T_PER_Minor());}

    //////////// Converstion functions ///////////
    public static double M_PER_nmol() {
        return 1.0;
    }
    public static double L_PER_um(){
        return 1 * 1/RealWorldParams.cellLength;
        // L/um = L/cellLen * cellLen/um. Note the that cellLen/um reads
        // cellLen PER um, which is 1/cellLen.
    }
    public static double T_PER_second(){
        return T_PER_Minor() * MinorLoop_PER_MajorLoop() * MajorLoop_PER_CellCycle() * CellCycle_PER_second();
    }
}

//
//public static int N_v_T_index = 0;
//public static int N_i_T_index = 1;
//public static int N_b_T_index = 2;
//public static int N_intern_T_index = 3;
//public static int N_v_R_index = 4;
//public static int N_i_R_index = 5;
//public static int N_b_R_index = 6;
//public static int N_intern_R_index = 7;


//    public static void Inject_DEBUG(Grid grid, int currentDay){
//        if (currentInjectionIndex < numberOfInjections){
//            if (currentDay == injectionTimes[currentInjectionIndex]){
//                System.out.println("Hey I am here Injected");
//                double[] currentStateVariables = grid.PKStateVariables.get(grid.PKStateVariables.size() - 1);
//                currentStateVariables[0] += injectionAmounts[currentInjectionIndex];
//                grid.PKStateVariables.set(grid.PKStateVariables.size() - 1,currentStateVariables);
//                currentInjectionIndex += 1;
//            }
//        }
//    }



//M