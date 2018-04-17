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
import org.ichthyop.dataset.variable.NetcdfTiledArray;

/**
 * Class that is dedicated to the interpolation by using closest distance.
 *
 * 
 * @author pverley
 * @author nbarrier
 */
public class InterpolationClosest extends AbstractInterpolation {

    // constants
    private final int IDW_RADIUS = 2;

    
    public InterpolationClosest(AbstractDatasetVariable var)
    {
        super(var);
    }

    // interpolate Inverse Distance Weight
    @Override
    public double interpolate(double[] pGrid, double time) {

        double value;
        NetcdfTiledArray stack[] = this.getVar().getStack();

        boolean coast = this.getVar().getGrid().isCloseToCost(pGrid);
        
        int n[] = coast ? new int[]{0, 1} : new int[]{1 - IDW_RADIUS, 1 + IDW_RADIUS}; // 8 points

        // On the left, we round to the closest index (higher or lower int): 1.7 => 2
        // On the right, we take the lower index nontheless: 1.7 => 1
        int i = coast ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = coast ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = coast ? (int) Math.round(pGrid[2]) : (int) pGrid[2];
         
        double dt = stack.length > 1
                ? Math.abs((time - stack[0].getTimeStamp()) / (stack[1].getTimeStamp() - stack[0].getTimeStamp()))
                : 0.d;

 
        double dist;
        double distmin;
        int iindex = -1;
        int kindex = -1;
        int jindex = -1;
    
        // Reinitialize the minimum distance value
        distmin = Double.MAX_VALUE;

        // Loop over a square around the grid cell
        for (int ii=n[0]; ii<n[1]; ii++) {
            int ci = this.getVar().getGrid().continuity(i + ii);
            
            for (int jj = n[0]; jj < n[1]; jj++) {
                int cj = j + jj;
            
                for (int kk = n[0]; kk < n[1]; kk++) {
                    
                    // Here, we check that the point is within the array limit
                    // What about land-sea mask?
                    if (!this.getVar().isOut(ci, cj, k + kk)) {
                        // If the variable is in water, the distance is computed
                        dist = distance(pGrid, new int[]{i + ii, cj, k + kk});
                        
                        // If the computed distance is le than the initial minimum value
                        // the distmin array is updated and the i, j, k indexes are also updated
                        if (dist < distmin) {
                            distmin = dist;
                            iindex = i + ii;
                            jindex = j + jj;
                            kindex = k + kk;
                        }                     
                    }
                }
            }
        }
     
        // Computes the time-interpolation for the closest neighbour
        value = interpolateTime(iindex, jindex, kindex, dt);

        return value;

    }
    
    private double distance(double[] xyz, int[] ijk) {
        double distance = 0.d;
        for (int n = 0; n < xyz.length; n++) {
            distance += Math.abs(Math.pow(xyz[n] - ijk[n], 2));
        }
        return distance;
    }

}
