/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2018
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
package org.ichthyop.release;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.grid.IGrid;

/**
 *
 * @author pverley
 */
public class ReleaseFactory extends IchthyopLinker {

    public static void createUniformReleaseTxtFile() {

        int incr = 120;

        IGrid grid = getSimulationManager().getGrid();
        int nx = grid.get_nx();
        int ny = grid.get_ny();
        for (int i = 0; i < nx; i += incr) {
            for (int j = 0; j < ny; j += incr) {
                int lat = (int) Math.round(grid.getLat(i, j));
                int lon = (int) Math.round(grid.getLon(i, j));
                List<float[]> coords = new ArrayList(incr * incr);
                for (int ii = 0; ii < incr; ii += 3) {
                    for (int jj = 0; jj < incr; jj += 3) {
                        if ((i + ii) < nx && (j + jj) < ny) {
                            if (grid.isInWater(i + ii, j + jj)) {
                                coords.add(new float[]{(float) grid.getLat(i + ii, j + jj), (float) grid.getLon(i + ii, j + jj)});
                            }
                        }
                    }
                }
                if (!coords.isEmpty()) {
                    writeReleaseTxtFile(coords, lat, lon);
                }
            }
        }
    }

    private static void writeReleaseTxtFile(List<float[]> coords, int lat, int lon) {

        StringBuilder file = new StringBuilder();
        file.append(getSimulationManager().getConfigurationFile().getParent());
        file.append(File.separator).append("release").append(File.separator);
        file.append(String.format("%03d", Math.abs(lat)));
        file.append(lat >= 0 ? "N" : "S");
        file.append(String.format("%03d", Math.abs(lon)));
        file.append(lon >= 0 ? "E" : "W");
        file.append("_release.txt");
        System.out.println(file);
        try (BufferedWriter bfr = new BufferedWriter(new FileWriter(file.toString()))) {
            bfr.append("# longitude latitude");
            bfr.newLine();
            for (float[] coord : coords) {
                String line = Float.toString(coord[1]) + " " + Float.toString(coord[0]);
                bfr.append(line);
                bfr.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ReleaseFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
