/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class AdvectionAction extends AbstractAction {

    private boolean isEuler = true;
    private boolean isForward = true;

    public void loadParameters() {

        try {
            isEuler = getParameter("scheme").matches(AdvectionScheme.FORWARD_EULER.getKey());
        } catch (Exception ex) {
            /*  set RK4 as default in case could not determine the scheme
             * defined by user.
             */
            isEuler = false;
            // print the info in the log
            getSimulationManager().getLogger().info("Failed to read the advection numerical scheme. Set by default to " + AdvectionScheme.RUNGE_KUTTA_4.getName());
        }
        isForward = getSimulationManager().getTimeManager().get_dt() >= 0;
        int n = 3 / 0;
    }

    public void execute(IBasicParticle particle) {
        if (isForward) {
            advectForward(particle, getSimulationManager().getTimeManager().getTime());
        } else {
            advectBackward(particle, getSimulationManager().getTimeManager().getTime());
        }
    }

    private void advectForward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = isEuler
                ? advectEuler(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt())
                : advectRk4(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt());
        //Logger.getAnonymousLogger().info("dx " + mvt[0] + " dy " + mvt[1] + " dz " + mvt[2]);
        particle.increment(mvt);
    }

    private double[] advectEuler(double pGrid[], double time, double dt) {

        int dim = pGrid.length;
        double[] dU = new double[dim];

        dU[0] = getSimulationManager().getDataset().get_dUx(pGrid, time);
        dU[1] = getSimulationManager().getDataset().get_dVy(pGrid, time);
        if (dim > 2) {
            dU[2] = getSimulationManager().getDataset().get_dWz(pGrid, time);
        }

        for (int i = 0; i < dim; i++) {
            dU[i] *= dt;
        }
        return dU;
    }

    private void advectBackward(IBasicParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;
        double dt = getSimulationManager().getTimeManager().get_dt();

        if (isEuler) {
            mvt = advectEuler(pgrid = particle.getGridCoordinates(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = advectEuler(pgrid, time, dt);
        } else {
            mvt = advectRk4(pgrid = particle.getGridCoordinates(), time, dt);
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

        double[] k1 = advectEuler(p0, time, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k1[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return new double[]{.5d * k1[0], .5d * k1[1], 0};
        }

        double[] k2 = advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k2[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return new double[]{.5d * k2[0], .5d * k2[1], 0};
        }

        double[] k3 = advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + k3[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return new double[]{k3[0], k3[1], 0};
        }

        double[] k4 = advectEuler(pk, time + dt, dt);

        for (int i = 0; i < dim; i++) {
            dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
        }

        return (dU);

    }

    public enum AdvectionScheme {

        FORWARD_EULER("euler", "Forward Euler"),
        RUNGE_KUTTA_4("rk4", "Runge Kutta 4");
        private String key;
        private String name;

        AdvectionScheme(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }
    }
}
