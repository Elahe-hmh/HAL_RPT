package LEARN_HERE.Diffusibles;

import HAL.GridsAndAgents.Grid2Ddouble;
import HAL.GridsAndAgents.PDEGrid2D;
import HAL.Gui.UIGrid;
import HAL.Gui.UILabel;
import HAL.Gui.UIWindow;
import HAL.Gui.TickTimer;
import HAL.Rand;
import HAL.Tools.ODESolver.ODESolver;
import HAL.Util;

/**
 * Created by rafael on 7/18/17.
 */

 class ReacionODE {

    public final double omega;

    public final static int u_INDEX = 0, v_INDEX = 1;

    public ReacionODE(double omega) {
        this.omega = omega;
    }

    public void RHS(double t, double[] currentValues, double[] derivatives) {
        double u = currentValues[u_INDEX];
        double v = currentValues[v_INDEX];
        derivatives[u_INDEX] = this.omega * v;
        derivatives[v_INDEX] = -this.omega * u;
    }
}

    public class ReactionDiffusion2D {
        static final int time = 300000;
        static final int visScale = 5;
        static final int tickRate = 0;

        public static void main(String[] args) {


            Rand rng = new Rand();
            int xDim = 100;
            int yDim = 100;
            double omega = 1;

            int[] hood = Util.CircleHood(false, 30);
            TickTimer trt = new TickTimer();
            UIWindow win = new UIWindow("2D Diffusion VesselOcclusion", true);
            UIGrid v1 = new UIGrid(xDim, yDim, visScale);
            UIGrid v2 = new UIGrid(xDim, yDim, visScale);
            ReacionODE reactionODE = new ReacionODE(omega);
            ODESolver solver = new ODESolver();


            // Calculation for Diffusion time steps and space step to get the normalized DiffCoef
            double x_end = 1;
            double dx = x_end / xDim;
            double dt = 0.001;
//        double D_u = 0.01;
//        double D_v = 0.005;
            double D = 0.000005;
            double D_u = D;
            double D_v = D;

            double r_u = D_u * dt / Math.pow(dx, 2);
            double r_v = D_v * dt / Math.pow(dx, 2);
            double f = 0.04;
            double k = 0.06;

            win.AddCol(0, new UILabel("u"));
            win.AddCol(0, v1);
            win.AddCol(1, new UILabel("v"));
            win.AddCol(1, v2);
            win.RunGui();

            //set up grids
            PDEGrid2D u = new PDEGrid2D(xDim, yDim);
            PDEGrid2D v = new PDEGrid2D(xDim, yDim);

            PDEGrid2D u_abs = new PDEGrid2D(xDim, yDim);
            PDEGrid2D v_abs = new PDEGrid2D(xDim, yDim);

//        for (int i = 0; i < u.length; i++) {
//            if (rng.Double()<0.1){
//                u.Set(i,rng.Double());
//            }
//        }
//
//        for (int i = 0; i < u.length; i++) {
//            if (rng.Double()<0.1){
//                v.Set(i,rng.Double());
//            }
//        }

            int number = u.MapHood(hood, xDim / 2, yDim / 2);

            for (int i = 0; i < number; i++) {
                u.Set(hood[i], rng.Double());
                v.Set(hood[i], rng.Double());
            }
            u.Update();
            v.Update();
            //run loop
            for (int i = 0; i < time; i++) {
//            win.TickPause(1);
//            trt.TickPause(tickRate);
                //reaction
                // We can effectively bring some forms of the boundary condition to the RHS of the PDE system.

//
                for (int j = 0; j < u.length; j++) {
                    double u_val = u.Get(j);
                    double v_val = v.Get(j);
                    double[] currValues = new double[]{u_val, v_val};
                    double[] nextValues = new double[]{0, 0};
//                u.Add(j,(-u_val*Math.pow(v_val,2)+f*(1-u_val))*dt);
//                v.Add(j,(u_val*Math.pow(v_val,2)-(f+k)*v_val)*dt);
                    solver.Runge4(reactionODE::RHS, currValues, nextValues, 0, dt);
                    u.Set(j, nextValues[reactionODE.u_INDEX]);
                    v.Set(j, nextValues[reactionODE.v_INDEX]);
//                u.Add(j, v_val*dt);
//                v.Add(j,-u_val*dt);
//                double[] uv_update = solver.Euler();
                }

                u.DiffusionADI(r_u);
                v.DiffusionADI(r_v);

                u.Update();
                v.Update();

//            System.out.println(Math.pow(v.Get(0),2) + Math.pow(u.Get(0),2));

                for (int j = 0; j < u.length; j++) {
                    u_abs.Set(j, Math.abs(u.Get(j)));
                    v_abs.Set(j, Math.abs(v.Get(j)));
                }

                u_abs.Update();
                v_abs.Update();

                //advection
//            g1.Advection(0.01,0.01,0);
//            g1.Update();
//            //diffusion
//            g2.DiffusionADI(0.01);
//            g2.Update();
                //draw results
                v1.DrawPDEGrid(u_abs, Util::HeatMapRGB);
                v2.DrawPDEGrid(v_abs, Util::HeatMapRBG);
            }
            win.Close();
        }
    }
