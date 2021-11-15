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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.previmer.ichthyop.grid.NemoGrid;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestNemoGrid extends SimulationManagerAccessor {

    private NemoGrid nemoGrid;

    double[] depthT = new double[] { 5.62495166e+03, 5.37517676e+03, 5.12591895e+03, 4.87730322e+03,
        4.62948486e+03, 4.38265430e+03, 4.13704688e+03, 3.89294971e+03,
        3.65071191e+03, 3.41075586e+03, 3.17358838e+03, 2.93981177e+03,
        2.71013330e+03, 2.48537085e+03, 2.26645361e+03, 2.05441382e+03,
        1.85036548e+03, 1.65547168e+03, 1.47089294e+03, 1.29772437e+03,
        1.13692200e+03, 9.89228882e+02, 8.55111206e+02, 7.34715027e+02,
        6.27852478e+02, 5.34019714e+02, 4.52442902e+02, 3.82144379e+02,
        3.22016907e+02, 2.70896210e+02, 2.27623322e+02, 1.91092514e+02,
        1.60283997e+02, 1.34282272e+02, 1.12283485e+02, 9.35941238e+01,
        7.76245117e+01, 6.38790512e+01, 5.19451294e+01, 4.14818535e+01,
        3.22092896e+01, 2.38987103e+01, 1.63639660e+01, 9.45404911e+00,
        3.04677272e+00};

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
    
    @Test
    public void testDepthT() {
        
        double[] actual = new double[nemoGrid.get_nz()];
        double[][][] nemoDepthT = nemoGrid.getDepthT();
        for (int k = 0; k < nemoGrid.get_nz(); k++) { 
            actual[k] =   nemoDepthT[k][5][5]; 
        }
        assertArrayEquals(depthT, actual, 0.001);
        
    }
    
    @Test
    public void testDepthW() {
        
        double[] actual = new double[nemoGrid.get_nz() + 1];
        double[][][] nemoDepthW = nemoGrid.getDepthW();
        for (int k = 0; k < nemoGrid.get_nz() + 1; k++) { 
            actual[k] =   nemoDepthW[k][5][5]; 
        }
        assertArrayEquals(depthW, actual, 0.001);
        
    }

    @BeforeAll
    public void prepareData() throws Exception {
        String fileName = getClass().getClassLoader().getResource("test-nemo3d.xml").getFile();
        getSimulationManager().getParameterManager().setConfigurationFile(new File(fileName));
        nemoGrid = new NemoGrid();
        nemoGrid.loadParameters();
        nemoGrid.sortInputFiles();
        nemoGrid.getDimNC();
        nemoGrid.readConstantField();
    }
}