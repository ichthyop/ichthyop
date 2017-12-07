/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ichthyop.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 *
 * @author pverley
 */
public class GregorianCalendar extends Calendar {

    final private long EPOCH_OFFSET;
    final private java.util.GregorianCalendar cld;
    
    public GregorianCalendar() {
        this(1900, Calendar.JANUARY, 1, 0, 0);
    }

    public GregorianCalendar(int year, int month, int day, int hour, int minute) {
        this(year, month, day, hour, minute, null);
    }

    public GregorianCalendar(int year, int month, int day, int hour, int minute, Date gregorianCutover) {

        cld = new java.util.GregorianCalendar();
        if (null != gregorianCutover) {
            cld.setGregorianChange(gregorianCutover);
        }
        TimeZone tz = TimeZone.getTimeZone("GMT");
        SimpleTimeZone stz = new SimpleTimeZone(0, tz.getID());
        cld.setTimeZone(stz);
        cld.set(year, month, day, hour, minute);
        EPOCH_OFFSET = cld.getTimeInMillis();
    }

    @Override
    protected void computeTime() {

        cld.set(fields[YEAR], fields[MONTH], fields[DAY_OF_MONTH], fields[HOUR_OF_DAY], fields[MINUTE]);
        time = cld.getTimeInMillis() - EPOCH_OFFSET;
    }

    @Override
    protected void computeFields() {

        cld.setTimeInMillis(time + EPOCH_OFFSET);
        for (int i = 0; i < FIELD_COUNT; i++) {
            fields[i] = cld.get(i);
        }
    }

    @Override
    public void add(int field, int amount) {
        complete();
        cld.add(field, amount);
        setTimeInMillis(cld.getTimeInMillis() - EPOCH_OFFSET);
    }

    @Override
    public void roll(int field, boolean up) {
        complete();
        cld.roll(field, up);
        setTimeInMillis(cld.getTimeInMillis() - EPOCH_OFFSET);
    }

    @Override
    public int getMinimum(int field) {
        return cld.getMinimum(field);
    }

    @Override
    public int getMaximum(int field) {
        return cld.getMaximum(field);
    }

    @Override
    public int getGreatestMinimum(int field) {
        return cld.getGreatestMinimum(field);
    }

    @Override
    public int getLeastMaximum(int field) {
        return cld.getLeastMaximum(field);
    }

}
