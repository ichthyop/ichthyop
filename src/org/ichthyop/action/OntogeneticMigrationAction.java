/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.ichthyop.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.ichthyop.particle.IParticle;

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
    private double[] time;
    // The probability matrix as provided in the CMS configuration file
    // probability[nTime][nDepth]
    private float[][] probability;
    // The maximum of probability of the matrix for every time step
    // maxProbability[nTime]
    // maxProbability[iTime] = max(probability[iTime][:])
    private float[] maxProbability;
    
    @Override
    public String getKey() {
        return "action.migration.ontogenetic";
    }

    @Override
    public void loadParameters() throws Exception {

        String pathname = getConfiguration().getFile("action.migration.ontogenetic.cms_ovm_config_file");
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
                time = new double[nTime];
                double cumulatedTime = getSimulationManager().getTimeManager().get_tO();
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
                error("Error parsing the CMS Ontogenetic Vertical Migration configuration file", ex);
            }
        } catch (FileNotFoundException ex) {
            error("CMS Ontogenetic Vertical Migration configuration file not found" , ex);
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
                warning("Ontogenetic Vertical Migration: the sum of probability for time step {0} equals {1}.", new Object[]{time[iTime], sum});
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
        double currentTime = getSimulationManager().getTimeManager().getTime();
        for (double lTime : time) {
            if (currentTime >= lTime) {
                iTime++;
            } else {
                break;
            }
        }
        iTime = Math.min(iTime, time.length - 1);

        // @Lysel You must perform a time check. Does currentTime take you to
        // another column of your matrix ? Yes ==> you must set a new depth for
        // this new matrix time. No ? The particle is already in the matrix time
        // and at the right depth ==> no need to set a new depth, just exit the
        // function.
        double previousTime = currentTime - getSimulationManager().getTimeManager().get_dt();
        int iPreviousTime = 0;
        for (double lTime : time) {
            if (previousTime >= lTime) {
                iPreviousTime++;
            } else {
                break;
            }
        }
        iPreviousTime = Math.min(iPreviousTime, time.length - 1);
        double t0 = getSimulationManager().getTimeManager().get_tO();
        // The particle is in the same CMS time step, nothing to do
        if ((previousTime >= t0) && (iPreviousTime == iTime)) {
            return;
        }

        // The particle just arrived in a new CMS time step. Set a new depth.
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
