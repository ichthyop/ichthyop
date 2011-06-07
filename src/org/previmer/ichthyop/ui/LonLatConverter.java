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

        DegMinSec("Degree(s), minute(s), second(s)"),
        DecimalDeg("Decimal degree(s)"),
        DegDecimalMin("Degree(s), Decimal minute(s)"),
        Invalid("Invalid format");
        private String name;

        LonLatFormat(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static LonLatFormat getFormat(String value) {
        String testValue = value.trim();
        if (isDecimal(testValue)) {
            return LonLatFormat.DecimalDeg;
        } else if (testValue.endsWith("\"")) {
            return LonLatFormat.DegMinSec;
        } else if (testValue.endsWith("'")) {
            return LonLatFormat.DegDecimalMin;
        } else {
            return LonLatFormat.Invalid;
        }
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

    static public String convert(String value, LonLatFormat format) {

        LonLatFormat valueFormat = getFormat(value);
        if (valueFormat.equals(LonLatFormat.Invalid)) {
            throw new NumberFormatException("Invalid lon/lat format " + value);
        }

        if (valueFormat.equals(format)) {
            // nothing to do
            return value;
        }

        String newValue = value;
        switch (valueFormat) {
            case DecimalDeg:
                newValue = convert(decimalDegToDegDecimalMin(value), format);
                break;
            case DegDecimalMin:
                newValue = convert(degDecimalMinToDegMinSec(value), format);
                break;
            case DegMinSec:
                newValue = convert(degMinSecToDecimal(value), format);
                break;
        }

        return newValue;
    }

    static private String decimalDegToDegDecimalMin(String coord) throws NumberFormatException {

        if (!getFormat(coord).equals(LonLatFormat.DecimalDeg)) {
            throw new NumberFormatException("Expected format " + LonLatFormat.DecimalDeg.getName() + " but found " + getFormat(coord).getName());
        }
        double dCoord = Double.valueOf(coord);
        int deg = (int) dCoord;
        double minf = round(Math.abs(dCoord - deg), 5) * 60;
        StringBuffer strCoord = new StringBuffer();
        strCoord.append(deg);
        strCoord.append("° ");
        strCoord.append(minf);
        strCoord.append("\'");
        return strCoord.toString();
    }

    static private String degDecimalMinToDecimal(String coord) throws NumberFormatException {

        if (!getFormat(coord).equals(LonLatFormat.DegDecimalMin)) {
            throw new NumberFormatException("Expected format " + LonLatFormat.DegDecimalMin.getName() + " but found " + getFormat(coord).getName());
        }
        double deg = Double.valueOf(coord.substring(0, coord.indexOf('°')).trim());
        double min = Double.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")).trim());
        double decicoord = Math.signum(deg) * (Math.abs(deg) + (min / 60));
        return String.valueOf(round(decicoord, 5));
    }

    static private String degDecimalMinToDegMinSec(String coord) throws NumberFormatException {

        if (!getFormat(coord).equals(LonLatFormat.DegDecimalMin)) {
            throw new NumberFormatException("Expected format " + LonLatFormat.DegDecimalMin.getName() + " but found " + getFormat(coord).getName());
        }

        String deg = coord.substring(0, coord.indexOf('°')).trim();
        double minf = Double.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")).trim());
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

    static private String degMinSecToDegDecimalMin(String coord) throws NumberFormatException {

        if (!getFormat(coord).equals(LonLatFormat.DegMinSec)) {
            throw new NumberFormatException("Expected format " + LonLatFormat.DegMinSec.getName() + " but found " + getFormat(coord).getName());
        }

        String deg = coord.substring(0, coord.indexOf('°')).trim();
        double min = Double.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")).trim());
        double sec = Double.valueOf(coord.substring(coord.indexOf("\'") + 1, coord.indexOf("\"")).trim());

        double decMin = round(min + sec / 60, 5);

        StringBuffer strCoord = new StringBuffer();
        strCoord.append(deg);
        strCoord.append("° ");
        strCoord.append(decMin);
        strCoord.append("\'");
        return strCoord.toString();
    }

    static private String decimalToDegMinSec(double coord) throws NumberFormatException {

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

    static private String degMinSecToDecimal(String coord) throws NumberFormatException {

        double deg = Double.valueOf(coord.substring(0, coord.indexOf('°')).trim());
        double min = Double.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")).trim());
        double sec = Double.valueOf(coord.substring(coord.indexOf("\'") + 1, coord.indexOf("\"")).trim());
        double decicoord = Math.signum(deg) * (Math.abs(deg) + (min / 60) + (sec / 3600));
        return String.valueOf(round(decicoord, 5));
    }
}
