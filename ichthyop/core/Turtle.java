/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.core;

import ichthyop.io.Configuration;
import ichthyop.io.Dataset;
import ichthyop.util.Constant;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author pverley
 */
public class Turtle extends Particle {

    private static int startHour, startMinute, endHour, endMinute;
    private static Calendar now, startTime, endTime;
    private static boolean FLAG_ACTIVE_ORIENTATION, FLAG_ADVECTION;
    private int durationActivePeriod;
    private int[] durationSwimmingSpeed, durationSwimmingOrientation;
    private int[] currentSpeedActivity, currentOrientationActivity;
    private int[] speedActivity, orientationActivity;
    private float[] swimmingSpeed, swimmingOrientation;
    private boolean isActive;

    public Turtle(int index, boolean is3D, double xmin, double xmax, double ymin, double ymax, double depthMin, double depthMax) {
        super(index, is3D, xmin, xmax, ymin, ymax, depthMin, depthMax);
    }

    public Turtle(int index, boolean is3D, int numZone, double x, double y, double depth) {
        super(index, is3D, numZone, x, y, depth);
    }

    public Turtle(int index, boolean is3D, double lon, double lat, double depth, boolean living) {
        super(index, is3D, lon, lat, depth, living);
    }

    @Override
    void init() {

        super.init();
        FLAG_ADVECTION = Configuration.isAdvection();
        FLAG_ACTIVE_ORIENTATION = Configuration.isActiveOrientation();
        if (FLAG_ACTIVE_ORIENTATION) {
            String activePeriod[] = Configuration.getActivePeriod().split(" ");
            startHour = Integer.valueOf(activePeriod[0].split(":")[0]);
            startMinute = Integer.valueOf(activePeriod[0].split(":")[1]);
            endHour = Integer.valueOf(activePeriod[1].split(":")[0]);
            endMinute = Integer.valueOf(activePeriod[1].split(":")[1]);
            durationActivePeriod = computeActivePeriodDuration();
            speedActivity = Configuration.getSwimmingSpeedActivity();
            orientationActivity = Configuration.getSwimmingOrientationActivity();
            swimmingSpeed = Configuration.getSwimmingSpeed();
            swimmingOrientation = Configuration.getSwimmingOrientation();

            durationSwimmingSpeed = new int[speedActivity.length];
            for (int i = 0; i < speedActivity.length; i++) {
                durationSwimmingSpeed[i] = (int) ((speedActivity[i] / 100.f) * durationActivePeriod);
            }

            durationSwimmingOrientation = new int[orientationActivity.length + 1];
            int durationTotalOrientation = 0;
            for (int i = 0; i < orientationActivity.length; i++) {
                durationSwimmingOrientation[i] = (int) ((orientationActivity[i] / 100.f) * durationActivePeriod);
                durationTotalOrientation += durationSwimmingOrientation[i];
            }
            durationSwimmingOrientation[orientationActivity.length] = Math.max(0, durationActivePeriod - durationTotalOrientation);
            /*for (int i : durationSwimmingOrientation) {
            System.out.println(i + " / " + durationActivePeriod);
            }*/

            resetActivity();

        }
    }

    private void resetActivity() {
        isActive = false;
        currentSpeedActivity = new int[durationSwimmingSpeed.length];
        currentOrientationActivity = new int[durationSwimmingOrientation.length];
    }

    private int computeActivePeriodDuration() {
        GregorianCalendar start, end;
        start = new GregorianCalendar(1982, 05, 13, startHour, startMinute);
        end = new GregorianCalendar(1982, 05, 13, endHour, endMinute);
        return (int) (end.getTimeInMillis() - start.getTimeInMillis());
    }

    @Override
    void move(double time) throws ArrayIndexOutOfBoundsException {

        /** Advection diffusion */
        if (FLAG_ADVECTION) {
            advectForward(time);
            grid2Geog();
        }
        if (FLAG_ACTIVE_ORIENTATION) {
            swimTurtle(time);
        }

        /** Test if particules is living */
        if (isOnEdge(Dataset.get_nx(), Dataset.get_ny())) {
            die(Constant.DEAD_OUT);
        } else if (!Dataset.isInWater(this)) {
            die(Constant.DEAD_BEACH);
        }
    }

    private void swimTurtle(double time) {

        if (isActivePeriod(time)) {
            if (!isActive) {
                isActive = true;
            }
            // go swim !!
            double speed = getSpeed();
            double orientation = getOrientation();
            //System.out.println(speed + " " + orientation);
            double newLon = getLon() + getdlon(speed, orientation);
            double newLat = getLat() + getdlat(speed, orientation);
            //System.out.println(getLon() + " " + newLon + " - " + getLat() + " " + newLat);
            this.setLLD(newLon, newLat, getDepth());
            geog2Grid();
        } else {
            if (isActive) {
                resetActivity();
            }
        }
    }

    private double getSpeed() {

        int rank = (int) (Math.random() * durationSwimmingSpeed.length);
        //System.out.println("particle " + index() + " " + rank);
        if (currentSpeedActivity[rank] < durationSwimmingSpeed[rank]) {
            currentSpeedActivity[rank] += dt;
            return swimmingSpeed[rank];
        } else {
            return getSpeed();
        }
    }

    private double getOrientation() {

        int rank = (int) (Math.random() * durationSwimmingOrientation.length);

        if (currentOrientationActivity[rank] < durationSwimmingOrientation[rank]) {
            if (rank < swimmingOrientation.length) {
                return swimmingOrientation[rank];
            } else {
                return Math.random() * 360.d;
            }
        } else {
            return getOrientation();
        }
    }

    private double getdlat(double speed, double direction) {

        double dlat = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double sin = Math.sin(alpha);
        if (Math.abs(sin) < 1E-8) {
            sin = 0.d;
        }
        dlat = speed * sin / Constant.ONE_DEG_LATITUDE_IN_METER * dt;
        /*System.out.println(speed + " " + direction);
        System.out.println("sin(alpha): " + sin);
        System.out.println("dlat(m): " + (speed * sin) + " dlat(°): " + dlat + " lat: " + (float) getLat());*/
        //System.out.println("dlat " + (float) dlat + " " + direction);
        return dlat;
    }

    private double getdlon(double speed, double direction) {

        double dlon = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double one_deg_lon_meter = Constant.ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * getLat() / 180.d);
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

    public boolean isActivePeriod(double time) {

        if (now == null) {
            return false;
        }
        now.setTimeInMillis((long) (time * 1e3));
        syncCalendars();
        //SimpleDateFormat dtf = new SimpleDateFormat("HH:mm");
        //System.out.println(dtf.format(startTime.getTime()) + " " + dtf.format(now.getTime()) + " " + dtf.format(endTime.getTime()) + " " + (now.after(startTime) && now.before(endTime)));
        return (now.equals(startTime) || now.after(startTime)) && now.before(endTime);
    }

    private void syncCalendars() {
        startTime = (Calendar) now.clone();
        endTime = (Calendar) now.clone();

        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);

    }

    public static void setCalendar(Calendar cld) {
        now = cld;
    }
}
