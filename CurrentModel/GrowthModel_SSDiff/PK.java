package GrowthModel_SSDiff;
import HAL.Tools.ODESolver.ODESolver;

public class PK {

    public Grid grid;
    public ODESolver solver;


    public PK(Grid gird){
        this.grid = gird;
        this.solver = new ODESolver();
    }


    public void RHS_MINIPBPK(double t, double[] currentValues, double[] derivatives){
        double C_BP = currentValues[0];
        double C_b = currentValues[1];
        double C_intern = currentValues[2];
        double fraction = currentValues[3];

        double C_int = this.grid.PKStateVariables.get(this.grid.PKStateVariables.size()-1)[ParamsPBPK.C_int_index];
        double R_tilde = ParamsPBPK.R_T/ParamsPBPK.V_int_Domain;
        // TODO: We need to match the time scale of ODE system with the time scale of the Major and Minor
        double timeScale = 1/ParamsPBPK.k_int_T;
        derivatives[0] = timeScale*(-ParamsPBPK.lambda_bio * C_BP);
//        derivatives[1] = -timeScale*(ParamsPBPK.k_int_T * C_b);
        derivatives[1] = timeScale*(C_int*ParamsPBPK.k_on*(R_tilde - C_b) - ParamsPBPK.k_off*C_b - ParamsPBPK.k_int_T * C_b);
        derivatives[2] = timeScale*(ParamsPBPK.k_int_T*C_b - ParamsPBPK.k_rel_T*C_intern);
        derivatives[3] = timeScale*(-ParamsPBPK.lambda_phys * fraction);
    }


    public int DoseRateCalc_DEBUG(double t_0, double t_f, int currentDay, int currentHour){
        // according to any kinetic model that we have in our model, this function
        // calculates the current dose rate and pushes it in the corresponding
        // array list hosted by the grid.

        if (currentDay == 0 && currentHour == 0){
            // Initializes with zero in the first call.
            this.grid.DoseRateList.add(new double[]{0.0});
            return 0;
        }

        double[] nextValues = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}; // C_BP, C_v, C_int, C_b, C_intern, fraction
        double[] currentValues = this.grid.PKStateVariables.get(this.grid.PKStateVariables.size()-1);

        double[] ODE_nextValues = {0.0, 0.0, 0.0, 0.0}; // C_BP, C_b, C_intern, fraction
        double[] ODE_currentValues = {currentValues[ParamsPBPK.C_BP_index], currentValues[ParamsPBPK.C_b_index], currentValues[ParamsPBPK.C_intern_index], currentValues[ParamsPBPK.fraction_index]};
//        double t_f_ODE = t_f * 60 / (1/ParamsPBPK.k_int_T);
//        double t_f_ODE = 1;u

        // The coeff 1/24 here in the following line is to effectively force the PBPK run once per day (or quite
        // equivelently run 1/24 its time per hour).
        double t_f_ODE = t_f/24;
        double result = this.solver.Runge45(this::RHS_MINIPBPK, ODE_currentValues, ODE_nextValues, t_0, t_f_ODE, Params.dtStart*0.00001, Params.errorTolerance);

        nextValues[ParamsPBPK.C_BP_index] = ODE_nextValues[0];
        nextValues[ParamsPBPK.C_v_index] = ParamsPBPK.calculate_Cv(ODE_nextValues[0],ODE_nextValues[1]);
        nextValues[ParamsPBPK.C_int_index] = ParamsPBPK.calculate_Cint(ODE_nextValues[0],ODE_nextValues[1]);
        nextValues[ParamsPBPK.C_b_index] = ODE_nextValues[1];
        nextValues[ParamsPBPK.C_intern_index] = ODE_nextValues[2];
        nextValues[ParamsPBPK.fraction_index] = ODE_nextValues[3];




        this.grid.PKStateVariables.add(nextValues);
        double N_BP = nextValues[ParamsPBPK.C_BP_index] * ParamsPBPK.V_BP;
//        System.out.println("ParamsPBPK.V_BP iss:  " +ParamsPBPK.V_BP);



        double N_v = nextValues[ParamsPBPK.C_v_index] * ParamsPBPK.V_v_Domain;
        double N_int = nextValues[ParamsPBPK.C_int_index] * ParamsPBPK.V_int_Domain;
        double N_b = nextValues[ParamsPBPK.C_b_index] * ParamsPBPK.V_int_Domain;
        double N_intern = nextValues[ParamsPBPK.C_intern_index] * ParamsPBPK.V_int_Domain;
        double f = nextValues[ParamsPBPK.fraction_index];

//        System.out.println(N_BP+N_v+N_int+N_b+N_intern);

