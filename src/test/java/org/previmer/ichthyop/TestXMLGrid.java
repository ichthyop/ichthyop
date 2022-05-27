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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.previmer.GridType;
import org.previmer.ichthyop.io.GridFile;
import org.previmer.ichthyop.io.XGrid;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestXMLGrid extends SimulationManagerAccessor {
    
    private List<XGrid> grids;
    
    @BeforeAll
    public void prepareData() throws Exception {
        String fileName = getClass().getClassLoader().getResource("test-xmlgrid/grid-template.xml").getFile();
        File file = new File(fileName); 
        GridFile gridFile = new GridFile(file); 
        grids = gridFile.getGrids();
         
    }
    
    @Test
    public void countGrids() {
        assertEquals(3, grids.size());
    }
    
    @Test
    public void testNemoTypes() {
        XGrid nemoGrid = grids.get(0);
        assertEquals(true, nemoGrid.isEnabled());
        assertEquals(GridType.NEMO, nemoGrid.getType());
        assertEquals("grid-nemo", nemoGrid.getKey());
    }
    
    @Test
    public void testRomsTypes() {
        XGrid romsGrid = grids.get(1);
        assertEquals(false, romsGrid.isEnabled());
        assertEquals(GridType.ROMS, romsGrid.getType());
        assertEquals("grid-roms", romsGrid.getKey());
    }
    
    @Test
    public void testMarsTypes() {
        XGrid marsGrid = grids.get(2);
        assertEquals(true, marsGrid.isEnabled());
        assertEquals(GridType.MARS, marsGrid.getType());
        assertEquals("grid-mars", marsGrid.getKey());
    }
    
    @Test void testAddingGrid() { 
        XGrid regularGrid = new XGrid("grid-regular", "regular");
        assertEquals(true, regularGrid.isEnabled());
        assertEquals(GridType.REGULAR, regularGrid.getType());
        assertEquals("grid-regular", regularGrid.getKey());
        grids.add(regularGrid);
        assertEquals(4, grids.size());
        
    }
    
}