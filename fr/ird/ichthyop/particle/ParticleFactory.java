/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.particle;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.particle.Iv2Particle;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ParticleFactory implements ISimulationAccessor {

    public static IBasicParticle createParticle(int index, double lon, double lat, double depth, boolean living) {

        Iv2Particle particle = new Iv2Particle();
        particle.setIndex(index);
        if (living) {
            particle.setLon(lon);
            particle.setLat(lat);
            particle.setDepth(depth);
            if (Double.isNaN(depth)) {
                particle.make2D();
            }
            particle.geo2Grid();
        } else {
            particle.kill("");
        }
        return particle;
    }

    public static IBasicParticle createParticle(int index, double lon, double lat, double depth) {
        return createParticle(index, lon, lat, depth, true);
    }

    public static IBasicParticle createParticle(int index, double lon, double lat) {
        return createParticle(index, lon, lat, Double.NaN, true);
    }

    public static IBasicParticle createParticle(int index, double xmin, double xmax, double ymin, double ymax, double upDepth, double lowDepth) {

        int DROP_MAX = 2000;
        /** Constructs a new Particle */
        Iv2Particle particle = new Iv2Particle();
        particle.setIndex(index);
        boolean is3D = !Double.isNaN(upDepth);
        if (!is3D) {
            particle.make2D();
        }

        boolean outZone = true;
        double x = 0, y = 0, depth = 0;
        int counter = 0;
        /** Attempts of random release */
        while (outZone) {
            x = xmin + Math.random() * (xmax - xmin);
            y = ymin + Math.random() * (ymax - ymin);
            particle.setX(x);
            particle.setY(y);
            if (is3D) {
                depth = -1.D * (upDepth + Math.random() * (lowDepth - upDepth));
                particle.setDepth(depth);
            }
            //Logger.getAnonymousLogger().info("x " + x + " y " + y + " depth " + depth);
            //Logger.getAnonymousLogger().info("water " + particle.isInWater() + " edge " + particle.isOnEdge());
            //Logger.getAnonymousLogger().info(index + " - num zone " + particle.getNumZone(TypeZone.RELEASE));
            int numReleaseZone = particle.getNumZone(TypeZone.RELEASE);
            outZone = !particle.isInWater() || (numReleaseZone == -1) || particle.isOnEdge();

            if (counter++ > DROP_MAX) {
                throw new NullPointerException("Unable to release particle. Check out the zone definitions.");
            }
        }

        /** initialises */
        particle.geo2Grid();
        particle.grid2Geo();

        return particle;
    }

    public ISimulation getSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
