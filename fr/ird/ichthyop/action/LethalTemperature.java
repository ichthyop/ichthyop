/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.util.Constant;
import fr.ird.ichthyop.arch.IGrowingParticle;
import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.particle.GrowingParticle;

/**
 *
 * @author pverley
 */
public class LethalTemperature extends AbstractAction {

    private float lethal_tp, lethalTpEgg, lethalTpLarva;
    private boolean FLAG_GROWTH;
    private int egg;

    public void loadParameters() {

        FLAG_GROWTH = getSimulation().getActionManager().getAction(getProperty("action.key")).isEnabled();
        if (!FLAG_GROWTH) {
            lethal_tp = Float.valueOf(getParameter("lethal.temperature"));
        } else {
            lethalTpEgg = Float.valueOf(getParameter("lethal.temperature.egg"));
            lethalTpLarva = Float.valueOf(getParameter("lethal.temperature.larva"));
            egg = Integer.valueOf(getSimulation().getParameterManager(GrowingParticle.class).getProperty("stage.egg.code"));
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
        double temperature = getSimulation().getDataset().getTemperature(particle.getGridPoint(), getSimulation().getStep().getTime());
        if (temperature < lethal_tp) {
            particle.kill(Constant.DEAD_COLD);
        }
    }

    private void checkTp(IGrowingParticle particle) {
        double temperature = getSimulation().getDataset().getTemperature(particle.getGridPoint(), getSimulation().getStep().getTime());
        boolean frozen = ((particle.getStage() == egg) && (temperature < lethalTpEgg)) || ((particle.getStage() > egg) && (temperature < lethalTpLarva));
        if (frozen) {
            particle.kill(Constant.DEAD_COLD);
        }
    }
}
