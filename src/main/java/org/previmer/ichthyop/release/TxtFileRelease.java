/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
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

package org.previmer.ichthyop.release;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class TxtFileRelease extends AbstractRelease {

    private File textFile;
    private boolean is3D;
    private int nbParticles;

    @Override
    public void loadParameters() throws IOException {

        textFile = getFile(getParameter("txtfile"));
        is3D = getSimulationManager().getDataset().is3D();
        nbParticles = -1;
    }

    private File getFile(String filename) throws IOException {

        String pathname = IOTools.resolveFile(filename);
        File file = new File(pathname);
        if (!file.isFile()) {
            throw new FileNotFoundException("Drifter file " + filename + " not found.");
        }
        if (!file.canRead()) {
            throw new IOException("Drifter file " + file + " cannot be read");
        }
        return file;
    }

    @Override
    public int release(ReleaseEvent event) throws IOException {

        int cpt = 0;
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.US);

        BufferedReader bfIn = new BufferedReader(new FileReader(textFile));
        String line;
        int iline = 1;
        while ((line = bfIn.readLine()) != null) {
            if (!line.startsWith("#") & !(line.length() < 1)) {
                strCoord = line.trim().split(" ");
                coord = new double[strCoord.length];
                for (int i = 0; i < strCoord.length; i++) {
                    try {
                        coord[i] = nbFormat.parse(strCoord[i].trim()).doubleValue();
                    } catch (ParseException ex) {
                        IOException ioex = new IOException("{Drifter release} Failed to read drifter position at line " + (index + 1) + " ==> " + ex.getMessage());
                        ioex.setStackTrace(ex.getStackTrace());
                        throw ioex;
                    }
                }
                IParticle particle;
                if (is3D) {
                    double depth = coord.length > 2
                            ? coord[2]
                            : 0.d;
                    if (depth > 0) {
                        depth *= -1;
                    }
                    particle = ParticleFactory.createGeoParticle(index, coord[0], coord[1], depth);
                } else {
                    particle = ParticleFactory.createGeoParticle(index, coord[0], coord[1]);
                }
                if (null != particle) {
                    //Logger.getAnonymousLogger().info("Adding new particle: " + particle.getLon() + " " + particle.getLat());
                    getSimulationManager().getSimulation().getPopulation().add(particle);
                    index++;
                    cpt++;
                } else {
                    getLogger().log(Level.WARNING, "Drifter release - Drifter at line {0} ({1}) is not in water. Line ignored.", new Object[]{iline, line});
                    //throw new IOException("{Drifter release} Drifter at line " + iline + " (" + line + ") is not in water");
                }
            }
            iline++;
        }
        
        this.nbParticles = cpt;
        
        return index;
    }

    private int readNbParticles() throws IOException {
        
        int index = 0;
        BufferedReader bfIn = new BufferedReader(new FileReader(textFile));
        String line;
        while ((line = bfIn.readLine()) != null) {
            if (!line.startsWith("#") & !(line.length() < 1)) {
                index++;
            }
        }
        return index;
    }

    @Override
    public int getNbParticles() {
        return nbParticles;
    }
}
