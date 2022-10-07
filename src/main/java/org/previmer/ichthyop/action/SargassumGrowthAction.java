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

import org.previmer.ichthyop.io.SargassumBiomassTracker;
import org.previmer.ichthyop.io.SargassumCarbonTracker;
import org.previmer.ichthyop.io.SargassumNitrogenTracker;
import org.previmer.ichthyop.io.SargassumPhosphorTracker;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.SargassumParticleLayer;

/**
 *
 * @author Witold Podlejski
 */
public class SargassumGrowthAction extends AbstractAction {

    /** Name of the temperature variable to use in the module */
    private String temperature_field;

    /** The three state variables, Carbon, Nitrogen and Phosphorus content.*/
    private double C,N,P;

    /** The Nitrogen and Phosphorus quotas corresponding to N/C and P/C.*/
    private double quotaN,quotaP;

    /** The Nitrogen and Phosphorus quotas maxima and minima.*/
    private double minQuotaN,minQuotaP,maxQuotaN,maxQuotaP;

    /** The temperature parameters.*/
    private double minT, maxT, optT;

    /** The uptake rates for C, n, P.*/
    private double uptakeC, uptakeN, uptakeP;

    /** Half saturation term for nitrogen and phosphorous absorption */
    private double saturationN, saturationP;

    /** Maximum uptake velocity for nitrogen and phosphorous absorption */
    private double uptakeVelocityN, uptakeVelocityP;

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

        /** Loading parameters */

        minQuotaN = Double.parseDouble(getParameter("minimum_quota_nitrogen"));
        maxQuotaN = Double.parseDouble(getParameter("maximum_quota_nitrogen"));
        minQuotaP = Double.parseDouble(getParameter("minimum_quota_phosphor"));
        maxQuotaP = Double.parseDouble(getParameter("maximum_quota_phosphor"));
        quotaN = (maxQuotaN - minQuotaN) /2;
        quotaP = (maxQuotaP - minQuotaP) /2;

        maxT = Double.parseDouble(getParameter("maximum_temperature"));
        minT = Double.parseDouble(getParameter("minimum_temperature"));
        optT = Double.parseDouble(getParameter("optimal_temperature"));

        uptakeC = Double.parseDouble(getParameter("uptake_carbon"));
        uptakeN = Double.parseDouble(getParameter("uptake_nitrogen"));
        uptakeP = Double.parseDouble(getParameter("uptake_phosphor"));
        saturationN = Double.parseDouble(getParameter("half_saturation_nitrogen"));
        saturationP = Double.parseDouble(getParameter("half_saturation_phosphor"));

//        getSimulationManager().getOutputManager().addPredefinedTracker(SargassumDensityTracker.class);
//        temperature_field = getParameter("temperature_field");
//        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
//        food_field = getParameter("food_field");
//        getSimulationManager().getDataset().requireVariable(food_field, getClass());
//
    }

    @Override
    public void init(IParticle particle) {        
        SargassumParticleLayer sargassumLayer = (SargassumParticleLayer) particle.getLayer(SargassumParticleLayer.class);
        sargassumLayer.setN(sargassumLayer.getC() * quotaN);
        sargassumLayer.setP(sargassumLayer.getC() * quotaP);
    }

    @Override
    public void execute(IParticle particle) {
       // TODO
        // recuperqtion champ grille U/V double temp = getSimulationManager().getDataset().get(temperature_field, debLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        // recuperation grille vent: cf. WindDriftFileAction
    }

}
