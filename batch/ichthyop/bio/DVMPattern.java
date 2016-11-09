package ichthyop.bio;

import java.util.Calendar;

import ichthyop.io.Dataset;
import ichthyop.core.Simulation;
import java.text.SimpleDateFormat;

/**
 * Simulates Diel Vertical Migration.
 * <br>
 * Nocturnal diel vertical migration is a common zooplankton behavior in
 * which organisms reside in surface or near-surface waters at night and at
 * deeper depths during the day.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class DVMPattern {

    private final static boolean DEBUG = false; // debugging purpose

///////////////////////////////
// Declaration of the constants
///////////////////////////////

    private final static int DAYTIME = 7;

    private final static int NIGTHTIME = 19;

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    private static Calendar calendar;

    private static double depthDay;

    private static double depthNight;

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Initializes the daytime depth and the nighttime depth.
     */
    public static void init() {

        depthDay = Simulation.getDepthDay();
        depthNight = Simulation.getDepthNight();
    }

    /**
     * Computes the depth of the particle according to the diel vertical
     * migration behavior. At <code>DAYTIME</code> the particle stays at
     * <code>DEPTH_DAYTIME</code> depth and jumps to
     * <code>DEPTH_NIGHTTIME</code> at <code>NIGHTTIME</code>.
     *
     * @param x a double, the x grid coordinate of the particle
     * @param y a double, the y grid coordinate of the particle
     * @param time a double, the current time [second] of the simulation
     * @param data the Dataset of the simulation
     * @return a double, the depth [meter] of the particle at this time
     * of the day.
     */
    public static double getDepth(double x, double y, double time,
                                  Dataset data) {

        double bottom = data.getDepth(x, y, 0);
        calendar.setTimeInMillis((long) (time * 1e3));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (DEBUG) {
            SimpleDateFormat dateformat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm");
            dateformat.setCalendar(calendar);
            float depth = (hour >= DAYTIME && hour < NIGTHTIME)
                          ? (float) Math.max(bottom, depthDay)
                          : (float) Math.max(bottom, depthNight);
            System.out.println("DVM " + dateformat.format(calendar.getTime())
                               + " - h: " + hour + " -depth: " + depth);
        }

        if (hour >= DAYTIME && hour < NIGTHTIME) {
            return Math.max(bottom, depthDay);
        } else {
            return Math.max(bottom, depthNight);
        }
    }

    /**
     * Determines whether the specified time is daytime or nighttime.
     *
     * @param time a double, the specified time [second]
     * @return <code>true</code> if daytime, <code>false</code> otherwise.
     */
    public static boolean isDaytime(double time) {

        if (calendar == null) {
            return false;
        }
        calendar.setTimeInMillis((long) (time * 1e3));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= DAYTIME && hour < NIGTHTIME);
    }

    /**
     * Sets the calendar that will help determining wether it is day or night
     * time.
     * @param the Calendar of the current simulation.
     */
    public static void setCalendar(Calendar cld) {
        calendar = cld;
    }

    //---------- End of class
}
