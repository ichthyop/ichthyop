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

package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.event.NextStepListener;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public interface IDataset extends NextStepListener {

    public DistanceGetter getDistGetter();

    public void setUp() throws Exception ;

    public double[] latlon2xy(double lat, double lon);

    public double[] xy2latlon(double xRho, double yRho);

    public double depth2z(double x, double y, double depth);

    public double z2depth(double x, double y, double z);

    double get_dUx(double[] pGrid, double time);

    double get_dVy(double[] pGrid, double time);

    double get_dWz(double[] pGrid, double time);

    public boolean isInWater(double[] pGrid);

    public boolean isInWater(int i, int j);

    boolean isCloseToCost(double[] pGrid);

    public boolean isOnEdge(double[] pGrid);

    public double getBathy(int i, int j);

    public int get_nx();

    public int get_ny();

    public int get_nz();

    public double getdxi(int j, int i);

    public double getdeta(int j, int i);

    public void init() throws Exception;

    public Number get(String variableName, double[] pGrid, double time);

    public void requireVariable(String name, Class<?> requiredBy);

    public void removeRequiredVariable(String name, Class<?> requiredBy);

    public double getLatMin();

    public double getLatMax();

    public double getLonMin();

    public double getLonMax();

    public double getLon(int igrid, int jgrid);

    public double getLat(int igrid, int jgrid);

    public double getDepthMax();

    public boolean is3D();

    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception;

    public double xTore(double x);

    public double yTore(double y);

    public boolean isProjected();
}
