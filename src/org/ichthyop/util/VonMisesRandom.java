/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2018
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
package org.ichthyop.util;

import java.util.Random;

/**
 * Construct a Von Mises circular random variable with circular (also intrinsic
 * mean) equal to mu and concentration parameter equal to kappa
 *
 * @author Robby McKilliam
 * @url https://github.com/eclab/mason/blob/master/mason/sim/util/distribution/VonMises.java
 */
public class VonMisesRandom {

    private final Random U = new MTRandom();

    protected double my_k;

    // cached vars for method nextDouble(a) (for performance only)
    private double k_set = -1.0;
    private double tau, rho, r;

    /**
     * Returns a random number from the distribution; bypasses the internal
     * state.
     *
     * @throws IllegalArgumentException if <tt>k &lt;= 0.0</tt>.
     */
    public double nextDouble(double k) {
        /**
         * ****************************************************************
         *                                                                *
         * Von Mises Distribution - Acceptance Rejection * *
         * ***************************************************************** *
         * FUNCTION : - mwc samples a random number from the von Mises *
         * distribution ( -Pi <= x <= Pi) with parameter    *
         *               k > 0 via rejection from the wrapped Cauchy * distibution. *
         * REFERENCE: - D.J. Best, N.I. Fisher (1979): Efficient * simulation of
         * the von Mises distribution, * Appl. Statist. 28, 152-157. *
         * SUBPROGRAM: - drand(seed) ... (0,1)-Uniform generator with * unsigned
         * long integer *seed. * * Implemented by F. Niederl, August 1992 *
         * ****************************************************************
         */
        double u, v, w, c, z;

        if (k <= 0.0) {
            throw new IllegalArgumentException();
        }

        if (k_set != k) {                                               // SET-UP
            tau = 1.0 + Math.sqrt(1.0 + 4.0 * k * k);
            rho = (tau - Math.sqrt(2.0 * tau)) / (2.0 * k);
            r = (1.0 + rho * rho) / (2.0 * rho);
            k_set = k;
        }

        // GENERATOR 
        do {
            u = U.nextDouble();                                // U(0/1) 
            v = U.nextDouble();                                // U(0/1) 
            z = Math.cos(Math.PI * u);
            w = (1.0 + r * z) / (r + z);
            c = k * (r - w);
        } while ((c * (2.0 - c) < v) && (Math.log(c / v) + 1.0 < c));         // Acceptance/Rejection 

        return (U.nextDouble() > 0.5) ? Math.acos(w) : -Math.acos(w);        // Random sign //
        // 0 <= x <= Pi : -Pi <= x <= 0 //
    }
}
