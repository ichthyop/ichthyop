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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.particle.ZoneParticleLayer;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
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
        return new ArrayInt.D3(1, dimensions().get(1).getLength(), dimensions().get(2).getLength());
    }

    public void track() {
        IBasicParticle particle;
        ZoneParticleLayer zparticle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
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
    public Attribute[] attributes() {
        List<Attribute> listAttributes = new ArrayList();
        for (Attribute attr : super.attributes()) {
            listAttributes.add(attr);
        }
        for (TypeZone type : TypeZone.values()) {
            listAttributes.add(new Attribute("type_zone " + type.getCode(), type.toString()));
            List<Zone> zones = getSimulationManager().getZoneManager().getZones(type);
            if (null != zones) {
                for (Zone zone : zones) {
                    listAttributes.add(new Attribute(type.toString() + "_zone " + zone.getIndex(), zone.getKey()));
                }
            } else {
                listAttributes.add(new Attribute(type.toString() + "_zone", "none for this run"));
            }
        }
        return listAttributes.toArray(new Attribute[listAttributes.size()]);
    }
}
