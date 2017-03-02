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

import java.io.IOException;
import org.ichthyop.io.BlockType;
import org.ichthyop.io.LengthTracker;
import org.ichthyop.io.StageTracker;
import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.LengthParticle;
import org.ichthyop.particle.StageParticle;
import org.ichthyop.stage.LengthStage;
import org.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class SoleGrowthAction extends AbstractAction {

    private String temperature_field;
    private double dt_day;
    private LengthStage lengthStage;
    private float[] c1, c2;

    @Override
    public void loadParameters() throws Exception {

        // Request the temperature variable from the hydrodynamic dataset
        temperature_field = getConfiguration().getString("action.growth.sole.temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());

        // Add the length tracker
        getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        
        // Add the stage tracker
        getSimulationManager().getOutputManager().addPredefinedTracker(StageTracker.class);

        // Time step expressed in day
        dt_day = (double) getSimulationManager().getTimeManager().get_dt() / Constant.ONE_DAY;

        // Pre-defined stages of the sole larva
        lengthStage = new LengthStage(BlockType.ACTION, getBlockKey());
        lengthStage.init();

        // Coefficients of the growth equation
        // dLength(dt) = c1 * (temperature ^ c2) * dt
        c1 = new float[lengthStage.getNStage()];
        String[] sCoeff = getConfiguration().getArrayString("action.growth.sole.c1");
        if (sCoeff.length != c1.length) {
            throw new IOException("In Sole Growth section, the number of c1 coefficients must be equal to the number of stages.");
        }
        for (int iStage = 0; iStage < c1.length; iStage++) {
            c1[iStage] = Float.parseFloat(sCoeff[iStage]);
        }
        c2 = new float[lengthStage.getNStage()];
        sCoeff = getConfiguration().getArrayString("action.growth.sole.c2");
        if (sCoeff.length != c2.length) {
            throw new IOException("In Sole Growth section, the number of c2 coefficients must be equal to the number of stages.");
        }
        for (int iStage = 0; iStage < c2.length; iStage++) {
            c2[iStage] = Float.parseFloat(sCoeff[iStage]);
        }
    }

    @Override
    public void init(IParticle particle) {
        LengthParticle.setLength(particle, lengthStage.getThreshold(0));
    }

    @Override
    public void execute(IParticle particle) {
        double temp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        LengthParticle.incrementLength(particle, grow(lengthStage.getStage(particle), temp));
        StageParticle.setStage(particle, lengthStage.getStage(LengthParticle.getLength(particle)));
    }

    private double grow(int stage, double temperature) {
        return c1[stage] * Math.pow(temperature, c2[stage]) * dt_day;
    }

}
