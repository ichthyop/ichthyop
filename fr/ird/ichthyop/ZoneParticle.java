/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import java.util.Iterator;

/**
 *
 * @author pverley
 */
public abstract class ZoneParticle extends BasicParticle implements IZoneParticle {

    public int getNumZone() {
        int nZone = -1;
        boolean foundZone = false;
        Iterator iter = ICFile.getInstance().getZones().iterator();
        while (!foundZone && iter.hasNext()) {
            Zone znTmp = (Zone) iter.next();
            if (znTmp.isXYInZone(getX(), getY())) {
                nZone = znTmp.getIndex();
                foundZone = true;
            }
        }
        return nZone;
    }
}
