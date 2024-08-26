package Examples.Template_Models;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2D;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;

class TemplateOnLatticeCell extends AgentSQ2D<TemplateOnLattice2D> {
    int exampleProperty;

    void InitCell(){
        this.exampleProperty=Util.RGB(G.rng.Double(),G.rng.Double(),G.rng.Double());
    }

    void Step(){
        //this.exampleProperty=Util.RGB(G.rng.Double(),G.rng.Double(),G.rng.Double());
        this.MoveSQ(G.hood[G.rng.Int(MapHood(G.hood))]);
        //this.MoveSQ(G.rng.Int(G.length));
    }
}

class TemplateOnLattice2D extends AgentGrid2D<TemplateOnLatticeCell> {
    Rand rng=new Rand();
    //int[]hood=Util.MooreHood(false);
    int[] coords=  {1,1,-1,1,-1,-1,1,-1};
    int[]hood=Util.GenHood2D(coords);
    public TemplateOnLattice2D(int x, int y) {
        super(x, y, TemplateOnLatticeCell.class);
    }



    void ModelStep(){
//        int counter = 0; // This happens early in the morning at 6 am.
//        int totalCellCount = this.pops; // totalCellCount = 2400
//        int perHourUpdateBudget = totalCellCount/24; // = 2400/24 = 100
//        updateSteadyStateOxygen();
        for (TemplateOnLatticeCell cell : this) {
            cell.Step();
//            counter ++;
//            if (counter % perHourUpdateBudget == 0){
//                updateSteadyStateOxygen();
//                // You are here? then 1 hour is passed!!
//            }
            System.out.println("wait here!");

        }
    }

    void InitGrid(){
        NewAgentSQ(xDim/2,yDim/2);
        NewAgentSQ(xDim/2+1,yDim/2);
        NewAgentSQ(xDim/2+2,yDim/2);
        NewAgentSQ(xDim/2+3,yDim/2);
        NewAgentSQ(xDim/2+4,yDim/2);
        for(TemplateOnLatticeCell cell: this){
            cell.InitCell();
        }
    }
    public static void main(String[]args){
        TemplateOnLattice2D grid=new TemplateOnLattice2D(50,50);
        GridWindow vis=new GridWindow(50,50,10);

        //grid.NewAgentSQ(grid.xDim/2,grid.yDim/2);
        grid.InitGrid();

        while(true){
            grid.ModelStep();



            for (int i=0;i<grid.length;i++){
                TemplateOnLatticeCell cell=grid.GetAgent(i);
                if(cell!=null){
                    vis.SetPix(i,cell.exampleProperty);
                }else{
                    vis.SetPix(i, Util.BLACK);
                }
            }
            vis.TickPause(100);
        }
    }
}
