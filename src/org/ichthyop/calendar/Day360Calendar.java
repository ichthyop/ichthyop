package org.ichthyop.calendar;

/**
 * Import the abstract class Calendar
 */
import java.util.Calendar;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.FIELD_COUNT;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

/**
 * <p>
 * The class extends the abstact class {@link java.util.Calendar}. It provides
 * methods for converting between a specific instant in time and a set of fields
 * such as <code>YEAR</code>, <code>MONTH</code>, <code>DAY_OF_MONTH</code>,
 * <code>HOUR</code>, and so on, according with the 360_day calendar. An instant
 * in time is represented by a millisecond value that is, by default, an offset
 * from January 1, 1900 00:00:00.000 GMT. The origin can be set by the user in
 * one of the constructors.</p>
 *
 * @author P.Verley 2015
 * @see java.util.Calendar
 */
public class Day360Calendar extends Calendar {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Origin of time
     */
    final private long time_o;
    
///////////////////////////////
// Declaration of the constants
///////////////////////////////
    public static final int ONE_SECOND = 1000;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;

///////////////
// Constructors
///////////////
    /**
     * Constructs a Gregorian Calendar with origin of time January 1, 1900
     * 00:00:00.000 GMT.
     */
    public Day360Calendar() {
        this(1900, JANUARY, 1, 0, 0);
    }

    /**
     * Constructs a Gregorian Calendar with origin of time set by parameters.
     * Hours, minutes, seconds are automatically set to 00:00:00.000
     *
     * @param year an int, the year origin
     * @param month an int, the month origin
     * @param day an int, the day origin
     * @param hour an int, the hour origin
     * @param minute an int, the minute origin
     */
    public Day360Calendar(int year, int month, int day, int hour, int minute) {

        fields = new int[FIELD_COUNT];
        long time2Day_o = (long) (day - 1 + month * 30 + (year - 1) * 360);
        long millis_o = dayToMillis(time2Day_o);
        int millisInDay_o = 1000 * (60 * (minute + 60 * hour));
        time_o = millis_o + millisInDay_o;
        setTimeInMillis(0);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Converts time as milliseconds to day
     *
     * @param millis a long, the time in millisecond
     * @return long, the floor of the quotient millis / (24 * 3600 * 1000)
     */
    private static long millisToDay(long millis) {
        return (millis / ONE_DAY);
    }

    /**
     * Converts time as day to milliseconds
     *
     * @param day a long, the time in day
     * @return long the time in millisecond
     */
    private static long dayToMillis(long day) {
        return day * ONE_DAY;
    }

    /**
     * Converts time as milliseconds to time field values.
     */
    @Override
    protected void computeFields() {
        
        int year, month, dayOfMonth, dayOfYear;
        long timeInDay = millisToDay(time + time_o);
        //System.out.println("cf timeInDay " + timeInDay);

        dayOfYear = (int) timeInDay % 360; // zero-based day of year
        year = (int) (timeInDay / 360L);
        month = dayOfYear / 30;
        dayOfMonth = dayOfYear - month * 30 + 1;
        //System.out.println("cf dayOfYear " + dayOfYear);

        set(YEAR, ++year);
        set(MONTH, month); // 0-based
        set(DAY_OF_MONTH, dayOfMonth);
        set(DAY_OF_YEAR, ++dayOfYear); // converted to 1-based

        int millisInDay = (int) (time + time_o - (timeInDay * ONE_DAY));
        if (millisInDay < 0) {
            millisInDay += ONE_DAY;
        }

        set(MILLISECOND, millisInDay % 1000);
        millisInDay /= 1000;
        set(SECOND, millisInDay % 60);
        millisInDay /= 60;
        set(MINUTE, millisInDay % 60);
        millisInDay /= 60;
        set(HOUR_OF_DAY, millisInDay);

    }

    /**
     * Converts time field values to milliseconds.
     */
    @Override
    protected void computeTime() {

        long time2Day = (long) (fields[DAY_OF_MONTH] - 1 + fields[MONTH] * 30
                + (fields[YEAR] - 1) * 360);
        long millis = dayToMillis(time2Day);
        int millisInDay = 1000
                * (fields[SECOND]
                + 60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
        time = millis + millisInDay - time_o;
    }

//////////////////////////////////
// Inherited methods not redefined
//////////////////////////////////
    @Override
    public int getGreatestMinimum(int field) {
        return 0;
    }

    @Override
    public int getLeastMaximum(int field) {
        return 0;
    }

    @Override
    public int getMaximum(int field) {
        return 0;
    }

    @Override
    public int getMinimum(int field) {
        return 0;
    }

    @Override
    public void add(int field, int amount) {
    }

    @Override
    public void roll(int field, boolean up) {
    }
    //---------- End of class
}
