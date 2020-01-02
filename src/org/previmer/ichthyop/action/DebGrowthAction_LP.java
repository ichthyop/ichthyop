/*
 * Copyright (C) 2012 gandres
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.io.DebDryWTracker;
import org.previmer.ichthyop.io.DebETracker;
import org.previmer.ichthyop.io.DebEHTracker;
import org.previmer.ichthyop.io.DebERTracker;
import org.previmer.ichthyop.io.DebVTracker;
import org.previmer.ichthyop.io.DebLengthTracker;
import org.previmer.ichthyop.io.DebWetWTracker;
import org.previmer.ichthyop.particle.DebParticleLayer;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author gandres
 */
public class DebGrowthAction_LP extends AbstractAction {

    private String temperature_field;
    private String food_field;
    private double p_Am;        // J cm-2 d-1, max. surf. specific assimilation rate
    // Energetic related parameters
    //private double E_m;       // J/cm^3, reserve capacity 
    private double E_G;       // Volume specific costs of strcture
    private double p_M;         // J cm-3 d-1; Volume specific maint. cost
    // private double Kappa_X;       // Fraction of odd energy fixed in reserves
    // Temperature related params
    private double TA;      // Arehnius temp (K) Pecquerie et al. 2009
    private double T1;	 // K, Ref temp = 16C (avg. mid-water temp in GoL)
    private static double length_init; // = 0.025d; // Initial length [millimeter] for the particles.
    private static double feeding_length;// = 4.5d; // hreshold [millimeter] between Yolk-Sac Larvae and Feeding Larvae
    private double shape_larvae; // size related conversion params
    private double E_0; // initial reserve
    //private double Vj; // Structure at mouth opening (yolk_to_feeding)
    private double E_Hb;  // Maturity threshold at birth
    // private double E_Hh;  // Maturity threshold at hatching
    private double E_Hj;  // Maturity threshold at metamorphosis
    private double E_Hp;  // Maturity threshold at puberty
    private double k_J;   // Maturity maintenance rate coefficient
    private double L_b;
    private double L_j;
    private double Kappa;  // fraction of mobilized reserve allocated to soma
    private double Kappa_R;  // fraction of reproduction energy fixed in eggs
    private double T_AL; // Ahr. temperature at higher boundary
    private double T_L;  // Lower boundary of thermal range
    private double T_AH;  // Ahr. temperature at higher boundary
    private double T_H;  // Upper boundary of thermal range
    private double dt;  // time step
    //private double F_m;  // surface area specific rate
    private double K;   // half saturation constance
    private double V_dot;   // energy conductance

    private double sigmaMb, sigmaMj;
    private double dV;
    private double muV, muE;
    private double wV, wE;
    private double dVw, dEw, dE;   // parameters for computation of wet weight

    private ModelType modelType;
    private FunctionTcor tcorFunc;
    private boolean addPhysTracker;

    /**
     * Pointer to the class method to compute Tcorr.
     */
    interface FunctionTcor {

        double getCorr(double T_kelvin);
    }

