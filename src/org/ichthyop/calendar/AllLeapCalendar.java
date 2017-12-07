/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ichthyop.calendar;

/**
 *
 * @author pverley
 */
public class AllLeapCalendar extends InterannualCalendar {
    
    public AllLeapCalendar(int year, int month, int day, int hour, int minute) {
        super(year, month, day, hour, minute, Type.ALL_LEAP);
    }
    
    
    
}
