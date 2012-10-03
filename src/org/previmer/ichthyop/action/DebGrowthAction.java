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

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.ETracker;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.DebParticleLayer;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author gandres
 */
public class DebGrowthAction extends AbstractAction {

    private String temperature_field;
    private String food_field;
    double p_Xm ;        // J cm-2 d-1, max. surf. specific ingestion rate
    double ae   ;      // - Assimilation efficiency; E. capensis = 0.71 James et al. (1989)
    double XK_chl ;        // Half saturation constant for sat derived  [chlorophyll-a] mg m3
    // Energetic related parameters
    double E_m  ;       // J/cm^3, reserve capacity 
    double E_g  ;       // J cm-3; // Cost for growth 
    double p_M  ;         // J cm-3 d-1; Volume specific maint. cost
    double Kappa ;       // - energy (pC) allocation rule 
    // Temperature related params
    double TA   ;      // Arehnius temp (K) Pecquerie et al. 2009
    double T1   ;	 // K, Ref temp = 16C (avg. mid-water temp in GoL)
    
    double dt ; 
    
    
    
    @Override
    public void loadParameters() throws Exception {
        
        p_Xm   = Double.valueOf(getParameter("ingestion_rate"))/(86400.0*100.0);        // 325;J cm-2 d-1, max. surf. specific ingestion rate
        // divisé par 86400 pour la conversion en secondes et divisé par 100 pour la conversion en mm-2
        ae     =  Double.valueOf(getParameter("assimilation_efficiency"));     // 0.71 ;- Assimilation efficiency; E. capensis = 0.71 James et al. (1989)
        XK_chl = Double.valueOf(getParameter("half_saturation"));        //0.1; Half saturation constant for sat derived  [chlorophyll-a] mg m3
        // Energetic related parameters
        E_m    = Double.valueOf(getParameter("reserve_capacity"))/1000.0;       //2700; J/cm^3, reserve capacity 
        // divisé par 1000 pour la conversion en mm-3
        E_g    = Double.valueOf(getParameter("cost_growth"))/1000.0;       //4000; J cm-3; // Cost for growth 
        // divisé par 1000 pour la conversion en mm-3
        p_M    = Double.valueOf(getParameter("volume_cost_maintenance"))/(86400.0*1000.0);         //49; J cm-3 d-1; Volume specific maint. cost
        // divisé par 86400 pour la conversion en secondes et divisé par 1000 pour la conversion en mm-3
        Kappa  = Double.valueOf(getParameter("allocation_rule"));        //0.7; - energy (pC) allocation rule 
        // Temperature related params
        TA     = Double.valueOf(getParameter("arrhenius"));      //9800 ; Arrhenius temp (K) Pecquerie et al. 2009
        T1     = Double.valueOf(getParameter("ref_temp"));	 //273 + 16; K, Ref temp = 16C (avg. mid-water temp in GoL)
                //p= [p_Xm ae XK_chl E_m E_g p_M Kappa TA T1 mu_E shape_larvae ]; // pack params

        dt = getSimulationManager().getTimeManager().get_dt();//Double.valueOf(getParameter("")); //1 ; 
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        food_field = getParameter("food_field");
        getSimulationManager().getDataset().requireVariable(food_field, getClass());
        
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
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(ETracker.class);
        }
    }

    @Override
    public void execute(IBasicParticle particle) {
        DebParticleLayer debLayer = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field, debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double f= getSimulationManager().getDataset().get(food_field, debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double[] res_deb= grow(dt, debLayer.getE(), debLayer.getV(), debLayer.getE_R(),debLayer.getVj() , temp, f);
        debLayer.setE(res_deb[0]);
        debLayer.setV(res_deb[1]);
        debLayer.setE_R(res_deb[2]);
        debLayer.computeLength();
        

        if (res_deb[3]==0){
            particle.kill(ParticleMortality.STARVATION);
        }
       
    }
    
    private double[] grow(double dt, double Et, double Vt, double E_Rt, double Vj, double temperature, double food){
    // Set initial conditions :
        double V= Vt;  
        double E = Et; 
        double E_R = E_Rt; 

        
        double tempK_surface = 273 + temperature;
        double Tcorr = Math.exp(TA/T1-TA/(tempK_surface));
        double f;
        if (V < Vj){ // no feeding < size-at-mouth opening
            f = 0;
        }
        else {
            f = food/(food+XK_chl); 
        }// functional response

        
        // Correction of physiology parameters for temperature :
        double p_XmT = p_Xm * Tcorr;    
        double p_MT = p_M * Tcorr ;
        double p_AmT = p_XmT * ae;

        //ENERGETIC FLUXES (J d-1)
        double flow_P_A= p_AmT * f * Math.pow(V,2/3.0); 	                  // assimilated energy
        double flow_P_M = p_MT * V;                              // energy lost to maintenance
        double flow_Pc = E /(Kappa * (E/V) + E_g) * (E_g * (p_AmT/E_m) * Math.pow(V,-1/3.0) + p_M); // energy for utilisation
        double flow_Pg = Math.max((double) 0, Kappa * flow_Pc - flow_P_M);     // energy directed to strucutral growth
        double flow_P_J =  V * (1 - Kappa)/Kappa * p_MT ;        // maturity maintenance 
        double flow_pR = ((1-Kappa) * flow_Pc) - flow_P_J;       

        //// STATE VARIABLES - Differential equations ////////////////////////////////////////
        double dEdt = flow_P_A - flow_Pc ;   // Reserves, J ; dE = pA - pC;
        double dVdt = flow_Pg/E_g;           // Structure, cm^3 ; dV = (kap * pC - p_M)/EG;
        double dE_Rdt = flow_pR;  // Repro buffer / maturation, J

        //Integration
        E = E + dEdt * dt ;   V = V + dVdt * dt ;  E_R = E_R + dE_Rdt * dt ;

        
        // compute weight
        //double dV = 1; 
        //W_dw[j] = V * dV  + (E/mu_E);  
        //Compute DRY weight (g, dw) * 4.1 = Wet weight
        
        // starvation test
        int starvation;
        if ( Kappa * flow_Pc < flow_P_M && (1-Kappa) * flow_Pc < flow_P_J){
            starvation = 0 ; 
            
        }
        else {
            starvation  = 1;
        } //no starvation

        double[] res={E,V,E_R, (double) starvation};
        return res; 
    }
    
}