        // TODO: (3) convert Activity to dose rate for each cell type (S_factor)
//        System.out.println(N_int);
        double A_tumor = (N_v + N_int + N_b + N_intern) * f * ParamsPBPK.lambda_phys ; // unit: M/T
        double[] CurrentDoseRate = {A_tumor/ParamsPBPK.Activity_PER_Bq()*ParamsPBPK.E_beta / (ParamsPBPK.totalPopDomain * ParamsPBPK.M_cell)}; // Gray/Second
        this.grid.DoseRateList.add(CurrentDoseRate);
        return 0;
    }



//    public void RHS_DEBUG(double t, double[] currentValues, double[] derivatives){
//        double N = currentValues[0];
//        derivatives[0] = -ParamsPBPK.lambda_phys * N;
//    }


//    public void RHS(double t, double[] currentValues, double[] derivatives){
//
//
//
//        double N_v_T = currentValues[Params.N_v_T_index];
//        double N_i_T = currentValues[Params.N_i_T_index];
//        double N_b_T = currentValues[Params.N_b_T_index];
//        double N_intern_T = currentValues[Params.N_intern_T_index];
//        double N_v_R = currentValues[Params.N_v_R_index];
//        double N_i_R = currentValues[Params.N_i_R_index];
//        double N_b_R = currentValues[Params.N_b_R_index];
//        double N_intern_R = currentValues[Params.N_intern_R_index];
//
//
//        double F_Domain = Params.F_Domain;
//        double PS_Domain = Params.PS_Domain;
//        double PS_R = Params.PS_R;
//        double k_on = Params.k_on;
//        double k_off = Params.k_off;
//        double k_int_T = Params.k_int_T;
//        double k_int_R = Params.k_int_Rest;
//        double k_rel_T = Params.k_rel_T;
//        double k_rel_R = Params.k_rel_Rest;
//        double V_v_Domain = Params.V_v_Domain;
//        double V_v_R = Params.V_v_R;
//        double V_i_Domain = Params.V_i_Domain;
//        double V_i_R = Params.V_i_R;
//        double R_T = Params.R_T;
//        double R_rest = Params.R_rest;
//
//
//        derivatives[Params.N_v_T_index] = F_Domain*(N_v_R/V_v_R - N_v_T/V_v_Domain) + PS_Domain*(N_i_T/V_i_Domain - N_v_T/V_v_Domain);
//        derivatives[Params.N_i_T_index] = - PS_Domain*(N_i_T/V_i_Domain - N_v_T/V_v_Domain) - k_on*N_i_T*(R_T - N_b_T) + k_off*N_b_T;
//        derivatives[Params.N_b_T_index] =  k_on*N_i_T*(R_T - N_b_T) - k_off*N_b_T - k_int_T*N_b_T;
//        derivatives[Params.N_intern_T_index] = k_int_T*N_b_T - k_rel_T*N_intern_T;
//        derivatives[Params.N_v_R_index] = F_Domain*(N_v_T/V_v_Domain - N_v_R/V_v_R) + PS_R*(N_i_R/V_i_R - N_v_R/V_v_R);
//        derivatives[Params.N_i_R_index] = - PS_R*(N_i_R/V_i_R - N_v_R/V_v_R) - k_on*N_i_R*(R_rest - N_b_R) + k_off*N_b_R;;
//        derivatives[Params.N_b_R_index] = k_on*N_i_R*(R_rest - N_b_R) - k_off*N_b_R - k_int_R*N_b_R;
//        derivatives[Params.N_intern_R_index] = k_int_R*N_b_R - k_rel_R*N_intern_R;
//    }
//
//
//
//    public void DoseRateCalc(double t_0, double t_f){
//        // according to any kinetic model that we have in our model, this function
//        // calculates the current dose rate and pushes it in the corresponding
//        // array list hosted by the grid.
//        double[] nextValues = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
//        double[] currentValues = this.grid.PKStateVariables.get(this.grid.PKStateVariables.size()-1);
//        double result = this.solver.Runge45(this::RHS, currentValues, nextValues, t_0, t_f, Params.dtStart, Params.errorTolerance);
//        System.out.println(nextValues[0]);
//        this.grid.PKStateVariables.add(nextValues);
//
//        // but use the middle layer as the representative to determine
//        // the dynamics happening for all cells.
//    }


}
