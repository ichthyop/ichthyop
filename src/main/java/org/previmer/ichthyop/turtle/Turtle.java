/*
 * February the 1st, 16:15. Nathan can you see that change ?
 * 
To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.turtle;

import java.util.HashMap;
import java.util.List;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ParticleMortality;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class Turtle extends AbstractAction {

    private static boolean FLAG_ACTIVE_ORIENTATION;
    private HashMap<Integer, Integer[]> currentSpeedActivity, currentOrientationActivity;
    private boolean isActive;
    private long[] timerZone;
    private boolean[] startTiming;

    /*
     * public Turtle(int index, boolean is3D, double xmin, double xmax, double ymin,
     * double ymax, double depthMin, double depthMax) { super(index, is3D, xmin,
     * xmax, ymin, ymax, depthMin, depthMax); }
     * 
     * public Turtle(int index, boolean is3D, int numZone, double x, double y,
     * double depth) { super(index, is3D, numZone, x, y, depth); }
     * 
     * public Turtle(int index, boolean is3D, double lon, double lat, double depth,
     * boolean living) { super(index, is3D, lon, lat, depth, living); }
     */

    @Override
    public void loadParameters() {
        
        FLAG_ACTIVE_ORIENTATION = Boolean.valueOf(getParameter("turle_orientation"));
        if (FLAG_ACTIVE_ORIENTATION) {
            try {
                getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"),
                        TypeZone.ORIENTATION);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int nbZones = getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION).size();
            currentSpeedActivity = new HashMap<>(nbZones);
            currentOrientationActivity = new HashMap<>(nbZones);
            for (Zone zone : getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION)) {
                resetActivePeriodCounters((OrientationZone) zone);
            }
            timerZone = new long[nbZones];
            startTiming = new boolean[nbZones];
        }
    }
    
    private List<Zone> getOrientationZones() {
        return getSimulationManager().getZoneManager().getZones(TypeZone.ORIENTATION);
    }

    private void resetActivePeriodCounters(OrientationZone zone) {
        isActive = false;

        /* reset speed activity */
        Integer[] zeros = new Integer[zone.getSpeedActivity().length];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i] = 0;
        }
        currentSpeedActivity.put(zone.getIndex(), zeros);

        /* reset orientation activity */
        zeros = new Integer[zone.getOrientationActivity().length];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i] = 0;
        }
        currentOrientationActivity.put(zone.getIndex(), zeros);
    }

    @Override
    public void execute(IParticle particle) {

        if (FLAG_ACTIVE_ORIENTATION) {
            swimTurtle(particle, getSimulationManager().getTimeManager().getTime());
        }

        /*
        /** Test if particules is living */
        if (getSimulationManager().getDataset().isOnEdge(particle.getGridCoordinates())) {
            particle.kill(ParticleMortality.OUT_OF_DOMAIN);
            return;
        }
    }

    private void swimTurtle(IParticle particle, double time) {

        OrientationZone orientZone = null;
        int dt = getSimulationManager().getTimeManager().get_dt();
        for (Zone tmpZone : getOrientationZones()) { 
            if(tmpZone.isParticleInZone(particle)) {
                orientZone = (OrientationZone) tmpZone;
                break;
            }
        }
        
        if (orientZone != null) {
            int numZone = orientZone.getIndex();
            // retrieve the orientation zone
            OrientationZone zone = getOrientationZone(numZone);
            // check if did not exceed turtle activity in this zone
            if (!startTiming[numZone]) {
                startTiming[numZone] = true;
            }
            if (timerZone[numZone] >= zone.getTurtleActivity()) {
                return;
            }
            // check wether it is active period for the corresponding zone
            if (isActivePeriod(time, zone)) {
                if (!isActive) {
                    isActive = true;
                }
                // go swim !!
                double speed = getSpeed(time, zone);
                double orientation = getOrientation(time, zone);
                // System.out.println(speed + " " + orientation);
                double newLon = particle.getLon() + getdlon(particle, speed, orientation);
                double newLat = particle.getLat() + getdlat(particle, speed, orientation);
                // System.out.println(getLon() + " " + newLon + " - " + getLat() + " " +
                // newLat);
                double[] newposition = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
                double[] oldposition = particle.getGridCoordinates();
                double[] mvt = new double[newposition.length];
                for(int i = 0; i < newposition.length; i++) { 
                    mvt[i] = newposition[i] - oldposition[i];  
                }
                particle.increment(mvt);
                
            } else {
                if (isActive) {
                    resetActivePeriodCounters(zone);
                }
            }
        }
        for (int i = 0; i < timerZone.length; i++) {
            if (startTiming[i]) {
                timerZone[i] += dt;
            }
        }
    }

    private OrientationZone getOrientationZone(int index) {
        for (Zone zone : getOrientationZones()) {
            if (index == zone.getIndex()) {
                return (OrientationZone) zone;
            }
        }
        return null;
    }

    private double getSpeed(double time, OrientationZone zone) {

        int dt = getSimulationManager().getTimeManager().get_dt();
        int rank = (int) (Math.random() * zone.getSwimmingSpeed().length);
        // System.out.println("particle " + index() + " " + rank + " - zone " +
        // zone.getIndex() + " " + currentSpeedActivity.get(zone.getIndex())[rank]);

        if (currentSpeedActivity.get(zone.getIndex())[rank] < zone.getDurationSwimmingSpeed(time, rank)) {
            currentSpeedActivity.get(zone.getIndex())[rank] += (int) dt;
            // get base speed
            double swimSpeed = zone.getSwimmingSpeed()[rank];
            // get the range for this speed
            double range = zone.getSpeedRange()[rank];
            // base_speed - range <= real_speed <= base_speed + range
            swimSpeed += (2.d * Math.random() - 1.d) * range;
            return swimSpeed;
        } else {
            return getSpeed(time, zone);
        }
    }

    private double getOrientation(double time, OrientationZone zone) {

        int rank = (int) (Math.random() * zone.getOrientationActivity().length);
        int dt = getSimulationManager().getTimeManager().get_dt();

        if (currentOrientationActivity.get(zone.getIndex())[rank] < zone.getDurationSwimmingOrientation(time, rank)) {
            currentOrientationActivity.get(zone.getIndex())[rank] += (int) dt;
            if (rank < zone.getSwimmingOrientation().length) {
                // get base orientation
                double swimOrientation = zone.getSwimmingOrientation()[rank];
                // get orientation range
                double range = zone.getOrientationRange()[rank];
                // base_orientation - range <= real_orientation <= base_orientation + range
                swimOrientation += (2.d * Math.random() - 1.d) * range;
                return swimOrientation;
            } else {
                return Math.random() * 360.d;
            }
        } else {
            return getOrientation(time, zone);
        }
    }

    private double getdlat(IParticle particle, double speed, double direction) {

        int dt = getSimulationManager().getTimeManager().get_dt();
        double dlat = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double sin = Math.sin(alpha);
        if (Math.abs(sin) < 1E-8) {
            sin = 0.d;
        }
        dlat = speed * sin / Constant.ONE_DEG_LATITUDE_IN_METER * dt;
        /*
         * System.out.println(speed + " " + direction);
         * System.out.println("sin(alpha): " + sin); System.out.println("dlat(m): " +
         * (speed * sin) + " dlat(°): " + dlat + " lat: " + (float) getLat());
         */
        // System.out.println("dlat " + (float) dlat + " " + direction);
        return dlat;
    }

    private double getdlon(IParticle particle, double speed, double direction) {
        
        int dt = getSimulationManager().getTimeManager().get_dt();

        double dlon = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double one_deg_lon_meter = Constant.ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * particle.getLat() / 180.d);
        double cos = Math.cos(alpha);
        if (Math.abs(cos) < 1E-8) {
            cos = 0.d;
        }
        dlon = speed * cos / one_deg_lon_meter * dt;
        /*
         * System.out.println(speed + " " + direction);
         * System.out.println("cos(alpha): " + cos); System.out.println("dlon(m): " +
         * (speed * cos) + " dlon(°): " + dlon + " lon: " + (float) getLon());
         */
        // System.out.println("dlon " + (float) dlon);
        return dlon;
    }

    public boolean isActivePeriod(double time, OrientationZone zone) {
        return zone.isActivePeriod(time);
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }


    
}
