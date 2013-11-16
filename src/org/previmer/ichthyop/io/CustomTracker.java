/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.arch.IParticle;

/**
 *
 * @author pverley
 */
public class CustomTracker extends FloatTracker {

    private final String variableName;

    public CustomTracker(String variableName) {
        this.variableName = variableName;
        getSimulationManager().getDataset().requireVariable(variableName, getClass());
    }

    @Override
    public String getName() {
        return variableName;
    }

    @Override
    float getValue(IParticle particle) {
        return getSimulationManager().getDataset().get(variableName, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).floatValue();
    }
}
