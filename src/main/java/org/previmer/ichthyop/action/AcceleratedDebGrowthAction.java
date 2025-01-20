/*
 * Copyright (C) 2024 jflores
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

import org.previmer.ichthyop.io.DebETracker;
import org.previmer.ichthyop.io.DebVTracker;
import org.previmer.ichthyop.io.DebEHTracker;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.AcceleratedDebParticleLayer;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 * @author jflores Modified by : Jorge FLORES-VALIENTE (2022-06-23)
 */

public class AcceleratedDebGrowthAction extends AbstractAction {

    // Forcing variables
    private String temperature_field;
    private String food_field;

    // DEB parameters
    private double K_X; // (-); Half saturation constant; calibrated from average food in the
                        // environment.
    private double p_Am; // (J cm-2 d-1); Surface-area-specific maximum assimilation rate.
    private double V_dot; // (cm d-1); Energy conductance.
    private double E_G; // (J cm-3); Volume-specific costs of structure.
    private double p_M; // (J cm-3 d-1); Specific Volume-linked somatic maintenance rate.
    private double kap; // (-) fraction of mobilized reserve allocated to soma.
    private double L_b; // (cm); Volumetric length at birth.
    private double L_j; // (cm); Volumetric length at metamorphosis.
    private double E_Hb; // (J); Maturity threshold at birth.
    private double E_Hj; // (J); Maturity threshold at metamorphosis.
    private double E_Hp; // (J); Maturity threshold at puberty.
    private double k_J; // (d-1); Maturity maintenance rate coefficient.

    // Temperature related parameters
    double T_K; // Kelvin
    double T_ref; // K, Reference temp
    double T_A; // Arehnius temp
    double T_L; // K Lower temp boundary
    double T_H; // K Upper temp boundary
    double T_AL; // K Arrh. temp for lower boundary
    double T_AH; // K Arrh. temp for upper boundary
    double TC_threshold; // TC threshold to kill particles

    // Time step
    double dt; // Time step

