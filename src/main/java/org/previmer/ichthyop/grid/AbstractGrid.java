/*
 *ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 *http://www.ichthyop.org
 *
 *Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-today
 *http://www.ird.fr
 *
 *Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 *Contributors (alphabetically sorted):
 *Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 *Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 *Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 *Stephane POUS, Nathan PUTMAN.
 *
 *Ichthyop is a free Java tool designed to study the effects of physical and
 *biological factors on ichthyoplankton dynamics. It incorporates the most
 *important processes involved in fish early life: spawning, movement, growth,
 *mortality and recruitment. The tool uses as input time series of velocity,
 *temperature and salinity fields archived from oceanic models such as NEMO,
 *ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 *generates output files that can be post-processed easily using graphic and
 *statistical software.
 *
 *To cite Ichthyop, please refer to Lett et al. 2008
 *A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 *Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 *doi:10.1016/j.envsoft.2008.02.005
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation (version 3 of the License). For a full
 *description, see the LICENSE file.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.previmer.ichthyop.grid;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.SetupListener;

public abstract class AbstractGrid extends SimulationManagerAccessor implements InitializeListener, SetupListener {

    private final String filename;
    private final String gridKey;

    /**
     * Grid dimension
     */
    private int nx, ny, nz;

    /**
     * Origin for grid index
     */
    private int ipo, jpo;

    private double lonMin, latMin, lonMax, latMax;
    
    public abstract void init() throws Exception;
    public abstract void setUp() throws Exception;
    public abstract boolean is3D();
    public abstract double getBathy(int i, int j);
    public abstract double getLon(int i, int j);
    public abstract double getLat(int i, int j);
    public abstract double getdxi(int j, int i);
    public abstract double getdeta(int j, int i);
    public abstract boolean isInWater(int i, int j);
    public abstract void loadParameters();
    public abstract boolean isInWater(double[] pGrid);
    public abstract boolean isCloseToCost(double[] pGrid);
    public abstract double depth2z(double x, double y, double depth);
    public abstract double z2depth(double x, double y, double z);
    public abstract double[] xy2latlon(double xRho, double yRho);
    public abstract double[] latlon2xy(double lat, double lon);
    public abstract boolean isOnEdge(double[] pGrid);
    
    public AbstractGrid(String filename) {
        this.filename = filename;
        this.gridKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }
    
    
    boolean enhanced() {
        try {
            return Boolean.valueOf(getParameter("enhanced_mode"));
        } catch (NullPointerException ex ) {
            return true;
        }
    }

    public String getParameter(String key) {
        return getSimulationManager().getGridManager().getParameter(gridKey, key);
    }
    
    public boolean findParameter(String key) {
        // Check whether the parameter can be retrieved
        try {
            getSimulationManager().getGridManager().getParameter(gridKey, key);
        } catch (NullPointerException ex) {
            // Tue parameter does not exist
            return false;
        }
        // The parameter does exist
        return true;
    }

    public String getFilename() {
        return this.filename;
    }
    
    public int get_nx() {
        return this.nx;
    }

    public int get_ny() {
        return this.ny;
    }

    public int get_nz() {
        return this.nz;
    }

    public void set_nx(int value) {
        this.nx = value;
    }

    public void set_ny(int value) {
        this.ny = value;
    }

    public void set_nz(int value) {
        this.nz = value;
    }

    public int get_ipo() {
        return this.ipo;
    }

    public int get_jpo() {
        return this.jpo;
    }

    public void set_ipo(int value) {
        this.ipo = value;
    }

    public void set_jpo(int value) {
        this.jpo = value;
    }
    
    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    public double getLonMax() {
        return lonMax;
    }

}
