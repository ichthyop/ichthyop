/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.arch.ISimulation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 *
 * @author pverley
 */
public class TxtFileRelease implements IReleaseProcess {

    public void release() throws IOException {

        File fDrifter = new File(getSimulation().getParameterManager().getValue("release.drifter.pathname"));
        if (!fDrifter.exists() || !fDrifter.canRead()) {
            throw new IOException("Drifter file " + fDrifter + " cannot be read");
        }

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
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }
}
