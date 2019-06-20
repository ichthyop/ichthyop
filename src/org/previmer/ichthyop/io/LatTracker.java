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
public class LatTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        return (float) particle.getLat();
    }
}
