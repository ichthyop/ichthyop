/*
 *  Copyright (C) 2011 pverley
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.previmer.ichthyop.arch.IParticle;

/**
 *
 * @author pverley
 */
public class SnoozeAction extends AbstractAction {

    private Calendar calendar;
    private Date snooze, wakeup;

    public void loadParameters() throws Exception {
        calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        hourFormat.setCalendar(calendar);
        snooze = hourFormat.parse(getParameter("start_snooze"));
        wakeup = hourFormat.parse(getParameter("stop_snooze"));
    }

    public void execute(IParticle particle) {
        calendar.setTimeInMillis((long) (getSimulationManager().getTimeManager().getTime() * 1e3));
        long timeDay = getSecondsOfDay(calendar);
        calendar.setTime(snooze);
        long timeSnooze = getSecondsOfDay(calendar);
        calendar.setTime(wakeup);
        long timeWakeup = getSecondsOfDay(calendar);

        if (timeDay >= timeSnooze && timeDay < timeWakeup) {
            particle.increment(new double[]{0, 0, 0}, true, true);
        } else {
            // do nothing;
        }
    }

    private long getSecondsOfDay(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60;
    }
}
