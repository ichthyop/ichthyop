package org.ichthyop.calendar;

/**
 * Import the abstract class Calendar
 */
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * <p>
 * The class extends the abstract class {@link java.util.Calendar}. It provides
 * methods for converting between a specific instant in time and a set of fields
 * such as <code>YEAR</code>, <code>MONTH</code>, <code>DAY_OF_MONTH</code>,
 * <code>HOUR</code>, and so on, according with the Gregorian calendar. An
 * instant in time is represented by a millisecond value that is, by default, an
 * offset from January 1, 1900 00:00:00.000 GMT. The origin can be set by the
 * user in one of the constructors.</p>
 * The class is a very simplified version of the Gregorian Calendar, except that
 * the epoch is not automatically set to January 1, 1970 (Gregorian), midnight
 * UTC.
 *
 * @author P.Verley 2007
 * @see java.util.Calendar
 * @see java.util.GregorianCalendar
 */
abstract class InterannualCalendar extends Calendar {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    // Origin of time
    private final int[] epoch_fields;

    // Type of calendar
    private final Type type;

    private final Calendar cld = new GregorianCalendar();

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

    public enum Type {
        PROLEPTIC_GREGORIAN,
        JULIAN,
        ALL_LEAP,
        NO_LEAP
    }

///////////////
// Constructors
///////////////
    /**
     * Constructs an interannual Calendar with origin of time set by parameters.
     * Hours, minutes, seconds are automatically set to 00:00:00.000
     *
     * @param year an int, the year origin
     * @param month an int, the month origin
     * @param day an int, the day origin
     * @param hour an int, the hour origin
     * @param minute an int, the minute origin
     * @param type
     */
    public InterannualCalendar(int year, int month, int day, int hour, int minute, Type type) {

        this.type = type;
        epoch_fields = new int[FIELD_COUNT];
        fields = new int[FIELD_COUNT];
        epoch_fields[YEAR] = year;
        epoch_fields[MONTH] = month;
        epoch_fields[DAY_OF_MONTH] = day;
        epoch_fields[HOUR_OF_DAY] = hour;
        epoch_fields[MINUTE] = minute;
        epoch_fields[SECOND] = 0;
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /*
     * Converts time as milliseconds to day
     *
     * @param millis a long, the time in millisecond
     * @return long, the floor of the quotient millis / (24 * 3600 * 1000)
     */
    private static long millisToDay(long millis) {
        return (millis / ONE_DAY);
    }

    /*
     * Converts time as day to milliseconds
     *
     * @param day a long, the time in day
     * @return long the time in millisecond
     */
    private static long dayToMillis(long day) {
        return day * ONE_DAY;
    }

    @Override
    protected void computeFields() {

        int rawYear, year, month, dayOfMonth, dayOfYear;
        boolean isLeap;
        long epoch_hour = epoch_fields[HOUR_OF_DAY] * ONE_HOUR + epoch_fields[MINUTE] * ONE_MINUTE;
        long timeInDay = millisToDay(time + epoch_hour);

        long timeInDay_o = timeInDay + (isLeap(epoch_fields[YEAR])
                ? LEAP_NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1
                : NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1);

        long nbDays = 0;
        int currentYear = epoch_fields[YEAR];
        int signum = (int) Math.signum(timeInDay_o);
        while (Math.abs(nbDays) < Math.abs(timeInDay_o)) {
            nbDays = nbDays + signum * (isLeap(currentYear) ? 366 : 365);
            currentYear += signum;
        }
        currentYear -= signum;
        nbDays = nbDays - signum * (isLeap(currentYear) ? 366 : 365);
        rawYear = (int) currentYear;
        dayOfYear = (int) (timeInDay_o - nbDays);
        isLeap = isLeap(rawYear);
        if (dayOfYear < 0) {
            rawYear--;
            dayOfYear += (isLeap ? 366 : 365);
        }
        if (dayOfYear > (isLeap ? 365 : 364)) {
            dayOfYear = dayOfYear - signum * (isLeap ? 366 : 365);
            rawYear += signum;
        }

        int correction = 0;
        int march1 = isLeap ? 60 : 59; // zero-based DAY for March 1
        if (dayOfYear >= march1) {
            correction = isLeap ? 1 : 2;
        }

        month = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
        dayOfMonth = dayOfYear - (isLeap ? LEAP_NUM_DAYS[month] : NUM_DAYS[month]) + 1; // one-based DOM

        year = rawYear;
        set(YEAR, year);
        set(MONTH, month); // 0-based
        set(DAY_OF_MONTH, dayOfMonth);
        set(DAY_OF_YEAR, ++dayOfYear);

        int millisInDay = (int) (time + epoch_hour - timeInDay * ONE_DAY);
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
     * Determines if the given year is a leap year. Returns true if the given
     * year is a leap year.
     *
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     */
    private boolean isLeap(int year) {

        switch (type) {
            case PROLEPTIC_GREGORIAN:
                return ((year % 4 == 0) && ((!(year % 100 == 0)) | (year % 400 == 0)));
            case JULIAN:
                return (year % 4 == 0);
            case ALL_LEAP:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void computeTime() {

        int yearOn = fields[YEAR];
        int monthOn = fields[MONTH];
        long time2Day = (long) (fields[DAY_OF_MONTH] - 1);
        time2Day += isLeap(yearOn) ? LEAP_NUM_DAYS[monthOn] : NUM_DAYS[monthOn];

        int signum = (int) Math.signum(yearOn - epoch_fields[YEAR]);
        for (int year = epoch_fields[YEAR]; year != yearOn; year = year + signum) {
            time2Day = time2Day + signum * (isLeap(year) ? 366 : 365);
        }

        time2Day = time2Day - signum * (epoch_fields[DAY_OF_MONTH] - 1);
        time2Day = time2Day - signum * (isLeap(epoch_fields[YEAR])
                ? LEAP_NUM_DAYS[epoch_fields[MONTH]]
                : NUM_DAYS[epoch_fields[MONTH]]);

        long millis = dayToMillis(time2Day);
        long millisInDay = fields[MILLISECOND]
                + fields[SECOND] * ONE_SECOND
                + fields[MINUTE] * ONE_MINUTE
                + fields[HOUR_OF_DAY] * ONE_HOUR;
        long epoch_hour = epoch_fields[HOUR_OF_DAY] * ONE_HOUR + epoch_fields[MINUTE] * ONE_MINUTE;
        time = millis + millisInDay - signum * epoch_hour;
    }

    @Override
    public int getMinimum(int field) {
        return cld.getMinimum(field);
    }

    @Override
    public int getMaximum(int field) {
        return cld.getMaximum(field);
    }

    @Override
    public int getGreatestMinimum(int field) {
        return cld.getGreatestMinimum(field);
    }

    @Override
    public int getLeastMaximum(int field) {
        return cld.getLeastMaximum(field);
    }

    @Override
    public void add(int field, int amount) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void roll(int field, boolean up) {
        throw new UnsupportedOperationException("Not supported.");
    }
    //---------- End of class
}
