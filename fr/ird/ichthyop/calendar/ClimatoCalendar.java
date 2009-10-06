package fr.ird.ichthyop.calendar;

/** Import the abstract class Calendar */
import java.util.Calendar;

/**
 * <p>The class extends the abstact class {@link java.util.Calendar}. It provides
 * methods for converting between a specific instant in time and a set of
 * fields such as <code>YEAR</code>, <code>MONTH</code>,
 * <code>DAY_OF_MONTH</code>, <code>HOUR</code>, and so on, according with the
 * 360 days-a-year calendar. An instant in time is represented by a millisecond
 * value that is an offset from January 1, year 1 00:00:00</p>
 * In a 360 days-a-year calendar 1 year = 12 monthes & 1 month = 30 days
 *
 * @author P.Verley 2007
 * @see java.util.Calendar
 */

public class ClimatoCalendar extends Calendar {

///////////////////////////////
// Declaration of the constants
///////////////////////////////

    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = 60 * ONE_SECOND;
    private static final int ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final long ONE_MONTH = 30 * ONE_DAY;
    private static final long ONE_YEAR = 12 * ONE_MONTH;

///////////////
// Constructors
///////////////

    /**
     * Constructs a 360-days-a-year calendar with origin of time
     * January 1, year 1 00:00:00
     *
     */
    public ClimatoCalendar() {

        fields = new int[FIELD_COUNT];
        set(YEAR, 1);
        set(MONTH, 1);
        set(DAY_OF_MONTH, 1);
        set(HOUR_OF_DAY, 0);
        set(MINUTE, 0);
        set(SECOND, 0);
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Converts time as milliseconds to time field values.
     */
    protected void computeFields() {

        int year, month, dayOfMonth, dayOfYear;
        long timeInDay = millisToDay(time);
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

        int millisInDay = (int) (time - (timeInDay * ONE_DAY));
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
    protected void computeTime() {

        long time2Day = (long) (fields[DAY_OF_MONTH] - 1 + fields[MONTH] * 30 +
                                (fields[YEAR] - 1) * 360);
        long millis = dayToMillis(time2Day);
        int millisInDay = 1000 *
                          (fields[SECOND] +
                           60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
        time = millis + millisInDay;

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
