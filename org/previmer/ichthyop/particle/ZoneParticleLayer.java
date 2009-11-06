/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IZoneParticle;
import java.util.Iterator;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class ZoneParticleLayer extends ParticleLayer implements IZoneParticle {

    public ZoneParticleLayer(IBasicParticle particle) {
        super(particle);
    }

    public int getNumZone(TypeZone type) {
        int nZone = -1;
        boolean foundZone = false;
        Iterator iter = getSimulationManager().getZoneManager().getZones(type).iterator();
        while (!foundZone && iter.hasNext()) {
            Zone znTmp = (Zone) iter.next();
            if (znTmp.isPointInZone(particle())) {
                nZone = znTmp.getIndex();
                foundZone = true;
            }
        }
        return nZone;
    }

    @Override
    public void init() {
        // nothing to do
    }
}
