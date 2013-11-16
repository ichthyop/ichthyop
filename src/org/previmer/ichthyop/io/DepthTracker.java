/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.arch.IParticle;

/**
 *
 * @author pverley
 */
public class DepthTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        return (float) particle.getDepth();
    }
}
