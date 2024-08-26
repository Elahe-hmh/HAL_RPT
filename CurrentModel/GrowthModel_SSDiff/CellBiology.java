package GrowthModel_SSDiff;

public class CellBiology {

    public Cell cell;


    public CellBiology(Cell cell){
        this.cell = cell;
    }


    public void DivProbCalc(){
        // this function calculates the division probabaility of the cell
        // based on several factors like the cell type, oxygen level, etc
        this.cell.divisionProb = 1;

        if (this.cell.type == Params.HYPO_TCELL_TYPE){
            this.cell.divisionProb = Params.DIVISON_PROB_MAX * this.cell.oxygen/Params.VESSEL_OXYGEN_CONCENTRATION;
        }
        if (this.cell.type == Params.NORMAL_TCELL_TYPE){
            // TODO: Magic number here for good visualization
            this.cell.divisionProb = 10*Params.DIVISON_PROB_MAX * this.cell.oxygen/Params.VESSEL_OXYGEN_CONCENTRATION;
        }
    }



}
