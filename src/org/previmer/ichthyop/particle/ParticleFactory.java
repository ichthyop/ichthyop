/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IMasterParticle;
import org.previmer.ichthyop.arch.IZoneParticle;

/**
 *
 * @author pverley
 */
public class ParticleFactory {

    public static IBasicParticle createParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) {

        IMasterParticle particle = new MasterParticle();
        particle.setIndex(index);
        boolean living = mortality.equals(ParticleMortality.ALIVE);
        if (living) {
            particle.setLon(lon);
            particle.setLat(lat);
            particle.setDepth(depth);
            if (Double.isNaN(depth)) {
                particle.make2D();
            }
            particle.geo2Grid();
        } else {
            particle.kill(mortality);
        }
        return particle;
    }

    public static IBasicParticle createParticle(int index, double lon, double lat, double depth) {
        return createParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public static IBasicParticle createParticle(int index, double lon, double lat) {
        return createParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public static IBasicParticle createParticle(int index, double xmin, double xmax, double ymin, double ymax, double upDepth, double lowDepth) {

        int DROP_MAX = 2000;
        /** Constructs a new Particle */
        IMasterParticle particle = new MasterParticle();
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
                //System.out.println("x " + x + " y " + y + " depth " + depth);
                particle.setDepth(depth);
                particle.geo2Grid();
            }
            //System.out.println("x " + x + " y " + y + " depth " + depth);
            //System.out.println("water " + particle.isInWater() + " edge " + particle.isOnEdge());
            //System.out.println(index + " - num zone " + ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE));
            int numReleaseZone = ((IZoneParticle) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
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
}
