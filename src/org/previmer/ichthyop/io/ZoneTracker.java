/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.io;

import java.util.Iterator;
import java.util.List;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ZoneParticleLayer;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author P. VERLEY (philippe.verley@ird.fr)
 */
public class ZoneTracker extends AbstractTracker {

    public ZoneTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
        addCustomDimension(new Dimension("type_zone", TypeZone.values().length));
    }

    @Override
    Array createArray() {
        ArrayInt.D3 array = new ArrayInt.D3(1, getNParticle(), TypeZone.values().length);
        // Initialises zone array with -99
        for (int iZone = 0; iZone < TypeZone.values().length; iZone++) {
            for (int iP = 0; iP < getNParticle(); iP++) {
                array.set(0, iP, iZone, -99);
            }
        }
        return array;
    }

    @Override
    public void track() {
        IParticle particle;
        ZoneParticleLayer zparticle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            zparticle = (ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class);
            Index index = getArray().getIndex();
            for (TypeZone type : TypeZone.values()) {
                index.set(0, particle.getIndex(), type.getCode());
                getArray().setInt(index, zparticle.getNumZone(type));
            }
        }
    }

    @Override
    public void addRuntimeAttributes() {

        for (TypeZone type : TypeZone.values()) {
            addAttribute(new Attribute("type_zone " + type.getCode(), type.toString()));
            List<Zone> zones = getSimulationManager().getZoneManager().getZones(type);
            if (null != zones) {
                for (Zone zone : zones) {
                    addAttribute(new Attribute(type.toString() + "_zone " + zone.getIndex(), zone.getKey()));
                }
            } else {
                addAttribute(new Attribute(type.toString() + "_zone", "none for this run"));
            }
        }
        // Particle not released yet set to -99
        addAttribute(new Attribute("not_released_yet", -99));
        // Particle out of zone set to -1
        addAttribute(new Attribute("out_of_zone", -1));
    }
}
