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
    
    double[] depthT = new double[] { 5624.9517, 5375.177, 5125.919, 4877.303, 4629.485, 4382.6543, 4137.047, 3892.9497,
            3650.712, 3410.7559, 3173.5884, 2939.8118, 2710.1333, 2485.3708, 2266.4536, 2054.4138, 1850.3655, 1655.4717,
            1470.893, 1297.7244, 1136.922, 989.2289, 855.1112, 734.715, 627.8525, 534.0197, 452.4429, 382.14438,
            322.0169, 270.8962, 227.62332, 191.09251, 160.284, 134.28227, 112.283485, 93.594124, 77.62451, 63.87905,
            51.94513, 41.481853, 32.20929, 23.89871, 16.363966, 9.454049, 3.0467727 };
            
    double[] depthW = new double[] { 5750.0, 5500.0063, 5250.476, 5001.522, 4753.283, 4505.9326, 4259.681, 4014.7896,
            3771.574, 3530.419, 3291.788, 3056.2346, 2824.4119, 2597.082, 2375.1194, 2159.5059, 1951.3186, 1751.701,
            1561.822, 1382.8185, 1215.7251, 1061.4005, 920.4562, 793.2019, 679.6166, 579.3501, 491.7565, 415.9509,
            350.88147, 295.4039, 248.3496, 208.58119, 175.03276, 146.73515, 122.82839, 102.56431, 85.30241, 70.50136,
            57.708504, 46.548466, 36.712124, 27.946293, 20.04454, 12.839149, 6.1942425, 0.0 };


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
        nemoGrid.readConstantField();
    }
}