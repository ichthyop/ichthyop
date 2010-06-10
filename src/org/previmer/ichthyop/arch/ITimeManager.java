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
