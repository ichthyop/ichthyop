/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.arch.IBasicParticle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class TxtFileRelease extends AbstractReleaseProcess {

    private File textFile;

    @Override
    public void loadParameters() throws IOException {
        
        textFile = getFile(getParameter("txtfile"));
    }

    private File getFile(String filename) throws IOException {

        File folder = new File(System.getProperty("user.dir"));
        String pathname = new File(folder.toURI().resolve(filename)).getAbsolutePath();

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

        int index = 0;
        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.getDefault());
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(textFile));
            String line;
            while ((line = bfIn.readLine()) != null) {
                if (!line.startsWith("#") & !(line.length() < 1)) {
                    strCoord = line.split(" ");
                    coord = new double[strCoord.length];
                    for (int i = 0; i < strCoord.length; i++) {
                        try {
                            coord[i] = nbFormat.parse(strCoord[i].trim()).doubleValue();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    IBasicParticle particle = coord.length > 2
                            ? ParticleFactory.createParticle(index, coord[0], coord[1], -coord[2])
                            : ParticleFactory.createParticle(index, coord[0], coord[1]);

                    if (getSimulationManager().getDataset().isInWater(particle.getGridCoordinates())) {
                        //Logger.getAnonymousLogger().info("Adding new particle: " + particle.getLon() + " " + particle.getLat());
                        getSimulationManager().getSimulation().getPopulation().add(particle);
                        index++;
                    } else {
                        throw new IOException("Drifter at line " + (index + 1) + "is not in water");
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new IOException("Problem reading drifter file " + textFile);
        }
        return index;
    }

    public int getNbParticles() {
        int nbParticles = 0;
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(textFile));
            String line;
            while ((line = bfIn.readLine()) != null) {
                if (!line.startsWith("#") & !(line.length() < 1)) {
                    nbParticles++;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(TxtFileRelease.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nbParticles;
    }
}
