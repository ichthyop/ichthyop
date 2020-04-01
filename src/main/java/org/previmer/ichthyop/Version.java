/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
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
