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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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

import org.previmer.ichthyop.io.*;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.SargassumParticleLayer;

/**
 *
 * @author Witold Podlejski
 */
public class SargassumGrowthAction extends AbstractAction {

    /** Name of the nc variables to use in the module. */
    private String temperature_field, NO3_field, NH4_field, PO4_field, I_field, S_field, V_field, U_field;

    /** The Nitrogen and Phosphorus quotas corresponding to N/C and P/C.*/
    private double quotaN,quotaP;

    /** The Nitrogen and Phosphorus quotas maxima and minima.*/
    private double minQuotaN,minQuotaP,maxQuotaN,maxQuotaP;

    /** The temperature parameters.*/
    private double Tmin, Tmax, Topt;

    /** The maximum uptake rate for C.*/
    private double maxUptakeC;

    /** Half saturation term for nitrogen and phosphorous absorption. */
    private double saturationN, saturationP;

    /** Maximum uptake velocity for nitrogen and phosphorous absorption. */
    private double uptakeVelocityN, uptakeVelocityP;

    /** Mortality parameters. */
    private double mortality, mortality_coefficient, half_mortality;

//    /** Irradiance loader */
//    private IrradianceLoader irradianceLoader;

    /** Irradiance parameters */
    private double IOpt, ICut;

    /** Salinity parameters */
    private double alphaS, betaS;


