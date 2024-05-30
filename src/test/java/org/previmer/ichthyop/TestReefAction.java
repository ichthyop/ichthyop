/*
 *ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 *http://www.ichthyop.org
 *
 *Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-today
 *http://www.ird.fr
 *
 *Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 *Contributors (alphabetically sorted):
 *Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.previmer.ichthyop.action.orientation.ReefOrientationAction;
import org.previmer.ichthyop.io.ConfigurationFile;
import org.previmer.ichthyop.manager.ParameterManager;

import org.junit.jupiter.api.BeforeAll;


/**
 * Test of the Von Mises distributions. Expected values are extracted from
 * Python, using: import numpy as np from scipy.stats import vonmises kappa =
 * 3.99 mean, var, skew, kurt = vonmises.stats(kappa, moments='mvsk')
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class TestReefAction {

    ConfigurationFile cfgFile;
    ReefOrientationAction action;

    double[] lonp = new double[] {-3, 5, 8, 5, -5, -3};
    double[] latp = new double[] {0, 0, 13, 18, 10, 0};

    @BeforeAll
    public void prepareData() throws Exception {

        String configurationFile = this.getClass().getClassLoader().getResource("reef/reef.xml").getPath();
        File file = new File(configurationFile);
        ParameterManager.getInstance().setConfigurationFile(file);
        action = new ReefOrientationAction();
    }

    /** Testing the number of maps */
    @Test
    public void testClosestPoint() {

        double[] point = new double[2];
        double[] output;

        double expected[] = new double[] {8, 13};
        point = new double[] {12, 15};
        output = action.findClosestPointPolygon(point, lonp, latp);
        assertArrayEquals(expected, output);

        expected[0] = -3.5;
        expected[1] = 2.5;
        point = new double[] {-1, 3};
        output = action.findClosestPointPolygon(point, lonp, latp);
        assertArrayEquals(expected, output);

        expected[0] = -3.8461538462;
        expected[1] = 4.2307692308;
        point = new double[] {-10, 3};
        output = action.findClosestPointPolygon(point, lonp, latp);
        assertArrayEquals(expected, output);

    }

}
