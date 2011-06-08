/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.BlockType;

/**
 *
 * @author pverley
 */
public class IniStageLayer extends ParticleLayer {

    private double spawningTemperature;

    public IniStageLayer(IBasicParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        String temperature_field = getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "option.biology_dataset", "temperature_field");
        double time = getSimulationManager().getTimeManager().get_tO();
        spawningTemperature = getSimulationManager().getDataset().get(temperature_field, particle().getGridCoordinates(), time).doubleValue();
    }

    public double getSpawingTemperature() {
        return spawningTemperature;
    }
}
