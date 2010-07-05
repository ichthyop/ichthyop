/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.core;

import ichthyop.util.Constant;
import java.awt.Color;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author pverley
 */
public class OrientationZone extends Zone {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private int[] speedActivity, orientationActivity;
    private float[] swimmingSpeed, swimmingOrientation;
    private float[] speedRange, orientationRange;
    private String[] activePeriod;
    private int[] startHour, startMinute, endHour, endMinute;
    private Calendar now, startTime, endTime;
    private int[] durationActivePeriod;
    private int turtleActivity;

///////////////
// Constructors
///////////////
    public OrientationZone(int index,
            double lon1, double lat1,
            double lon2, double lat2,
            double lon3, double lat3,
            double lon4, double lat4,
            int depth1, int depth2,
            Color color) {

        super(Constant.ORIENTATION, index, lon1, lat1, lon2, lat2, lon3, lat3, lon4, lat4, depth1, depth2, color);
    }

    public OrientationZone(int index) {

        this(index, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12000, Color.WHITE);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    @Override
    public void init() {
        super.init();
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
        sb.append("  [Swim period] ");
        for (String s : activePeriod) {
            sb.append(s);
            sb.append(" ");
        }
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
    public int getTurtleActivity() {
        return turtleActivity;
    }

    /**
     * @param turtleActivity the turtleActivity to set
     */
    public void setTurtleActivity(float turtleActivity) {
        this.turtleActivity = (int) (turtleActivity * Constant.ONE_DAY);
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
