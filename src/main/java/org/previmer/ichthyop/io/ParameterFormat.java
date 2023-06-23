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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

package org.previmer.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum ParameterFormat {

    TEXT("value not set yet"),
    DATE("year 2000 month 01 day 01 at 00:00"),
    DURATION("0000 day(s) 01 hour(s) 00 minute(s)"),
    HOUR("12:00"),
    FLOAT("0.0"),
    INTEGER("0"),
    COMBO("value not set yet"),
    BOOLEAN("true"),
    FILE("file not set yet"),
    PATH(System.getProperty("user.dir")),
    TEXTFILE(System.getProperty("user.dir")),
    CLASS("class not set yet"),
    LIST("value not set yet"),
    ZONEFILE(System.getProperty("user.dir")),
    GRIDFILE(System.getProperty("user.dir")),
    VARFILE(System.getProperty("user.dir")),
    LONLAT("0.0");

    private String defaultValue;

    ParameterFormat(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static ParameterFormat getFormat(String formatStr) {
        for (ParameterFormat type : values()) {
            if (type.toString().equals(formatStr))
                return type;
        }
        return null;
    }

    public String getDefault() {
        return defaultValue;
    }

}
