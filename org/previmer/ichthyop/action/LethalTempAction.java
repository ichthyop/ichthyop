/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.particle.GrowingParticle;

/**
 *
 * @author pverley
 */
public class LethalTempAction extends AbstractAction {

    private float lethal_tp, lethalTpEgg, lethalTpLarva;
    private boolean FLAG_GROWTH;
    private int egg;

    public void loadParameters() {

        FLAG_GROWTH = getSimulationManager().getActionManager().isEnabled("action.growth");
        if (!FLAG_GROWTH) {
            lethal_tp = Float.valueOf(getParameter("lethal.temperature"));
        } else {
            lethalTpEgg = Float.valueOf(getParameter("lethal.temperature.egg"));
            lethalTpLarva = Float.valueOf(getParameter("lethal.temperature.larva"));
            egg = Integer.valueOf(getSimulationManager().getPropertyManager(GrowingParticle.class).getProperty("stage.egg.code"));
        }
    }

    public void execute(IBasicParticle particle) {

        if (FLAG_GROWTH) {
            checkTp((IGrowingParticle)particle);
        } else {
            checkTp(particle);
        }

    }

    private void checkTp(IBasicParticle particle) {
        double temperature = getSimulationManager().getDataset().getTemperature(particle.getGridPoint(), getSimulationManager().getTimeManager().getTime());
        if (temperature < lethal_tp) {
            particle.kill(Constant.DEAD_COLD);
        }
    }

    private void checkTp(IGrowingParticle particle) {
        double temperature = getSimulationManager().getDataset().getTemperature(particle.getGridPoint(), getSimulationManager().getTimeManager().getTime());
        boolean frozen = ((particle.getStage() == egg) && (temperature < lethalTpEgg)) || ((particle.getStage() > egg) && (temperature < lethalTpLarva));
        if (frozen) {
            particle.kill(Constant.DEAD_COLD);
        }
    }
}
