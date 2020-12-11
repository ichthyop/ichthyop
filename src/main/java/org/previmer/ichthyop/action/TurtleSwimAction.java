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
package org.previmer.ichthyop.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.arch.IZoneParticle;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.TurtleLayer;
import org.previmer.ichthyop.particle.ZoneParticleLayer;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class TurtleSwimAction extends AbstractAction {

    private OrientationZone[] zones;
    private int dt;

    public void loadParameters() throws Exception {
        
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.ORIENTATION);

        zones = new OrientationZone[getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION).size()];
        for (Zone zone : getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION)) {
            zones[zone.getIndex()] = new OrientationZone(zone);
            System.out.println(zones[zone.getIndex()].toString());
        }

        dt = getSimulationManager().getTimeManager().get_dt();
    }

    public void execute(IParticle particle) {

        TurtleLayer turtle =  (TurtleLayer) particle.getLayer(TurtleLayer.class);
        int numZone = ((IZoneParticle) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.ORIENTATION);
        if (numZone != -1) {
            // check if did not exceed turtle activity in this zone
            if (!turtle.hasStartedTiming(numZone)) {
                turtle.setStartTiming(numZone, true);
            }
            if (turtle.getTimerZone(numZone) >= zones[numZone].getTurtleActivity()) {
                return;
            }
            // check wether it is active period for the corresponding zone
            double time = getSimulationManager().getTimeManager().getTime();
            if (isActivePeriod(time, zones[numZone])) {
                if (!turtle.isActive()) {
                    turtle.setActive(true);
                }
                // go swim !!
                double speed = getSpeed(turtle, time, zones[numZone]);
                double orientation = getOrientation(turtle, time, zones[numZone]);
                System.out.println(speed + " " + orientation);
                double newLon = particle.getLon() + getdlon(particle, speed, orientation);
                double newLat = particle.getLat() + getdlat(speed, orientation);
                //System.out.println(getLon() + " " + newLon + " - " + getLat() + " " + newLat);
                double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
                double[] move = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
            particle.increment(move);
            } else {
                if (turtle.isActive()) {
                    turtle.resetActivePeriodCounters(zones[numZone]);
                }
            }
        }
        for (int i = 0; i < zones.length; i++) {
            if (turtle.hasStartedTiming(i)) {
                turtle.incrementTimerZone(i, dt);
            }
        }
    }

    private double getSpeed(TurtleLayer turtle, double time, OrientationZone zone) {

        int rank = (int) (Math.random() * zone.getSwimmingSpeed().length);
        //System.out.println("particle " + index() + " " + rank + " - zone " + zone.getIndex() + " " + currentSpeedActivity.get(zone.getIndex())[rank]);

        if (turtle.getCurrentSpeedActivity(zone, rank) < zone.getDurationSwimmingSpeed(time, rank)) {
            turtle.incrementCurrentSpeedActivity(zone, rank, dt);
            // get base speed
            double swimSpeed = zone.getSwimmingSpeed()[rank];
            // get the range for this speed
            double range = zone.getSpeedRange()[rank];
            //  base_speed - range <= real_speed <= base_speed + range
            swimSpeed += (2.d * Math.random() - 1.d) * range;
            return swimSpeed;
        } else {
            return getSpeed(turtle, time, zone);
        }
    }

    private double getOrientation(TurtleLayer turtle, double time, OrientationZone zone) {

        int rank = (int) (Math.random() * zone.getOrientationActivity().length);

        if (turtle.getCurrentOrientationActivity(zone, rank) < zone.getDurationSwimmingOrientation(time, rank)) {
            turtle.incrementCurrentOrientationActivity(zone, rank, dt);
            if (rank < zone.getSwimmingOrientation().length) {
                // get base orientation
                double swimOrientation = zone.getSwimmingOrientation()[rank];
                // get orientation range
                double range = zone.getOrientationRange()[rank];
                //  base_orientation - range <= real_orientation <= base_orientation + range
                swimOrientation += (2.d * Math.random() - 1.d) * range;
                return swimOrientation;
            } else {
                return Math.random() * 360.d;
            }
        } else {
            return getOrientation(turtle, time, zone);
        }
    }

    private double getdlat(double speed, double direction) {

        double dlat = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double sin = Math.sin(alpha);
        if (Math.abs(sin) < 1E-8) {
            sin = 0.d;
        }
        // ONE_DEG_LATITUDE_IN_METER = 111138.d;
        dlat = speed * sin / 111138.d * dt;
        /*System.out.println(speed + " " + direction);
        System.out.println("sin(alpha): " + sin);
        System.out.println("dlat(m): " + (speed * sin) + " dlat(°): " + dlat + " lat: " + (float) getLat());*/
        //System.out.println("dlat " + (float) dlat + " " + direction);
        return dlat;
    }

    private double getdlon(IParticle particle, double speed, double direction) {

        double dlon = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        // ONE_DEG_LATITUDE_IN_METER = 111138.d;
        double one_deg_lon_meter = 111138.d * Math.cos(Math.PI * particle.getLat() / 180.d);
        double cos = Math.cos(alpha);
        if (Math.abs(cos) < 1E-8) {
            cos = 0.d;
        }
        dlon = speed * cos / one_deg_lon_meter * dt;
        /*System.out.println(speed + " " + direction);
        System.out.println("cos(alpha): " + cos);
        System.out.println("dlon(m): " + (speed * cos) + " dlon(°): " + dlon + " lon: " + (float) getLon());*/
        //System.out.println("dlon " + (float) dlon);
        return dlon;
    }

    private boolean isActivePeriod(double time, OrientationZone zone) {
        return zone.isActivePeriod(time);
    }

    class OrientationZone extends Zone {

        private int[] speedActivity, orientationActivity;
        private float[] swimmingSpeed, swimmingOrientation;
        private float[] speedRange, orientationRange;
        private String[] activePeriod;
        private int[] startHour, startMinute, endHour, endMinute;
        private Calendar now, startTime, endTime;
        private int[] durationActivePeriod;
        private long turtleActivity;

        OrientationZone(Zone zone) {
            super(zone.getType(), zone.getKey(), zone.getIndex());
            readParameters(zone);
            reset();
        }

        private void readParameters(Zone zone) {

            /* read swimming speed */
            setSwimmingSpeed(readFloats(zone.getParameter("swimming_speed")));

            /* read speed range */
            setSpeedRange(readFloats(zone.getParameter("speed_range")));

            /* read speed activity */
            setSpeedActivity(readIntegers(zone.getParameter("speed_activity")));

            /* read swimming orientation */
            setSwimmingOrientation(readFloats(zone.getParameter("swimming_orientation")));

            /* read orientation range */
            setOrientationRange(readFloats(zone.getParameter("orientation_range")));

            /* read orientation activity  */
            setOrientationActivity(readIntegers(zone.getParameter("orientation_activity")));

            /* read active period */
            setActivePeriod(readStrings(zone.getParameter("active_period")));

            /* read turtle activity */
            setTurtleActivity(Float.valueOf(zone.getParameter("turtle_activity").getValue()));
        }

        private float[] readFloats(XParameter xparam) {
            ArrayList<Float> values = new ArrayList<>();
            String[] tokens = xparam.getValue().split("\"");
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    values.add(Float.valueOf(token));
                }
            }
            float[] arrValues = new float[values.size()];
            for (int i = 0; i < arrValues.length; i++) {
                arrValues[i] = values.get(i);
            }
            return arrValues;
        }

        private int[] readIntegers(XParameter xparam) {
            ArrayList<Integer> values = new ArrayList<>();
            String[] tokens = xparam.getValue().split("\"");
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    values.add(Integer.valueOf(token));
                }
            }
            int[] arrValues = new int[values.size()];
            for (int i = 0; i < arrValues.length; i++) {
                arrValues[i] = values.get(i);
            }
            return arrValues;
        }

        private String[] readStrings(XParameter xparam) {
            ArrayList<String> values = new ArrayList<>();
            String[] tokens = xparam.getValue().split("\"");
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    values.add(token);
                }
            }
            return values.toArray(new String[values.size()]);
        }

        private void reset() {
            //
            startHour = new int[activePeriod.length];
            startMinute = new int[activePeriod.length];
            endHour = new int[activePeriod.length];
            endMinute = new int[activePeriod.length];

            for (int i = 0; i < activePeriod.length; i++) {
                String[] period = activePeriod[i].split(" ");
                startHour[i] = Integer.valueOf(period[0].split(":")[0]);
                startMinute[i] = Integer.valueOf(period[0].split(":")[1]);
                endHour[i] = Integer.valueOf(period[1].split(":")[0]);
                endMinute[i] = Integer.valueOf(period[1].split(":")[1]);
            }

            durationActivePeriod = new int[activePeriod.length];
            for (int i = 0; i < activePeriod.length; i++) {
                durationActivePeriod[i] = computeActivePeriodDuration(i);
            }

            System.out.println(toString());
        }

        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            sb.append("[[ Orientation zone ");
            sb.append(getIndex());
            sb.append(" ]]\n");
            sb.append("  [Swim period] (");
            for (String s : activePeriod) {
                sb.append(s);
                sb.append(") (");
            }
            sb.append(")");
            sb.append("\n");
            sb.append("  [Speed] ");
            for (int i = 0; i < speedActivity.length; i++) {
                sb.append("(");
                sb.append(swimmingSpeed[i]);
                sb.append("m/s - activity: ");
                sb.append(speedActivity[i]);
                sb.append("% - range: +/-");
                sb.append(speedRange[i]);
                sb.append("m/s)");
            }
            sb.append("\n");
            sb.append("  [Orientation] ");
            for (int i = 0; i < orientationActivity.length; i++) {
                sb.append("(");
                sb.append(swimmingOrientation[i]);
                sb.append("° - activity: ");
                sb.append(orientationActivity[i]);
                sb.append("% - range: ");
                sb.append(orientationRange[i]);
                sb.append("%)");
            }
            sb.append("\n");
            return sb.toString();
        }

        private int computeActivePeriodDuration(int indexPeriod) {
            GregorianCalendar start, end;
            start = new GregorianCalendar(1982, 05, 13, startHour[indexPeriod], startMinute[indexPeriod]);
            end = new GregorianCalendar(1982, 05, 13, endHour[indexPeriod], endMinute[indexPeriod]);
            return (int) (end.getTimeInMillis() - start.getTimeInMillis());
        }

        public boolean isActivePeriod(double time) {

            if (now == null) {
                return false;
            }
            now.setTimeInMillis((long) (time * 1e3));
            for (int i = 0; i < activePeriod.length; i++) {
                syncCalendars(i);
                //SimpleDateFormat dtf = new SimpleDateFormat("HH:mm");
                //System.out.println(dtf.format(startTime.getTime()) + " " + dtf.format(now.getTime()) + " " + dtf.format(endTime.getTime()) + " " + (now.after(startTime) && now.before(endTime)));
                if (now.equals(startTime) || (now.after(startTime) && now.before(endTime))) {
                    return true;
                }
            }
            return false;
        }

        public int findActivePeriod(double time) {
            if (now == null) {
                return -1;
            }
            now.setTimeInMillis((long) (time * 1e3));
            for (int i = 0; i < activePeriod.length; i++) {
                syncCalendars(i);
                //SimpleDateFormat dtf = new SimpleDateFormat("HH:mm");
                //System.out.println(dtf.format(startTime.getTime()) + " " + dtf.format(now.getTime()) + " " + dtf.format(endTime.getTime()) + " " + (now.after(startTime) && now.before(endTime)));
                if (now.equals(startTime) || (now.after(startTime) && now.before(endTime))) {
                    return i;
                }
            }
            return -1;
        }

        private void syncCalendars(int indexPeriod) {
            startTime = (Calendar) now.clone();
            endTime = (Calendar) now.clone();

            startTime.set(Calendar.HOUR_OF_DAY, startHour[indexPeriod]);
            startTime.set(Calendar.MINUTE, startMinute[indexPeriod]);
            endTime.set(Calendar.HOUR_OF_DAY, endHour[indexPeriod]);
            endTime.set(Calendar.MINUTE, endMinute[indexPeriod]);

        }

        public void setCalendar(Calendar cld) {
            now = cld;
        }

        public float[] getSwimmingSpeed() {
            return swimmingSpeed;
        }

        public float[] getSwimmingOrientation() {
            return swimmingOrientation;
        }

        /**
         * @param swimmingSpeed the swimmingSpeed to set
         */
        public void setSwimmingSpeed(float[] swimmingSpeed) {
            this.swimmingSpeed = swimmingSpeed;
        }

        /**
         * @return the speedActivity
         */
        public int[] getSpeedActivity() {
            return speedActivity;
        }

        /**
         * @param speedActivity the speedActivity to set
         */
        public void setSpeedActivity(int[] speedActivity) {
            this.speedActivity = speedActivity;
        }

        /**
         * @return the orientationActivity
         */
        public int[] getOrientationActivity() {
            return orientationActivity;
        }

        /**
         * @param orientationActivity the orientationActivity to set
         */
        public void setOrientationActivity(int[] orientationActivity) {
            this.orientationActivity = orientationActivity;
        }

        /**
         * @param swimmingOrientation the swimmingOrientation to set
         */
        public void setSwimmingOrientation(float[] swimmingOrientation) {
            this.swimmingOrientation = swimmingOrientation;
        }

        /**
         * @return the activePeriod
         */
        public String[] getActivePeriod() {
            return activePeriod;
        }

        /**
         * @param activePeriod the activePeriod to set
         */
        public void setActivePeriod(String[] activePeriod) {
            this.activePeriod = activePeriod;
        }

        /**
         * @return the durationSwimmingSpeed
         */
        public int getDurationSwimmingSpeed(double time, int rank) {
            int indexPeriod = findActivePeriod(time);
            if (indexPeriod != -1) {
                return (int) ((speedActivity[rank] / 100.f) * durationActivePeriod[indexPeriod]);
            }
            return 0;
        }

        /**
         * @return the durationSwimmingOrientation
         */
        public int getDurationSwimmingOrientation(double time, int rank) {

            int indexPeriod = findActivePeriod(time);
            if (indexPeriod != -1) {
                if (rank > orientationActivity.length - 1) {
                    int durationTotalOrientation = 0;
                    for (int orientationDuration : orientationActivity) {
                        durationTotalOrientation += orientationDuration;
                    }
                    return Math.max(0, durationActivePeriod[indexPeriod] - durationTotalOrientation);
                }
                return (int) ((orientationActivity[rank] / 100.f) * durationActivePeriod[indexPeriod]);
            }
            return 0;
        }

        /**
         * @return the turtleActivity in seconds
         */
        public long getTurtleActivity() {
            return turtleActivity;
        }

        /**
         * @param turtleActivity the turtleActivity to set
         */
        public void setTurtleActivity(float turtleActivity) {
            this.turtleActivity = (long) (turtleActivity * Constant.ONE_DAY);
        }

        /**
         * @return the speedRange
         */
        public float[] getSpeedRange() {
            return speedRange;
        }

        /**
         * @param speedRange the speedRange to set
         */
        public void setSpeedRange(float[] speedRange) {
            this.speedRange = speedRange;
        }

        /**
         * @return the orientationRange
         */
        public float[] getOrientationRange() {
            return orientationRange;
        }

        /**
         * @param orientationRange the orientationRange to set
         */
        public void setOrientationRange(float[] orientationRange) {
            this.orientationRange = orientationRange;
        }
    }

    @Override
    public void init(IParticle particle) {
        // TODO Auto-generated method stub

    }
}
