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

    double[] depthT = new double[] { 5.62495166e+03, 5.37517676e+03, 5.12591895e+03, 4.87730322e+03, 4.62948486e+03,
            4.38265430e+03, 4.13704688e+03, 3.89294971e+03, 3.65071191e+03, 3.41075586e+03, 3.17358838e+03,
            2.93981177e+03, 2.71013330e+03, 2.48537085e+03, 2.26645361e+03, 2.05441382e+03, 1.85036548e+03,
            1.65547168e+03, 1.47089294e+03, 1.29772437e+03, 1.13692200e+03, 9.89228882e+02, 8.55111206e+02,
            7.34715027e+02, 6.27852478e+02, 5.34019714e+02, 4.52442902e+02, 3.82144379e+02, 3.22016907e+02,
            2.70896210e+02, 2.27623322e+02, 1.91092514e+02, 1.60283997e+02, 1.34282272e+02, 1.12283485e+02,
            9.35941238e+01, 7.76245117e+01, 6.38790512e+01, 5.19451294e+01, 4.14818535e+01, 3.22092896e+01,
            2.38987103e+01, 1.63639660e+01, 9.45404911e+00, 3.04677272e+00 };

    double[] depthW = new double[] { 5750.0, 5500.0063, 5250.476, 5001.522, 4753.283, 4505.9326, 4259.681, 4014.7896,
            3771.574, 3530.419, 3291.788, 3056.2346, 2824.4119, 2597.082, 2375.1194, 2159.5059, 1951.3186, 1751.701,
            1561.822, 1382.8185, 1215.7251, 1061.4005, 920.4562, 793.2019, 679.6166, 579.3501, 491.7565, 415.9509,
            350.88147, 295.4039, 248.3496, 208.58119, 175.03276, 146.73515, 122.82839, 102.56431, 85.30241, 70.50136,
            57.708504, 46.548466, 36.712124, 27.946293, 20.04454, 12.839149, 6.1942425, 0.0 };

    double[] e3t = new double[] { 249.99731, 249.53499, 248.96004, 248.24577, 247.35962, 246.26195, 244.90497,
            243.23154, 241.17409, 238.65388, 235.58086, 231.8545, 227.3662, 222.00368, 215.65831, 208.23524, 199.66656,
            189.9266, 179.04709, 167.12944, 154.35034, 140.9574, 127.25339, 113.5706, 100.23924, 87.55642, 75.761406,
            65.02142, 55.428616, 47.00673, 39.72388, 33.507984, 28.261766, 23.875618, 20.23743, 17.239403, 14.782234,
            12.777269, 11.147204, 9.825839, 8.757267, 7.894795, 7.1997576, 6.640353, 6.19057 };

    @Test
    public void testNx() {
        assertEquals(182, nemoGrid.get_nx());
    }

    @Test
    public void testNy() {
        assertEquals(149, nemoGrid.get_ny());
    }

    @Test
    public void testNz() {
        assertEquals(30, nemoGrid.get_nz());
    }

    // @Test
    // public void testCyclic() {
    // // nx = 26
    // double precision = 1e-4;
    // assertEquals(1, nemoGrid.getCyclicValue(25), precision);
    // assertEquals(24, nemoGrid.getCyclicValue(0), precision);
    // assertEquals(1, nemoGrid.getCyclicValue(1), precision);
    // assertEquals(24, nemoGrid.getCyclicValue(24), precision);
    // assertEquals(0.6, nemoGrid.getCyclicValue(24.6), precision);
    // assertEquals(23.9, nemoGrid.getCyclicValue(-0.1), precision);

    // }

    @Test
    public void testMask() {
        assertEquals(true, nemoGrid.isInWater(36, 55));
        assertEquals(false, nemoGrid.isInWater(36, 55, 10));
        assertEquals(true, nemoGrid.isInWater(29, 64));
        assertEquals(false, nemoGrid.isInWater(29, 64, 21));
        assertEquals(true, nemoGrid.isInWater(36, 62));
        assertEquals(true, nemoGrid.isInWater(36, 62, 9));
        assertEquals(true, nemoGrid.isInWater(35, 59));
        assertEquals(true, nemoGrid.isInWater(35, 59, 9));
        assertEquals(true, nemoGrid.isInWater(30, 63));
        assertEquals(false, nemoGrid.isInWater(30, 63, 25));
        assertEquals(true, nemoGrid.isInWater(24, 58));
        assertEquals(false, nemoGrid.isInWater(24, 58, 24));
        assertEquals(false, nemoGrid.isInWater(34, 63));
        assertEquals(false, nemoGrid.isInWater(34, 63, 13));
        assertEquals(false, nemoGrid.isInWater(34, 55));
        assertEquals(false, nemoGrid.isInWater(34, 55, 22));
        assertEquals(false, nemoGrid.isInWater(23, 56));
        assertEquals(false, nemoGrid.isInWater(23, 56, 29));
        assertEquals(true, nemoGrid.isInWater(36, 54));
        assertEquals(false, nemoGrid.isInWater(36, 54, 20));
        assertEquals(false, nemoGrid.isInWater(34, 64));
        assertEquals(false, nemoGrid.isInWater(34, 64, 12));
        assertEquals(true, nemoGrid.isInWater(35, 58));
        assertEquals(true, nemoGrid.isInWater(35, 58, 18));
        assertEquals(true, nemoGrid.isInWater(27, 61));
        assertEquals(false, nemoGrid.isInWater(27, 61, 10));
        assertEquals(false, nemoGrid.isInWater(34, 54));
        assertEquals(false, nemoGrid.isInWater(34, 54, 11));
        assertEquals(true, nemoGrid.isInWater(33, 60));
        assertEquals(false, nemoGrid.isInWater(33, 60, 9));
        assertEquals(true, nemoGrid.isInWater(35, 56));
        assertEquals(false, nemoGrid.isInWater(35, 56, 4));
        assertEquals(true, nemoGrid.isInWater(34, 58));
        assertEquals(true, nemoGrid.isInWater(34, 58, 16));
        assertEquals(false, nemoGrid.isInWater(32, 57));
        assertEquals(false, nemoGrid.isInWater(32, 57, 16));
        assertEquals(true, nemoGrid.isInWater(25, 60));
        assertEquals(false, nemoGrid.isInWater(25, 60, 19));
        assertEquals(true, nemoGrid.isInWater(32, 61));
        assertEquals(false, nemoGrid.isInWater(32, 61, 10));
        assertEquals(true, nemoGrid.isInWater(32, 60));
        assertEquals(false, nemoGrid.isInWater(32, 60, 20));
        assertEquals(false, nemoGrid.isInWater(32, 57));
        assertEquals(false, nemoGrid.isInWater(32, 57, 25));
        assertEquals(false, nemoGrid.isInWater(27, 54));
        assertEquals(false, nemoGrid.isInWater(27, 54, 3));
        assertEquals(true, nemoGrid.isInWater(27, 60));
        assertEquals(false, nemoGrid.isInWater(27, 60, 9));
        assertEquals(true, nemoGrid.isInWater(31, 58));
        assertEquals(false, nemoGrid.isInWater(31, 58, 17));
        assertEquals(true, nemoGrid.isInWater(32, 61));
        assertEquals(false, nemoGrid.isInWater(32, 61, 2));
        assertEquals(true, nemoGrid.isInWater(26, 60));
        assertEquals(false, nemoGrid.isInWater(26, 60, 21));
        assertEquals(true, nemoGrid.isInWater(36, 56));
        assertEquals(true, nemoGrid.isInWater(36, 56, 29));
        assertEquals(true, nemoGrid.isInWater(25, 64));
        assertEquals(true, nemoGrid.isInWater(25, 64, 4));
        assertEquals(true, nemoGrid.isInWater(22, 63));
        assertEquals(true, nemoGrid.isInWater(22, 63, 13));

    }

    @Test
    public void testMaskParticles() {

        double[] pGrid2d, pGrid3d;

        pGrid2d = new double[] { 32.177562, 60.439902 };
        pGrid3d = new double[] { 32.177562, 60.439902, 17.851227 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 24.919904, 58.238550 };
        pGrid3d = new double[] { 24.919904, 58.238550, 3.590780 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 23.114253, 60.063932 };
        pGrid3d = new double[] { 23.114253, 60.063932, 24.592239 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 29.970720, 54.191932 };
        pGrid3d = new double[] { 29.970720, 54.191932, 23.412250 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 20.341828, 57.015748 };
        pGrid3d = new double[] { 20.341828, 57.015748, 16.503921 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 34.091980, 60.601735 };
        pGrid3d = new double[] { 34.091980, 60.601735, 11.808316 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 20.079823, 56.900776 };
        pGrid3d = new double[] { 20.079823, 56.900776, 2.005843 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 31.522881, 60.180154 };
        pGrid3d = new double[] { 31.522881, 60.180154, 20.225434 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 24.590136, 58.287687 };
        pGrid3d = new double[] { 24.590136, 58.287687, 13.152738 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 32.498298, 55.354741 };
        pGrid3d = new double[] { 32.498298, 55.354741, 20.939612 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 36.357205, 56.982823 };
        pGrid3d = new double[] { 36.357205, 56.982823, 25.125087 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 24.228803, 59.699649 };
        pGrid3d = new double[] { 24.228803, 59.699649, 28.290124 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 29.794675, 59.908728 };
        pGrid3d = new double[] { 29.794675, 59.908728, 24.818297 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 30.064713, 59.743252 };
        pGrid3d = new double[] { 30.064713, 59.743252, 0.339708 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 29.728282, 60.532008 };
        pGrid3d = new double[] { 29.728282, 60.532008, 10.439364 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 23.792388, 60.521033 };
        pGrid3d = new double[] { 23.792388, 60.521033, 21.169726 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 36.196733, 58.314184 };
        pGrid3d = new double[] { 36.196733, 58.314184, 4.977261 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 27.601131, 62.965466 };
        pGrid3d = new double[] { 27.601131, 62.965466, 15.110062 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 34.388947, 57.675619 };
        pGrid3d = new double[] { 34.388947, 57.675619, 1.575802 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 31.891148, 58.358649 };
        pGrid3d = new double[] { 31.891148, 58.358649, 5.799899 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 25.056428, 62.919234 };
        pGrid3d = new double[] { 25.056428, 62.919234, 0.537132 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 33.834563, 62.061940 };
        pGrid3d = new double[] { 33.834563, 62.061940, 23.017233 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 26.740598, 61.038886 };
        pGrid3d = new double[] { 26.740598, 61.038886, 6.493816 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 34.978754, 55.002269 };
        pGrid3d = new double[] { 34.978754, 55.002269, 10.015199 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 29.881639, 63.194826 };
        pGrid3d = new double[] { 29.881639, 63.194826, 26.914358 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 34.989501, 61.142413 };
        pGrid3d = new double[] { 34.989501, 61.142413, 20.428018 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 31.773037, 63.988470 };
        pGrid3d = new double[] { 31.773037, 63.988470, 0.923329 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 32.329323, 55.494483 };
        pGrid3d = new double[] { 32.329323, 55.494483, 4.776131 };
        assertEquals(false, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 28.522514, 62.681261 };
        pGrid3d = new double[] { 28.522514, 62.681261, 18.022874 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(false, nemoGrid.isInWater(pGrid3d));
        pGrid2d = new double[] { 36.253422, 55.624929 };
        pGrid3d = new double[] { 36.253422, 55.624929, 16.739629 };
        assertEquals(true, nemoGrid.isInWater(pGrid2d));
        assertEquals(true, nemoGrid.isInWater(pGrid3d));

    }

    @Test
    public void testcloseToCoastParticles() {

        double[] pGrid3d;

        pGrid3d = new double[] { 27.864138, 62.739776, 25.795268 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 22.712241, 59.857330, 16.075374 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 35.654567, 62.996834, 7.198985 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 24.807870, 62.198495, 17.639074 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 21.110180, 57.813281, 23.003689 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 22.577400, 61.527881, 26.000478 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 35.157431, 57.451565, 19.005240 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 34.907540, 60.814606, 14.023901 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 33.125578, 59.312105, 26.683748 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 24.298059, 59.409615, 27.945148 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 34.711726, 57.292412, 17.965610 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 25.636351, 61.477968, 13.086388 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 24.701213, 62.906694, 11.851347 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 24.715353, 60.587516, 17.186502 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 30.286792, 60.447768, 25.825310 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 25.938844, 62.944070, 27.510525 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 34.216972, 61.172355, 16.689331 };
        assertEquals(true, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 35.582747, 57.807356, 15.718211 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 22.429547, 60.108933, 8.088254 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));
        pGrid3d = new double[] { 22.475056, 58.259612, 20.216463 };
        assertEquals(false, nemoGrid.isCloseToCoast(pGrid3d));

    }

    @Test
    public void testHorizontalInterpolationT() {
        double[][] expected = new double[4][];
        double[][] actual;
        double[] pGrid2d;

        expected[0] = new double[] { 32.000000, 32.000000, 33.000000, 33.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.930190, 0.930190, 0.069810, 0.069810 };
        expected[3] = new double[] { 0.159645, 0.840355, 0.159645, 0.840355 };
        pGrid2d = new double[] { 32.569810, 59.340355 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.188721, 0.188721, 0.811279, 0.811279 };
        expected[3] = new double[] { 0.362640, 0.637360, 0.362640, 0.637360 };
        pGrid2d = new double[] { 21.311279, 56.137360 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.995276, 0.995276, 0.004724, 0.004724 };
        expected[3] = new double[] { 0.513274, 0.486726, 0.513274, 0.486726 };
        pGrid2d = new double[] { 30.504724, 57.986726 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.267942, 0.267942, 0.732058, 0.732058 };
        expected[3] = new double[] { 0.106931, 0.893069, 0.106931, 0.893069 };
        pGrid2d = new double[] { 32.232058, 60.393069 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 27.000000, 27.000000, 28.000000, 28.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.022395, 0.022395, 0.977605, 0.977605 };
        expected[3] = new double[] { 0.965335, 0.034665, 0.965335, 0.034665 };
        pGrid2d = new double[] { 28.477605, 58.534665 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.128050, 0.128050, 0.871950, 0.871950 };
        expected[3] = new double[] { 0.027888, 0.972112, 0.027888, 0.972112 };
        pGrid2d = new double[] { 24.371950, 58.472112 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.529057, 0.529057, 0.470943, 0.470943 };
        expected[3] = new double[] { 0.557864, 0.442136, 0.557864, 0.442136 };
        pGrid2d = new double[] { 23.970943, 59.942136 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.092039, 0.092039, 0.907961, 0.907961 };
        expected[3] = new double[] { 0.394894, 0.605106, 0.394894, 0.605106 };
        pGrid2d = new double[] { 32.407961, 59.105106 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.963337, 0.963337, 0.036663, 0.036663 };
        expected[3] = new double[] { 0.296823, 0.703177, 0.296823, 0.703177 };
        pGrid2d = new double[] { 23.536663, 60.203177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 21.000000, 21.000000, 22.000000, 22.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.174903, 0.174903, 0.825097, 0.825097 };
        expected[3] = new double[] { 0.691688, 0.308312, 0.691688, 0.308312 };
        pGrid2d = new double[] { 22.325097, 59.808312 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 60.000000, 61.000000, 60.000000, 61.000000 };
        expected[2] = new double[] { 0.219603, 0.219603, 0.780397, 0.780397 };
        expected[3] = new double[] { 0.058214, 0.941786, 0.058214, 0.941786 };
        pGrid2d = new double[] { 31.280397, 61.441786 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 34.000000, 34.000000, 35.000000, 35.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.199100, 0.199100, 0.800900, 0.800900 };
        expected[3] = new double[] { 0.326823, 0.673177, 0.326823, 0.673177 };
        pGrid2d = new double[] { 35.300900, 59.173177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 61.000000, 62.000000, 61.000000, 62.000000 };
        expected[2] = new double[] { 0.440776, 0.440776, 0.559224, 0.559224 };
        expected[3] = new double[] { 0.230809, 0.769191, 0.230809, 0.769191 };
        pGrid2d = new double[] { 21.059224, 62.269191 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 28.000000, 28.000000, 29.000000, 29.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.817116, 0.817116, 0.182884, 0.182884 };
        expected[3] = new double[] { 0.946111, 0.053889, 0.946111, 0.053889 };
        pGrid2d = new double[] { 28.682884, 57.553889 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 32.000000, 32.000000, 33.000000, 33.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.310686, 0.310686, 0.689314, 0.689314 };
        expected[3] = new double[] { 0.776325, 0.223675, 0.776325, 0.223675 };
        pGrid2d = new double[] { 33.189314, 55.723675 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 29.000000, 29.000000, 30.000000, 30.000000 };
        expected[1] = new double[] { 56.000000, 57.000000, 56.000000, 57.000000 };
        expected[2] = new double[] { 0.312109, 0.312109, 0.687891, 0.687891 };
        expected[3] = new double[] { 0.094400, 0.905600, 0.094400, 0.905600 };
        pGrid2d = new double[] { 30.187891, 57.405600 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.673670, 0.673670, 0.326330, 0.326330 };
        expected[3] = new double[] { 0.588125, 0.411875, 0.588125, 0.411875 };
        pGrid2d = new double[] { 31.826330, 55.911875 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 24.000000, 24.000000, 25.000000, 25.000000 };
        expected[1] = new double[] { 61.000000, 62.000000, 61.000000, 62.000000 };
        expected[2] = new double[] { 0.121859, 0.121859, 0.878141, 0.878141 };
        expected[3] = new double[] { 0.870549, 0.129451, 0.870549, 0.129451 };
        pGrid2d = new double[] { 25.378141, 61.629451 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 34.000000, 34.000000, 35.000000, 35.000000 };
        expected[1] = new double[] { 54.000000, 55.000000, 54.000000, 55.000000 };
        expected[2] = new double[] { 0.733388, 0.733388, 0.266612, 0.266612 };
        expected[3] = new double[] { 0.124829, 0.875171, 0.124829, 0.875171 };
        pGrid2d = new double[] { 34.766612, 55.375171 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.781363, 0.781363, 0.218637, 0.218637 };
        expected[3] = new double[] { 0.489703, 0.510297, 0.489703, 0.510297 };
        pGrid2d = new double[] { 31.718637, 60.010297 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "T");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

    }

    @Test
    public void testHorizontalInterpolationU() {
        double[][] expected = new double[4][];
        double[][] actual;
        double[] pGrid2d;

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.430190, 0.430190, 0.569810, 0.569810 };
        expected[3] = new double[] { 0.159645, 0.840355, 0.159645, 0.840355 };
        pGrid2d = new double[] { 32.569810, 59.340355 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.688721, 0.688721, 0.311279, 0.311279 };
        expected[3] = new double[] { 0.362640, 0.637360, 0.362640, 0.637360 };
        pGrid2d = new double[] { 21.311279, 56.137360 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 29.000000, 29.000000, 30.000000, 30.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.495276, 0.495276, 0.504724, 0.504724 };
        expected[3] = new double[] { 0.513274, 0.486726, 0.513274, 0.486726 };
        pGrid2d = new double[] { 30.504724, 57.986726 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.767942, 0.767942, 0.232058, 0.232058 };
        expected[3] = new double[] { 0.106931, 0.893069, 0.106931, 0.893069 };
        pGrid2d = new double[] { 32.232058, 60.393069 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 27.000000, 27.000000, 28.000000, 28.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.522395, 0.522395, 0.477605, 0.477605 };
        expected[3] = new double[] { 0.965335, 0.034665, 0.965335, 0.034665 };
        pGrid2d = new double[] { 28.477605, 58.534665 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.628050, 0.628050, 0.371950, 0.371950 };
        expected[3] = new double[] { 0.027888, 0.972112, 0.027888, 0.972112 };
        pGrid2d = new double[] { 24.371950, 58.472112 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 22.000000, 22.000000, 23.000000, 23.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.029057, 0.029057, 0.970943, 0.970943 };
        expected[3] = new double[] { 0.557864, 0.442136, 0.557864, 0.442136 };
        pGrid2d = new double[] { 23.970943, 59.942136 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.592039, 0.592039, 0.407961, 0.407961 };
        expected[3] = new double[] { 0.394894, 0.605106, 0.394894, 0.605106 };
        pGrid2d = new double[] { 32.407961, 59.105106 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 22.000000, 22.000000, 23.000000, 23.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.463337, 0.463337, 0.536663, 0.536663 };
        expected[3] = new double[] { 0.296823, 0.703177, 0.296823, 0.703177 };
        pGrid2d = new double[] { 23.536663, 60.203177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 21.000000, 21.000000, 22.000000, 22.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.674903, 0.674903, 0.325097, 0.325097 };
        expected[3] = new double[] { 0.691688, 0.308312, 0.691688, 0.308312 };
        pGrid2d = new double[] { 22.325097, 59.808312 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 60.000000, 61.000000, 60.000000, 61.000000 };
        expected[2] = new double[] { 0.719603, 0.719603, 0.280397, 0.280397 };
        expected[3] = new double[] { 0.058214, 0.941786, 0.058214, 0.941786 };
        pGrid2d = new double[] { 31.280397, 61.441786 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 34.000000, 34.000000, 35.000000, 35.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.699100, 0.699100, 0.300900, 0.300900 };
        expected[3] = new double[] { 0.326823, 0.673177, 0.326823, 0.673177 };
        pGrid2d = new double[] { 35.300900, 59.173177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 61.000000, 62.000000, 61.000000, 62.000000 };
        expected[2] = new double[] { 0.940776, 0.940776, 0.059224, 0.059224 };
        expected[3] = new double[] { 0.230809, 0.769191, 0.230809, 0.769191 };
        pGrid2d = new double[] { 21.059224, 62.269191 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 27.000000, 27.000000, 28.000000, 28.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.317116, 0.317116, 0.682884, 0.682884 };
        expected[3] = new double[] { 0.946111, 0.053889, 0.946111, 0.053889 };
        pGrid2d = new double[] { 28.682884, 57.553889 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 32.000000, 32.000000, 33.000000, 33.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.810686, 0.810686, 0.189314, 0.189314 };
        expected[3] = new double[] { 0.776325, 0.223675, 0.776325, 0.223675 };
        pGrid2d = new double[] { 33.189314, 55.723675 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 29.000000, 29.000000, 30.000000, 30.000000 };
        expected[1] = new double[] { 56.000000, 57.000000, 56.000000, 57.000000 };
        expected[2] = new double[] { 0.812109, 0.812109, 0.187891, 0.187891 };
        expected[3] = new double[] { 0.094400, 0.905600, 0.094400, 0.905600 };
        pGrid2d = new double[] { 30.187891, 57.405600 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.173670, 0.173670, 0.826330, 0.826330 };
        expected[3] = new double[] { 0.588125, 0.411875, 0.588125, 0.411875 };
        pGrid2d = new double[] { 31.826330, 55.911875 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 24.000000, 24.000000, 25.000000, 25.000000 };
        expected[1] = new double[] { 61.000000, 62.000000, 61.000000, 62.000000 };
        expected[2] = new double[] { 0.621859, 0.621859, 0.378141, 0.378141 };
        expected[3] = new double[] { 0.870549, 0.129451, 0.870549, 0.129451 };
        pGrid2d = new double[] { 25.378141, 61.629451 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 33.000000, 33.000000, 34.000000, 34.000000 };
        expected[1] = new double[] { 54.000000, 55.000000, 54.000000, 55.000000 };
        expected[2] = new double[] { 0.233388, 0.233388, 0.766612, 0.766612 };
        expected[3] = new double[] { 0.124829, 0.875171, 0.124829, 0.875171 };
        pGrid2d = new double[] { 34.766612, 55.375171 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.281363, 0.281363, 0.718637, 0.718637 };
        expected[3] = new double[] { 0.489703, 0.510297, 0.489703, 0.510297 };
        pGrid2d = new double[] { 31.718637, 60.010297 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "U");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

    }

    @Test
    public void testHorizontalInterpolationV() {

        double[][] expected = new double[4][];
        double[][] actual;
        double[] pGrid2d;

        expected[0] = new double[] { 32.000000, 32.000000, 33.000000, 33.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.930190, 0.930190, 0.069810, 0.069810 };
        expected[3] = new double[] { 0.659645, 0.340355, 0.659645, 0.340355 };
        pGrid2d = new double[] { 32.569810, 59.340355 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 55.000000, 56.000000, 55.000000, 56.000000 };
        expected[2] = new double[] { 0.188721, 0.188721, 0.811279, 0.811279 };
        expected[3] = new double[] { 0.862640, 0.137360, 0.862640, 0.137360 };
        pGrid2d = new double[] { 21.311279, 56.137360 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 56.000000, 57.000000, 56.000000, 57.000000 };
        expected[2] = new double[] { 0.995276, 0.995276, 0.004724, 0.004724 };
        expected[3] = new double[] { 0.013274, 0.986726, 0.013274, 0.986726 };
        pGrid2d = new double[] { 30.504724, 57.986726 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.267942, 0.267942, 0.732058, 0.732058 };
        expected[3] = new double[] { 0.606931, 0.393069, 0.606931, 0.393069 };
        pGrid2d = new double[] { 32.232058, 60.393069 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 27.000000, 27.000000, 28.000000, 28.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.022395, 0.022395, 0.977605, 0.977605 };
        expected[3] = new double[] { 0.465335, 0.534665, 0.465335, 0.534665 };
        pGrid2d = new double[] { 28.477605, 58.534665 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 57.000000, 58.000000, 57.000000, 58.000000 };
        expected[2] = new double[] { 0.128050, 0.128050, 0.871950, 0.871950 };
        expected[3] = new double[] { 0.527888, 0.472112, 0.527888, 0.472112 };
        pGrid2d = new double[] { 24.371950, 58.472112 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.529057, 0.529057, 0.470943, 0.470943 };
        expected[3] = new double[] { 0.057864, 0.942136, 0.057864, 0.942136 };
        pGrid2d = new double[] { 23.970943, 59.942136 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.092039, 0.092039, 0.907961, 0.907961 };
        expected[3] = new double[] { 0.894894, 0.105106, 0.894894, 0.105106 };
        pGrid2d = new double[] { 32.407961, 59.105106 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 23.000000, 23.000000, 24.000000, 24.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.963337, 0.963337, 0.036663, 0.036663 };
        expected[3] = new double[] { 0.796823, 0.203177, 0.796823, 0.203177 };
        pGrid2d = new double[] { 23.536663, 60.203177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 21.000000, 21.000000, 22.000000, 22.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.174903, 0.174903, 0.825097, 0.825097 };
        expected[3] = new double[] { 0.191688, 0.808312, 0.191688, 0.808312 };
        pGrid2d = new double[] { 22.325097, 59.808312 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 30.000000, 30.000000, 31.000000, 31.000000 };
        expected[1] = new double[] { 60.000000, 61.000000, 60.000000, 61.000000 };
        expected[2] = new double[] { 0.219603, 0.219603, 0.780397, 0.780397 };
        expected[3] = new double[] { 0.558214, 0.441786, 0.558214, 0.441786 };
        pGrid2d = new double[] { 31.280397, 61.441786 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 34.000000, 34.000000, 35.000000, 35.000000 };
        expected[1] = new double[] { 58.000000, 59.000000, 58.000000, 59.000000 };
        expected[2] = new double[] { 0.199100, 0.199100, 0.800900, 0.800900 };
        expected[3] = new double[] { 0.826823, 0.173177, 0.826823, 0.173177 };
        pGrid2d = new double[] { 35.300900, 59.173177 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 20.000000, 20.000000, 21.000000, 21.000000 };
        expected[1] = new double[] { 61.000000, 62.000000, 61.000000, 62.000000 };
        expected[2] = new double[] { 0.440776, 0.440776, 0.559224, 0.559224 };
        expected[3] = new double[] { 0.730809, 0.269191, 0.730809, 0.269191 };
        pGrid2d = new double[] { 21.059224, 62.269191 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 28.000000, 28.000000, 29.000000, 29.000000 };
        expected[1] = new double[] { 56.000000, 57.000000, 56.000000, 57.000000 };
        expected[2] = new double[] { 0.817116, 0.817116, 0.182884, 0.182884 };
        expected[3] = new double[] { 0.446111, 0.553889, 0.446111, 0.553889 };
        pGrid2d = new double[] { 28.682884, 57.553889 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 32.000000, 32.000000, 33.000000, 33.000000 };
        expected[1] = new double[] { 54.000000, 55.000000, 54.000000, 55.000000 };
        expected[2] = new double[] { 0.310686, 0.310686, 0.689314, 0.689314 };
        expected[3] = new double[] { 0.276325, 0.723675, 0.276325, 0.723675 };
        pGrid2d = new double[] { 33.189314, 55.723675 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 29.000000, 29.000000, 30.000000, 30.000000 };
        expected[1] = new double[] { 56.000000, 57.000000, 56.000000, 57.000000 };
        expected[2] = new double[] { 0.312109, 0.312109, 0.687891, 0.687891 };
        expected[3] = new double[] { 0.594400, 0.405600, 0.594400, 0.405600 };
        pGrid2d = new double[] { 30.187891, 57.405600 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 54.000000, 55.000000, 54.000000, 55.000000 };
        expected[2] = new double[] { 0.673670, 0.673670, 0.326330, 0.326330 };
        expected[3] = new double[] { 0.088125, 0.911875, 0.088125, 0.911875 };
        pGrid2d = new double[] { 31.826330, 55.911875 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 24.000000, 24.000000, 25.000000, 25.000000 };
        expected[1] = new double[] { 60.000000, 61.000000, 60.000000, 61.000000 };
        expected[2] = new double[] { 0.121859, 0.121859, 0.878141, 0.878141 };
        expected[3] = new double[] { 0.370549, 0.629451, 0.370549, 0.629451 };
        pGrid2d = new double[] { 25.378141, 61.629451 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 34.000000, 34.000000, 35.000000, 35.000000 };
        expected[1] = new double[] { 54.000000, 55.000000, 54.000000, 55.000000 };
        expected[2] = new double[] { 0.733388, 0.733388, 0.266612, 0.266612 };
        expected[3] = new double[] { 0.624829, 0.375171, 0.624829, 0.375171 };
        pGrid2d = new double[] { 34.766612, 55.375171 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

        expected[0] = new double[] { 31.000000, 31.000000, 32.000000, 32.000000 };
        expected[1] = new double[] { 59.000000, 60.000000, 59.000000, 60.000000 };
        expected[2] = new double[] { 0.781363, 0.781363, 0.218637, 0.218637 };
        expected[3] = new double[] { 0.989703, 0.010297, 0.989703, 0.010297 };
        pGrid2d = new double[] { 31.718637, 60.010297 };
        actual = nemoGrid.get2dInterpolationCoefficients(pGrid2d, "V");
        assertArrayEquals(expected[0], actual[0]);
        assertArrayEquals(expected[1], actual[1]);
        assertArrayEquals(expected[2], actual[2], 1e-10);
        assertArrayEquals(expected[3], actual[3], 1e-10);

    }

    // @Test
    // public void testDepthT() {

    // double[] actual = new double[nemoGrid.get_nz()];
    // double[][][] nemoDepthT = nemoGrid.getDepthT();
    // for (int k = 0; k < nemoGrid.get_nz(); k++) {
    // actual[k] = nemoDepthT[k][5][5];
    // }
    // assertArrayEquals(depthT, actual, 0.001);

    // }

    // @Test
    // public void testE3T() {

    // double[] actual = new double[nemoGrid.get_nz()];
    // double[][][] nemoE3T = nemoGrid.getE3T();
    // for (int k = 0; k < nemoGrid.get_nz(); k++) {
    // actual[k] = nemoE3T[k][5][5];
    // }
    // assertArrayEquals(e3t, actual, 0.001);

    // }

    // @Test
    // public void testDepthW() {

    // double[] actual = new double[nemoGrid.get_nz() + 1];
    // double[][][] nemoDepthW = nemoGrid.getDepthW();
    // for (int k = 0; k < nemoGrid.get_nz() + 1; k++) {
    // actual[k] = nemoDepthW[k][5][5];
    // }
    // assertArrayEquals(depthW, actual, 0.001);

    // }

    @BeforeAll
    public void prepareData() throws Exception {
        String fileName = getClass().getClassLoader().getResource("test-nemo/test-nemo3d.xml").getFile();
        getSimulationManager().getParameterManager().setConfigurationFile(new File(fileName));
        nemoGrid = new NemoGrid();
        nemoGrid.loadParameters();
        nemoGrid.sortInputFiles();
        nemoGrid.getDimNC();
        nemoGrid.readConstantField();
    }
}