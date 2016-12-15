/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.action;

import java.util.logging.Level;
import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class AdvectionAction extends AbstractAction {

    private boolean isRK4;
    private boolean isForward;
    private boolean horizontal;
    private boolean vertical;
    // Threshold for CFL error message
    public static final float THRESHOLD_CFL = 1.0f;

    @Override
    public void loadParameters() throws Exception {

        /* numerical scheme */
        try {
            isRK4 = getParameter("scheme").equals(AdvectionScheme.RUNGE_KUTTA_4.getName());
        } catch (Exception ex) {
            /*  set RK4 as default in case could not determine the scheme
             * defined by user.
             */
            isRK4 = true;
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
            moveForwardInTime(particle, getSimulationManager().getTimeManager().getTime());
        } else {
            moveBackwardInTime(particle, getSimulationManager().getTimeManager().getTime());
        }
    }

    private void moveForwardInTime(IParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = isRK4
                ? computeMoveRK4(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt())
                : computeMove(particle.getGridCoordinates(), time, getSimulationManager().getTimeManager().get_dt());
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

    private double[] computeMove(double pGrid[], double time, double dt) {

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
     * Advects the particle forward in time with the appropriate scheme (Euler or
     * Runge Kutta 4). The process is a bit more complex than forward advection.
     * <pre>
     * Let's take X(t) = |x, y, z the particle vector position at time = t.
     * X1(t - dt) = X(t) - Ua(t, x, y, z)dt with vector X1 = |x1, y1, z1
     * X(t - dt) = X(t) - Ua(t, x1, y1, z1)dt
     * With Ua the input model velocity vector.
     * </pre>
     */
    private void moveBackwardInTime(IParticle particle, double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;
        double dt = getSimulationManager().getTimeManager().get_dt();

        if (isRK4) {
            mvt = computeMove(pgrid = particle.getGridCoordinates(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            if (getSimulationManager().getDataset().isOnEdge(pgrid)) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            mvt = computeMove(pgrid, time, dt);
        } else {
            mvt = computeMoveRK4(pgrid = particle.getGridCoordinates(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            if (getSimulationManager().getDataset().isOnEdge(pgrid)) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            mvt = computeMoveRK4(pgrid, time, dt);
        }

        if (!horizontal) {
            mvt[0] = 0;
            mvt[1] = 1;
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
    private double[] computeMoveRK4(double[] p0, double time,
            double dt) throws ArrayIndexOutOfBoundsException {

        int dim = p0.length;
        double[] dU = new double[dim];
        double[] pk = new double[dim];

        double[] k1 = computeMove(p0, time, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k1[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return (dim > 2)
                    ? new double[]{.5d * k1[0], .5d * k1[1], 0}
                    : new double[]{.5d * k1[0], .5d * k1[1]};
        }

        double[] k2 = computeMove(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + .5d * k2[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return (dim > 2)
                    ? new double[]{.5d * k2[0], .5d * k2[1], 0}
                    : new double[]{.5d * k2[0], .5d * k2[1]};
        }

        double[] k3 = computeMove(pk, time + dt / 2, dt);

        for (int i = 0; i < dim; i++) {
            pk[i] = p0[i] + k3[i];
        }
        if (getSimulationManager().getDataset().isOnEdge(pk)) {
            return (dim > 2)
                    ? new double[]{k3[0], k3[1], 0}
                    : new double[]{k3[0], k3[1]};
        }

        double[] k4 = computeMove(pk, time + dt, dt);

        for (int i = 0; i < dim; i++) {
            dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
        }

        return (dU);
    }

    public enum AdvectionScheme {

        FORWARD_EULER("euler", "Forward Euler"),
        RUNGE_KUTTA_4("rk4", "Runge Kutta 4");
        private final String key;
        private final String name;

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
