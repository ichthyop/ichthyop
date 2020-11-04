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
    final public static Version V30B = new Version("3.0b", "2010/07/08");
    final public static Version V31 = new Version("3.1", "2012/11/19");
    final public static Version V32 = new Version("3.2", "2012/11/20");
    final public static Version V33 = new Version("3.3", "2017/03/23");
    //
    final public static Version[] VALUES = new Version[]{V30B, V31, V32, V33};
    /*
     * 
     */
    private final String number;
    private final String date;

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
