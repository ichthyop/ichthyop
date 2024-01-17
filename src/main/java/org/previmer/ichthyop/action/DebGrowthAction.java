/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software.
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.DebEHTracker;
import org.previmer.ichthyop.io.DebETracker;
import org.previmer.ichthyop.io.DebERTracker;
import org.previmer.ichthyop.io.DebVTracker;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.DebParticleLayer;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.ParticleMortality;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.LengthStage;

/**
 *
 * @author gandres
 */
public class DebGrowthAction extends AbstractAction {

    private String temperature_field;
    private String food_field;
    private double p_Xm;        // J cm-2 d-1, max. surf. specific ingestion rate
    private double ae;      // - Assimilation efficiency; E. capensis = 0.71 James et al. (1989)
    private double XK_chl;        // Half saturation constant for sat derived  [chlorophyll-a] mg m3
    // Energetic related parameters
    private double E_m;       // J/cm^3, reserve capacity
    private double E_g;       // J cm-3; // Cost for growth
    private double p_M;         // J cm-3 d-1; Volume specific maint. cost
    private double Kappa;       // - energy (pC) allocation rule
    // Temperature related params
    private double TA;      // Arehnius temp (K) Pecquerie et al. 2009
    private double T1;	 // K, Ref temp = 16C (avg. mid-water temp in GoL)
    private static double length_init; // = 0.025d; // Initial length [millimeter] for the particles.
    private static double feeding_length;// = 4.5d; // hreshold [millimeter] between Yolk-Sac Larvae and Feeding Larvae
    private double shape_larvae; // size related conversion params
    private double E_init; // Réserve initiale
    private double Vj; // Structure at mouth opening (yolk_to_feeding)
    private LengthStage lengthStage;

    private double dt;

