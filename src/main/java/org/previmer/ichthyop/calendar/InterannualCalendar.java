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

package org.previmer.ichthyop.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
/** Import the abstract class Calendar */
import java.util.Calendar;

/**
 * <p>
 * The class extends the abstact class {@link java.util.Calendar}. It provides
 * methods for converting between a specific instant in time and a set of fields
 * such as <code>YEAR</code>, <code>MONTH</code>, <code>DAY_OF_MONTH</code>,
 * <code>HOUR</code>, and so on, according with the Gregorian calendar. An
 * instant in time is represented by a millisecond value that is, by default, an
 * offset from January 1, 1900 00:00:00.000 GMT. The origin can be set by the
 * user in one of the constructors.
 * </p>
 * The class is a very simplified version of the Gregorian Calendar, except that
 * the epoch is not automatically set to January 1, 1970 (Gregorian), midnight
 * UTC.
 *
 * @author P.Verley 2007
 * @see java.util.Calendar
 * @see java.util.GregorianCalendar
 */
public class InterannualCalendar extends Calendar {

    /**
    	 *
    	 */
    private static final long serialVersionUID = 6855081607747702909L;
    ///////////////////////////////
    // Declaration of the variables
    ///////////////////////////////
    /**
     * Origin of time
     */
    final private int[] epoch_fields;
    ///////////////////////////////
    // Declaration of the constants
    ///////////////////////////////
    public static final int ONE_SECOND = 1000;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;
    private static final int NUM_DAYS[] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 }; // 0-based, for
                                                                             // day-in-year
    private static final int LEAP_NUM_DAYS[] = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 }; // 0-based,
                                                                                                          // for
                                                                                                          // day-in-year

    ///////////////
    // Constructors
    ///////////////
    /**
     * Constructs a Gregorian Calendar with origin of time January 1, 1900
     * 00:00:00.000 GMT.
     */
    public InterannualCalendar() {
        this(1582, OCTOBER, 15, 0, 0);
    }

    /**
     * Constructs a Gregorian Calendar with origin of time set by parameters. Hours,
     * minutes, seconds are automatically set to 00:00:00.000
     * 
     * @param year   an int, the year origin
     * @param month  an int, the month origin
     * @param day    an int, the day origin
     * @param hour   an int, the hour origin
     * @param minute an int, the minute origin
     */
    public InterannualCalendar(int year, int month, int day, int hour, int minute) {

        epoch_fields = new int[FIELD_COUNT];
        fields = new int[FIELD_COUNT];
        setEpoch(YEAR, year);
        setEpoch(MONTH, month);
        setEpoch(DAY_OF_MONTH, day);
        setEpoch(HOUR_OF_DAY, hour);
        setEpoch(MINUTE, minute);
        setEpoch(SECOND, 0);
        setTimeInMillis(0);
    }

    public InterannualCalendar(String time_origin, SimpleDateFormat INPUT_DATE_FORMAT) throws ParseException {
        Calendar calendar_o = Calendar.getInstance();
        calendar_o.setTime(INPUT_DATE_FORMAT.parse(time_origin));
        int year_o = calendar_o.get(Calendar.YEAR);
        int month_o = calendar_o.get(Calendar.MONTH);
        int day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
        int hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
        int min_o = calendar_o.get(Calendar.MINUTE);
        epoch_fields = new int[FIELD_COUNT];
        fields = new int[FIELD_COUNT];
        setEpoch(YEAR, year_o);
        setEpoch(MONTH, month_o);
        setEpoch(DAY_OF_MONTH, day_o);
        setEpoch(HOUR_OF_DAY, hour_o);
        setEpoch(MINUTE, min_o);
        setEpoch(SECOND, 0);
        setTimeInMillis(0);
    }

    ////////////////////////////
    // Definition of the methods
    ////////////////////////////
    /**
     * Sets the given epoch field (origin of time) to the given value.
     *
     * @param field an int, one of the field of the epoch.
     * @param value an int, the value of the given field.
     * @see java.util.Calendar for details about the available fields.
     */
    private void setEpoch(int field, int value) {
        epoch_fields[field] = value;
    }

    /**
     * Converts time as milliseconds to day
     *
     * @param millis a long, the time in millisecond
     * @return long, the floor of the quotient millis / (24 * 3600 * 1000)
     */
    private static long millisToDay(long millis) {
        return (millis / ONE_DAY);
    }

    /**
     * Converts time as day to milliseconds
     *
     * @param day a long, the time in day
     * @return long the time in millisecond
     */
    private static long dayToMillis(long day) {
        return day * ONE_DAY;
    }

    /**
     * Converts time as milliseconds to time field values.
     */
    @Override
    protected void computeFields() {
        int rawYear, year, month, dayOfMonth, dayOfYear;
        boolean isLeap;
        long timeInDay = millisToDay(time);

        long timeInDay_o = timeInDay
                + (isLeap(epoch_fields[YEAR]) ? LEAP_NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1
                        : NUM_DAYS[epoch_fields[MONTH]] + epoch_fields[DAY_OF_MONTH] - 1);

        long nbDays = 0;
        int currentYear = epoch_fields[YEAR];
        while (nbDays < timeInDay_o) {
            nbDays += isLeap(currentYear) ? 366 : 365;
            currentYear++;
        }
        --currentYear;
        nbDays -= isLeap(currentYear) ? 366 : 365;
        rawYear = (int) currentYear;
        dayOfYear = (int) (timeInDay_o - nbDays);
        isLeap = isLeap(rawYear);

        if (dayOfYear > (isLeap ? 365 : 364)) {
            dayOfYear -= (isLeap ? 366 : 365);
            rawYear++;
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

        int millisInDay = (int) (time - timeInDay * ONE_DAY);
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
     * Determines if the given year is a leap year. Returns true if the given year
     * is a leap year.
     *
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     */
    private boolean isLeap(int year) {
        return ((year % 4 == 0) && ((!(year % 100 == 0)) | (year % 400 == 0)));
    }

    /**
     * Converts time field values to milliseconds.
     */
    @Override
    protected void computeTime() {

        int yearOn = fields[YEAR];
        int monthOn = fields[MONTH];
        boolean isLeap = isLeap(yearOn);
        long time2Day = (long) (fields[DAY_OF_MONTH] - 1);
        try {
            time2Day += isLeap ? (long) (LEAP_NUM_DAYS[monthOn]) : (long) (NUM_DAYS[monthOn]);
        } catch (Exception ex) {
        }

        for (int incYear = epoch_fields[YEAR]; incYear < yearOn; incYear++) {
            time2Day += isLeap(incYear) ? 366 : 365;
        }

        time2Day -= (epoch_fields[DAY_OF_MONTH] - 1);
        time2Day -= isLeap(epoch_fields[YEAR]) ? LEAP_NUM_DAYS[epoch_fields[MONTH]] : NUM_DAYS[epoch_fields[MONTH]];

        long millis = dayToMillis(time2Day);
        int millisInDay = fields[MILLISECOND]
                + 1000 * (fields[SECOND] + 60 * (fields[MINUTE] + 60 * fields[HOUR_OF_DAY]));
        time = millis + millisInDay;
    }

    //////////////////////////////////
    // Inherited methods not redefined
    //////////////////////////////////
    @Override
    public int getGreatestMinimum(int field) {
        return 0;
    }

    @Override
    public int getLeastMaximum(int field) {
        return 0;
    }

    @Override
    public int getMaximum(int field) {
        return 0;
    }

    @Override
    public int getMinimum(int field) {
        return 0;
    }

    @Override
    public void add(int field, int amount) {
    }

    @Override
    public void roll(int field, boolean up) {
    }
    // ---------- End of class
}
