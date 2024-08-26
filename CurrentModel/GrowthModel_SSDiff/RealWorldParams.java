package GrowthModel_SSDiff;

public class RealWorldParams {
    // This class will contain the real-world parameters of the model.
    // The followings are the units of the parameters
    // Time: second
    // Length: um
    // Amount: nmol
    // pressure: mmHg
    // mass: kg
    // macroLength: cm
    // macroVolume: lit


    public static double minVesselDistance = 100.0; // um
    public static String vesselDensityFunctionName = "bump";



//    ██████   ██████  ███    ███  █████  ██ ███    ██     ██████   █████  ██████   █████  ███    ███
//    ██   ██ ██    ██ ████  ████ ██   ██ ██ ████   ██     ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//    ██   ██ ██    ██ ██ ████ ██ ███████ ██ ██ ██  ██     ██████  ███████ ██████  ███████ ██ ████ ██
//    ██   ██ ██    ██ ██  ██  ██ ██   ██ ██ ██  ██ ██     ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//    ██████   ██████  ██      ██ ██   ██ ██ ██   ████     ██      ██   ██ ██   ██ ██   ██ ██      ██



    public static double domainSize = 4 * um_PER_mm(); // um
    // (domainSize) = 4 mm

    public static double vesselSpacing = 100.0; // um
    // (vesselSpacing) = 100 um

    public static double vesselDensity = Math.pow(1/(2*vesselSpacing),2); // count/um^2
    // To derive the formula above, we calculated the number of circles with radius (R) that
    // we can pack into a box (with side L) arranged on a grid compactly. The number is
    // N = (L/(2R))^2.



//      ██████ ███████ ██      ██          ██████   █████  ██████   █████  ███    ███
//     ██      ██      ██      ██          ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//     ██      █████   ██      ██          ██████  ███████ ██████  ███████ ██ ████ ██
//     ██      ██      ██      ██          ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//      ██████ ███████ ███████ ███████     ██      ██   ██ ██   ██ ██   ██ ██      ██



    public static double cellLength = 10.0; //um
    // (cellLength) = 10 um

    public static double initialTumorRad = 1000.0; // um
    // (initialTumorRad) = 200 um

    public static double cellCycleDuration = 1 * second_PET_day(); //second
    // (1) day


    public static double repairRate = 0.3; // per hour

    public static double[] alphaValues = {0.15, 0.1*0.107}; // First one for normal and the second one for the hypo
    public static double[] betaValues = {0.048, 0.1*0.024}; // First one for normal and the second one for the hypo

//    public static double[] alphaValues = {0.15, 0.0001* 0.107}; // First one for normal and the second one for the hypo
//    public static double[] betaValues = {0.048, 0.0001* 0.024}; // First one for normal and the second one for the hypo


//      ██████  ██████   ██████  ██     ██ ████████ ██   ██     ██████   █████  ██████   █████  ███    ███
//     ██       ██   ██ ██    ██ ██     ██    ██    ██   ██     ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//     ██   ███ ██████  ██    ██ ██  █  ██    ██    ███████     ██████  ███████ ██████  ███████ ██ ████ ██
//     ██    ██ ██   ██ ██    ██ ██ ███ ██    ██    ██   ██     ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//      ██████  ██   ██  ██████   ███ ███     ██    ██   ██     ██      ██   ██ ██   ██ ██   ██ ██      ██


    public static double D_O2 = 2E-5 * Math.pow(um_PER_cm(), 2); // um^2/s
    // (D_O2) = 2E-5 cm^2/s from [1]

    // TODO: Take care of the following parameters
    // healthy, normal, hypo, necro, apop, vessel
//    public static double[] CELLS_CONSUMPTION_RATE_LIST = {500,6000,2000,1,1,0};
    public static double cb = 1500.0; // background consumption
    public static double[] CELLS_CONSUMPTION_RATE_LIST = {cb,5*cb,2*cb,1,1,0};

    public static double P_O2_Vessel = 100.0; // mmHg
    public static double O2_HenryConstant = 1.3E-3 / mmHg_PER_atm() * nmol_PER_mol() / um3_PER_Lit(); // nmol/um^3/mmHg
    // (O2_HenryConstant) mol/L/atm
    public static double P_O2_Vein = 30.0; // mmHg
    public static double HypoxicThreshold = 10.0; // mmHg
    public static double NecroticThreshold = 0.5; // mmHg (was 2)