    @Override
    public void loadParameters() throws Exception {

        // Forcing variables
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        food_field = getParameter("food_field");
        getSimulationManager().getDataset().requireVariable(food_field, getClass());

        // DEB parameters
        K_X = Double.valueOf(getParameter("K_X")); // 1.6 (-);
        p_Am = Double.valueOf(getParameter("p_Am")) / (86400.0 * 100.0); // 84.97 (J.cm-2.d-1); divided by 86400 for
                                                                         // conversion in seconds and divided by 100 for
                                                                         // conversion in mm-2.
        V_dot = Double.valueOf(getParameter("V_dot")) * 10 / (86400); // 0.04124 (cm d-1); energy conductance; divided
                                                                      // by 86400 for conversion in seconds and
                                                                      // multiplied by 10 for conversion in mm.
        E_G = Double.valueOf(getParameter("E_G")) / 1000.0; // 5283; (J cm-3); divided by 1000 for conversion to mm-3.
        p_M = Double.valueOf(getParameter("p_M")) / (86400.0 * 1000.0); // 80.71; (J cm-3 d-1); divided by 86400 for
                                                                        // conversion in seconds and divided by 1000 for
                                                                        // conversion in mm-3.
        kap = Double.valueOf(getParameter("kap")); // 0.5512 (-);
        L_b = Double.valueOf(getParameter("L_b")) * 10; // (cm); multiplied by 10 for conversion in mm.
        L_j = Double.valueOf(getParameter("L_j")) * 10; // (cm); multiplied by 10 for conversion in mm.
        E_Hb = Double.valueOf(getParameter("E_Hb")); // (J);
        E_Hj = Double.valueOf(getParameter("E_Hj")); // (J);
        E_Hp = Double.valueOf(getParameter("E_Hp")); // (J);
        k_J = Double.valueOf(getParameter("k_J")) / (86400); // (d-1); divided by 86400 for conversion in seconds

        // Temperature related parameters
        T_K = 273.15; // Conversion to Kelvin degrees.
        T_ref = Double.valueOf(getParameter("T_ref")) + T_K; // (K); Reference temperature.
        T_A = Double.valueOf(getParameter("T_A")); // (K); Arrhenius temperature.
        T_L = Double.valueOf(getParameter("T_L")) + T_K; // (K); Lower temperature boundary.
        T_H = Double.valueOf(getParameter("T_H")) + T_K; // (K); Upper temperature boundary.
        T_AL = Double.valueOf(getParameter("T_AL")); // (K); Arrhenius temperature for lower boundary.
        T_AH = Double.valueOf(getParameter("T_AH")); // (K); Arrhenius temperature for upper boundary.
        TC_threshold = Double.valueOf(getParameter("TC_threshold"));// TC threshold to kill particles

        // Time step
        dt = getSimulationManager().getTimeManager().get_dt();

        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("length_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("stage_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(StageTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("E_tracker"));
            addTracker = Boolean.valueOf(getParameter("V_tracker"));
            addTracker = Boolean.valueOf(getParameter("E_H_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(DebETracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebVTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebEHTracker.class);
        }
    }

    @Override
    public void execute(IParticle particle) {
        AcceleratedDebParticleLayer debLayer = (AcceleratedDebParticleLayer) particle.getLayer(AcceleratedDebParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field,
                debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime())
                .doubleValue();
        double f = getSimulationManager().getDataset().get(food_field, debLayer.particle().getGridCoordinates(),
                getSimulationManager().getTimeManager().getTime()).doubleValue();
        double[] res_deb = grow(dt, debLayer.getE(), debLayer.getV(), debLayer.getE_H(), debLayer.getE_R(), temp, f);

        debLayer.setE(res_deb[0]);
        debLayer.setV(res_deb[1]);
        debLayer.setE_H(res_deb[4]);
        debLayer.setE_R(res_deb[5]);
        debLayer.computeLength();

        if (res_deb[2] == 1) {
            particle.kill(ParticleMortality.STARVATION);
        }

        if (res_deb[3] == 1) {
            particle.kill(ParticleMortality.DEAD_HOT);
        }
    }

    private double[] grow(double dt, double Et, double Vt, double E_Ht, double E_Rt, double temperature, double food) {
        // Set initial conditions :
        double V = Vt;
        double E = Et;
        double E_H = E_Ht;
        double E_R = E_Rt;

        double tempK = T_K + temperature;

        // Temperature correction factor
        double TC;

        // 1-parameter equation for Temperature Correction
        // TC = Math.exp(T_A/T_ref - T_A/(tempK));

        // 5-parameter equation for Temperature Correction
        double s_A = Math.exp(T_A / T_ref - T_A / tempK); // Arrhenius factor;
        double s_L_ratio = (1 + Math.exp(T_AL / T_ref - T_AL / T_L)) / (1 + Math.exp(T_AL / tempK - T_AL / T_L));
        double s_H_ratio = (1 + Math.exp(T_AH / T_H - T_AH / T_ref)) / (1 + Math.exp(T_AH / T_H - T_AH / tempK));

        if (tempK <= T_ref) {
            TC = s_A * s_L_ratio;
        } else {
            TC = s_A * s_H_ratio;
        }

        // Correction of physiology parameters for temperature :
        double p_AmT = TC * p_Am;
        double V_dotT = TC * V_dot;
        double p_MT = TC * p_M;
        double k_JT = TC * k_J;

        // Scaled functional response
        double f;
        if (E_H < E_Hb) { // no feeding < size-at-mouth opening
            f = 0;
        } else {
            f = food / (food + K_X);
        }

        // Metabolic acceleration – abj model
        double s_M;
        if (E_H < E_Hb) {
            s_M = 1;
        } else if ((E_Hb <= E_H) && (E_H < E_Hj)) {
            s_M = Math.pow(V, 1 / 3.0) / L_b;
        } else {
            s_M = L_j / L_b;
        }

        // Only two parameters are accelerated by s_M: p_Am and v
        p_AmT = s_M * p_AmT;
        V_dotT = s_M * V_dotT;

        // ENERGETIC FLUXES (J d-1)
        double p_A_flux;
        if (E_H < E_Hb) {
            p_A_flux = 0.0; // No energy assimilation
        } else {
            p_A_flux = p_AmT * f * Math.pow(V, 2.0 / 3.0); // Assimilated energy
        }

        double p_M_flux = p_MT * V; // Energy lost to maintenance
        double p_C_flux = (E / V) * (E_G * V_dotT * Math.pow(V, 2.0 / 3.0) + p_M_flux) / (kap * E / V + E_G); // Energy
                                                                                                              // for
                                                                                                              // utilisation
        // double p_G_flux = Math.max((double) 0, kap * p_C_flux - p_M_flux); // Energy
        // directed to strucutral growth
        double p_J_flux = k_JT * E_H; // Maturity maintenance

        //// STATE VARIABLES - Differential equations
        //// ////////////////////////////////////////
        double dEdt = p_A_flux - p_C_flux; // Reserve
        double dVdt = ((kap * p_C_flux) - p_M_flux) / E_G; // Structure

        double dE_Hdt; // Maturation
        double dE_Rdt; // Reproduction buffer

        if (E_H < E_Hp) {
            dE_Hdt = (1 - kap) * p_C_flux - p_J_flux; // Cumulated energy invested into development, J
            dE_Rdt = 0; // Repro buffer
        } else {
            dE_Hdt = 0; // Cumulated energy invested into development, J
            dE_Rdt = (1 - kap) * p_C_flux - p_J_flux; // Repro buffer
        }

        // Integration
        E = E + dEdt * dt;
        V = V + dVdt * dt;
        E_H = E_H + dE_Hdt * dt;
        E_R = E_R + dE_Rdt * dt;

        // starvation test
        int starvation;
        if (kap * p_C_flux < p_M_flux || (1 - kap) * p_C_flux < p_J_flux) {
            starvation = 1; // starvation
        } else {
            starvation = 0; // no starvation
        }

        // TC threshold test
        int dead_hot;
        if (TC <= TC_threshold) {
            dead_hot = 1;
        } else {
            dead_hot = 0;
        }

        double[] res = { E, V, (double) starvation, (double) dead_hot, E_H, E_R };
        return res;
    }

    @Override
    public void init(IParticle particle) {
        // TODO: implement the init method
    }
}