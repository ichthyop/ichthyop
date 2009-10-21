/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.particle;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IZoneParticle;
import java.util.Iterator;

/**
 *
 * @author pverley
 */
public abstract class ZoneParticle extends BasicParticle implements IZoneParticle {

    public int getNumZone(TypeZone type) {
        int nZone = -1;
        boolean foundZone = false;
        Iterator iter = getSimulationManager().getZoneManager().getZones(type).iterator();
        while (!foundZone && iter.hasNext()) {
            Zone znTmp = (Zone) iter.next();
            if (znTmp.isPointInZone(this)) {
                nZone = znTmp.getIndex();
                foundZone = true;
            }
        }
        return nZone;
    }
}
