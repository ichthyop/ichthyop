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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
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

    // The depth vector as provided in the CMS configuration file
    private float[] depth;
    // The time vector transformed in Ichthyop time
    // Let's say that CMS config file provide a vector such as [dt1 dt2 ... dtn]
    // time = [t0+dt1 t0+dt1+dt2 ... t0+sum(dt1:dtn)] with t0 the initial time
    // of the simulation. That way time can be easily compared to the current
    // time of the simulation
    private long[] time;
    // The probability matrix as provided in the CMS configuration file
    // probability[nTime][nDepth]
    private float[][] probability;
    // The maximum of probability of the matrix for every time step
    // maxProbability[nTime]
    // maxProbability[iTime] = max(probability[iTime][:])
    private float[] maxProbability;

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
        // Uncomment this function to display in the console how Ichthyop loaded
        // the CMS configuration file.
        //printCMSInputFile();
    }
    
    @Override
    public void init(IParticle particle) {
        // Nothing to do
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
                depth = new float[nDepth];
                String[] sDepth = bfIn.readLine().trim().split(" ");
                for (int iDepth = 0; iDepth < nDepth; iDepth++) {
                    depth[iDepth] = Integer.parseInt(sDepth[iDepth]);
                }
                // Line 4: time steps in second since the beginning of transport
                time = new long[nTime];
                long cumulatedTime = getSimulationManager().getTimeManager().get_tO();
                String[] sTime = bfIn.readLine().trim().split(" ");
                for (int iTime = 0; iTime < nTime; iTime++) {
                    cumulatedTime += Long.parseLong(sTime[iTime]);
                    time[iTime] = cumulatedTime;
                }
                // Line 5 to EOF: matrix probability of presence
                probability = new float[nTime][nDepth];
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
                // I set a warning. It could be as well Level.SEVERE to generate
                // an error that would stop the simulation.
                getLogger().log(Level.WARNING, "Ontogenetic Vertical Migration: the sum of probability for time step {0} equals {1}.", new Object[]{time[iTime], sum});
            }
        }

        // For every time step, determines the biggest probability
        maxProbability = new float[time.length];
        for (int iTime = 0; iTime < time.length; iTime++) {
            maxProbability[iTime] = 0;
            for (int iDepth = 0; iDepth < depth.length; iDepth++) {
                maxProbability[iTime] = Math.max(probability[iTime][iDepth], maxProbability[iTime]);
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
        sb.append("Time values (converted to Ichthyop time): ");
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

        // Find the corresponding time step in the matrix of probability
        int iTime = 0;
        long currentTime = getSimulationManager().getTimeManager().getTime();
        for (long lTime : time) {
            if (currentTime >= lTime) {
                iTime++;
            } else {
                break;
            }
        }
        
        // @Lysel You must perform a time check. Does currentTime take you to
        // another column of your matrix ? Yes ==> you must set a new depth for
        // this new matrix time. No ? The particle is already in the matrix time
        // and at the right depth ==> no need to set a new depth, just exit the
        // function.
        

        // Set a depth level, depending of the probability vector of the
        // water column at this time step.
        // @Lysel You will have to perform some tests, but I hope this short
        // algorithm will ensure that you have an overall vertical distribution
        // of your particles that is consistent with the probability vector.
        // This is something you must analyse on post-processing by writting 
        // a code in R or Matlab that will check for a given time-step how
        // many particles are in each depth level.
        float proba;
        int iDepth;
        do {
            iDepth = (int) Math.round((depth.length - 1) * Math.random());
            proba = probability[iTime][iDepth];
        } while (proba <= 0 || proba < (Math.random() * maxProbability[iTime]));

        // Set the depth of the particle, around the selected depth level
        double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), -depth[iDepth]) - particle.getZ();
        // The second boolean argument, set to true, means that the other processes
        // won't be able to change the depth of the particle (e.g. advection).
        // For combining effects of vertical advection and migration, set it to
        // false.
        particle.increment(new double[]{0.d, 0.d, dz}, false, true);
    }

}
