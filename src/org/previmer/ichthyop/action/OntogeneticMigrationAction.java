/*
 * Copyright (C) 2014 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;

/**
 * This class implements the ontogenetic vertical migration similarly to CMS. It
 * uses the same text-based input file so that the experiments can be
 * reproducible in Ichthyop. The input file provides a time varying vector of
 * probabilities for the particles to be found at a given depth. Here is the
 * structure of configuration file: Line 1: number of time steps in the time
 * vector Line 2: number of depth levels in the probability vector Line 3: depth
 * levels in meter Line 4: time steps in second since the beginning of transport
 * Line 5 to EOF: probability matrix. Time in column and depth in row. For
 * example the first column provides the probability of presence in the water
 * column for the first time step (careful: time step refers to the time step in
 * the CMS configuration file, and do not refer to Ichthyop time step. Indeed
 * one time step of the configuration might cover several Ichthyop time steps).
 * The sum of the probabilities over the water column is 100. Example of a CMS
 * Ontogenetic Vertical Migration configuration file: % 5 % 10 % 1 5 10 15 20 25
 * 30 35 40 45 % 172800 1296000 518400 172800 1296000 % 20 00 00 00 00 % 20 00
 * 00 00 15 % 15 00 00 00 25 % 15 00 03 03 35 % 15 05 57 13 20 % 10 30 39 27 05
 * % 05 46 01 31 00 % 00 17 00 18 00 % 00 01 00 06 00 % 00 00 00 01 00
 *
 *
 *
 * @author P. Verley
 * @author L. Garavelli
 */
public class OntogeneticMigrationAction extends AbstractAction {

    private int[] depth;
    private long[] time;
    private int[][] probability;

    @Override
    public void loadParameters() throws Exception {

        String pathname = IOTools.resolveFile(getParameter("cms_ovm_config_file"));
        File file = new File(pathname);
        if (!file.isFile()) {
            throw new FileNotFoundException("CMS Ontogenetic Vertical Migration configuration file " + pathname + " not found.");
        }
        if (!file.canRead()) {
            throw new IOException("CMS Ontogenetic Vertical Migration configuration file " + pathname + " cannot be read.");
        }
        readCMSInputFile(file);
        //printCMSInputFile();
    }

    /**
     * Reads the CMS Ontogenetic Vertical Migration configuration file.
     *
     * @param file, the path of the configuration file
     */
    private void readCMSInputFile(File file) {

        BufferedReader bfIn;
        try {
            bfIn = new BufferedReader(new FileReader(file));
            try {
                // Line 1: number of time steps in the time vector
                int nTime = Integer.valueOf(bfIn.readLine().trim());
                // Line 2: number of depth levels in the probability vector
                int nDepth = Integer.valueOf(bfIn.readLine().trim());
                // Line 3: depth levels in meter
                depth = new int[nDepth];
                String[] sDepth = bfIn.readLine().trim().split(" ");
                for (int iDepth = 0; iDepth < nDepth; iDepth++) {
                    depth[iDepth] = Integer.parseInt(sDepth[iDepth]);
                }
                // Line 4: time steps in second since the beginning of transport
                time = new long[nTime];
                String[] sTime = bfIn.readLine().trim().split(" ");
                for (int iTime = 0; iTime < nTime; iTime++) {
                    time[iTime] = Long.parseLong(sTime[iTime]);
                }
                // Line 5 to EOF: matrix probability of presence
                probability = new int[nTime][nDepth];
                for (int iDepth = 0; iDepth < nDepth; iDepth++) {
                    String[] sProba = bfIn.readLine().trim().split(" ");
                    for (int iTime = 0; iTime < nTime; iTime++) {
                        probability[iTime][iDepth] = Integer.parseInt(sProba[iTime]);
                    }
                }
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Error parsing the CMS Ontogenetic Vertical Migration configuration file", ex);
            }
        } catch (FileNotFoundException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        // Perform a quick check over the probabilities, to make sure that the
        // sum over the water column is 100 for every time step.
        for (int iTime = 0; iTime < time.length; iTime++) {
            int sum = 0;
            for (int iDepth = 0; iDepth < depth.length; iDepth++) {
                sum += probability[iTime][iDepth];
            }
            if (sum != 100) {
                getLogger().log(Level.WARNING, "Ontogenetic Vertical Migration: the sum of probability for time step {0} equals {1}.", new Object[]{time[iTime], sum});
            }
        }
    }

    /**
     * This function prints in the console the content of the CSM configuration
     * file so that one can checks that it has been read properly by Ichthyop.
     */
    private void printCMSInputFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Number of time steps: ");
        sb.append(probability.length);
        sb.append('\n');
        sb.append("Number of depth levels: ");
        sb.append((probability[0].length));
        sb.append('\n');
        sb.append("Time values: ");
        for (int iTime = 0; iTime < time.length; iTime++) {
            sb.append(time[iTime]);
            sb.append(' ');
        }
        sb.append('\n');
        for (int iDepth = 0; iDepth < depth.length; iDepth++) {
            sb.append(depth[iDepth]);
            sb.append(' ');
        }
        sb.append('\n');
        for (int iTime = 0; iTime < time.length; iTime++) {
            sb.append("Probability of presence for time ");
            sb.append(time[iTime]);
            sb.append("\n  ");
            for (int iDepth = 0; iDepth < depth.length; iDepth++) {
                sb.append(probability[iTime][iDepth]);
                sb.append(' ');
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());
    }

    @Override
    public void execute(IParticle particle) {

        long elapsed = getSimulationManager().getTimeManager().getTime() - getSimulationManager().getTimeManager().get_tO();
        
    }

}
