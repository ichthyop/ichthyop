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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.previmer.ichthyop.action.orientation.ReefOrientationAction;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class TestVmDistribution {


    private double PREC = 1e-3;

    private double[] getValues(double mu, double kappa) {

        ReefOrientationAction action = new ReefOrientationAction();
        int N = 100000000;
        double[] values = new double[N];
        for(int k = 0; k < N; k++) {
            values[k] = action.randomVonMisesJava(0, kappa);
        }

        return values;

    }

    /** Test when units are in seconds */
    @Test
    public void testVM1() throws Exception {

        double kappa = 3.99;
        double mu = 0;

        double values[] = getValues(mu, kappa);

        double actual_mean = computeMean(values);
        double actual_var = computeVar(values, actual_mean);

        assertEquals(0, actual_mean, PREC);
        assertEquals(0.29917428538882523, actual_var, PREC);

    }

        /** Test when units are in seconds */
    @Test
    public void testVM2() throws Exception {

        double kappa = 7;
        double mu = 0;

        double values[] = getValues(mu, kappa);

        double actual_mean = computeMean(values);
        double actual_var = computeVar(values, actual_mean);

        assertEquals(0, actual_mean, PREC);
        assertEquals(0.15520174291472044, actual_var, PREC);

    }

    @Test
    public void testVM3() throws Exception {

        double kappa = 0.5;
        double mu = 0;

        double values[] = getValues(mu, kappa);

        double actual_mean = computeMean(values);
        double actual_var = computeVar(values, actual_mean);

        assertEquals(0, actual_mean, PREC);
        assertEquals(2.3488033436687363, actual_var, PREC);

    }

    private double computeMean(double[] values) {

        double result = 0;
        int N = values.length;
        for (int k = 0; k < N; k++) {
            result += values[k];
        }

        result /= N;
        return result;

    }

    private double computeVar(double[] values, double mean) {

        double result = 0;
        int N = values.length;
        for (int k = 0; k < N; k++) {
            result += Math.pow(values[k] - mean, 2);
        }

        result /= N;
        return result;

    }

}
