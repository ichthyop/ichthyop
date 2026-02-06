/*
 *ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 *http://www.ichthyop.org
 *
 *Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-today
 *http://www.ird.fr
 *
 *Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 *Contributors (alphabetically sorted):
 *Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
 *Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 *Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 *Stephane POUS, Nathan PUTMAN.
 *
 *Ichthyop is a free Java tool designed to study the effects of physical and
 *biological factors on ichthyoplankton dynamics. It incorporates the most
 *important processes involved in fish early life: spawning, movement, growth,
 *mortality and recruitment. The tool uses as input time series of velocity,
 *temperature and salinity fields archived from oceanic models such as NEMO,
 *ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 *generates output files that can be post-processed easily using graphic and
 *statistical software.
 *
 *To cite Ichthyop, please refer to Lett et al. 2008
 *A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 *Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 *doi:10.1016/j.envsoft.2008.02.005
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation (version 3 of the License). For a full
 *description, see the LICENSE file.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.previmer.ichthyop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.manager.TimeManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class TestNcTime {

    /** Test when units are in seconds */
    @Test
    public void testDate1() throws Exception {

        // check for units in seconds.
        String units = "seconds since 1950-01-01 00:00:00";

        // first test the unit date
        assertEquals(1577836800.0, DatasetUtil.getDate(0, units));

        // then test the date + 800 days, i.e 1952-03-11
        assertEquals(1646956800.0, DatasetUtil.getDate(800 * 24 * 60 * 60, units));

        // then test the date - 800 days, i.e 1947-10-24
        assertEquals(1508716800.0, DatasetUtil.getDate(-800 * 24 * 60 * 60, units));

    }

    /** Test when units are in seconds, but seconds are not provided in units */
    @Test
    public void testDate5() throws Exception {

        // check for units in seconds.
        String units = "seconds since 1950-01-01 00:00";

        // first test the unit date
        assertEquals(1577836800.0, DatasetUtil.getDate(0, units));

        // then test the date + 800 days, i.e 1952-03-11
        assertEquals(1646956800.0, DatasetUtil.getDate(800 * 24 * 60 * 60, units));

        // then test the date - 800 days, i.e 1947-10-24
        assertEquals(1508716800.0, DatasetUtil.getDate(-800 * 24 * 60 * 60, units));

    }

    /** Test when units are in seconds, but seconds are not provided in units */
    @Test
    public void testDate2() throws Exception {

        // check for units in seconds.
        String units = "seconds since 1950-01-01";

        // first test the unit date
        assertEquals(1577836800.0, DatasetUtil.getDate(0, units));

        // then test the date + 800 days, i.e 1952-03-11
        assertEquals(1646956800.0, DatasetUtil.getDate(800 * 24 * 60 * 60, units));

        // then test the date - 800 days, i.e 1947-10-24
        assertEquals(1508716800.0, DatasetUtil.getDate(-800 * 24 * 60 * 60, units));

    }

    /** Test when units are in days, but seconds are not provided in units */
    @Test
    public void testDate3() throws Exception {

        // check for units in seconds.
        String units = "days since 2000-01-01";

        // first test the unit date
        assertEquals(3155673600.0, DatasetUtil.getDate(0, units));

        // then test the date + 800 days
        assertEquals(3224793600.0, DatasetUtil.getDate(800, units));

        // then test the date - 800 days
        assertEquals(3086553600.0, DatasetUtil.getDate(-800, units));

    }

    /** Test when units are in hours, but seconds are not provided in units */
    @Test
    public void testDate4() throws Exception {

        // check for units in seconds.
        String units = "hour since 2000-01-01";

        // first test the unit date
        assertEquals(3155673600.0, DatasetUtil.getDate(0, units));

        // then test the date + 800 days
        assertEquals(3224793600.0, DatasetUtil.getDate(800 * 24, units));

        // then test the date - 800 days
        assertEquals(3086553600.0, DatasetUtil.getDate(-800 * 24, units));

    }

    /** Test when units are in seconds */
    @Test
    public void testDurationNoLeap() throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse("2010-03-01 00:00:00", formatter);
        assertEquals(3474057600., TimeManager.getDurationNoLeap(TimeManager.DATE_REF, date));

        date = LocalDateTime.parse("2020-01-15 00:00:00", formatter);
        assertEquals(3785529600., TimeManager.getDurationNoLeap(TimeManager.DATE_REF, date));

        date = LocalDateTime.parse("2020-12-31 05:00:00", formatter);
        assertEquals(3815787600.0, TimeManager.getDurationNoLeap(TimeManager.DATE_REF, date));

    }

       /** Test when units are in seconds */
    @Test
    public void testDurationLeap() throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse("2010-03-01 00:00:00", formatter);
        assertEquals(3476390400., TimeManager.getDurationLeap(TimeManager.DATE_REF, date));

        date = LocalDateTime.parse("2020-01-15 00:00:00", formatter);
        assertEquals(3788035200., TimeManager.getDurationLeap(TimeManager.DATE_REF, date));

        date = LocalDateTime.parse("2020-12-31 05:00:00", formatter);
        assertEquals(3818379600.0, TimeManager.getDurationLeap(TimeManager.DATE_REF, date));

    }

    @Test
    public void testDatesLeapNoLeap() throws Exception {

        String units = "hour since 2000-01-01";

        // first test date 2020-12-31 05:00 in leap mode
        assertEquals(3818379600., DatasetUtil.getDateLeap(184085, units));

        // then test date 2020-12-31 05:00 in no leap mode
        assertEquals(3815787600.0, DatasetUtil.getDateNoLeap(183941, units));

        units = "days since 2000-03-05";
        // first test date 2020-12-31 05:00 in leap mode
        assertEquals(3818361600., DatasetUtil.getDateLeap(7606, units));

        // first test date 2020-12-31 05:00 in no-leap mode
        assertEquals(3815769600., DatasetUtil.getDateNoLeap(7601, units));

    }

}
