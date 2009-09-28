/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class Advection extends AbstractAction {

    private boolean isEuler = true;
    private boolean isForward = true;

    public void loadParameters() {

        isEuler = getParameter("numerical_scheme").matches("Euler");
        isForward = getSimulation().getStep().get_dt() < 0;
    }

    public void execute(IBasicParticle particle) {
        if (isForward) {
            advectForward(particle, getSimulation().getStep().getTime());
        } else {
            advectBackward(particle, getSimulation().getStep().getTime());
        }
    }

    private void advectForward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = isEuler
                ? getSimulation().getDataset().advectEuler(particle.getGridPoint(), time, getSimulation().getStep().get_dt())
                : advectRk4(particle.getGridPoint(), time, getSimulation().getStep().get_dt());

        particle.increment(mvt);
    }

    private void advectBackward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;
        double dt = getSimulation().getStep().get_dt();

        if (isEuler) {
            mvt = getSimulation().getDataset().advectEuler(pgrid = particle.getGridPoint(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = getSimulation().getDataset().advectEuler(pgrid, time, dt);
        } else {
            mvt = advectRk4(pgrid = particle.getGridPoint(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = advectRk4(pgrid, time, dt);
        }

        particle.increment(mvt);
    }

    /**
     * Advects the particle with the NetCDF dataset velocity field, using a
     * Runge Kutta 4th order scheme.
     *
     * @param p0 a double[] grid coordinates (x, y, z) of the particle.
     * @param time a double, the current time [second] of the simulation
     * @param dt a double, the time step [second] of the simulation.
     * @return a double[], the move of the particle on the grid (dx, dy, dz)
     * @throws an ArrayIndexOutOfBoundsException if the particle is out of the
     * domain.
     */
    private double[] advectRk4(double[] p0, double time,
            double dt) throws ArrayIndexOutOfBoundsException {

        int dim = p0.length;
        double[] dU = new double[dim];
        double[] pk = new double[dim];

        double[] k1 = getSimulation().getDataset().advectEuler(p0, time, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k1[i];
        }
        if (getSimulation().getDataset().isOnEdge(pk)) {
            return new double[]{.5d * k1[0], .5d * k1[1], 0};
        }

        double[] k2 = getSimulation().getDataset().advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k2[i];
        }
        if (getSimulation().getDataset().isOnEdge(pk)) {
            return new double[]{.5d * k2[0], .5d * k2[1], 0};
        }

        double[] k3 = getSimulation().getDataset().advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + k3[i];
        }
        if (getSimulation().getDataset().isOnEdge(pk)) {
            return new double[]{k3[0], k3[1], 0};
        }

        double[] k4 = getSimulation().getDataset().advectEuler(pk, time + dt, dt);

        for (int i = 0; i < dim; i++) {
            dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
        }

        return (dU);

    }
}
