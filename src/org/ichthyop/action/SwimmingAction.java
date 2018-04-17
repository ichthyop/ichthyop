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

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.ichthyop.util.IOTools;
import org.ichthyop.particle.IParticle;
import org.ichthyop.util.MTRandom;

/**
 * This class simulates active swimming, given the swimming velocity as an age
 * function. The swimming velocity is provided in a semicolon separated CSV
 * file, with two columns the age of the particle in days and the corresponding
 * swimming velocity in metres per second. The swimming velocity in the CSV file
 * can be interpreted as CRUISING velocity or MAXIMAL velocity. CRUISING means
 * that the particle will swim at the exact velocity defined in the CSV file.
 * MAXIMAL means that the particle will swim at random velocity ranging from
 * zero to the velocity defined in the CSV file.
 *
 * @author P.Verley (philippe.verley@ird.fr)
 */
public class SwimmingAction extends AbstractAction {

    // speed in m/s
    private double[] speeds;
    // ages in seconds
    private double[] ages;
    private double dt;
    private boolean constant;
    private final double TWO_PI = 2.d * Math.PI;
    private final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private final double TO_RADIAN = Math.PI / 180.d;
    private Random random1, random2;

    @Override
    public String getKey() {
        return "action.swimming";
    }

    @Override
    public void loadParameters() throws Exception {

        // Read swimming velocity file
        if (!getConfiguration().isNull("action.swimming.speed.file")) {
            String velocity_file = getConfiguration().getString("action.swimming.speed.file");
            String pathname = getConfiguration().resolve(velocity_file);
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("Could not find swimming speed file " + pathname);
            }
            if (!f.canRead()) {
                throw new IOException("Could not read swimming speed file " + pathname);
            }
            Locale.setDefault(Locale.US);
            // open velocities csv file
            CSVReader reader = new CSVReader(new FileReader(pathname), ';');
            List<String[]> lines = reader.readAll();
            // init arrays
            ages = new double[lines.size() - 1];
            speeds = new double[ages.length];
            // read ages (days converted to seconds) and velocities
            for (int i = 0; i < ages.length; i++) {
                String[] line = lines.get(i + 1);
                if (line.length < 2 || line[0].isEmpty()) {
                    continue;
                }
                ages[i] = Double.valueOf(line[0]) * 3600.d * 24.d;
                speeds[i] = Double.valueOf(line[1]);
            }
        } else if (!getConfiguration().isNull("action.swimming.speed")) {
            ages = new double[]{-1.d, Double.MAX_VALUE};
            speeds = new double[]{getConfiguration().getDouble("action.swimming.speed")};
        } else {
            throw new IOException("[action] Random swimming, could not find parameter action.swimming.speed or action.swimming.speed.file");
        }

        // Simulation time step
        dt = getSimulationManager().getTimeManager().get_dt();

        // Whether the velocity should be constant or random
        constant = getConfiguration().getBoolean("action.swimming.speed.constant");

        // Random number generator
        random1 = new MTRandom();
        random2 = new MTRandom();
    }

    @Override
    public void execute(IParticle particle) {

        // find the swimming velocity for this particle
        double speed = getSpeed(particle) * (constant ? 1.d : 2 * random1.nextDouble());
        // convert it to a move in a random direction
        //System.out.println((particle.getAge() / (3600.d * 24.d)) + " " + speed);
        double distance = speed * dt / ONE_DEG_LATITUDE_IN_METER;
        double theta = TWO_PI * random2.nextDouble();
        double dlon = distance * Math.cos(theta) / Math.cos(particle.getLat() * TO_RADIAN);
        double dlat = distance * Math.sin(theta);

        // move the particle
        particle.incrLat(dlat);
        particle.incrLon(dlon);
    }

    @Override
    public void init(IParticle particle) {
        // nothing to do
    }

    /**
     * Get the swimming velocity of a given particle in m.s-1.
     *
     * @param particle
     * @return the swimming velocity of the particle in m.s-1
     */
    private double getSpeed(IParticle particle) {
        float age = particle.getAge();
        for (int i = 0; i < ages.length - 1; i++) {
            if (ages[i] <= age && age < ages[i + 1]) {
                return speeds[i];
            }
        }
        return speeds[ages.length - 1];
    }

}
