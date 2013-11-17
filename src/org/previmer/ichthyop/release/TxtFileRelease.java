/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        nbParticles = readNbParticles();
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

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.US);

        BufferedReader bfIn = new BufferedReader(new FileReader(textFile));
        String line;
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
                } else {
                    throw new IOException("{Drifter release} Drifter at line " + (index + 1) + " is not in water");
                }
            }
        }
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
