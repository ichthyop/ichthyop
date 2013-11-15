/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;
import java.util.Iterator;
import org.previmer.ichthyop.arch.IParticle;

/**
 *
 * @author pverley
 */
public class ZoneParticleLayer extends ParticleLayer {

    public ZoneParticleLayer(IParticle particle) {
        super(particle);
    }

    public int getNumZone(TypeZone type) {
        int nZone = -1;
        boolean foundZone = false;
        if (null != getSimulationManager().getZoneManager().getZones(type)) {
            Iterator iter = getSimulationManager().getZoneManager().getZones(type).iterator();
            while (!foundZone && iter.hasNext()) {
                Zone znTmp = (Zone) iter.next();
                if (znTmp.isParticleInZone(particle())) {
                    nZone = znTmp.getIndex();
                    foundZone = true;
                }
            }
        }
        return nZone;
    }

    @Override
    public void init() {
        // nothing to do
    }
}
