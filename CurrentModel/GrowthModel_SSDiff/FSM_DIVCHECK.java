package GrowthModel_SSDiff;

import java.util.ArrayList;

public class FSM_DIVCHECK {

    // FSM_DIVCHECK will get the following parameters throught its methods
    // @params: N_pl, oxygen, type, survivalProb
    // and will return
    // return: divideFlag, IsAlive, disposeFlag, type

    public Cell cell;

    public FSM_DIVCHECK(Cell cell){
        // constructor of the class
        this.cell = cell;
    }


    public int FSM_Run(ArrayList<double[]> lookupTable){
        // trouble reading the name? This is finite state machine run.
        // Logically, we first need to determine the new cell type (NormalTCell, HypoTCELL, NEC) based on the
        // oxygen parameters. Then, based on the updated type, (which is in fact the type of the cell that the cell was
        // in for the entire duration of the last day)
        if (this.cell.isAlive == true && this.cell.type != Params.VESSEL_TYPE){
            double NecroThreshold = Params.NECROTIC_THRESHOLD * this.cell.G.rng.Gaussian(1,0.2);
            if (this.cell.oxygen < NecroThreshold ){
                this.cell.ChangeType(Params.NECRO_TCELL_TYPE);
            }else {
//                double hypoThreshold = Params.HYPOXIC_THRESHOLD * Math.abs(this.cell.G.rng.Gaussian(1,0.1));
                double hypoThreshold = Params.HYPOXIC_THRESHOLD * this.cell.G.rng.Gaussian(1,0.2);
//                double hypoThreshold = Params.HYPOXIC_THRESHOLD  + this.cell.G.rng.Gaussian(0,0.1* Params.HYPOXIC_THRESHOLD);
                if (this.cell.oxygen < hypoThreshold) {
                    this.cell.ChangeType(Params.HYPO_TCELL_TYPE);
                } else {
                    this.cell.ChangeType(Params.NORMAL_TCELL_TYPE);
                }
                this.cell.GetSurvivalProb(lookupTable);
                if (this.cell.G.rng.Double()<(1-cell.survivalProb)){
                    this.cell.ChangeType(Params.APOP_TCELL_TYPE);
                }else{
                    this.DivideCheck();
                    return 0;
                }
            }
        }
        this.DisposeCheck();
        return 0;
    }


    public void DisposeCheck(){
        // if some conditions are satisfied, then sets the dispose flag to be true
        if (this.cell.type == Params.APOP_TCELL_TYPE){
            if(this.cell.G.rng.Double()<Params.APOP_REMOVAL_PROB){
                this.cell.disposeFlag = true;
            }else{
                if (this.cell.howManyDaysDead >= Params.strictRemovalCoeff * Params.ApopRemovalTime){
                    this.cell.disposeFlag = true;
                }else{
                    this.cell.disposeFlag = false;
                }
            }
        }
//        if (this.cell.type == Params.NECRO_TCELL_TYPE){
//            if(this.cell.G.rng.Double()<Params.NECROTIC_REMOVAL_PROB){
//                this.cell.disposeFlag = true;
//            }
//            else{
//                if (this.cell.howManyDaysDead >= Params.strictRemovalCoeff * Params.NecroRemovalTime){
//                    this.cell.disposeFlag = true;
//                }else{
//                    this.cell.disposeFlag = false;
//                }
//            }
//        }



    }
    public void DivideCheck(){
        // if some conditions are satisfied, then sets the divide flat to true
        // this.cell.divideFlag = true.
        // TODO: This needs to be implemented. The "cell divides" block in the
        //  flow chart should be implemented


        if (this.cell.type != Params.VESSEL_TYPE) {
            if (this.cell.G.rng.Double()<this.cell.divisionProb){
                this.cell.divisionFlag = true;
            }
            else {
                this.cell.divisionFlag = false;
            }
        }


    }

//    public void DivideRun(){
//        // this is a temporary function to test the output of the code
//        if (this.cell.type == Params.NORMAL_TCELL_TYPE || this.cell.type == Params.HYPO_TCELL_TYPE){
//            int emptySites = this.cell.MapEmptyHood(Params.divHood);
//            if (emptySites > 0){
//                int selectedSiteIndex = this.cell.G.rng.Int(emptySites);
//                int newAgentSiteIndex = Params.divHood[selectedSiteIndex];
//                this.cell.G.NewAgentSQ(newAgentSiteIndex).Init(this.cell.type, );
//            }
//            if (this.cell.G.rng.Double()<0.25){
//                this.cell.Init(Params.NECRO_TCELL_TYPE);
//            }
//        }
//    }
}
