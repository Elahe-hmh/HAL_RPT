package GrowthModel_SSDiff;

import javax.management.relation.RelationService;

public class ParamsPBPK {



//    ██ ███    ██      ██ ███████  ██████ ████████ ██  ██████  ███    ██
//    ██ ████   ██      ██ ██      ██         ██    ██ ██    ██ ████   ██
//    ██ ██ ██  ██      ██ █████   ██         ██    ██ ██    ██ ██ ██  ██
//    ██ ██  ██ ██ ██   ██ ██      ██         ██    ██ ██    ██ ██  ██ ██
//    ██ ██   ████  █████  ███████  ██████    ██    ██  ██████  ██   ████

/////////////                NOOOO THERAPY
//    public static double[] injectionAmounts = {}; // nmol
//    public static double[] hotFraction = {};
//    public static double[] injectionTimes = {}; // Days of the injection




    // Note that we can not inject at the first day
/////////////                THERAPY 1
    public static double[] injectionAmounts = {100,100,100,100}; // nmol
    public static double[] hotFraction = {0.1,0.1,0.1,0.1};
    public static double[] injectionTimes = {5,35,65,95}; // Days of the injection


    /////////////                THERAPY 2
//    public static double[] injectionAmounts = {50,50,50,50  ,50,50,50,50,}; // nmol
//    public static double[] hotFraction = {0.1,0.1,0.1,0.1,   0.1,0.1,0.1,0.1};
//    public static double[] injectionTimes = {5,12,19,26,33,40,47,54}; // Days of the injection


    /////////////                THERAPY 3
//    public static double[] injectionAmounts = {100,100,100,100}; // nmol
//    public static double[] hotFraction = {0.1,0.1,0.1,0.1};
//    public static double[] injectionTimes = {5,12,14,16}; // Days of the injection


    /////////////                THERAPY 4
//    public static double[] injectionAmounts = {70,70,70,70,  100,20}; // nmol
//    public static double[] hotFraction = {0.1,0.1,0.1,0.1,  0.1,0.1};
////    public static double[] injectionTimes = {5,12,14,17,   20,23}; // Days of the injection
//
//    public static double[] injectionTimes = {35,42,44,47,   50,53}; // Days of the injection


    /////////////                THERAPY 5
//    public static double[] injectionAmounts = {70,70,70,70,  100,100, 100}; // nmol
//    public static double[] hotFraction = {0.1,0.1,0.1,0.1,  0.1,0.1, 0.1};
//    public static double[] injectionTimes = {5,12,14,17,   20,24, 26}; // Days of the injection













    public static int numberOfInjections = injectionTimes.length;
    public static int currentInjectionIndex = 0;



//    ██   ██ ██ ███    ██ ███████ ████████ ██  ██████
//    ██  ██  ██ ████   ██ ██         ██    ██ ██
//    █████   ██ ██ ██  ██ █████      ██    ██ ██
//    ██  ██  ██ ██  ██ ██ ██         ██    ██ ██
//    ██   ██ ██ ██   ████ ███████    ██    ██  ██████


    public static double lambda_phys = RealWorldParams.lambda_phys / T_PER_second();
    public static double lambda_bio = RealWorldParams.lambda_bio / T_PER_second();

    public static double k_off = RealWorldParams.k_off / T_PER_second();
    public static double k_on = RealWorldParams.k_on / T_PER_second();
    public static double k_int_T = RealWorldParams.k_int_T / T_PER_second();
    public static double k_rel_T = RealWorldParams.k_rel_T / T_PER_second();

    public static double xDim = RealWorldParams.domainSize * L_PER_um();
    public static double V_BP = RealWorldParams.V_BP;
    public static double V_Domain = (xDim)*(xDim)*(xDim); // dm^3 = 1 lit
    public static double V_int_Domain = RealWorldParams.interestitialFraction * V_Domain; // lit
    public static double perVesselVolume = RealWorldParams.perVesselVolume * V_PER_um3(); // lit
    public static double perCellReceptor = RealWorldParams.perCellReceptor;
    public static double perVesselFlow = RealWorldParams.Flow_PER_Vessel / T_PER_second();
    public static double perVesselPS = RealWorldParams.PS_PER_Vessel / T_PER_second();
    public static double E_beta = RealWorldParams.E_beta; //Joule
    public static double M_cell = 1 * Math.pow(RealWorldParams.cellLength * L_PER_um(),3); // kg


    //// These parameters will be initialized by the init param function ////
    public static double R_T = -1; // calculated by perHourUpdateParams
    public static double V_v_Domain = -1; // calculated by initParams
    public static double F_T = -1;
    public static double PS = -1;
    public static double totalPopDomain = -1;



//    ██ ███    ██ ██████  ███████ ██   ██
//    ██ ████   ██ ██   ██ ██       ██ ██
//    ██ ██ ██  ██ ██   ██ █████     ███
//    ██ ██  ██ ██ ██   ██ ██       ██ ██
//    ██ ██   ████ ██████  ███████ ██   ██


    public static int C_BP_index = 0;
    public static int C_v_index = 1;
    public static int C_int_index = 2;
    public static int C_b_index = 3;
    public static int C_intern_index = 4;
    public static int fraction_index = 5;

//
//    ███████ ██    ██ ███    ██  ██████ ████████ ██  ██████  ███    ██ ███████
//    ██      ██    ██ ████   ██ ██         ██    ██ ██    ██ ████   ██ ██
//    █████   ██    ██ ██ ██  ██ ██         ██    ██ ██    ██ ██ ██  ██ ███████
//    ██      ██    ██ ██  ██ ██ ██         ██    ██ ██    ██ ██  ██ ██      ██
//    ██       ██████  ██   ████  ██████    ██    ██  ██████  ██   ████ ███████
//


