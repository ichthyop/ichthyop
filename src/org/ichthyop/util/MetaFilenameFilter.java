/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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

package org.ichthyop.util;

/** import java.util */
import java.util.regex.Pattern;

/** import java.io */
import java.io.File;

/**
 * This file name filter only uses shell meta-characters.
 * It accepts the following meta-character:
 * <ul>
 * <li> <b>?</b> for any single character
 * <li> <b>*</b> for any String.
 * </ul>
 * The class manipulates regex.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see java.io.FilenameFilter
 */
public class MetaFilenameFilter implements java.io.FilenameFilter {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The regex pattern
     */
    private final Pattern pattern;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new MetaFilenameFilter with the specified file mask.
     *
     * @param fileMask The file mask String.
     */
    public MetaFilenameFilter(String fileMask) {

        /** Add \Q \E around substrings of fileMask that are not
         * meta-characters */
        String regexpPattern = fileMask.replaceAll("[^\\*\\?]+", "\\\\Q$0\\\\E");
        /** Replace all "*" by the corresponding java regex meta-characters */
        regexpPattern = regexpPattern.replaceAll("\\*", ".*");
        /** Replace all "?" by the corresponding java regex meta-characters */
        regexpPattern = regexpPattern.replaceAll("\\?", ".");
        /** Create the pattern */
        this.pattern = Pattern.compile(regexpPattern);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found, not used here
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name matches the pattern;
     * <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
    }

    //---------- End of class
}
