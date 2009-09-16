/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Advection extends AbstractAction {

    private boolean isEuler = true;
    private boolean isForward = true;
    private double dt;

    public void loadParameters() {

        dt = Integer.valueOf(getParameter("time.timeStep"));
        isEuler = getParameter("model.scheme").matches(getProperty("model.scheme.euler"));
        isForward = getParameter("time.timeArrow").matches(getProperty("time.timeArrow.forward"));
    }

    public void execute(IBasicParticle particle) {
        if (isForward) {
            advectForward(particle, getSimulation().getTime());
        } else {
            advectBackward(particle, getSimulation().getTime());
        }
    }

    private void advectForward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = isEuler
                ? getSimulation().getDataset().advectEuler(particle.getGridPoint(), time, dt)
                : getSimulation().getDataset().advectRk4(particle.getGridPoint(), time, dt);

        particle.increment(mvt);
    }

    private void advectBackward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;

        if (isEuler) {
            mvt = getSimulation().getDataset().advectEuler(pgrid = particle.getGridPoint(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = getSimulation().getDataset().advectEuler(pgrid, time, dt);
        } else {
            mvt = getSimulation().getDataset().advectRk4(pgrid = particle.getGridPoint(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = getSimulation().getDataset().advectRk4(pgrid, time, dt);
        }

        particle.increment(mvt);
    }
}
