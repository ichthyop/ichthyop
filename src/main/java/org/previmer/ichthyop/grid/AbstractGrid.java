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

public abstract class AbstractGrid extends SimulationManagerAccessor {

    private final String gridFileName;
    private final String gridKey;

    /**
     * Grid dimension
     */
    private int nx, ny, nz;

    /**
     * Origin for grid index
     */
    private int ipo, jpo;

    public abstract void init();
    public abstract void setup();

    public abstract boolean is3D();

    public AbstractGrid(String gridFileName) {
        this.gridFileName = gridFileName;
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
        return getSimulationManager().getDatasetManager().getParameter(gridKey, key);
    }

    
    public int getNx() {
        return this.nx;
    }

    public int getNy() {
        return this.ny;
    }

    public int getNz() {
        return this.nz;
    }

    public void setNx(int value) {
        this.nx = value;
    }

    public void setNy(int value) {
        this.ny = value;
    }

    public void setNz(int value) {
        this.nz = value;
    }

    public int getIpo() {
        return this.ipo;
    }

    public int getJpo() {
        return this.jpo;
    }

    public void setIpo(int value) {
        this.ipo = value;
    }

    public void setJpo(int value) {
        this.jpo = value;
    }

}
