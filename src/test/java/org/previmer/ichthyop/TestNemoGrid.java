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

package org.previmer.ichthyop;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.previmer.ichthyop.grid.NemoGrid;

public class TestNemoGrid extends SimulationManagerAccessor{
    
    private NemoGrid nemoGrid;

    @Test
    public void testNx() {
        assertEquals(26, nemoGrid.get_nx());
    }
    
    @Test
    public void testNy() {
        assertEquals(16, nemoGrid.get_ny());
    }
    
    @Test
    public void testNz() {
        assertEquals(45, nemoGrid.get_nz());
    }
    
    @Test
    public void testCyclic() {
        // nx = 26
        double precision = 1e-4;
        assertEquals(1, nemoGrid.getCyclicValue(25), precision);
        assertEquals(24, nemoGrid.getCyclicValue(0), precision);
        assertEquals(1, nemoGrid.getCyclicValue(1), precision);
        assertEquals(24, nemoGrid.getCyclicValue(24), precision);
        assertEquals(0.6, nemoGrid.getCyclicValue(24.6), precision);
        assertEquals(23.9, nemoGrid.getCyclicValue(-0.1), precision);
        
    }
    
    
    @Before 
    public void prepareData() throws Exception{
        String fileName = getClass().getResource("/test-nemo3d.xml").getFile();
        getSimulationManager().getParameterManager().setConfigurationFile(new File(fileName));
        nemoGrid = new NemoGrid();
        nemoGrid.loadParameters();
        nemoGrid.sortInputFiles();
        nemoGrid.getDimNC();
    }
}