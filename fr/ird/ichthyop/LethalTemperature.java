/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class LethalTemperature extends AbstractAction {

    private float lethal_tp, lethalTpEgg, lethalTpLarva;
    private boolean FLAG_GROWTH;
    private static int EGG;

    public void loadParameters() {

        FLAG_GROWTH = getSimulation().getActionManager().getAction("growth").isEnabled();
        if (!FLAG_GROWTH) {
            lethal_tp = Float.valueOf(getParameter("lethal.temperature"));
        } else {
            lethalTpEgg = Float.valueOf(getParameter("lethal.temperature.egg"));
            lethalTpLarva = Float.valueOf(getParameter("lethal.temperature.larva"));
            EGG = 0;
        }
    }

    public void execute(IBasicParticle particle) {

        if (FLAG_GROWTH) {
        } else {
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
        boolean frozen = ((particle.getStage() == EGG) && (temperature < lethalTpEgg)) || ((particle.getStage() > EGG) && (temperature < lethalTpLarva));
        if (frozen) {
            particle.kill(Constant.DEAD_COLD);
        }
    }
}
