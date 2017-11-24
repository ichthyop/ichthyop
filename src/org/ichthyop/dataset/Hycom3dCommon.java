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
package org.ichthyop.dataset;

import org.ichthyop.dataset.variable.AbstractDatasetVariable;
import org.ichthyop.dataset.variable.WDatasetVariable;
import org.ichthyop.grid.AbstractRegularGrid;
import org.ichthyop.grid.RectilinearGrid;

/**
 *
 * @author pverley
 */
public abstract class Hycom3dCommon extends AbstractOceanDataset {

    public Hycom3dCommon(String prefix) {
        super(prefix);
    }

    @Override
    AbstractRegularGrid createGrid() {
        return new RectilinearGrid(getKey() + ".grid");
    }

    @Override
    public AbstractDatasetVariable createUVariable() {
        return createVariable("eastward_sea_water_velocity", NLAYER, TILING_H, TILING_V);
    }

    @Override
    public AbstractDatasetVariable createVVariable() {
        return createVariable("northward_sea_water_velocity", NLAYER, TILING_H, TILING_V);
    }

    @Override
    public AbstractDatasetVariable createWVariable() {
        int tilingv = Math.max(TILING_H * TILING_V / grid.get_nz(), 1);
        return new WDatasetVariable(
                createVariable("eastward_sea_water_velocity", NLAYER, tilingv, grid.get_nz()),
                createVariable("northward_sea_water_velocity", NLAYER, tilingv, grid.get_nz()),
                grid);
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        return getVariable("ocean_dataset_u").getDouble(pGrid, time) / getGrid().get_dx((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        return getVariable("ocean_dataset_v").getDouble(pGrid, time) / getGrid().get_dy((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        return getVariable("ocean_dataset_w").getDouble(pGrid, time) / getGrid().get_dz((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
    }
}
