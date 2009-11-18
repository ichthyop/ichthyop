/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

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

    public boolean hasNextStep();

    public String timeToString();

    public void addNextStepListener(NextStepListener listener);

    public void addLastStepListener(LastStepListener listener);

    public void firstStepTriggered();

    public int index();

    public int getNumberOfSteps();

    public String stepToString();


}