    public static double NecroRemovalTime = 10.0 * second_PET_day(); //
    // (10) days
    public static double ApopRemovealTime = 2.0 * second_PET_day();
    // (2) days




//
//    ██████  ██████  ██████  ██   ██     ██████   █████  ██████   █████  ███    ███
//    ██   ██ ██   ██ ██   ██ ██  ██      ██   ██ ██   ██ ██   ██ ██   ██ ████  ████
//    ██████  ██████  ██████  █████       ██████  ███████ ██████  ███████ ██ ████ ██
//    ██      ██   ██ ██      ██  ██      ██      ██   ██ ██   ██ ██   ██ ██  ██  ██
//    ██      ██████  ██      ██   ██     ██      ██   ██ ██   ██ ██   ██ ██      ██
//


    // we used the macroVolume unit for the parameters below as they are for PBPK



    public static double perCellReceptor = 5E5; //count
    // from [2]

    public static double interestitialFraction = 0.4; // no units
    // from Kletting



    public static double k_on = 0.046 / second_PER_minute(); // lit/nmol/s
    // (0.046) lit/nmol/min
    public static double K_D = 1; // nmol/lit
    // nmol/lit
    public static double k_off = k_on * K_D; // 1/s

    public static double k_int_T = 0.001 /second_PER_minute(); // 1/s
    // (0.001) 1/min
    public static double k_rel_T = 2E-4 / second_PER_minute(); // 1/s
    // (2E-4) 1/min

    public static double lambda_phys = 7E-5 / second_PER_minute() ;
    // (7E-5) 1/min
    public static double lambda_bio = 16E-5 / second_PER_minute() ;

    public static double Hemato = 0.4; // unit less
    // (0.4) [unit less]
    public static double BW = 70; // kg -- body weight
    // (70) kg
    public static double BH = 170; // cm -- body height
    // (170) cm
    public static double BSA = Math.sqrt(BH*BW/3600); // m^2 -- body surface area
    public static double V_p = 2.8*(1-Hemato)*BSA; // Lit
    public static double V_BP = 0.15 * V_p; // V_BP = V_art = 15% V_p

    public static double perVesselVolume =  Math.pow(cellLength,2) * domainSize; // um^3

    // TODO: Flow per vessel or flow per vessel per unit length of capillay? If the latter, then we need to include the zDim
    public static double Flow_PER_Vessel =  5.0  * Lit_PER_nLit()  ; // lit/s
    // (5) nLit/sec
    public static double PS_PER_Vessel = 6.0/5.0 * Flow_PER_Vessel; // lit/s
    // The conversion factor 6/5 is unit less and we derived using the PBPK paper of Kletting.
    // In that paper we have F_tu = f_tu * V_tu, and PS_tu = k_tu * V_tu, where f_tu = 0.5 and
    // k_tu = 0.6.

    public static double E_beta = 2.14E-14 ; // Joule



//    public static double patientWeight = 76 * Lit_PER_Kg() * um3_PER_Lit() ; // um^3
//    // (76) kg

//    public static double interestitialFraction_REST = 0.2; // non dim
//    public static double vascularFraction_REST = 0.2 ; // non dim
//    public static double PScoeff_REST = 0.2;// non dim
//    public static double R_rest_Density = 0.2 / um3_PER_Lit(); // nmol/um^3
//    // ( ** ) nmol/lit

    //    public static double k_int_Rest = 0.001 /second_PER_minute(); // 1/s
//    // (0.001) 1/min
//    public static double k_rel_Rest =  3E-4 / second_PER_minute(); //
//    // (3E-4) 1/min






//      ██████  ██████  ███    ██ ██    ██ ███████ ██████  ███████ ██  ██████  ███    ██
//     ██      ██    ██ ████   ██ ██    ██ ██      ██   ██ ██      ██ ██    ██ ████   ██
//     ██      ██    ██ ██ ██  ██ ██    ██ █████   ██████  ███████ ██ ██    ██ ██ ██  ██
//     ██      ██    ██ ██  ██ ██  ██  ██  ██      ██   ██      ██ ██ ██    ██ ██  ██ ██
//      ██████  ██████  ██   ████   ████   ███████ ██   ██ ███████ ██  ██████  ██   ████



    public static double mmHg_PER_atm() { return 760; }

    public static double um3_PER_ml() {
        return 1E12;
    }

    public static double ml_PER_L(){ return 1000; }

    public static double second_PER_minute() {
        return 60.0;
    }

    public static double um3_PER_Lit(){ return 1E15;}

    public static double um_PER_cm(){
        return 10d* 1000d;
    }

    public static double um_PER_mm(){
        return 1000d;
    }

    public static double nmol_PER_mol(){ return 1E9; }

    public static double Lit_PER_nLit(){ return 1E-9; }

    public static double Lit_PER_Kg(){ return 1; }

    public static double second_PET_day(){ return 86400.0;}

    public static double hour_PER_second(){
        return 1.0/3600;
    }



}

// References:
// [1] Birindelli2021-el
// [2] https://onlinelibrary.wiley.com/doi/10.1007/s00268-005-0544-5
