/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.release;

import fr.ird.ichthyop.particle.ParticleFactory;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IBasicParticle;
import java.io.BufferedReader;
import java.io.File;
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

    private String pathname;

    @Override
    void loadParameters() {
        pathname = getParameter("release.drifter.pathname");
    }

    private File getFile(String pathname) throws IOException {

        File file = new File(pathname);
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Drifter file " + file + " cannot be read");
        }
        return file;
    }

    @Override
    void proceedToRelease(ReleaseEvent event) throws IOException {

        File fDrifter = getFile(pathname);

        int index = 0;
        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.getDefault());
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
            String line;
            while ((line = bfIn.readLine()) != null) {
                if (!line.startsWith("#") & !(line.length() < 1)) {
                    strCoord = line.split(" ");
                    coord = new double[strCoord.length];
                    for (int i = 0; i < strCoord.length; i++) {
                        try {
                            coord[i] = nbFormat.parse(strCoord[i].trim()).
                                    doubleValue();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    IBasicParticle particle = coord.length > 2
                            ? ParticleFactory.createParticle(index, coord[0], coord[1], -coord[2])
                            : ParticleFactory.createParticle(index, coord[0], coord[1]);

                    if (getSimulation().getDataset().isInWater(particle.getGridPoint())) {
                        getSimulation().getPopulation().add(particle);
                        index++;
                    } else {
                        throw new IOException("Drifter at line " + (index + 1) + "is not in water");
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new IOException("Problem reading drifter file " + fDrifter);
        }
        Logger.getAnonymousLogger().info("Released " + index + " particles.");
    }

    public int getNbParticles() {
        int nbParticles = 0;
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(getFile(pathname)));
            while ((bfIn.readLine()) != null) {
                nbParticles++;
            }

        } catch (IOException ex) {
            Logger.getLogger(TxtFileRelease.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nbParticles;
    }
}
