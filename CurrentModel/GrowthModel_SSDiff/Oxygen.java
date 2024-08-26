package GrowthModel_SSDiff;

import HAL.GridsAndAgents.Grid2Ddouble;

import static HAL.Util.CircleHood;

public class Oxygen {
//    public Grid2Ddouble oxygenGrid;
    public Grid grid;
    int r_max;
    
    public Oxygen(Grid grid) {
        this.grid = grid;
//        this.oxygenGrid = new Grid2Ddouble(Params.xDim, Params.yDim);
        this.r_max = Params.r_max;
    }
    
    void UpdateSteadyStateOxygen(int currentDay, double r_average){
        double k_eff; //effective consumption rate in the neigborhood of tumor.

        this.grid.oxygenGrid.SetAll(0);
        
        for (int vesselIndex : this.grid.vesselsIndex) { // this loops on all of the vessels
            int[] SShood = CircleHood(true, r_max); // the circle hood in which we calculate the steady state
            int SShoodCount = this.grid.MapHood(SShood,vesselIndex);
            k_eff = this.VesselHoodSSK_effCalc(SShood,SShoodCount);
//            k_eff = this.VesselHoodSSK_effCalc_DEBUG(SShood,SShoodCount);
            this.grid.oxygenGrid = HoodOxygenGridUpdate(k_eff, SShood, SShoodCount, vesselIndex, this.grid.oxygenGrid, currentDay,r_average);
        }
    }

    Grid2Ddouble HoodOxygenGridUpdate(double k_eff, int[] SShood, int SShoodCount, int vesselIndex,
                                      Grid2Ddouble currentOxygenGrid, int currentDay, double r_average){


        // the mechanism below toggles betweeen blocked and unblocked vessels.
//        if (this.grid.rng.Double()<0.0){
//            this.grid.GetAgent(vesselIndex).blockedVessel = !this.grid.GetAgent(vesselIndex).blockedVessel;
//        }

        // the machanism below is to implement the cyclic hypoxia
        double oxygenAmp;
        if (this.grid.GetAgent(vesselIndex).blockedVessel){
//            oxygenAmp = Params.VESSEL_OXYGEN_CONCENTRATION * Math.abs(Math.sin(0.1*Params.globalTime)) * 0;
            oxygenAmp = 0;
        }
        else{
            oxygenAmp = Params.VESSEL_OXYGEN_CONCENTRATION * this.grid.GetAgent(vesselIndex).vesselAmp;
        }


        //Pressure in tumor
        double distVesselFromCenter = this.grid.Dist(vesselIndex,this.grid.I(grid.xDim/2,grid.yDim/2));
        double blockageProbabilityFactor =0.005/2;

//        double distVesselRatio = distVesselFromCenter/ ((grid.xDim/2)*Math.sqrt(2));
//        double pressureProbabilityLinear = (1 - distVesselRatio) * blockageProbabilityFactor;
//        double pressureProbabilityQuadratic = (1 - Math.pow(distVesselRatio, 2)) * blockageProbabilityFactor;
//        double pressureProbabilityExponential = Math.exp(-distVesselRatio) * blockageProbabilityFactor;
//        double pressureProbabilityInverse= (1 / (1 + distVesselRatio)) * blockageProbabilityFactor;
//        double pressureProbabilityLogarithmic= (1 - Math.log(1 + distVesselRatio)) * blockageProbabilityFactor;
//        double pressureProbabilitySigmoid= (1 / (1 + Math.exp(distVesselRatio - 1))) * blockageProbabilityFactor;
//        double pressureProbabilityTanh=  (1 - Math.tanh(distVesselRatio))  * blockageProbabilityFactor;
//        double pressureProbabilityGaussian=  Math.exp(-Math.pow(distVesselRatio, 2) / (2 * 50 * 50))   * blockageProbabilityFactor;
//        if (this.grid.rng.Double() < pressureProbabilityLinear) {
////        if (distVesselFromCenter< grid.xDim/8 && this.grid.rng.Double()<0.0001){
//            this.grid.GetAgent(vesselIndex).blockedVessel = true;
////            this.grid.GetAgent(vesselIndex).vesselAmp *= 0.5;
//        }

//        double distVesselRatio_r_avg= distVesselFromCenter/r_average;
//        if ((1-distVesselRatio_r_avg)>=0) {
//            double pressureProbabilityLinear = (1 - distVesselRatio_r_avg) * blockageProbabilityFactor;
//
//            if (this.grid.rng.Double() < pressureProbabilityLinear) {
////        if (distVesselFromCenter< grid.xDim/8 && this.grid.rng.Double()<0.0001){
//                this.grid.GetAgent(vesselIndex).blockedVessel = true;
////            this.grid.GetAgent(vesselIndex).vesselAmp *= 0.5;
//            }
//        }




//         the following code is to remove some of the central vessels over time
//        double distVesselFromCenter = this.grid.Dist(vesselIndex,this.grid.I(grid.xDim/2,grid.yDim/2));
//        if (distVesselFromCenter<grid.xDim/3 && this.grid.rng.Double()<0.001){
////            this.grid.GetAgent(vesselIndex).blockedVessel = true;
//            this.grid.GetAgent(vesselIndex).vesselAmp *= 0.5;
//        }
        // the following code is to remove some of the outer vessels over time
//        distVesselFromCenter = this.grid.Dist(vesselIndex,this.grid.I(grid.xDim/2,grid.yDim/2));
//        if (distVesselFromCenter>grid.xDim/4 &&distVesselFromCenter<grid.xDim*2/4 && this.grid.rng.Double()<0.0001){
//            this.grid.GetAgent(vesselIndex).blockedVessel = true;
//        }


//        if(currentDay==0) {
//            int x = grid.ItoX(vesselIndex);
//            int y = grid.ItoY(vesselIndex);
//            if ((y < grid.yDim / 3 || y > grid.yDim*2  / 3) && (x < grid.yDim / 3 || x > grid.yDim*2  / 3) && grid.rng.Double() < 0.1) {
//                grid.GetAgent(vesselIndex).blockedVessel = true;
//            }
//        }
//            if (x > grid.xDim / 3 && y > grid.yDim * 2 / 3 && grid.rng.Double() < 0.9) {
//                grid.GetAgent(vesselIndex).vesselAmp *= 0.5;
//            }
//            if (x < grid.xDim / 3 && y > grid.yDim * 2 / 3 && grid.rng.Double() < 0.9) {
//                grid.GetAgent(vesselIndex).vesselAmp *= 0.5;
//            }
//        }



        for (int i=0; i<SShoodCount ; i++) { // this gets a statistics of the cells present in the hood
            double r = this.grid.Dist(vesselIndex,SShood[i]);
            double Oxval = oxygenAmp * Math.exp(-k_eff*r);
            currentOxygenGrid.Add(SShood[i],Oxval);
//            this.oxygenGrid.Add(SShood[i],Oxval);
        }
        return currentOxygenGrid;

    }

