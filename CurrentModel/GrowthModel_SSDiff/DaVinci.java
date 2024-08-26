package GrowthModel_SSDiff;


import HAL.Gui.GridWindow;
import HAL.Gui.PlotLine;
import HAL.Gui.PlotWindow;

import java.util.ArrayList;

import static HAL.Util.*;


class DaVinci {
    private Grid grid;
    public GridWindow gridWin;
    public GridWindow gridWinAge;

    public PlotWindow plotWin;
//    public GridWindow gridWinOxygen;
//    public GridWindow gridWinVessel;

    public PlotLine testLine;


    public DaVinci(Grid grid){
        this.plotWin = new PlotWindow("Tumor Immune Example",250,250,4,0,0,1,0.000001);
        this.gridWin = new GridWindow("my nice simulation", Params.xDim, Params.yDim, 2);
        this.gridWinAge = new GridWindow("Age", Params.xDim, Params.yDim, 2);

//        this.gridWinOxygen = new GridWindow("Oxygen Only", Params.xDim, Params.yDim, 2);
//        this.gridWinVessel=new GridWindow("Vessels Only", Params.xDim, Params.yDim, 2);

        this.testLine=new PlotLine(this.plotWin,GREEN);

        this.grid = grid;
    }


    public void plot(double t, double value){
        testLine.AddSegment(t, value);
    }


    
    public void gridDraw(boolean[] maskList){
        this.gridWin.Clear(BLACK);
        for (int i = 0; i < this.gridWin.length; i++) {
            Cell c = grid.GetAgent(i);
            if (c==null){
                if (maskList[6] == true){
                    double oxygenConc = grid.oxygenGrid.Get(i);
//                    this.gridWin.SetPix(i, HeatMapRGB(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    this.gridWin.SetPix(i, HeatMapBGR(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
//                    this.gridWin.SetPix(i, HeatMapBRG(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                }
            }else{
                if (c.type == Params.VESSEL_TYPE && c.blockedVessel){
                    double oxygenConc = grid.oxygenGrid.Get(i);
//                    this.gridWin.SetPix(i, HeatMapRGB(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    this.gridWin.SetPix(i, HeatMapBGR(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
//                    this.gridWin.SetPix(i, HeatMapBRG(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    continue;
                }
                if (maskList[c.type] == true) {
                    this.gridWin.SetPix(i, c.color);
//                    this.gridWin.SetPix(i, HeatMapRGB((Params.globalTime - c.birthTime)/24,0,20));
                }
            }
        }

    }

    public void gridDrawAge(boolean[] maskList){
        this.gridWinAge.Clear(BLACK);
        for (int i = 0; i < this.gridWinAge.length; i++) {
            Cell c = grid.GetAgent(i);
            if (c==null){
                if (maskList[6] == true){
                    double oxygenConc = grid.oxygenGrid.Get(i);
//                    this.gridWin.SetPix(i, HeatMapRGB(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    this.gridWinAge.SetPix(i, HeatMapBGR(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
//                    this.gridWin.SetPix(i, HeatMapBRG(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                }
            }else{
                if (c.type == Params.VESSEL_TYPE && c.blockedVessel){
                    double oxygenConc = grid.oxygenGrid.Get(i);
//                    this.gridWin.SetPix(i, HeatMapRGB(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    this.gridWinAge.SetPix(i, HeatMapBGR(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
//                    this.gridWin.SetPix(i, HeatMapBRG(oxygenConc,0,3*Params.VESSEL_OXYGEN_CONCENTRATION));
                    continue;
                }
                if (maskList[c.type] == true) {
//                    this.gridWin.SetPix(i, c.color);
                    this.gridWinAge.SetPix(i, HeatMapRGB((Params.globalTime - c.birthTime)/24,0,20));
                }
            }
        }

    }

//    public void gridDrawOxygen() {
//        this.gridWinOxygen.Clear(BLACK);
//        for (int i = 0; i < this.gridWinOxygen.length; i++) {
//            double oxygenConc = grid.oxygenGrid.Get(i);
//            this.gridWinOxygen.SetPix(i, HeatMapBGR(oxygenConc, 0, 3 * Params.VESSEL_OXYGEN_CONCENTRATION));
//        }
//    }

//    public void gridDrawVessel() {
//        this.gridWinVessel.Clear(BLACK);
//        for (int i = 0; i < this.gridWinVessel.length; i++) {
//            Cell c = grid.GetAgent(i);
//            if(c!= null) {
//                if (c.type == Params.VESSEL_TYPE && c.blockedVessel == false) {
//                    this.gridWinVessel.SetPix(i, c.color);
//                }
//            }
//        }
//
//    }



//    public void drawField(GridWindow vis){
//        for (int i = 0; i < vis.length; i++) {
//            double c = grid.oxygenGrid.Get(i);
//            vis.SetPix(i, HeatMapRGB(c,0,10));
//        }
//    }

//    public void drawField(){
//        for (int i = 0; i < this.gridWin.length; i++) {
//            double c = grid.oxygenGrid.Get(i);
//            this.gridWin.SetPix(i, HeatMapRGB(c,0,10));
//        }
//    }



//    public void draw(GridWindow vis) {
//        for (int x = 0; x < this.grid.xDim; x++) {
//            for (int y = 0; y < this.grid.yDim; y++) {
//                Cell drawMe = this.grid.GetAgent(x, y);
////                if (drawMe != null) {
////                    vis.SetPix(x + iModel * this.grid.xDim, y, drawMe.type);
////                } else {
////                    vis.SetPix(x + iModel * this.grid.xDim, y, HeatMapRGB(this.grid.drug.Get(x, y)));
////                }
//            }
//        }
//    }
}