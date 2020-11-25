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

package org.previmer.ichthyop.util;

/** import java.util */
import java.util.Calendar;

/**
 * This interface defines and declares the constants used in the application.
 * It is been decided to gather the declaration of the constants in a single
 * file to avoid the multiplication of constant definitions troughout the code.
 *
 * <p>This class should no be modified unless you know exactly what you are
 * doing.</p>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public interface Constant {

///////////////////////////
// Definition of the labels
///////////////////////////

    /** Generic constant for characterizing the absence of any action */
    final public static int NONE = -1;

    /** Generic constant for characterizing an error status */
    final public static int ERROR = -1;

    /** Label for SINGLE mode  */
    final public static int SINGLE = 0;
    /** Label for SERIAL mode  */
    final public static int SERIAL = 1;

    /** Label for ROMS simulation */
    final public static int ROMS = 0;
    /** Label for MARS simulation */
    final public static int MARS = 1;

    /** Label for Gregorian calendar */
    final public static int GREGORIAN = 0;
    /** Label for Climatology calendar (360 days-a-year) */
    final public static int CLIMATO = 1;

    /** Label for Euler numerical scheme */
    final public static int EULER = 0;
    /** Label for Runge Kutta 4 numerical scheme */
    final public static int RK4 = 1;

    /** Label for recruitment with a criterion based on particle's age */
    final public static int RECRUIT_AGE = 1;
    /** Label for recruitment with a criterion based on particle's length */
    final public static int RECRUIT_LENGTH = 2;

    /** Label for characterizing release zone */
    final public static int RELEASE = 0;
    /** Label for characterizing recruitment zone */
    final public static int RECRUITMENT = 1;
    /** Label for characterizing orientation zones */
    final public static int ORIENTATION = 2; 

    /** Label for two-dimension simulation */
    final public static int SIMU_2D = 0;
    /** Label for three-dimension simulation */
    final public static int SIMU_3D = 2;

    /** Horizontal alignement of the colorbar*/
    final public static int HORIZONTAL = 0;
    /** Vertical alignement of the colorbar*/
    final public static int VERTICAL = 1;

    /** Label for zone release mode */
    final public static int RELEASE_ZONE = 0;
    /** Label for release mode in which initial coordinates of the
     * particles are read from a text input file */
    final public static int RELEASE_TXTFILE = 1;
    /** Label for release mode in which initial coordinates of the
     * particles are read from a netcdf ichthyop output file */
    final public static int RELEASE_NCFILE = 2;

    /** Label for the display of particle depth */
    final public static int DISPLAY_DEPTH = 0;
    /** Label for the display of particle initial zone */
    final public static int DISPLAY_ZONE = 1;
    /** Label for the display of sea water temperature at particle location */
    final public static int DISPLAY_TP = 2;

    /** Label for generating text output file (not available yet) */
    final public static int RECORD_TXT = 1;
    /** Label for generating NetCDF output file */
    final public static int RECORD_NC = 2;

    /** Label for alive particle */
    final public static int DEAD_NOT = 0;
    /** Label for out-of-domain particle */
    final public static String DEAD_OUT = "out";
    /** Label for dead-cold particle (dead cold egg when growth
     * is simulated).*/
    final public static String DEAD_COLD = "frozen";
    /** label for beached particle */
    final public static String DEAD_BEACH = "beached";
    /** Label for old dead particle */
    final public static String DEAD_OLD = "old";
    /** Label for dead cold larva, only when growth is simulated */
    final public static int DEAD_COLD_LARVE = 3;

    /** Label for the NetCDF Dimension type */
    public final static int DIMENSION = 0;
    /** Label for the NetCDF Variable type */
    public final static int VARIABLE = 1;
    /** Label for the NetCDF Global attribute type */
    public final static int ATTRIBUTE = 2;

    /** Label for isodepth transport */
    public static final int ISODEPTH = 1;
    /** Label for Diel Vertical Migration model */
    public static final int DVM = 2;

//////////////////////////
// Definition of constants
//////////////////////////

    // Time arrow constants
    public static final int FORWARD = 1;
    public static final int BACKWARD = -1;

    // Time constants
    public static final int ONE_SECOND = 1;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int ONE_HOUR = 60 * ONE_MINUTE;
    public static final int ONE_DAY = 24 * ONE_HOUR;

    // Default values for the range of the particle colorbar.
    public static final float TP_MIN = 10.0f;
    public static final float TP_MAX = 20.0f;
    public static final float BATHY_MIN = 0.0f;
    public static final float BATHY_MAX = 100.0f;

    // Date origin of the Gregorian calendar 1900/01/01 00:00
    public static final int YEAR_ORIGIN = 1900;
    public static final int MONTH_ORIGIN = Calendar.JANUARY;
    public static final int DAY_ORIGIN = 1;

    // Threshold for CFL error message
    public static final float THRESHOLD_CFL = 1.0f;

    // Default ouput filename
    public static final String OUTPUT_FILENAME_SINGLE = "single_simu";
    public static final String OUTPUT_FILENAME_SERIAL = "serial_simu";

///////////////////////////
// ROMS default field names
///////////////////////////

    final public static String XI_DIM_R = "xi_rho";
    final public static String ETA_DIM_R = "eta_rho";
    final public static String Z_DIM_R = "s_rho";
    final public static String TIME_DIM_R = "time";
    final public static String LON_R = "lon_rho";
    final public static String LAT_R = "lat_rho";
    final public static String MASK_R = "mask_rho";
    final public static String BATHY_R = "h";
    final public static String U3D_R = "u";
    final public static String V3D_R = "v";
    final public static String U2D_R = "ubar";
    final public static String V2D_R = "vbar";
    final public static String ZETA_R = "zeta";
    final public static String TP_R = "temp";
    final public static String SAL_R = "salt";
    final public static String TIME_R = "scrum_time";
    final public static String PN = "pn";
    final public static String PM = "pm";
    final public static String THETA_S = "theta_s";
    final public static String THETA_B = "theta_b";
    final public static String HC = "hc";
    final public static String KV_R = "AKt";

///////////////////////////
// MARS default field names
///////////////////////////

    final public static String XI_DIM_M = "longitude";
    final public static String ETA_DIM_M = "latitude";
    final public static String Z_DIM_M = "z";
    final public static String TIME_DIM_M = "time";
    final public static String LON_M = "longitude";
    final public static String LAT_M = "latitude";
    final public static String BATHY_M = "h0";
    final public static String U3D_M = "uz";
    final public static String V3D_M = "vz";
    final public static String U2D_M = "u";
    final public static String V2D_M = "v";
    final public static String ZETA_M = "xe";
    final public static String TP_M = "temp";
    final public static String SAL_M = "sal";
    final public static String TIME_M = "time";
    final public static String SIGMA = "z";
    final public static String KV_M = "kz";

    //---------- End of the interface
}
