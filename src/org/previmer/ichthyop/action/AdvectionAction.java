/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import java.util.logging.Level;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class AdvectionAction extends AbstractAction {

    private boolean isEuler;
    private boolean isForward;
    private boolean horizontal;
    private boolean vertical;
    // Threshold for CFL error message
    public static final float THRESHOLD_CFL = 1.0f;

    @Override
    public void loadParameters() throws Exception {

        /* numerical scheme */
        try {
            isEuler = getParameter("scheme").equals(AdvectionScheme.FORWARD_EULER.getName());
        } catch (Exception ex) {
            /*  set RK4 as default in case could not determine the scheme
             * defined by user.
             */
            isEuler = false;
            // print the info in the log
            getLogger().log(Level.INFO, "Failed to read the advection numerical scheme. Set by default to {0}", AdvectionScheme.RUNGE_KUTTA_4.getName());
        }

        /* time direction */
        isForward = getSimulationManager().getTimeManager().get_dt() >= 0;

        /* Horizontal advection enabled ? */
        try {
            horizontal = Boolean.valueOf(getParameter("horizontal"));
        } catch (Exception ex) {
            horizontal = true;
        }

        /* Vertical advection enabled ? */
        try {
            vertical = Boolean.valueOf(getParameter("vertical"));
        } catch (Exception ex) {
            vertical = true;
        }
    }
    
    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {       
        if (isForward) {
            advectForward(particle, getSimulationManager().getTimeManager().getTime());
        } else {
            advectBackward(particle, getSimulationManager().getTimeManager().getTime());
        } 
    }

    private void advectForward(IParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = isEuler
                ? advectEuler(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt())
                : advectRk4(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt());
        //Logger.getAnonymousLogger().info("dx " + mvt[0] + " dy " + mvt[1] + " dz " + mvt[2]);
        if (!horizontal) {
            mvt[0] = 0;
            mvt[1] = 0;
        }
        if (!vertical && mvt.length > 2) {
            mvt[2] = 0;
        }
        particle.increment(mvt);
    }

    private double[] advectEuler(double pGrid[], double time, double dt) {

        int dim = pGrid.length;
        double[] dU = new double[dim];

        dU[0] = getSimulationManager().getDataset().get_dUx(pGrid, time) * dt;
        if (Math.abs(dU[0]) > THRESHOLD_CFL) {
            getLogger().log(Level.WARNING, "CFL broken for U {0}", (float) dU[0]);
        }       
        dU[1] = getSimulationManager().getDataset().get_dVy(pGrid, time) * dt;
        if (Math.abs(dU[1]) > THRESHOLD_CFL) {
            getLogger().log(Level.WARNING, "CFL broken for V {0}", (float) dU[1]);
        }       
        if (dim > 2) {
            dU[2] = getSimulationManager().getDataset().get_dWz(pGrid, time) * dt;
            if (Math.abs(dU[2]) > THRESHOLD_CFL) {
                getLogger().log(Level.WARNING, "CFL broken for W {0}", (float) dU[2]);
            }            
        }
           
        return dU;
    }

    /**
     * Advects the particle forward in time with the apropriate scheme (Euler
     * or Runge Kutta 4).
     * The process is a bit more complex than forward advection.
     * <pre>
     * Let's take X(t) = |x, y, z the particle vector position at time = t.
     * X1(t - dt) = X(t) - Ua(t, x, y, z)dt with vector X1 = |x1, y1, z1
     * X(t - dt) = X(t) - Ua(t, x1, y1, z1)dt
     * With Ua the input model velocity vector.
     * </pre>
     */
    private void advectBackward(IParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;
        double dt = getSimulationManager().getTimeManager().get_dt();

        if (isEuler) {
            mvt = advectEuler(pgrid = particle.getGridCoordinates(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            if (getSimulationManager().getDataset().isOnEdge(pgrid)) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            mvt = advectEuler(pgrid, time, dt);
        } else {           
            mvt = advectRk4(pgrid = particle.getGridCoordinates(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            if (getSimulationManager().getDataset().isOnEdge(pgrid)) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            mvt = advectRk4(pgrid, time, dt);
        }
        
        if (!horizontal) {
            mvt[0] = 0;
            mvt[1] = 0;
        }
        if (!vertical && mvt.length > 2) {
            mvt[2] = 0;
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
            return (dim > 2) 
                    ? new double[]{.5d * k1[0], .5d * k1[1], 0}
                    : new double[]{.5d * k1[0], .5d * k1[1]};
        }

        double[] k2 = advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k2[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return (dim > 2)
                    ? new double[]{.5d * k2[0], .5d * k2[1], 0}
                    : new double[]{.5d * k2[0], .5d * k2[1]};
        }

        double[] k3 = advectEuler(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + k3[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return (dim > 2)
                    ? new double[]{k3[0], k3[1], 0}
                    : new double[]{k3[0], k3[1]};
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
