package org.previmer.ichthyop.calendar;

/** Import the abstract class Calendar */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <p>The class extends the abstact class {@link java.util.Calendar}. It provides
 * methods for converting between a specific instant in time and a set of
 * fields such as <code>YEAR</code>, <code>MONTH</code>,
 * <code>DAY_OF_MONTH</code>, <code>HOUR</code>, and so on, according with the
 * Gregorian calendar. An instant in time is represented by a millisecond
 * value that is, by default, an offset from January 1, 1900 00:00:00.000 GMT.
 * The origin can be set by the user in one of the constructors.</p>
 * The class is a very simplified version of the Gregorian Calendar, except that
 * the epoch is not automatically set to January 1, 1970 (Gregorian),
 * midnight UTC.
 *
 * @author P.Verley 2007
 * @see java.util.Calendar
 * @see java.util.GregorianCalendar
 */
public class InterannualCalendar extends Calendar {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Origin of time
     */
    int[] epoch_fields;
///////////////////////////////
// Declaration of the constants
///////////////////////////////
    public static final int ONE_SECOND = 1000;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;
    private static final int NUM_DAYS[] = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334}; // 0-based, for day-in-year
    private static final int LEAP_NUM_DAYS[] = {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335}; // 0-based, for day-in-year
    private static final int MONTH_LENGTH[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based
    private static final int LEAP_MONTH_LENGTH[] = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 0-based

///////////////
// Constructors
///////////////
    /**
     * Constructs a Gregorian Calendar with origin of time
     * January 1, 1900 00:00:00.000 GMT.
     *
     * 2011/02/23 pverley: some simulation may have an origin of time prior
     * to 1900 so a new origin has been set as the 1st day of the Gregorian
     * calendar October 15, 1582 00:00:00.000 GMT
     */
    public InterannualCalendar() {
        this(1582, OCTOBER, 15);
    }

    /**
     * Constructs a Gregorian Calendar with origin of time set by parameters.
     * Hours, minutes, seconds are automatically set to 00:00:00.000
     * @param year an int, the year origin
     * @param month an int, the month origin
     * @param day an int, the day origin
     */
    public InterannualCalendar(int year, int month, int day) {

        epoch_fields = new int[FIELD_COUNT];
        fields = new int[FIELD_COUNT];
        setEpoch(YEAR, year);
        setEpoch(MONTH, month);
        setEpoch(DAY_OF_MONTH, day);
        setEpoch(HOUR_OF_DAY, 0);
        setEpoch(MINUTE, 0);
        setEpoch(SECOND, 0);
        setTimeInMillis(0);
    }

    public InterannualCalendar(String origin, SimpleDateFormat dateFormat) throws ParseException {

        Calendar cld = new InterannualCalendar();
        dateFormat.setCalendar(cld);
        cld.setTime(dateFormat.parse(origin));
        epoch_fields = new int[FIELD_COUNT];
        fields = new int[FIELD_COUNT];
        setEpoch(YEAR, cld.get(Calendar.YEAR));
        setEpoch(MONTH, cld.get(Calendar.MONTH));
        setEpoch(DAY_OF_MONTH, cld.get(Calendar.DAY_OF_MONTH));
        setEpoch(HOUR_OF_DAY, cld.get(Calendar.HOUR_OF_DAY));
        setEpoch(MINUTE, cld.get(Calendar.MINUTE));
        setEpoch(SECOND, 0);
        setTimeInMillis(0);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Sets the given epoch field (origin of time) to the given value.
     *
     * @param field an int, one of the field of the epoch.
     * @param value an int, the value of the given field.
     * @see java.util.Calendar for details about the available fields.
     */
    public void setEpoch(int field, int value) {
        epoch_fields[field] = value;
    }

    /**
     * Converts time as milliseconds to day
     *
     * @param millis a long, the time in millisecond
     * @return long, the floor of the quotient millis / (24 * 3600 * 1000)
     */
    private static final long millisToDay(long millis) {
        return (millis / ONE_DAY);
    }

    /**
     * Converts time as day to milliseconds
     *
     * @param day a long, the time in day
     * @return long the time in millisecond
     */
    private static final long dayToMillis(long day) {
        return day * ONE_DAY;
    }

    /**
     * Converts time as milliseconds to time field values.
     */
    protected void computeFields() {
        int rawYear, year, month, dayOfMonth, dayOfYear;
        boolean isLeap;
        long timeInDay = millisToDay(time);
        int n400, n4, n1;

        long timeInDay_o = timeInDay + (isLeap(epoch_fields[YEAR])
                ? LEAP_NUM_DAYS[epoch_fields[MONTH]]
                + epoch_fields[DAY_OF_MONTH] - 1
                : NUM_DAYS[epoch_fields[MONTH]]
                + epoch_fields[DAY_OF_MONTH] - 1);

        n400 = (int) (timeInDay_o / 146097);
        dayOfYear = (int) (timeInDay_o % 146097);
        n4 = dayOfYear / 1461;
        dayOfYear %= 1461;
        n1 = dayOfYear / 365;
        dayOfYear %= 365; // zero-based day of year
        rawYear = 400 * n400 + 4 * n4 + n1;
        rawYear += epoch_fields[YEAR];

        isLeap = isLeap(rawYear);

        if (dayOfYear > (isLeap ? 365 : 364)) {
            dayOfYear -= (isLeap ? 366 : 365);
            rawYear++;
        }

        int correction = 0;
        int march1 = isLeap ? 60 : 59; // zero-based DAY for March 1
        if (dayOfYear >= march1) {
            correction = isLeap ? 1 : 2;
        }

        month = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
        dayOfMonth = dayOfYear
                - (isLeap ? LEAP_NUM_DAYS[month] : NUM_DAYS[month]) + 1; // one-based DOM

        year = rawYear;
        set(YEAR, year);
        set(MONTH, month); // 0-based
        set(DAY_OF_MONTH, dayOfMonth);
        set(DAY_OF_YEAR, ++dayOfYear);

        int millisInDay = (int) (time - timeInDay * ONE_DAY);
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
        set(AM_PM, millisInDay / 12); // Assume AM == 0
        set(HOUR, millisInDay % 12);

    }

    /**
     * Determines if the given year is a leap year. Returns true if the
     * given year is a leap year.
     *
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     */
    private boolean isLeap(int year) {
        return ((year % 4 == 0) && ((!(year % 100 == 0)) | (year % 400 == 0)));
    }

    /**
     * Converts time field values to milliseconds.
     */
    protected void computeTime() {

        int n400, n4, n1;
        int yearOn = fields[YEAR];
        int monthOn = fields[MONTH];
        boolean isLeap = isLeap(yearOn);
        long time2Day = (long) (fields[DAY_OF_MONTH] - 1);
        try {
            time2Day += isLeap
                    ? (long) (LEAP_NUM_DAYS[monthOn])
                    : (long) (NUM_DAYS[monthOn]);
        } catch (Exception ex) {
        }

        int deltaYear = yearOn - epoch_fields[YEAR];

        n400 = (int) (deltaYear / 400);
        deltaYear = (int) (deltaYear % 400);
        n4 = (int) (deltaYear / 4);
        n1 = deltaYear % 4;

        time2Day += (long) (n400 * 146097L + n4 * 1461L + n1 * 365L);

        time2Day -= (epoch_fields[DAY_OF_MONTH] - 1);
        time2Day -= isLeap(epoch_fields[YEAR])
                ? LEAP_NUM_DAYS[epoch_fields[MONTH]]
                : NUM_DAYS[epoch_fields[MONTH]];

        long millis = dayToMillis(time2Day);
        int millisInDay = fields[MILLISECOND]
                + 1000
                * (fields[SECOND]
                + 60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
        time = millis + millisInDay;
    }

//////////////////////////////////
// Inherited methods not redefined
//////////////////////////////////
    public int getGreatestMinimum(int field) {
        return 0;
    }

    public int getLeastMaximum(int field) {
        return 0;
    }

    public int getMaximum(int field) {
        return 0;
    }

    public int getMinimum(int field) {
        return 0;
    }

    public void add(int field, int amount) {
    }

    public void roll(int field, boolean up) {
    }
    //---------- End of class
}
