package org.previmer.ichthyop.util;

import java.text.ParseException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.previmer.ichthyop.calendar.Day360Calendar;
import org.previmer.ichthyop.calendar.FlawedCalendar;
import org.previmer.ichthyop.calendar.InterannualCalendar;

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

        if (isGregorian) {
            Calendar cld = new FlawedCalendar(1900, Calendar.JANUARY, 1, 0, 0);
            //Calendar cld = new FlawedCalendar(1858, Calendar.NOVEMBER, 17, 0, 0);
            dateFormat.setCalendar(cld);
            cld.setTime(dateFormat.parse(date));
           System.out.println(date + " (flawed " + typeDate + ") <==> " + (cld.getTimeInMillis() / 1000L) + " [second]");
        }
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

        if (isGregorian) {
            Calendar cld = new FlawedCalendar(1900, Calendar.JANUARY, 1, 0, 0);
            //Calendar cld = new FlawedCalendar(1858, Calendar.NOVEMBER, 17, 0, 0);
            dateFormat.setCalendar(cld);
            cld.setTimeInMillis(time * 1000L);
            System.out.println(time + " [second] <==> " + dateFormat.format(cld.getTime()) + " (flawed " + typeDate + ")");
        }
    }

    /**
     * 
     * @param t
     */
    public static void error(Throwable t) {

        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(t.getClass().getSimpleName());
        message.append(": ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());
    }
}
