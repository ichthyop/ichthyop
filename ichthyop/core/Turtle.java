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
    private static boolean FLAG_ACTIVE_ORIENTATION;
    private int durationActivePeriod;
    private int[] durationSwimmingSpeed, durationSwimmingOrientation;
    private int[] currentSpeedActivity, currentOrientationActivity;
    private int[] speedActivity, orientationActivity;

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

            resetActivity();
            
        }
    }

    private void resetActivity() {
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
        if (dt >= 0) {
            advectForward(time);
        } else {
            advectBackward(time);
        }

        if (FLAG_HDISP) {
            increment(data.getHDispersion(getPGrid(), dt));
        }

        if (FLAG_VDISP) {
            increment(data.getVDispersion(getPGrid(), time, dt));
        }

        /** Test if particules is living */
        if (isOnEdge(Dataset.get_nx(), Dataset.get_ny())) {
            die(Constant.DEAD_OUT);
        } else if (!Dataset.isInWater(this)) {
            die(Constant.DEAD_BEACH);
        }

        /** buoyancy */
        if (FLAG_BUOYANCY && living) {
            addBuoyancy(time);
        }

        /** vertical migration */
        if (FLAG_MIGRATION && living) {
            migrate(time);
        }

        if (FLAG_ACTIVE_ORIENTATION) {
            swimTurtle(time);
        }

        /** Transform (x, y, z) into (lon, lat, depth) */
        if (living) {
            grid2Geog();
        }
    }

    private void swimTurtle(double time) {

        if (isActivePeriod(time)) {
            // go swim !!
        }
    }

    private double getdlat(double speed, double direction) {

        double dlat = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        dlat = speed * Math.sin(alpha) / Constant.ONE_DEG_LATITUDE_IN_METER;
        return dlat;
    }

    private double getdlon(double speed, double direction) {

        double dlon = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double one_deg_lon_meter = Constant.ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * getLat() / 180.d);
        dlon = speed * Math.cos(alpha) / one_deg_lon_meter;
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
        return now.after(startTime) && now.before(endTime);
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
