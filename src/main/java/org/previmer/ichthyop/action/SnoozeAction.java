/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.previmer.ichthyop.particle.IParticle;

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
    
    @Override
    public void init(IParticle particle) {
        // Nothing to do
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
