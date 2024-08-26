package GrowthModel_SSDiff;

import HAL.Tools.ODESolver.ODESolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RadioBio {

    public Grid grid;
    public ODESolver solver;

    public RadioBio(Grid grid){
        this.grid = grid;
        this.solver = new ODESolver();
    }


    public void RHS(double t, double[] currentValues, double[] derivatives) {

    }


    public void UpdateParams(){

    }

    public void Solve(double[] currentValues, double[] nextValues, double t_0, double t_f, double dtStart, double errorTol){
        this.UpdateParams();
        this.solver.Runge45(this::RHS, currentValues, nextValues, t_0, t_f,dtStart, errorTol);
    }




    public ArrayList<double[]> SurvivalProbLookupTableCalc(int currentDay, int currentHour){
        // Calculates survival probability
        // Input: DoseRateList.
        // Output: Survival probability lookup table

        ArrayList<double[]> survivalProbLookupTable = new ArrayList<>();
        int maxLookupAge = Params.maxLookupAge;

        if (currentDay < maxLookupAge){
            if (currentDay == 0){
                double[] SF = {1.0, 1.0};
                survivalProbLookupTable.add(SF);
                return survivalProbLookupTable;
            }
            else{
                maxLookupAge = currentDay;
            }
        }

            // calculate the lookup table elements
        for (int age = 1; age <= maxLookupAge ; age++) {
            int T_PER_MAJOR = (int)(Params.T_PER_Major());
            int sliceLength = age * T_PER_MAJOR ;
            int lastSliceIndex = Params.globalTime ;
            int firstSliceIndex = lastSliceIndex - sliceLength;

//             Ensure indices are within bounds
            firstSliceIndex = Math.max(0, firstSliceIndex);
            lastSliceIndex = Math.min(this.grid.DoseRateList.size(), lastSliceIndex);


            List<double[]> TempDoseRateList = this.grid.DoseRateList.subList(firstSliceIndex,lastSliceIndex-4);
            ArrayList<double[]> slicedDoseRate = new ArrayList<>(TempDoseRateList);
            double[] SF = CalculateSF(slicedDoseRate);


            survivalProbLookupTable.add(SF);
        }


        return survivalProbLookupTable;
    }


    public double[] CalculateSF(ArrayList<double[]> slicedDose){

        double dt = Params.T_PER_Minor() * 3600;  // makes sure that the units of dt is seconds
        // note for dt above: dt should be in seconds since arr[0] is in the units of Gray/second
        // and we want dose to have units of gray.
        double Dose = 0;
        for (double[] arr : slicedDose) {
            if (arr.length > 0) {
                Dose += arr[0] * dt;
            }
        }

        double G_Factor = 0;
        int t = 0;
        for (double[] arr : slicedDose) {
            double amplitude = 0;
            for (int tp = 0; tp < t; tp++) {
                amplitude += slicedDose.get(tp)[0] * Math.exp(-Params.repairRate*(t-tp)) * dt;
            }
            G_Factor += arr[0] * amplitude * dt;
            t += 1;
        }
        G_Factor *= 2/(Dose*Dose+0.0000001);
//        System.out.println("G_Factor is: "+ G_Factor);

        double[] SF_Values = {0.0, 0.0};

        // First one for the normal and the second one for the hypo
        for (int i = 0; i < 2; i++) {
            SF_Values[i] = Math.exp(-Params.alphaValues[i]*Dose - Params.betaValues[i]*Dose*Dose*G_Factor);
        }
//        System.out.println("current G is: ");
        return SF_Values;
    }





    public double[] calculateSurvivalProb(ArrayList<double[]> localDoseRate){
        // the first element is the survival probability of NormalTCell and the second element is the survival probability of
        // HypoTCell
        double[] perCellTypeSurvivalProb = {0.6,0.2}; // the first element is for normal and the second element hypoxia
        return perCellTypeSurvivalProb;
    }

}



//    public ArrayList<double[]> SurvivalProbLookupTableCalc_OLD(int currentDay, int localCurrentHour){
//        // Calculates survival probability
//        // Input: DoseRateList.
//        // Output: Survival probability lookup table
//
//        ArrayList<double[]> survivalProbLookupTable = new ArrayList<>();
//
//
//        if (localCurrentHour == 24){
//            System.out.println("here");
//        }
//        if (currentDay ==0){
//            // will have 1 survival probability.
//            survivalProbLookupTable.add(new double[]{1,1});
//            return survivalProbLookupTable;
//        }
//
//
//
//        int currentGlobalHour = (int) (currentDay * Params.MinorLoop_PER_MajorLoop() + localCurrentHour);
//
//        int maxLookupAge = Params.maxLookupAge;
//        if (currentDay < Params.maxLookupAge){
//            maxLookupAge = currentDay;
////            maxLookupAge = Math.floorDiv(currentGlobalHour, 24);
//        }
//
//        for (int lookupAge = 1; lookupAge < maxLookupAge; lookupAge++) {
//            System.out.println((int)(currentGlobalHour- maxLookupAge *Params.MinorLoop_PER_MajorLoop()) + "TO " + (currentGlobalHour-1) );
//            List<double[]> localDoseRateList = this.grid.DoseRateList.subList((int)(currentGlobalHour- lookupAge *Params.MinorLoop_PER_MajorLoop()),currentGlobalHour-1);
//            ArrayList<double[]> slicedDoseRate = new ArrayList<>(localDoseRateList);
//            double[] perCellTypeSurvivalProb = calculateSurvivalProb(slicedDoseRate);
//            survivalProbLookupTable.add(perCellTypeSurvivalProb);
//        }
//        return survivalProbLookupTable;
//        // this is the simplest approach!!
////        if (this.cell.type == Params.NORMAL_TCELL_TYPE){
////            this.cell.survivalProb = 0.6;
//////            this.cell.survivalProb = 0.5+(this.cell.birthTime)/(100 + this.cell.birthTime);
////        }
////        if (this.cell.type == Params.HYPO_TCELL_TYPE){
////            this.cell.survivalProb = 0.7 ;
//////            this.cell.survivalProb = 0.5+(this.cell.birthTime)/(100 + this.cell.birthTime);
////        }
//    }

