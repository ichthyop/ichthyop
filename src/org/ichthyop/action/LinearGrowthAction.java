/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.action;

import org.ichthyop.output.LengthTracker;
import org.ichthyop.output.StageTracker;
import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.LengthParticle;
import org.ichthyop.particle.StageParticle;
import org.ichthyop.stage.LengthStage;
import org.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class LinearGrowthAction extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be be
     * colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d
    private String temperature_field;
    private LengthStage lengthStage;
    
    @Override
    public String getKey() {
        return "action.growth";
    }

    @Override
    public void loadParameters() throws Exception {
        tp_threshold = getConfiguration().getFloat("action.growth.threshold_temp");
        coeff1 = getConfiguration().getFloat("action.growth.coeff1");
        coeff2 = getConfiguration().getFloat("action.growth.coeff2");
        temperature_field = getConfiguration().getString("action.growth.temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        lengthStage = new LengthStage(getKey());
        lengthStage.init();

        if (getConfiguration().getBoolean("action.growth.length_tracker")) {
            getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        }
        
        if (getConfiguration().getBoolean("action.growth.stage_tracker")) {
            getSimulationManager().getOutputManager().addPredefinedTracker(StageTracker.class);
        }
    }

    @Override
    public void init(IParticle particle) {
        LengthParticle.setLength(particle, lengthStage.getThreshold(0));
        StageParticle.init(particle);
    }

    @Override
    public void execute(IParticle particle) {
        LengthParticle.incrementLength(particle, grow(getSimulationManager().getDataset().getVariable(temperature_field).getDouble(particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime())));
        StageParticle.setStage(particle, lengthStage.getStage(LengthParticle.getLength(particle)));
    }

    private double grow(double temperature) {

        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
        return (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;

    }
}
