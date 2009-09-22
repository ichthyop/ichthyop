/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.particle.Iv2Particle;

/**
 *
 * @author pverley
 */
public class ParticleFactory {

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
}
