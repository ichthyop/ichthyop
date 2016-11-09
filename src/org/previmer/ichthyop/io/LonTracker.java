/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public class LonTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        float lon = (float) particle.getLon();
        return lon > 180 ? lon - 360.f : lon;
    }
}
