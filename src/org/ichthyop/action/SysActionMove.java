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

import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.GriddedParticle;
import org.ichthyop.particle.Particle;
import org.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class SysActionMove extends AbstractAction {

    private CoastlineBehavior coastlineBehavior = CoastlineBehavior.BEACHING;

    @Override
    public void loadParameters() throws Exception {
        try {
            coastlineBehavior = CoastlineBehavior.getBehavior(getConfiguration().getString("app.transport.coastline_behavior"));
        } catch (Exception ex) {
            warning("Defauly coastline behavior set as {0} since the parameter could not be found in the configuration file.", coastlineBehavior.name());
        }
    }

    @Override
    public void execute(IParticle particle) {

        if (!particle.isLocked()) {
            checkCoastlineAndMove(particle);
            if (GriddedParticle.isOnEdge(particle)) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
        }
        particle.unlock();
    }

    /*
     * Implements specific behaviours in case the current move take the particle
     * inland.
     */
    private void checkCoastlineAndMove(IParticle particle) {

        double[] move;
        Particle p = (Particle) particle;
        switch (coastlineBehavior) {
            case NONE:
                /* Does nothing, the move might take the
                 * particle inland.
                 */
                p.applyMove();
                GriddedParticle.update(particle);
                break;
            case BEACHING:
                /*
                 * The move might take the particle inland
                 * and lead to a beaching.
                 */
                p.applyMove();
                GriddedParticle.update(particle);
                if (!GriddedParticle.isInWater(particle)) {
                    particle.kill(ParticleMortality.BEACHED);
                }
                break;
            case BOUNCING:
                /*
                 * The particle will act exactly as a billard ball.
                 */
                move = p.getMove();
                double[] xyz = GriddedParticle.xyz(particle);
                double[] nxyz = xyz(particle.getLat() + move[0], particle.getLon() + move[1], particle.getDepth() + move[2]);
                double[] bounce = bounceCostline(xyz[0], xyz[1], nxyz[0] - xyz[0], nxyz[1] - xyz[1]);
                double[] nlatlon = getSimulationManager().getGrid().xy2latlon(xyz[0] + bounce[0], xyz[1] + bounce[1]);
                particle.incrLat(nlatlon[0] - particle.getLat() - move[0]);
                particle.incrLon(nlatlon[1] - particle.getLon() - move[1]);
                break;
            case STANDSTILL:
                /*
                 * The particle will stand still instead of going in land.
                 */
                move = p.getMove();
                xyz = xyz(particle.getLat() + move[0],
                        particle.getLon() + move[1],
                        particle.getDepth() + move[2]);
                if (!getSimulationManager().getGrid().isInWater(xyz)) {
                    particle.incrLat(-move[0]);
                    particle.incrLon(-move[1]);
                    particle.incrDepth(-move[2]);
                }
                p.applyMove();
                GriddedParticle.update(particle);
                break;
        }
    }

    private double[] xyz(double lat, double lon, double depth) {
        double[] xy = getSimulationManager().getGrid().latlon2xy(lat, lon);
        double z = getSimulationManager().getGrid().depth2z(xy[0], xy[1], depth);
        return new double[]{xy[0], xy[1], z};
    }

    private double[] bounceCostline(double x, double y, double dx, double dy) {
        return bounceCostline(x, y, dx, dy, 0);
    }

    private double[] bounceCostline(double x, double y, double dx, double dy, int iter) {

        double newdx = dx;
        double newdy = dy;
        iter += 1;
        if (!getSimulationManager().getGrid().isInWater(new double[]{x + dx, y + dy})) {
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
                signum = (getSimulationManager().getGrid().isInWater(new double[]{s, ys}))
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
            if (!getSimulationManager().getGrid().isInWater(new double[]{x + newdx, y + newdy})) {
                if (iter < 10) {
                    return bounceCostline(x, y, newdx, newdy, iter);
                }
            }
        }
        return new double[]{newdx, newdy};
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    @Override
    public void init(IParticle particle) {
        GriddedParticle.update(particle);
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
