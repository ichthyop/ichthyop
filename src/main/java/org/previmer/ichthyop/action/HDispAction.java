/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.MTRandom;
import org.previmer.ichthyop.particle.IParticle;

/**
 * Simulates horizontal dispersion.
 *
 * <pre>
 * Ur = R * sqrt(2 * Kh / dt)
 * With R a real uniform random number in [-1; 1]
 * and Kh the Lagrangian horizontal diffusion of the form
 * Kh = pow(epsilon, 1/3) * pow(l, 4 / 3)
 * where l is the unresolved subgrid scale and epsilon the turbulent
 * dissipation rate.
 *
 * In this case, R is generated by a Mersenne Twister pseudo random
 * number generator, epsilon = 1e-9 m2/s3 and l is taken as the cell size.
 *
 * Therefore the move of the particle due to horizontal dispersion is
 * dX = R * sqrt(2 * Kh * dt)
 * </pre>
 *
 * <p>Copyright: Copyright (c) 2007-2011. Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class HDispAction extends AbstractAction {

    /**
     * Turbulent dissipation rate used in the parametrization of Lagrangian
     * horizontal diffusion.
     * @see Monin and Ozmidov, 1981
     */
    private static double epsilon;// = 1e-9;
    /**
     * epsilon16 = epsilon ^ (1/6)
     */
    private static double epsilon16;
    /**
     * Mersenne Twister pseudo random number generator
     */
    private MTRandom random;

    @Override
    public void loadParameters() throws Exception {
        random = new MTRandom();
        epsilon = Double.valueOf(getParameter("epsilon"));
        epsilon16 = Math.pow(epsilon, 1.d / 6.d);
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {
        particle.increment(getHDispersion(particle.getGridCoordinates(), getSimulationManager().getTimeManager().get_dt()));
    }

    /**
     * Generates an horizontal random move.
     *
     * @param pGrid, current particle grid coordinates
     * @param dt, time-step
     * @return a double[] {dx, dy}, the horizontal random move.
     */
    public double[] getHDispersion(double[] pGrid, double dt) {

        /*
         * Get the current cell (i, j) where the particle is located
         */
        int i = (int) Math.round(pGrid[0]), j = (int) Math.round(pGrid[1]);
        int nx = getSimulationManager().getDataset().get_nx();
        int ny = getSimulationManager().getDataset().get_ny();
        i = Math.max(Math.min(i, nx - 1), 0);
        j = Math.max(Math.min(j, ny - 1), 0);

        /*
         * Generates a random move and checks whether the move will take the
         * particle inland.
         * After five unsuccessful attempts, returns dx = dy = 0
         * This precaution should minimize the beaching of the particle moving
         * close by the coastline.
         */
        for (int n = 0; n < 5; n++) {
            double[] rMove = randomMove(i, j, dt);
            double[] rPos = (pGrid.length > 2)
                    ? new double[]{pGrid[0] + rMove[0], pGrid[1] + rMove[1], pGrid[2]}
                    : new double[]{pGrid[0] + rMove[0], pGrid[1] + rMove[1]};
            if (getSimulationManager().getDataset().isInWater(rPos)) {
                return new double[]{rMove[0], rMove[1]};
            }
        }
        return new double[]{0.d, 0.d};
    }

    /**
     * Generate an adimensionalized random move in cell (i, j)
     *
     * @param i, an Integer, the i-coordinate of the cell
     * @param j, an Integer, the j-coordinate of the cell
     * @param dt, a double, the time-step of the model
     * @return a double[] {dx, dy}, the horizontal random move.
     */
    private double[] randomMove(int i, int j, double dt) {
        double Rx = 2.d * random.nextDouble() - 1.d;
        double Ry = 2.d * random.nextDouble() - 1.d;
        double dL = 0.5d * (getSimulationManager().getDataset().getdxi(j, i) + getSimulationManager().getDataset().getdeta(j, i));
        // abs(dt) because it is negative integer in backward simulation
        double cff = Math.sqrt(2.d * Math.abs(dt)) * epsilon16 * Math.pow(dL, 2.d / 3.d);
        double dx = Rx * cff / getSimulationManager().getDataset().getdxi(j, i);
        double dy = Ry * cff / getSimulationManager().getDataset().getdeta(j, i);
        return new double[]{dx, dy};
    }
}
