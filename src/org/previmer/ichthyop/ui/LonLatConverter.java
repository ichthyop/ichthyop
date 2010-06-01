/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
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
package org.previmer.ichthyop.ui;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class LonLatConverter {

    public enum LonLatFormat {

        DegMinSec, Decimal;
    }

    public static LonLatFormat getFormat(String value) {
        return isDecimal(value) ? LonLatFormat.Decimal : LonLatFormat.DegMinSec;
    }

    private static boolean isDecimal(String input) {
        try {
            Float.parseFloat(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static private double round(double value, int n) {
        double r = (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
        return r;
    }

    static public String decimalToDegMin(double coord) {

        int deg = (int) coord;
        double minf = round(Math.abs(coord - deg), 5) * 60;
        int min = (int) minf;
        double sec = round(round((minf - min), 5) * 60, 5);

        StringBuffer strCoord = new StringBuffer();
        strCoord.append(deg);
        strCoord.append("° ");
        strCoord.append(min);
        strCoord.append("\' ");
        strCoord.append(sec);
        strCoord.append('\"');
        return strCoord.toString();
    }

    static public double degMinToDecimal(String coord) {

        double deg = Double.valueOf(coord.substring(0, coord.indexOf('°')).trim());
        double min = Double.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")).trim());
        double sec = Double.valueOf(coord.substring(coord.indexOf("\'") + 1, coord.indexOf("\"")).trim());
        double decicoord = Math.signum(deg) * (Math.abs(deg) + (min / 60) + (sec / 3600));
        return round(decicoord, 5);
    }
}
