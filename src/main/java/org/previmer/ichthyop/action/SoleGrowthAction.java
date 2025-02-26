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

import java.io.IOException;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.AbstractStage;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class SoleGrowthAction extends AbstractAction {

    private String temperature_field;
    private double dt_day;
    private AbstractStage AbstractStage;
    private float[] c1, c2;

    @Override
    public void loadParameters() throws Exception {

        // Request the temperature variable from the hydrodynamic dataset
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());

        // Add the length tracker
        getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);

        // Add the stage tracker
        getSimulationManager().getOutputManager().addPredefinedTracker(StageTracker.class);

        // Time step expressed in day
        dt_day = (double) getSimulationManager().getTimeManager().get_dt() / Constant.ONE_DAY;

        AbstractStage = new AbstractStage(BlockType.ACTION, getBlockKey(), (particle) -> ((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class)).getLength());
        AbstractStage.init();

        // Coefficients of the growth equation
        // dLength(dt) = c1 * (temperature ^ c2) * dt
        c1 = new float[AbstractStage.getNStage()];
        String[] sCoeff = getListParameter("c1");
        if (sCoeff.length != c1.length) {
            throw new IOException("In Sole Growth section, the number of c1 coefficients must be equal to the number of stages.");
        }
        for (int iStage = 0; iStage < c1.length; iStage++) {
            c1[iStage] = Float.parseFloat(sCoeff[iStage]);
        }
        c2 = new float[AbstractStage.getNStage()];
        sCoeff = getListParameter("c2");
        if (sCoeff.length != c2.length) {
            throw new IOException("In Sole Growth section, the number of c2 coefficients must be equal to the number of stages.");
        }
        for (int iStage = 0; iStage < c2.length; iStage++) {
            c2[iStage] = Float.parseFloat(sCoeff[iStage]);
        }
    }

    @Override
    public void init(IParticle particle) {
        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(AbstractStage.getThreshold(0));
    }

    @Override
    public void execute(IParticle particle) {
        LengthParticleLayer sole = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field, sole.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        sole.incrementLength(grow(AbstractStage.getStage(particle), temp));
        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        stageLayer.setStage(AbstractStage.getStage((float) sole.getLength()));
    }

    private double grow(int stage, double temperature) {
        return c1[stage] * Math.pow(temperature, c2[stage]) * dt_day;
    }

}
