/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ichthyop.calendar;

import java.util.Date;

/**
 *
 * @author pverley
 */
public class JulianCalendar extends StandardCalendar {
    
    public JulianCalendar(int year, int month, int day, int hour, int minute) {
        super(year, month, day, hour, minute, new Date(Long.MAX_VALUE));
    }
    
}
