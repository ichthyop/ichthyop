package org.ichthyop.util;

import java.text.ParseException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.ichthyop.calendar.Day360Calendar;
import org.ichthyop.calendar.InterannualCalendar;

/**
 *
 * @author pverley
 */
public class TimeConverter {

    /**
     * 
     * @param date
     * @throws java.text.ParseException
     */
    public static void date2time(String date) throws ParseException {

        boolean isGregorian = date.split("/")[0].length() > 2;

        SimpleDateFormat dateFormat = isGregorian
                ? new SimpleDateFormat("yyyy/MM/dd HH:mm")
                : new SimpleDateFormat("yy/MM/dd HH:mm");

        Calendar calendar = isGregorian
                ? new InterannualCalendar(1900, Calendar.JANUARY, 1, 0, 0)
                //? new InterannualCalendar(1858, Calendar.NOVEMBER, 17, 0, 0)
                : new Day360Calendar(1900, Calendar.JANUARY, 1, 0, 0);

        String typeDate = isGregorian
                ? "gregorian"
                : "360_day";

        dateFormat.setCalendar(calendar);
        calendar.setTime(dateFormat.parse(date));
        System.out.println(date + " (" + typeDate + ") <==> " + (calendar.getTimeInMillis() / 1000L) + " [second]");
    }

    /**
     * 
     * @param time
     * @param isGregorian
     */
    public static void time2date(long time, boolean isGregorian) {

        SimpleDateFormat dateFormat = isGregorian
                ? new SimpleDateFormat("yyyy/MM/dd HH:mm")
                : new SimpleDateFormat("yy/MM/dd HH:mm");

        Calendar calendar = isGregorian
                ? new InterannualCalendar(1900, Calendar.JANUARY, 1, 0, 0)
                //? new InterannualCalendar(1858, Calendar.NOVEMBER, 17, 0, 0)
                : new Day360Calendar(1900, Calendar.JANUARY, 1, 0, 0);

        String typeDate = isGregorian
                ? "gregorian"
                : "360_day";

        dateFormat.setCalendar(calendar);
        calendar.setTimeInMillis(time * 1000L);
        System.out.println(time + " [second] <==> " + dateFormat.format(calendar.getTime()) + " (" + typeDate + ")");
    }

    /**
     * 
     * @param t
     */
    public static void error(Throwable t) {

        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuilder message = new StringBuilder(t.getClass().getSimpleName());
        message.append(": ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());
    }
}
