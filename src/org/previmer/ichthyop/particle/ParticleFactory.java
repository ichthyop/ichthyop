/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;

/**
 *
 * @author pverley
 */
public class ParticleFactory extends SimulationManagerAccessor {

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) {

        Particle particle = new Particle();
        particle.setIndex(index);
        boolean living = mortality.equals(ParticleMortality.ALIVE);

        particle.setLon(lon);
        particle.setLat(lat);
        particle.setDepth(depth);
        if (Double.isNaN(depth)) {
            particle.make2D();
        }
        particle.geo2Grid();
        if (living) {
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

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth) {
        return createGeoParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public static IParticle createGeoParticle(int index, double lon, double lat) {
        return createGeoParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public static IParticle createSurfaceParticle(int index, double x, double y, boolean is3D) {
        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        if (is3D) {
            particle.setZ(getSimulationManager().getDataset().get_nz() - 1);
        } else {
            particle.make2D();
        }
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        particle.grid2Geo();
        return particle;
    }

    public static IParticle createZoneParticle(int index, double x, double y, double depth) {
        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        particle.setDepth(depth);
        /* bugfixing 2011/06/28
         * setDepth but z unset and then calling isInWater ==> crash
         * phv 2011/09/25: wondering wether the make2D should not occur before
         * the geo2grid, to be checked...
         */
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
        int numReleaseZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
        if (numReleaseZone == -1) {
            return null;
        }
        particle.grid2Geo();
        particle.geo2Grid();
        return particle;
    }

    public static IParticle createBottomParticle(int index, double x, double y) {

        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        /*
         * Make sure the particle is released at the bottom, ie z = 0
         */
        particle.setZ(0);
        /*
         * Test wether the grid point is on land or at the edge of the domain
         */
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        /*
         * Test wether the grid point is inside one of the release zones
         */
        int numReleaseZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
        if (numReleaseZone == -1) {
            return null;
        }
        /*
         * Converts (x, y, z) into (lon, lat, depth)
         */
        particle.grid2Geo();
        return particle;
    }
}