    @Override
    public void loadParameters() throws Exception {

        p_Xm = Double.valueOf(getParameter("ingestion_rate")) / (86400.0 * 100.0);        // 325;J cm-2 d-1, max. surf. specific ingestion rate
        // divisé par 86400 pour la conversion en secondes et divisé par 100 pour la conversion en mm-2
        ae = Double.valueOf(getParameter("assimilation_efficiency"));     // 0.71 ;- Assimilation efficiency; E. capensis = 0.71 James et al. (1989)
        XK_chl = Double.valueOf(getParameter("half_saturation"));        //0.1; Half saturation constant for sat derived  [chlorophyll-a] mg m3
        // Energetic related parameters
        E_m = Double.valueOf(getParameter("reserve_capacity")) / 1000.0;       //2700; J/cm^3, reserve capacity
        // divisé par 1000 pour la conversion en mm-3
        E_g = Double.valueOf(getParameter("cost_growth")) / 1000.0;       //4000; J cm-3; // Cost for growth
        // divisé par 1000 pour la conversion en mm-3
        p_M = Double.valueOf(getParameter("volume_cost_maintenance")) / (86400.0 * 1000.0);         //49; J cm-3 d-1; Volume specific maint. cost
        // divisé par 86400 pour la conversion en secondes et divisé par 1000 pour la conversion en mm-3
        Kappa = Double.valueOf(getParameter("allocation_rule"));        //0.7; - energy (pC) allocation rule
        // Temperature related params
        TA = Double.valueOf(getParameter("arrhenius"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009
        T1 = Double.valueOf(getParameter("ref_temp"));	 //273 + 16; K, Ref temp = 16C (avg. mid-water temp in GoL)
        //p= [p_Xm ae XK_chl E_m E_g p_M Kappa TA T1 mu_E shape_larvae ]; // pack params

        length_init = Double.valueOf(getParameter("initial_length"));
        feeding_length = Double.valueOf(getParameter("yolk2feeding_length"));

        shape_larvae = Double.valueOf(getParameter("shape")); //0.152 ; larvae < 3.7cm length and weight data  - Palomera et al.
        E_init = Double.valueOf(getParameter("initial_reserve")); //0.022;//0.89998;// //-0.087209;      // J, Reserve at size of hatching.
        // Structure at mouth opening (yolk_to_feeding)
        Vj = Math.pow(shape_larvae * feeding_length, 3);

        dt = getSimulationManager().getTimeManager().get_dt();//Double.valueOf(getParameter("")); //1 ;
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        food_field = getParameter("food_field");
        getSimulationManager().getDataset().requireVariable(food_field, getClass());

        // Init the length stage for tracking length stages.
        lengthStage = new LengthStage(BlockType.ACTION, getBlockKey());
        lengthStage.init();

        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("length_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
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

        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("E_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(DebETracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebERTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebVTracker.class);
            getSimulationManager().getOutputManager().addPredefinedTracker(DebEHTracker.class);
        }
    }

    @Override
    public void init(IParticle particle) {
        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(length_init);

        DebParticleLayer debLayer = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
        debLayer.setE(E_init);
        debLayer.setE_R(0);
        debLayer.setV(Math.pow(shape_larvae * length_init, 3));
    }

    @Override
    public void execute(IParticle particle) {
        DebParticleLayer debLayer = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field, debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double food = getSimulationManager().getDataset().get(food_field, debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double[] res_deb = grow(dt, debLayer.getE(), debLayer.getV(), debLayer.getE_R(), Vj, temp, food);
        debLayer.setE(res_deb[0]);
        debLayer.setV(res_deb[1]);
        debLayer.setE_R(res_deb[2]);
        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(computeLength(debLayer.getV()));
        stageLayer.setStage(lengthStage.getStage((float) lengthLayer.getLength()));

        if (res_deb[3] == 1) {
            particle.kill(ParticleMortality.STARVATION);
        }

    }

    private double computeLength(double V) {
        return Math.pow(V, 1 / 3.0) / shape_larvae;
    }

    private double[] grow(double dt, double Et, double Vt, double E_Rt, double Vj, double temperature, double food) {
        // Set initial conditions :
        double V = Vt;
        double E = Et;
        double E_R = E_Rt;

        double tempK_surface = 273 + temperature;
        double Tcorr = Math.exp(TA / T1 - TA / (tempK_surface));
        double f;
        if (V < Vj) { // no feeding < size-at-mouth opening
            f = 0;
        } else {
            f = food / (food + XK_chl);
        }// functional response

        // Correction of physiology parameters for temperature :
        double p_XmT = p_Xm * Tcorr;
        double p_MT = p_M * Tcorr;
        double p_AmT = p_XmT * ae;

        //ENERGETIC FLUXES (J d-1)
        double flow_P_A = p_AmT * f * Math.pow(V, 2 / 3.0); 	                  // assimilated energy
        double flow_P_M = p_MT * V;                              // energy lost to maintenance
        double flow_Pc = E / (Kappa * (E / V) + E_g) * (E_g * (p_AmT / E_m) * Math.pow(V, -1 / 3.0) + p_MT); // energy for utilisation
        double flow_Pg = Math.max((double) 0, Kappa * flow_Pc - flow_P_M);     // energy directed to strucutral growth
        double flow_P_J = V * (1 - Kappa) / Kappa * p_MT;        // maturity maintenance
        double flow_pR = ((1 - Kappa) * flow_Pc) - flow_P_J;

        //// STATE VARIABLES - Differential equations ////////////////////////////////////////
        double dEdt = flow_P_A - flow_Pc;   // Reserves, J ; dE = pA - pC;
        double dVdt = flow_Pg / E_g;           // Structure, cm^3 ; dV = (kap * pC - p_M)/EG;
        double dE_Rdt = flow_pR;  // Repro buffer / maturation, J

        //Integration
        E = E + dEdt * dt;
        V = V + dVdt * dt;
        E_R = E_R + dE_Rdt * dt;

        // compute weight
        //double dV = 1;
        //W_dw[j] = V * dV  + (E/mu_E);
        //Compute DRY weight (g, dw) * 4.1 = Wet weight
        // starvation test
        int starvation;
        if ((Kappa * flow_Pc < flow_P_M) || (((1 - Kappa) * flow_Pc) < flow_P_J)) {
            starvation = 1;
        } else {
            starvation = 0;
        } // no starvation

        double[] res = {E, V, E_R, (double) starvation};
        return res;
    }

}