    @Override
    public void loadParameters() throws Exception {

        String key = "accelerated.deb.enabled";
        if (this.isNull(key)) {
            // if the parameter is not found, use standard deb
            modelType = ModelType.STD;
        } else {
            // if boolean = true, use ABJ model, else use standard model
            modelType = Boolean.valueOf(getParameter(key)) ? ModelType.ABJ : ModelType.STD;
        }

        // Temperature related params
        TA = Double.valueOf(getParameter("arrhenius"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009

        // Temperature related params
        T_L = Double.valueOf(getParameter("lower_thermal_range"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009

        // Temperature related params
        T_H = Double.valueOf(getParameter("upper_thermal_range"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009

        // Temperature related params
        T_AL = Double.valueOf(getParameter("arrhenius_lower_thermal_range"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009

        // Temperature related params
        T_AH = Double.valueOf(getParameter("arrhenius_upper_thermal_range"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009

        //F_m = Double.valueOf(getParameter("surface_area_searching_rate")) / (86400.0 * 100.0);  // m3/cm2/day converted into m3/mm2/sec
        //this.Kappa_X = Double.valueOf(getParameter("fraction_fixed_reserve"));  // no unit

        p_Am = Double.valueOf(getParameter("assimilation rate")) / (86400.0 * 100.0);        // 325;J cm-2 d-1, max. surf. specific ingestion rate
        // divisé par 86400 pour la conversion en secondes et divisé par 100 pour la conversion en mm-2

        this.V_dot = Double.valueOf(getParameter("energy_conductance")) * 100 / (86400.0);  //   cm/day -> conversion into mm/sec

        this.Kappa = Double.valueOf(getParameter("fraction_mobilized_somma")); // no unit

        this.Kappa_R = Double.valueOf(getParameter("fraction_fixed_eggs")); // no unit

        // Energetic related parameters
        //E_m = Double.valueOf(getParameter("reserve_capacity")) / 1000.0;       //2700; J/cm^3, reserve capacity 
        this.p_M = Double.valueOf(getParameter("volume_specific_somatic_maintenance")) / (86400 * 1e3); // J/cm3/day conversion into J/mm3/day

        this.k_J = Double.valueOf(getParameter("maturity_maintenance_rate")) / (86400);  // day-1, converted into s-1 

        // divisé par 1000 pour la conversion en mm-3
        this.E_G = Double.valueOf(getParameter("cost_growth")) / 1000.0;       //4000; J cm-3; // Cost for growth 

        T1 = Double.valueOf(getParameter("ref_temp"));	 //273.15 + 20; K, Ref temp = 20C (avg. mid-water temp in GoL)
        //p= [p_Xm ae XK_chl E_m E_g p_M Kappa TA T1 mu_E shape_larvae ]; // pack params

        //this.E_Hh = Double.valueOf(getParameter("maturity_thres_hatching")); // J
        this.E_Hb = Double.valueOf(getParameter("maturity_thres_birth")); // J
        this.E_Hj = Double.valueOf(getParameter("maturity_thres_metamorphosis")); // J
        this.E_Hp = Double.valueOf(getParameter("maturity_thres_puberty")); // J

        length_init = Double.valueOf(getParameter("initial_length"));
        feeding_length = Double.valueOf(getParameter("yolk2feeding_length"));
        shape_larvae = Double.valueOf(getParameter("shape")); //0.152 ; larvae < 3.7cm length and weight data  - Palomera et al.
        E_0 = Double.valueOf(getParameter("initial_reserve")); //0.022;//0.89998;// //-0.087209;      // J, Reserve at size of hatching.

        // Structure at mouth opening (yolk_to_feeding)
        // Vj = Math.pow(shape_larvae * feeding_length, 3);

        dt = getSimulationManager().getTimeManager().get_dt();

        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());

        food_field = getParameter("food_field");
        getSimulationManager().getDataset().requireVariable(food_field, getClass());

        //this.K = (this.p_Xm) / (this.Kappa_X * this.F_m);
        // K should be defined using physio, but in practice calibration parameter
        this.K = Double.valueOf(getParameter("half_saturation_constant"));

        addPhysTracker = false;
        try {
            addPhysTracker = Boolean.valueOf(getParameter("deb_physio_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addPhysTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(DebLengthTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebDryWTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebWetWTracker.class);
        }

        boolean addTracker = false;
        try {
            addTracker = Boolean.valueOf(getParameter("deb_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(DebETracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebERTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebVTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebEHTracker.class);
        }

        if (modelType == ModelType.ABJ) {
            this.L_j = Double.valueOf(getParameter("length_metam"));
            this.L_b = Double.valueOf(getParameter("length_birth"));
        }
        
        if (this.addPhysTracker) {
            this.sigmaMb = Double.valueOf(getParameter("shape_coef_birth"));
            this.sigmaMj = Double.valueOf(getParameter("shape_coef_metam"));
            this.dV = Double.valueOf(getParameter("specific_dens_struc")) / (1.0e3);   // conversion from g/cm3 to g/mm3
            this.dVw = Double.valueOf(getParameter("specific_dens_struc_wet")) / (1.0e3);   // conversion from g/cm3 to g/mm3
            this.wV = Double.valueOf(getParameter("spec_weight_structure"));
            this.muV = Double.valueOf(getParameter("chem_pot_structure"));
            
            this.dE = Double.valueOf(getParameter("specific_dens_reserve")) / (1.0e3);
            this.dEw = Double.valueOf(getParameter("specific_dens_reserve_wet")) / (1.0e3);
            this.wE = Double.valueOf(getParameter("spec_weight_reserve"));
            this.muE = Double.valueOf(getParameter("chem_pot_reserve"));

        }

        // @TODO: add some test to define whether classical or updated Tcor function should
        // be used.
        if (false) {
            tcorFunc = (double z) -> this.computeTcorr(z);
        } else {
            tcorFunc = (double z) -> this.computeTcorrLp(z);
        }
    }

    @Override
    public void init(IParticle particle) {
        // Init the layer that contains all the DEB variables (state variables + physio)
        DebParticleLayer debLayer = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
        
        // Initialization of state variables.
        debLayer.setE(E_0);
        debLayer.setE_R(0);
        debLayer.setE_H(0);
        debLayer.setV(Math.pow(shape_larvae * length_init, 3));
    }

    @Override
    public void execute(IParticle particle) {

        // Recover the temperature and food fields.
        double temp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double food = getSimulationManager().getDataset().get(food_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();

        // Recover the DEB variables for the current particle. 
        DebParticleLayer debLayer = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
        
        // Computes the DEB growth for the given time step
        double[] res_deb = grow(dt, debLayer.getE(), debLayer.getV(), debLayer.getE_H(), debLayer.getE_R(), temp, food);
        debLayer.setE(res_deb[0]);
        debLayer.setV(res_deb[1]);
        debLayer.setE_H(res_deb[2]);
        debLayer.setE_R(res_deb[3]);
        
        if (this.addPhysTracker) {
            this.computeWeight(debLayer);
        }

        if (res_deb[4] == 0) {
            particle.kill(ParticleMortality.STARVATION);
        }

    }

    private double computeLength(double V) {
        return Math.pow(V, 1 / 3.0);
    }

    /**
     *
     * @param dt Time step
     * @param Et Reserve at beginning of time step
     * @param Vt Structure at beginning of time step
     * @param E_Rt Reproduction buffer at beginning of time step
     * @param E_Ht Energy invested into development beginning of time step
     * @param Vj
     * @param temperature Water temperature (C)
     * @param food Food concentration (units?)
     * @return
     */
    private double[] grow(double dt, double E, double V, double E_H, double E_R, double temperature, double food) {

        // Set initial conditions :
        double s_M = 1.0;

        // convert temperature (C) into (K)
        double tempK_surface = 273.15 + temperature;

        // Ahr. temperature
        double Tcorr = tcorFunc.getCorr(tempK_surface);

        double f;
        if (E_H < E_Hb) { // no feeding < size-at-mouth opening
            f = 0;
        } else {
            f = food / (food + this.K);
        }// functional response

        // Correction of physiology parameters for temperature :
        //double p_XmT = p_Xm * Tcorr;
        double p_AmT = this.p_Am * Tcorr;  // ingestion rate
        double V_dotT = this.V_dot * Tcorr;
        double p_MT = this.p_M * Tcorr;
        double k_JT = this.k_J * Tcorr;

        // Computation of metabolic acceleration if accelerated DEB
        if (this.modelType == ModelType.ABJ) {
            // Metabolic acceleration – abj model
            if (E_H < E_Hb) {
                s_M = 1;
            } else if ((E_Hb <= E_H) && (E_H < E_Hj)) {
                s_M = this.computeLength(V) / L_b;
            } else {
                s_M = L_j / L_b;
            }
        }

        // only two parameters are accelerated by s_M, p_Am and v
        p_AmT *= s_M;
        V_dotT *= s_M;

        //double FmT = this.F_m * s_M;
        //double p_XmT = p_AmT / this.Kappa_X;
        //ENERGETIC FLUXES (J d-1)
        double flow_p_A;
        if (E_H < this.E_Hb) {
            flow_p_A = 0.0;
        } else {
            //flow_p_A = this.Kappa_X * p_XmT * f * Math.pow(V, 2 / 3.0); 	                  // assimilated energy
            flow_p_A = p_AmT * f * Math.pow(V, 2 / 3.0);  // assimilated energy
        }

        double flow_p_M = p_MT * V;                              // energy lost to maintenance

        double flow_p_C = (E / V) * (this.E_G * V_dotT * Math.pow(V, 2 / 3.0) + flow_p_M) / (this.Kappa * E / V + this.E_G);// energy for utilisation
        double flow_p_J = k_JT * E_H;        // maturity maintenance 

        // Corresponds to (kappa_pc - pm)
        double flow_p_G = Math.max(0.0, Kappa * flow_p_C - flow_p_M) / this.E_G;     // energy directed to structural growth
        double flow_p_R = (1 - Kappa) * flow_p_C - flow_p_J;

        //// STATE VARIABLES - Differential equations ////////////////////////////////////////
        double dEdt = flow_p_A - flow_p_C;   // Reserves, J ; dE = pA - pC;
        double dVdt = flow_p_G;           // Structure, cm^3 ; dV = (kap * pC - p_M)/EG;

        double dHdt;
        double dRdt;

        if (E_H < E_Hp) {
            dHdt = flow_p_R;  // Cumulated energy invested into development, J
            dRdt = 0; // Repro buffer / maturation, J ; dE_H = (1 – kap) p_C – p_J
        } else {
            dHdt = 0;  // Cumulated energy invested into development, J
            dRdt = flow_p_R;  // Repro buffer / maturation, J ; dE_H = (1 – kap) p_C – p_J
        }

        //Integration
        E = E + dEdt * dt;
        V = V + dVdt * dt;
        E_H = E_H + dHdt * dt;
        E_R = E_R + dRdt * dt;

        // compute weight
        //double dV = 1; 
        //W_dw[j] = V * dV  + (E+E_R)/mu_E;  
        //Compute DRY weight (g, dw) * 4.1 = Wet weight
        // starvation test
        int starvation;
        if ((Kappa * flow_p_C < flow_p_M) && ((1 - Kappa) * flow_p_C < flow_p_J)) {
            starvation = 0;
        } else {
            starvation = 1;
        } //no starvation

        double[] res = {E, V, E_H, E_R, (double) starvation};
        return res;
    }

    private double computeTcorr(double T_kelvin) {
        double Tcorr = Math.exp(TA / T1 - TA / (T_kelvin));
        return Tcorr;
    }

    private double computeTcorrLp(double T_kelvin) {
        double c1 = this.computeTcorr(T_kelvin);
        double num = 1 + Math.exp((T_AL / T1) - (T_AL / T_L)) + Math.exp((T_AH / T_H) - (T_AH / T1));
        double den = 1 + Math.exp((T_AL / T_kelvin) - (T_AL / T_L)) + Math.exp((T_AH / T_H) - (T_AH / T_kelvin));
        return c1 * (num / den);
    }

    public enum ModelType {

        STD("Standard"),
        ABJ("Accelerated");

        private final String description;

        private ModelType(String des) {
            this.description = des;
        }

        @Override
        public String toString() {
            return this.description;
        }

    }

    private void computeWeight(DebParticleLayer debLayer) {

        double V = debLayer.getV();
        double E = debLayer.getE();
        double E_R = debLayer.getE_R();

        if (this.modelType == ModelType.ABJ) {
            double L = this.computeLength(V);
            double sigmaM = this.sigmaMb + (this.sigmaMj - this.sigmaMb) * (L - this.L_b) / (this.L_j - this.L_b);
            double Lw = Math.pow(V, 1.0 / 3.0) / sigmaM;

            double weightV = this.dV * V;
            double weightE = this.wE / this.muE * E;
            double weightR = this.wE / this.muE * E_R;
            double dryW = weightV + weightE + weightR;

            double WVw = this.dVw * V;
            double WEw = this.wE * this.dEw / (this.muE * this.dE) * E;
            double WRw = this.wE * this.dEw / (this.muE * this.dE) * E_R;
            double wetW = WVw + WEw + WRw;

            double F = this.Kappa_R * E_R / this.E_0;
            debLayer.setLw(Lw);
            debLayer.setdryW(dryW);
            debLayer.setwetW(wetW);
            debLayer.setF(F);

        }
    }

}
