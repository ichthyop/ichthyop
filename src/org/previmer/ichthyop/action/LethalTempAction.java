/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IGrowingParticle.Stage;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class LethalTempAction extends AbstractAction {

    private float lethal_tp, lethalTpEgg, lethalTpLarva;
    private boolean FLAG_GROWTH;
    private String temperature_field;

    public void loadParameters() throws Exception {

        FLAG_GROWTH = getSimulationManager().getActionManager().isEnabled("action.growth");
        temperature_field = getParameter("temperature_field");
        if (!FLAG_GROWTH) {
            lethal_tp = Float.valueOf(getParameter("lethal_temperature_egg"));
        } else {
            lethalTpEgg = Float.valueOf(getParameter("lethal_temperature_egg"));
            lethalTpLarva = Float.valueOf(getParameter("lethal_temperature_larva"));
        }
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("temp_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addCustomTracker(temperature_field);
        }
    }

    public void execute(IBasicParticle particle) {

        if (FLAG_GROWTH) {
            checkTpGrowingParticle(particle);
        } else {
            checkTp(particle);
        }

    }

    private void checkTp(IBasicParticle particle) {
        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        if (temperature < lethal_tp) {
            particle.kill(ParticleMortality.DEAD_COLD);
        }
    }

    private void checkTpGrowingParticle(IBasicParticle particle) {

        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        Stage stage = ((IGrowingParticle) particle.getLayer(GrowingParticleLayer.class)).getStage();
        boolean frozen = ((stage == Stage.EGG) && (temperature < lethalTpEgg)) || ((stage != Stage.EGG) && (temperature < lethalTpLarva));
        if (frozen) {
            particle.kill(ParticleMortality.DEAD_COLD);
        }
    }
}
