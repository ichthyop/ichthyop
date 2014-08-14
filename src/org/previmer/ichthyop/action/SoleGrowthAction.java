/*
 * Copyright (C) 2014 pverley
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

import java.io.IOException;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.LengthStage;
import org.previmer.ichthyop.util.Constant;

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
        temperature_field = getParameter("temperature_field");
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
        String[] sCoeff = getListParameter("c1");
        if (sCoeff.length != c1.length) {
            throw new IOException("In Sole Growth section, the number of c1 coefficients must be equal to the number of stages.");
        }
        for (int iStage = 0; iStage < c1.length; iStage++) {
            c1[iStage] = Float.parseFloat(sCoeff[iStage]);
        }
        c2 = new float[lengthStage.getNStage()];
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
        lengthLayer.setLength(lengthStage.getThreshold(0));
    }

    @Override
    public void execute(IParticle particle) {
        LengthParticleLayer sole = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field, sole.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        sole.incrementLength(grow(lengthStage.getStage(particle), temp));
        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        stageLayer.setStage(lengthStage.getStage((float) sole.getLength()));
    }

    private double grow(int stage, double temperature) {
        return c1[stage] * Math.pow(temperature, c2[stage]) * dt_day;
    }

}