    public static void initParams(int numberOfVessels){
        F_T = perVesselFlow * numberOfVessels;
        PS = perVesselPS * numberOfVessels;
        V_v_Domain = numberOfVessels * perVesselVolume;
        totalPopDomain = Params.xDim * Params.yDim * Params.zDim; // Params.xDim unit is 1 cell len
    }


    public static void Inject(Grid grid, int currentDay){
        if (currentInjectionIndex < numberOfInjections) {
            if (currentDay == injectionTimes[currentInjectionIndex]) {
                System.out.println("Hey I am here Injected");
                double[] currentStateVariables = grid.PKStateVariables.get(grid.PKStateVariables.size() - 1);
                double f = currentStateVariables[fraction_index];
                currentStateVariables[fraction_index] = (f*currentStateVariables[C_BP_index] + hotFraction[currentInjectionIndex]*injectionAmounts[currentInjectionIndex])/(currentStateVariables[C_BP_index]+injectionAmounts[currentInjectionIndex]);
                currentStateVariables[C_BP_index] += injectionAmounts[currentInjectionIndex]/V_BP;
                currentStateVariables[C_v_index] = calculate_Cv(currentStateVariables[C_BP_index],currentStateVariables[C_b_index]);
                currentStateVariables[C_int_index] = calculate_Cint(currentStateVariables[C_BP_index],currentStateVariables[C_b_index]);
                grid.PKStateVariables.set(grid.PKStateVariables.size() - 1, currentStateVariables);
                currentInjectionIndex += 1;
            }
        }
    }

    public static double calculate_Cv(double C_BP, double C_b) {
        double numerator = C_BP * C_b * F_T * k_on - C_BP * F_T * PS - C_BP * F_T * R_T/V_int_Domain * k_on - C_b * PS * k_off;
        double denominator = C_b * F_T * k_on + C_b * PS * k_on - F_T * PS - F_T * R_T/V_int_Domain * k_on - PS * R_T/V_int_Domain * k_on;
        return numerator / denominator;
    }

    public static double calculate_Cint(double C_BP, double C_b) {
        double numerator = -C_BP * F_T * PS - C_b * F_T * k_off - C_b * PS * k_off;
        double denominator = C_b * F_T * k_on + C_b * PS * k_on - F_T * PS - F_T * R_T/V_int_Domain * k_on - PS * R_T/V_int_Domain * k_on;
        return numerator / denominator;
    }



    public static double perHourUpdateParams(Grid grid){
        // updates parameters based in the state of the simulation
        // We are assuming that we are simulating a cube tumor where its height is
        // determined by the average radius of the tumor
        // To calculate the average radius, we simply ask what is the radius of the circle that can
        // contain the same number of cells that we currently have.
        int totalCellCount = grid.Pop() - grid.currentVesselCounts;
        double r_average = Math.sqrt(totalCellCount*Math.pow(Params.Volume_PER_Cell(),2/3)/Math.PI);
        double h_extrusionAverage = 2.0 * r_average;
        double popExtrudedCube = totalCellCount * h_extrusionAverage;
        R_T = popExtrudedCube * perCellReceptor / Avogadro_PER_nmol(); // nmol

        return r_average;
    }




//      ██████  ██████  ███    ██ ██    ██ ███████ ██████  ███████ ██  ██████  ███    ██
//     ██      ██    ██ ████   ██ ██    ██ ██      ██   ██ ██      ██ ██    ██ ████   ██
//     ██      ██    ██ ██ ██  ██ ██    ██ █████   ██████  ███████ ██ ██    ██ ██ ██  ██
//     ██      ██    ██ ██  ██ ██  ██  ██  ██      ██   ██      ██ ██ ██    ██ ██  ██ ██
//      ██████  ██████  ██   ████   ████   ███████ ██   ██ ███████ ██  ██████  ██   ████


    ////////////// Definition ////////////////////////
    public static double MajorLoop_PER_CellCycle(){ return 1.0; }
    public static double MinorLoop_PER_MajorLoop(){ return 24.0; }
    public static double T_PER_Minor(){ return 1.0; }
    public static double L_PER_dm(){ return 1.0; } // 1 L is one deci meter (10 cm)
    public static double Amount_PER_nmol(){ return 1.0; }

    // 1T = 1 MinorLoop (1 hour)
    // 1M = 1 nmol // Amount
    // 1L = 10 cm
    // 1E = 1 J (unit of energy)

    // derived: 1V = 1 lit (volume)
    // derived: 1C = 1 nmol/lit (concentration)
    // derived: 1A = 1M/1T (activity)
    // derived: 1mass = 1 kg (mass)

    //////////// Auxiliary function //////////////
    public static double CellCycle_PER_second() { return 1/RealWorldParams.cellCycleDuration; }

    //////////// Converstion functions ///////////

    public static double T_PER_second(){
        return T_PER_Minor() * MinorLoop_PER_MajorLoop() * MajorLoop_PER_CellCycle() * CellCycle_PER_second();
    }

    public static double L_PER_um(){
        return L_PER_dm() * 1E-5;
    }

    public static double V_PER_um3(){
        return Math.pow(L_PER_um(), 3);
    }

    public static double Avogadro_PER_nmol(){
        return 6E14;
    }

    public static double nmol_PER_oneParticle(){
        return 1E9/(6E23);
    }

    public static double Activity_PER_Bq(){
        return nmol_PER_oneParticle()/T_PER_second();
    }


    public static void LoadClass(){

    }



//    public static double L_PER_um(){
//        return 1 * 1/RealWorldParams.cellLength;
//        // L/um = L/cellLen * cellLen/um. Note the that cellLen/um reads
//        // cellLen PER um, which is 1/cellLen.
//    }
}
