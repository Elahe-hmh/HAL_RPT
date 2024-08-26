package GrowthModel_SSDiff;

import HAL.Rand;

import java.io.IOException;


public class Main {


    public static void main(String[] args) throws IOException {
        Params.LoadClass(); // loads the params class
        ParamsPBPK.LoadClass(); // loads the PBPK params class
        System.out.println(Params.markovTimeStep);
        int dayCount = -1;
        Main main = new Main();  // Create an instance of Main
        DataLogger logger = new DataLogger();
    //        PlotWindow plotWin = new PlotWindow("Tumor Immune Example",250,250,4,0,0,1,1);
    //        GridWindow win = new GridWindow("my nice simulation", Params.xDim, Params.yDim, 2);
        Grid model = new Grid(Params.xDim, Params.yDim,new Rand(),main);
        DaVinci drawer = new DaVinci(model);

        model.Init(dayCount, drawer, logger);
        ParamsPBPK.initParams(model.currentVesselCounts);
        // visualization mask order: Healthy, Normal, Hypo, Necro, Apop, Vessel, oxygenField
        boolean[] visualizationMaskList = {true,true,true,true,true,true,true};
        String popsFileName = "Results/populations.csv";
        String doseFileName = "Results/doses.csv";
        String imageBaseName = "Results/images";
        String pkStateVaribaleFileName = "Results/state_varibale.csv";

        for (int i = 0; i<120 ; i++) {
            dayCount += 1;
            System.out.println("Day count: " + dayCount);

    //            ParamsPBPK.perDayUpdateParams(model);
            ParamsPBPK.Inject(model, dayCount);
            model.Step(dayCount);
    //            drawer.draw(win);

            drawer.gridDraw(visualizationMaskList);
            drawer.gridDrawAge(visualizationMaskList);




            double[] valueList = MyUtils.lastElementOfDoubleArrayList(model.DoseRateList);
            drawer.plot(dayCount, valueList[0]);
//            System.out.println(valueList[0]);
//            drawer.plot(dayCount,model.CurrentCellsPops[Params.APOP_TCELL_TYPE]);
//            drawer.gridWin.TickPause(1);
            if (i%3 == 0){
                logger.saveFigureTotal(imageBaseName+i+".png",drawer);
            }


        }

        logger.log(model.PopsOverTime, popsFileName);
        logger.log(model.DoseRateList, doseFileName);
        logger.log(model.PKStateVariables, pkStateVaribaleFileName);
        System.out.println("All data logged!-Close the simulation");
    }

}
