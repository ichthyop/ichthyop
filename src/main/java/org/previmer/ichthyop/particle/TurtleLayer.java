/*
 *  Copyright (C) 2011 pverley
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
package org.previmer.ichthyop.particle;

import java.util.HashMap;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;

/**
 *
 * @author pverley
 */
public class TurtleLayer extends ParticleLayer {

    private HashMap<Integer, Integer[]> currentSpeedActivity, currentOrientationActivity;
    private boolean isActive;
    private long[] timerZone;
    private boolean[] startTiming;

    public TurtleLayer(IParticle particle) {
        super(particle);
    }

    @Override
    public void init() {

        int nbZones = getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION).size();
        currentSpeedActivity = new HashMap<>(nbZones);
        currentOrientationActivity = new HashMap<>(nbZones);
        for (Zone zone : getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION)) {
            resetActivePeriodCounters(zone);
        }
        timerZone = new long[nbZones];
        startTiming = new boolean[nbZones];
    }

    public void resetActivePeriodCounters(Zone zone) {
        isActive = false;

        /* reset speed activity */
        int speedActivityLength = 0;
        String[] tokens = zone.getParameter("speed_activity").getValue().split("\"");
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                speedActivityLength++;
            }
        }
        Integer[] zeros = new Integer[speedActivityLength];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i] = 0;
        }
        currentSpeedActivity.put(zone.getIndex(), zeros);

        /* reset orientation activity */
        int orientationActivityLength = 0;
        tokens = zone.getParameter("orientation_activity").getValue().split("\"");
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                speedActivityLength++;
            }
        }
        zeros = new Integer[orientationActivityLength];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i] = 0;
        }
        currentOrientationActivity.put(zone.getIndex(), zeros);
    }

    public int getCurrentOrientationActivity(Zone zone, int rank) {
        return currentOrientationActivity.get(zone.getIndex())[rank];
    }

    public void incrementCurrentOrientationActivity(Zone zone, int rank, int incr) {
        currentOrientationActivity.get(zone.getIndex())[rank] += incr;
    }

    public int getCurrentSpeedActivity(Zone zone, int rank) {
        return currentSpeedActivity.get(zone.getIndex())[rank];
    }

    public void incrementCurrentSpeedActivity(Zone zone, int rank, int incr) {
        currentSpeedActivity.get(zone.getIndex())[rank] += incr;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean hasStartedTiming(int numZone) {
        return startTiming[numZone];

    }

    public void setStartTiming(int numZone, boolean started) {
        startTiming[numZone] = started;
    }

    public long getTimerZone(int numZone) {
        return timerZone[numZone];
    }

    public void incrementTimerZone(int numZone, int incr) {
        timerZone[numZone] += incr;
    }
}
