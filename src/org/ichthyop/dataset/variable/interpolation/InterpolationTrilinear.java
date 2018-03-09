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
package org.ichthyop.dataset.variable.interpolation;

import org.ichthyop.dataset.variable.AbstractDatasetVariable;
import org.ichthyop.dataset.variable.TiledVariable;

/**
 * Class that is dedicated to the 3D inverse distance interpolation,
 *
 * 
 * @author pverley
 * @author nbarrier
 */
public class InterpolationTrilinear extends AbstractInterpolation {

    // constants
    private final int IDW_POWER = 2;
    private final int IDW_RADIUS = 1;
    
    public InterpolationTrilinear(AbstractDatasetVariable var)
    {
        super(var);
    }

    /** Tri-linear interpolation. Calculation is done
     * following the Wikipedia method (https://en.wikipedia.org/wiki/Trilinear_interpolation).
     * @param pGrid
     * @param time
     * @return 
     */ 
    @Override
    public double interpolate(double[] pGrid, double time) {

        boolean coast = this.getVar().getGrid().isCloseToCost(pGrid);

        int i = coast ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = coast ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = coast ? (int) Math.round(pGrid[2]) : (int) pGrid[2];
        
        double dx1 = Math.abs(pGrid[0] - i);
        double dx2 = Math.abs(i + 1 - pGrid[0]);
        double dy1 = Math.abs(pGrid[1] - j);
        double dy2 = Math.abs(j + 1 - pGrid[1]);
        double dz1 = Math.abs(pGrid[2] - k);
        double dz2 = Math.abs(k + 1 - pGrid[2]);
        
        // Recovers the index with periodicity
        int ci = this.getVar().getGrid().xTore(i);
        int cip1 = this.getVar().getGrid().xTore(i+1);
        int cj = this.getVar().getGrid().yTore(j);
        int cjp1 = this.getVar().getGrid().yTore(j + 1);
        
        // Interpolates along the x dimension
        double temp1 = dx1 * getValue(ci, cj, k, time) + dx2 * getValue(cip1, cj, k, time);   // C00
        double temp2 = dx1 * getValue(ci, cjp1, k, time) + dx2 * getValue(cip1, cjp1, k, time);  // C10   
        double temp3 = dx1 * getValue(ci, cj, k+1, time) + dx2 * getValue(cip1, cj, k+1, time);  // C01
        double temp4 = dx1 * getValue(ci, cjp1, k+1, time) + dx2 * getValue(cip1, cjp1, k+1, time);  // C11 
        
        // Interpolates along the y dimension 
        double temp5 = dy1 * temp1 + dy2 * temp2;  // C0
        double temp6 = dy1 * temp3 + dy2 * temp4;  // C1
        
        // Interpolates along the z-dimension
        double value = dz1 * temp5  + dz2 * temp6;
        
        return value;

    }

    /** Returns the value used in the spatial interpolation.
     * If out of domain, 0 is returned. Else, return the time
     * interpolated data.
     * 
     * @param ci
     * @param cj
     * @param k
     * @param time
     * @return 
     */
    private double getValue(int ci, int cj, int k, double time) {

        if (this.getVar().isOut(ci, cj, k)) {
            return 0;
        } else {
            return interpolateTime(ci, cj, k, time);
        }
    }
    
    
    
}
