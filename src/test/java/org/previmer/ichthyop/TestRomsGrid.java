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
import org.previmer.ichthyop.grid.RomsGrid;

import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestRomsGrid extends SimulationManagerAccessor {

    private RomsGrid romsGrid;
    private final double precision = 1e-3;

    private double[] s_rho = new double[] { -0.983333333333333, -0.95, -0.916666666666667, -0.883333333333333, -0.85,
            -0.816666666666667, -0.783333333333333, -0.75, -0.716666666666667, -0.683333333333333, -0.65,
            -0.616666666666667, -0.583333333333333, -0.55, -0.516666666666667, -0.483333333333333, -0.45,
            -0.416666666666667, -0.383333333333333, -0.35, -0.316666666666667, -0.283333333333333, -0.25,
            -0.216666666666667, -0.183333333333333, -0.15, -0.116666666666667, -0.0833333333333333, -0.05,
            -0.0166666666666667 };

    private double[] s_w = new double[] { -1, -0.966666666666667, -0.933333333333333, -0.9, -0.866666666666667,
            -0.833333333333333, -0.8, -0.766666666666667, -0.733333333333333, -0.7, -0.666666666666667,
            -0.633333333333333, -0.6, -0.566666666666667, -0.533333333333333, -0.5, -0.466666666666667,
            -0.433333333333333, -0.4, -0.366666666666667, -0.333333333333333, -0.3, -0.266666666666667,
            -0.233333333333333, -0.2, -0.166666666666667, -0.133333333333333, -0.1, -0.0666666666666667,
            -0.0333333333333333, 0 };

    private double[] Cs_r = new double[] { -0.9330103960714, -0.809234736243082, -0.698779852506126, -0.601008925572155,
            -0.515058561515485, -0.439938912698822, -0.374609180608102, -0.318031817255204, -0.269209327002322,
            -0.227207488202582, -0.191168387407458, -0.160316096993735, -0.133957252986043, -0.111478267917945,
            -0.0923404709172533, -0.0760741092400237, -0.0622718662187235, -0.0505823390259187, -0.0407037634668073,
            -0.0323781605226285, -0.0253860004109647, -0.0195414261290651, -0.01468804314646, -0.0106952599726512,
            -0.00745515186353988, -0.00487981407304801, -0.00289916971452338, -0.00145919897704131,
            -0.000520560097473743, -5.75774003738408e-05 };

    private double[] Cs_w = new double[] { -1, -0.869429795479072, -0.752375335993717, -0.648358846221329,
            -0.556616072909041, -0.476209483280913, -0.4061157365674, -0.345290064632543, -0.292711279777203,
            -0.247411330585219, -0.2084930394926, -0.175139141423755, -0.146615163717739, -0.122268134278327,
            -0.101522620848045, -0.0838752042221807, -0.0688881707843606, -0.0561829658539564, -0.0454337669897691,
            -0.0363614034315018, -0.0287277532969529, -0.0223306847108676, -0.0169995632230193, -0.0125913198106613,
            -0.00898705697568842, -0.00608916159254806, -0.00381888978335541, -0.00211438942163368,
            -0.000929128642234142, -0.000230703091829562, 0 };

    @Test
    public void testNx() {
        assertEquals(671, romsGrid.get_nx());
    }

    @Test
    public void testNy() {
        assertEquals(191, romsGrid.get_ny());
    }

    @Test
    public void testNz() {
        assertEquals(30, romsGrid.get_nz());
    }

    @Test
    public void testSigma() {
        assertArrayEquals(this.s_rho, romsGrid.getSigma(), precision);
    }

    @Test
    public void testSigmaW() {
        assertArrayEquals(this.s_w, romsGrid.getSigmaW(), precision);
    }

    public void testCsW() {
        assertArrayEquals(this.Cs_w, romsGrid.getCsW(), precision);
    }

    public void testCs() {
        assertArrayEquals(this.Cs_r, romsGrid.getCs(), precision);
    }

    @BeforeAll
    public void prepareData() throws Exception {
        String fileName = getClass().getClassLoader().getResource("test-roms/test-roms3d.xml").getFile();
        getSimulationManager().getParameterManager().setConfigurationFile(new File(fileName));

        romsGrid = new RomsGrid();
        romsGrid.loadParameters();
        romsGrid.openDataset();
        romsGrid.getDimNC();
        romsGrid.readConstantField();
        //romsGrid.reconstructDepth();
    }
}