package LEARN_HERE.Agents;

import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.GridsAndAgents.AgentGrid2D;
import HAL.Rand;
import HAL.Util;

import static HAL.Util.*;

/**
 * Created by Rafael on 9/5/2017.
 */

class Cell extends AgentSQ2Dunstackable<BirthDeath> {
    int color;
    double death_prob;
    double distanceFromCenter;

    public void InitCell(int color){
        this.distanceFromCenter = this.Dist(G.xDim/2,G.yDim/2);
        this.death_prob = this.func(distanceFromCenter);
        this.color = color;
    }

    public void Step() {


        if (G.rn.Double() < this.death_prob) {
            Dispose();
            return;
        }
        if (G.rn.Double() < G.BIRTH_PROB) {
            //int nOptions = G.MapEmptyHood(G.mooreHood, Xsq(), Ysq());
            int nOptions = MapEmptyHood(G.mooreHood);
            if(nOptions>0) {
                G.NewAgentSQ(G.mooreHood[G.rn.Int(nOptions)]).color=color;
            }
        }
    }

    double func(double d){
        double c = 1.1;
        double n = 4;
        return (Math.pow(d/10,n))/(Math.pow(c,n)+Math.pow(d/10,n));
    }
}

public class BirthDeath extends AgentGrid2D<Cell> {
    int BLACK=RGB(0,0,0);
    double DEATH_PROB=0.1;
    double BIRTH_PROB=1;
    Rand rn=new Rand();
    int[]mooreHood=MooreHood(false);
    int color;
    public BirthDeath(int x, int y,int color) {
        super(x, y, Cell.class);
        this.color=color;
    }
    public void Setup(double rad){
        int[]coords= CircleHood(true,rad);
        int nCoords= MapHood(coords,xDim/2,yDim/2);
        for (int i = 0; i < nCoords ; i++) {
            NewAgentSQ(coords[i]).InitCell(color);

        }
    }
    public void Step() {
        for (Cell c : this) {
            c.Step();
        }
        CleanAgents();
        ShuffleAgents(rn);
    }
    public void Draw(GridWindow vis){
        for (int i = 0; i < vis.length; i++) {
            Cell c = GetAgent(i);
            vis.SetPix(i, c == null ? BLACK : c.color);
        }
    }


    public static void main(String[] args) {
        BirthDeath t=new BirthDeath(100,100, Util.RED);
        GridWindow win=new GridWindow(100,100,5);
        t.Setup(20);
        for (int i = 0; i < 100000; i++) {
            win.TickPause(50);
            t.Step();
            t.Draw(win);
        }
    }
}