    @Override
    public void loadParameters() throws Exception {

        /** Adding trackers */
        boolean addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("biomass_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumBiomassTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("carbon_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumCarbonTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("nitrogen_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumNitrogenTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("phosphor_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumPhosphorTracker.class);
        }

        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_phosphor_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalPhosphorTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_temperature_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalTemperatureTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_irradiance_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalIrradianceTracker.class);
        }

        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_nitrogen_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalNitrogenTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_salinity_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalSalinityTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.parseBoolean(getParameter("environmental_wind_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(SargassumEnvironmentalWindTracker.class);
        }



        /** Loading parameters */

        minQuotaN = Double.parseDouble(getParameter("minimum_quota_nitrogen"));
        maxQuotaN = Double.parseDouble(getParameter("maximum_quota_nitrogen"));
        minQuotaP = Double.parseDouble(getParameter("minimum_quota_phosphor"));
        maxQuotaP = Double.parseDouble(getParameter("maximum_quota_phosphor"));
        quotaN = (maxQuotaN + minQuotaN) /2;
        quotaP = (maxQuotaP + minQuotaP) /2;

        Tmax = Double.parseDouble(getParameter("maximum_temperature"));
        Tmin = Double.parseDouble(getParameter("minimum_temperature"));
        Topt = Double.parseDouble(getParameter("optimal_temperature"));

        maxUptakeC = Double.parseDouble(getParameter("uptake_carbon"));
        uptakeVelocityN = Double.parseDouble(getParameter("uptake_nitrogen"));
        uptakeVelocityP = Double.parseDouble(getParameter("uptake_phosphor"));
        saturationN = Double.parseDouble(getParameter("half_saturation_nitrogen"));
        saturationP = Double.parseDouble(getParameter("half_saturation_phosphor"));
        mortality = Double.parseDouble(getParameter("mortality"));
        mortality_coefficient = Double.parseDouble(getParameter("mortality_coefficient"));
        half_mortality = Double.parseDouble(getParameter("half_mortality"));


        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        NO3_field = getParameter("NO3_field");
        getSimulationManager().getDataset().requireVariable(NO3_field, getClass());
        NH4_field = getParameter("NH4_field");
        getSimulationManager().getDataset().requireVariable(NH4_field, getClass());
        PO4_field = getParameter("PO4_field");
        getSimulationManager().getDataset().requireVariable(PO4_field, getClass());
        I_field = getParameter("irradiance_field");
        getSimulationManager().getDataset().requireVariable(I_field, getClass());
        S_field = getParameter("S_field");
        getSimulationManager().getDataset().requireVariable(S_field, getClass());
        U_field = getParameter("U_field");
        getSimulationManager().getDataset().requireVariable(U_field, getClass());
        V_field = getParameter("V_field");
        getSimulationManager().getDataset().requireVariable(V_field, getClass());

        /** Init IrradianceLoader */
//        String irradiance_field = getParameter("irradiance_field");
//        String lat_field = getParameter("lat_field");
//        String lon_field = getParameter("lon_field");
//        String irradiance_path = getParameter("irradiance_path");
//        String time_field = getParameter("time_field");
//        String file_filter = getParameter("file_filter");
        IOpt = Double.parseDouble(getParameter("optimal_irradiance"));
        ICut = Double.parseDouble(getParameter("cut_irradiance"));

        alphaS = Double.parseDouble(getParameter("alpha_salinity"));
        betaS = Double.parseDouble(getParameter("beta_salinity"));

//
//        irradianceLoader = new IrradianceLoader(irradiance_path, irradiance_field, lon_field, lat_field, time_field, getSimulationManager(), file_filter);

    }

    @Override
    public void init(IParticle particle) {        
        SargassumParticleLayer sargassumLayer = (SargassumParticleLayer) particle.getLayer(SargassumParticleLayer.class);
        sargassumLayer.setN(sargassumLayer.getC() * quotaN);
        sargassumLayer.setP(sargassumLayer.getC() * quotaP);
    }

    @Override
    public void execute(IParticle particle) {
        SargassumParticleLayer sargassumLayer = (SargassumParticleLayer) particle.getLayer(SargassumParticleLayer.class);

        /** Limitation due to temperature */
        double T = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double Tref = T <= Topt ? Tmin : Tmax;
        double temp_limitation = Math.exp(-2. * Math.pow((T - Topt)/(Tref - T),2));
        sargassumLayer.setT_env(T);

        /** Limitation due to nitrogen and phosphor content */
        quotaN = sargassumLayer.getQuotaN();
        double nitrogen_limitation = (1 - minQuotaN/quotaN)/(1 - minQuotaN/maxQuotaN);
        quotaP = sargassumLayer.getQuotaP();
        double phosphor_limitation = (1 - minQuotaP/quotaP)/(1 - minQuotaP/maxQuotaP);
        double nutrient_limitation = Math.min(Math.min(nitrogen_limitation,phosphor_limitation),1.);

        /** Limitation due to solar irradiance */
        double I = getSimulationManager().getDataset().get(I_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double solar_limitation = Math.max(0., (I - ICut) / IOpt * Math.exp(1 - (I - ICut) / IOpt));
        sargassumLayer.setI_env(I);

        /** Limitation due to salinity */
        double S = getSimulationManager().getDataset().get(S_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double salinity_limitation = Math.min(1., Math.max(0., alphaS * S + betaS));
        sargassumLayer.setS_env(S);

        /** Mortality factor due to wind */
        double V = getSimulationManager().getDataset().get(V_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double U = getSimulationManager().getDataset().get(U_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double W = Math.sqrt(U * U + V * V);
        double alphaWind = 1 - 1 / (1 + Math.exp(1.5 * (W - 5)));
        sargassumLayer.setW_env(W);

        /** C uptake and loss */
        double C = sargassumLayer.getC();
        double uptakeC = C * maxUptakeC * temp_limitation * nutrient_limitation * solar_limitation * salinity_limitation;
        double lossC = C * C * mortality * C / (C + half_mortality) * (1 + alphaWind * 3);

        /** N and P uptakes and losses */
        double N_concentration = getSimulationManager().getDataset().get(NH4_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        N_concentration += getSimulationManager().getDataset().get(NO3_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        sargassumLayer.setN_env(N_concentration);
        double uptakeN = uptakeVelocityN * C * N_concentration / (saturationN + N_concentration) * (maxQuotaN - quotaN) / (maxQuotaN - minQuotaN);
        double lossN = lossC * quotaN;

        double P_concentration = getSimulationManager().getDataset().get(PO4_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        sargassumLayer.setP_env(P_concentration);
        double uptakeP = uptakeVelocityP * C * P_concentration / (saturationP + P_concentration) * (maxQuotaP - quotaP) / (maxQuotaP - minQuotaP);
        double lossP = lossC * quotaP;
        double dt = (double)getSimulationManager().getTimeManager().get_dt()/ (24 * 3600);
        sargassumLayer.setC(C + (uptakeC - lossC) * dt);
        sargassumLayer.setN(sargassumLayer.getN() + (uptakeN - lossN) * dt);
        sargassumLayer.setP(sargassumLayer.getP() + (uptakeP - lossP) * dt);
        sargassumLayer.updateBiomass();

    }

}
