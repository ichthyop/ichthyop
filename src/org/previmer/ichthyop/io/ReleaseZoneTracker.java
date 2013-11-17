/*
 * Copyright (C) 2012 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 *
 * @author pverley
 */
public class ReleaseZoneTracker extends AbstractTracker {

    public ReleaseZoneTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addDrifterDimension();
    }

    @Override
    Array createArray() {
        return new ArrayInt.D1(getNParticle());
    }

    @Override
    void addRuntimeAttributes() {

        List<Zone> zones = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE);
        if (null != zones) {
            for (Zone zone : zones) {
                addAttribute(new Attribute("release_zone " + zone.getIndex(), zone.getKey()));
            }
        }
    }

    @Override
    public void track() {

        IParticle particle;
        ZoneParticleLayer zparticle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            zparticle = (ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class);
            getArray().setInt(getIndex().set(particle.getIndex()), zparticle.getNumZone(TypeZone.RELEASE));
        }

        // The variable is only written once, at first time step
        disable();
    }
}