    double VesselHoodSSK_effCalc(int[] SShood, int SShoodCount){
        // This calculates the effective k for the given vessel hood. k_effective is used to determine the Oxygen steady state
        // To do this, we calculate the average consumption rate in the hood, (we call it alpha_eff) and from that we can
        // easily calculate te effective k which is k = sqrt(alpha/D).
        // C(x) = Ae^{-kx} + B
        double alpha_eff = 0;
        double k_eff = 0;
        int cellType_AUX = -1;

        double[] cellStats = new double[Params.NUMBER_OF_CELL_TYPES]; //this will contain the number of cells in the hood
        for (int i=0; i<SShoodCount ; i++) { // this gets a statistics of the cells present in the hood
            Cell currentCell = this.grid.GetAgent(SShood[i]);
            if (currentCell == null){
                cellType_AUX = Params.HEALTHY_CELL_TYPE;
            }else{
                cellType_AUX = currentCell.type;
            }
            cellStats[cellType_AUX] ++;
        }
        for (int i = 0; i < Params.NUMBER_OF_CELL_TYPES; i++) {
            alpha_eff += cellStats[i]/SShoodCount*Params.CELLS_CONSUMPTION_RATE_LIST[i];
        }
        k_eff = Math.sqrt(alpha_eff/Params.D_O2);
        return k_eff;
    }

    double VesselHoodSSK_effCalc_DEBUG(int[] SShood, int SShoodCount){
        // Note that this is a placeholder to make sure we are creating appropriate oxygen map
        // for debug purposes
        double R = 150;
        double k_eff = 3/R;
        return k_eff;
    }


}
