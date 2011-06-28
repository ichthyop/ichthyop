/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IMasterParticle;

/**
 *
 * @author pverley
 */
public class ParticleFactory extends SimulationManagerAccessor {

    public static IBasicParticle createGeoParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) {

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
            if (!particle.isInWater() || particle.isOnEdge()) {
                return null;
            }
            if (!Double.isNaN(depth)) {
                if (getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0) > depth || depth > 0) {
                    return null;
                }
            }
        } else {
            particle.kill(mortality);
        }
        return particle;
    }

    public static IBasicParticle createGeoParticle(int index, double lon, double lat, double depth) {
        return createGeoParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public static IBasicParticle createGeoParticle(int index, double lon, double lat) {
        return createGeoParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public static IBasicParticle createGridParticle(int index, double x, double y, double depth) {
        IMasterParticle particle = new MasterParticle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        particle.setDepth(depth);
        /* bugfixing 2011/06/28
         * setDepth but z unset and then calling isInWater ==> crash
         */
        particle.geo2Grid();
        if (Double.isNaN(depth)) {
            particle.make2D();
        }
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        if (!Double.isNaN(depth)) {
            if (getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0) > depth || depth > 0) {
                return null;
            }
        }
        int numReleaseZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
        if (numReleaseZone == -1) {
            return null;
        }
        particle.grid2Geo();
        particle.geo2Grid();
        return particle;
    }
}
