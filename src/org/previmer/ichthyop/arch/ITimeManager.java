/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.arch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.previmer.ichthyop.event.LastStepListener;
import org.previmer.ichthyop.event.NextStepListener;
import java.util.Calendar;

/**
 *
 * @author pverley
 */
public interface ITimeManager {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    public static final int ONE_SECOND = 1;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_YEAR = 365 * ONE_DAY;
    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
    public static final SimpleDateFormat INPUT_DURATION_FORMAT = new SimpleDateFormat("DDDD 'day(s)' HH 'hour(s)' mm 'minute(s)'");

////////////////////////////
// Definition of the methods
////////////////////////////
    public long getTime();

    public long get_tO();

    public int get_dt();

    public long getTransportDuration();

    public Calendar getCalendar();

    public boolean hasNextStep() throws Exception;

    public String timeToString();

    public void addNextStepListener(NextStepListener listener);

    public void addLastStepListener(LastStepListener listener);

    public void firstStepTriggered() throws Exception;

    public int index();

    public int getNumberOfSteps();

    public String stepToString();

    public void lastStepTriggered();

    public long duration2seconds(String duration) throws ParseException;

    public long date2seconds(String date) throws ParseException;

    public SimpleDateFormat getInputDurationFormat();

    public SimpleDateFormat getInputDateFormat();
}
