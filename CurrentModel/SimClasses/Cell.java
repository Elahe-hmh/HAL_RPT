package SimClasses;

import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import SimClasses.Grid;

import java.io.Serializable;

import static HAL.Util.ProbScale;

public class Cell extends AgentSQ2Dunstackable<Grid> implements Serializable {
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


        // If the oxygen concentration is lower than the threshold, then the cell dies with a probability death_prob
        // Also, if the ATP production rate is lower than the threshold, then the cell dies with a probability ATP_Production_TUMOR
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