package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IMasterParticle;
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
            getLogger().warning("Defauly coastline behavior set as " + coastlineBehavior.name() + " since the parameter could not be found in the configuration file.");
        }
    }

    @Override
    public void execute(IMasterParticle particle) {
        if (!particle.isLocked()) {
            checkCoastlineAndMove(particle);
            if (particle.isOnEdge()) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            }
            particle.grid2Geo();
        }
        particle.unlock();
    }

    /*
     * Implements specific behaviours in case the current move take the particle
     * inland.
     */
    private void checkCoastlineAndMove(IMasterParticle particle) {

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
            double ys = y;
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
