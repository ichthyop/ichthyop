/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 *
 * @author pverley
 */
public class StandardCalendar extends Calendar {

    final private long EPOCH_OFFSET;
    final private GregorianCalendar cld;

    public StandardCalendar(int year, int month, int day, int hour, int minute) {
        this(year, month, day, hour, minute, null);
    }

    public StandardCalendar(int year, int month, int day, int hour, int minute, Date gregorianCutover) {

        cld = new GregorianCalendar();
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
//        complete();
//        cld.add(field, amount);
//        setTimeInMillis(cld.getTimeInMillis() - EPOCH_OFFSET);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void roll(int field, boolean up) {
        throw new UnsupportedOperationException("Not supported yet.");
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
