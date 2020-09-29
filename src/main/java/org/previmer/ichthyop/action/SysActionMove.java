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

import java.util.logging.Level;
import org.previmer.ichthyop.particle.Particle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class SysActionMove extends AbstractSysAction {

    private CoastlineBehavior coastlineBehavior = CoastlineBehavior.BEACHING;

    @Override
    public void loadParameters() throws Exception {
        try {
            coastlineBehavior = CoastlineBehavior.getBehavior(getParameter("app.transport", "coastline_behavior"));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Defauly coastline behavior set as {0} since the parameter could not be found in the configuration file.", coastlineBehavior.name());
        }
    }

    @Override
    public void execute(Particle particle) {
        if (!particle.isLocked()) {
            checkCoastlineAndMove(particle);
            if (particle.isOnEdge()) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            particle.grid2Geo();
        }
        // barrier.n: remove the unlock, which does seem to be pertinent
        //particle.unlock();
    }

    /*
     * Implements specific behaviours in case the current move take the particle
     * inland.
     */
    private void checkCoastlineAndMove(Particle particle) {

        double[] move;
        switch (coastlineBehavior) {
            case NONE:
                /* Does nothing, the move might take the
                 * particle inland.
                 */
                break;
            case BEACHING:
                /*
                 * The move might take the particle inland
                 * and lead to a beaching.
                 */
                particle.applyMove();
                if (!particle.isInWater()) {
                    particle.kill(ParticleMortality.BEACHED);
                }
                return;
            case BOUNCING:
                /*
                 * The particle will act exactly as a billard ball.
                 */
                move = particle.getMove();
                double[] bounce = bounceCostline(particle.getX(), particle.getY(), move[0], move[1]);
                particle.increment(new double[]{bounce[0] - move[0], bounce[1] - move[1]});
                break;
            case STANDSTILL:
                /*
                 * The particle will stand still instead of going in land.
                 */
                move = particle.getMove();
                if (!getSimulationManager().getDataset().isInWater(new double[]{particle.getX() + move[0], particle.getY() + move[1]})) {
                    particle.increment(new double[]{-1.d * move[0], -1.d * move[1]});
                }
                break;
        }
        particle.applyMove();
    }

    private double[] bounceCostline(double x, double y, double dx, double dy) {
        return bounceCostline(x, y, dx, dy, 0);
    }

    private double[] bounceCostline(double x, double y, double dx, double dy, int iter) {

        double newdx = dx;
        double newdy = dy;
        iter += 1;
        if (!getSimulationManager().getDataset().isInWater(new double[]{x + dx, y + dy})) {
            double s = x;
            double ds = dx;
            double signum = 1.d;
            double ys;
            boolean bounceMeridional = false;
            boolean bounceZonal = false;
            int n = 0;
            /* Iterative process to estimate the point of impact with the
             * costline.
             */
            while (n < 1000 && !(bounceMeridional | bounceZonal)) {
                ds *= 0.5d;
                s = s + signum * ds;
                ys = dy / dx * (s - x) + y;
                signum = (getSimulationManager().getDataset().isInWater(new double[]{s, ys}))
                        ? 1.d
                        : -1.d;
                bounceMeridional = Math.abs(Math.round(s + 0.5d) - (s + 0.5d)) < 1e-8;
                bounceZonal = Math.abs(Math.round(ys + 0.5d) - (ys + 0.5d)) < 1e-8;
                n++;
            }
            /* Compute dx1 such as dx = dx1 + dx2, dx1 in water, dx2 on land
             * or dy1 such as dy = dy1 + dy2, dy1 in water, dy2 on land
             */
            double dx1 = (Math.round(x) + Math.signum(dx) * 0.5d - x);
            double dy1 = (Math.round(y) + Math.signum(dy) * 0.5d - y);
            if (bounceMeridional & bounceZonal) {
                /* case1 : particle hits the cost on a corner */
                newdx = 2.d * dx1 - dx;
                newdy = 2.d * dy1 - dy;
            } else if (bounceMeridional) {
                /* case2: particle hits the meridional cost */
                newdx = 2.d * dx1 - dx;
                newdy = dy;
            } else if (bounceZonal) {
                /* case3: particle hits the zonal cost */
                newdy = 2.d * dy1 - dy;
                newdx = dx;
            }
            /* Ensure the new point is in water and repeat the process otherwise */
            if (!getSimulationManager().getDataset().isInWater(new double[]{x + newdx, y + newdy})) {
                if (iter < 10) {
                    return bounceCostline(x, y, newdx, newdy, iter);
                }
            }
        }
        return new double[]{newdx, newdy};
    }

    public enum CoastlineBehavior {

        NONE,
        BEACHING,
        BOUNCING,
        STANDSTILL;

        public static CoastlineBehavior getBehavior(String s) {
            try {
                for (CoastlineBehavior cb : CoastlineBehavior.values()) {
                    if (cb.name().equalsIgnoreCase(s)) {
                        return cb;
                    }
                }
            } catch (Exception ex) {
                return BEACHING;
            }
            return BEACHING;
        }
    }
}
