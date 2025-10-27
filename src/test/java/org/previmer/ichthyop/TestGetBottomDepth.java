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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.previmer.ichthyop.action.orientation.ReefOrientationAction;
import org.previmer.ichthyop.dataset.AbstractDataset;
import org.previmer.ichthyop.dataset.Mars3dDataset;
import org.previmer.ichthyop.dataset.NemoDataset;
import org.previmer.ichthyop.dataset.Roms3dDataset;
import org.previmer.ichthyop.io.ConfigurationFile;
import org.previmer.ichthyop.manager.ParameterManager;

import org.junit.jupiter.api.BeforeAll;


/**
 * Test of the Von Mises distributions. Expected values are extracted from
 * Python, using: import numpy as np from scipy.stats import vonmises kappa =
 * 3.99 mean, var, skew, kurt = vonmises.stats(kappa, moments='mvsk')
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class TestGetBottomDepth {

    ConfigurationFile cfgFile;
    AbstractDataset dataset;


    /** Testing the number of maps */
    @Test
    public void TestMars3DDataset() {

        String configurationFile = this.getClass().getClassLoader().getResource("templates/cfg-mars3d.xml").getPath();
        File file = new File(configurationFile);
        try {
            ParameterManager.getInstance().setConfigurationFile(file);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dataset = new Mars3dDataset();
        try {
            dataset.setUp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        double pGrid[] = new double[] {20, 28};
        double bathy = dataset.getBottomDepth(pGrid);

        bathy = dataset.getBottomDepth(pGrid);
    }

    /** Testing the number of maps */
    @Test
    public void TestRoms3DDataset() {

        String configurationFile = this.getClass().getClassLoader().getResource("templates/cfg-roms3d.xml").getPath();
        File file = new File(configurationFile);
        try {
            ParameterManager.getInstance().setConfigurationFile(file);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dataset = new Roms3dDataset();
        try {
            dataset.setUp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        double pGrid[] = new double[] {11, 22};
        double bathy = dataset.getBottomDepth(pGrid);

                // here, the point is supposed to be inland, therefore bathy == 0
        pGrid = new double[] {21, 33};
        bathy = dataset.getBottomDepth(pGrid);

        assertEquals(0, bathy);
    }

    /** Testing the number of maps */
    @Test
    public void TestNemoDataset() {

        String configurationFile = this.getClass().getClassLoader().getResource("templates/cfg-nemo3d.xml").getPath();
        File file = new File(configurationFile);
        try {
            ParameterManager.getInstance().setConfigurationFile(file);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dataset = new NemoDataset();
        try {
            dataset.setUp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int i, j;
        double pGrid[];
        double bathy;

        int i = 15;
        int j = 10;
        double pGrid[] = new double[] {i, j};
        double bathy = dataset.getBottomDepth(pGrid);
        assertEquals(4490.5747, bathy, 2);

        i = 0;
        j = 0;
        pGrid = new double[] {i, j};
        bathy = dataset.getBottomDepth(pGrid);
        assertEquals(0, bathy, 2);

        i = 1;
        j = 8;
        pGrid = new double[] {i, j};
        bathy = dataset.getBottomDepth(pGrid);
        assertEquals(3261.2410, bathy, 2);

    }

}
