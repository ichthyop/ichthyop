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

import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.SoleParticleLayer;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class SoleGrowthAction extends AbstractAction {

    private String temperature_field;
    private double dt;

    @Override
    public void loadParameters() throws Exception {
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        dt = (double) getSimulationManager().getTimeManager().get_dt() / Constant.ONE_DAY;
    }

    @Override
    public void execute(IParticle particle) {
        SoleParticleLayer sole = (SoleParticleLayer) particle.getLayer(SoleParticleLayer.class);
        double temp = getSimulationManager().getDataset().get(temperature_field, sole.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double dlength = grow(sole.getStage(), temp);
        sole.setLength(sole.getLength() + dlength);
    }

    private double grow(SoleParticleLayer.Stage stage, double temperature) {

        double dlength = 0;
        switch (stage) {
            case EGG:
                dlength = 0.0066 * Math.pow(temperature, 1.5739) * dt;
                break;
            case YOLK_SAC_LARVA:
                dlength = 0.0073 * Math.pow(temperature, 1.4619) * dt;
                break;
            case FEEDING_LARVA:
                dlength = 0.0011 * Math.pow(temperature, 1.9316) * dt;
                break;
            case METAMORPHOSING_LARVA:
                dlength = 0.0017 * Math.pow(temperature, 1.9316) * dt;
                break;
        }
        return dlength;
    }

}
