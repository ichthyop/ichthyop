/*
 * Copyright (C) 2011 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop;

/**
 *
 * @author pverley
 */
public class Version {

    /*
     * Careful with the dates. It should reflect the last change done in a
     * given version.
     * Ichthyop always expect that the date of older version is prior to a
     * newer version.
     */
    final public static Version v3_0_beta = new Version("3.0b", "2010/07/08");
    final public static Version v3_1 = new Version("3.1", "2012/11/19");
    final public static Version v3_2 = new Version("3.2", "2012/11/20");
    //
    final public static Version[] values = new Version[]{v3_0_beta, v3_1, v3_2};
    /*
     * 
     */
    private String number;
    private String date;

    public Version(String number, String date) {
        this.date = date;
        this.number = number;
    }

    public boolean priorTo(Version version) {
        if (null == date) {
            /*
             * I am an undated version so we can assume that I am older.
             */
            return true;
        } else if (null == version.date) {
            /*
             * I am a dated version and I am compared to an undated version so
             * we can assume that I am newer.
             */
            return false;
        } else {
            /*
             * We both are dated versions so let's compare dates.
             */
            return (date.compareTo(version.date) < 0);
        }
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return number + " (" + (date != null ? date : "undated") + ")";
    }
}
