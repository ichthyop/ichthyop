/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.action;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;

/**
 * This class simulates active swimming, given the swimming velocity as an age
 * function. The swimming velocity is provided in a semicolon separated CSV
 * file, with two columns the age of the particle in days and the corresponding
 * swimming velocity in metres per second. The swimming velocity in the CSV file
 * can be either constant or not. CONSTANT velocity means that the particle will
 * swim at the exact velocity defined in the CSV file. NON CONSTANT velocity
 * means that the particle will swim at random velocity ranging from zero to two
 * times the velocity defined in the CSV file so as to ensure an average
 * velocity equal to the velocity defined in the CSV file.
 *
 * @author P.Verley (philippe.verley@ird.fr)
 */
public class SwimmingAction extends AbstractAction {

    // speed in m/s
    private float[] speeds;
    // ages in seconds
    private float[] ages;
    private double dt;
    private boolean constant;

    @Override
    public void loadParameters() throws Exception {

        // Read swimming velocity file
        String velocity_file = getParameter("velocity_file");
        String pathname = IOTools.resolveFile(velocity_file);
        File f = new File(pathname);
        if (!f.isFile()) {
            throw new FileNotFoundException("Swimming velocity file " + pathname + " not found.");
        }
        if (!f.canRead()) {
            throw new IOException("Swimming velocity file " + pathname + " cannot be read.");
        }
        Locale.setDefault(Locale.US);
        // open velocities csv file
        CSVReader reader = new CSVReaderBuilder(new FileReader(pathname)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
        List<String[]> lines = reader.readAll();
        // init arrays
        ages = new float[lines.size() - 1];
        speeds = new float[ages.length];
        // read ages (days converted to seconds) and velocities
        for (int i = 0; i < ages.length; i++) {
            String[] line = lines.get(i + 1);
            if (line.length < 2 || line[0].isEmpty()) {
                continue;
            }
            ages[i] = Float.valueOf(line[0]) * 3600.f * 24.f;
            speeds[i] = Float.valueOf(line[1]);
        }

        // Simulation time step
        dt = getSimulationManager().getTimeManager().get_dt();

        // Whether the velocity should be constant or random
        constant = Boolean.valueOf(getParameter("constant_velocity"));
    }

    @Override
    public void execute(IParticle particle) {

        // Find the swimming velocity for this particle
        double speed = getSpeed(particle) * (constant ? 1.d : 2.d*Math.random());
        // Random x component of the swimming velocity
        double u = randomDir() * Math.random() * speed;
        // y component such as sqrt(x2 + y2) = speed
        double v = randomDir() * Math.sqrt(speed * speed - u * u);

        // Convert dx and dy from m.s-1 to grid displacement
        int i = (int) Math.round(particle.getX());
        int j = (int) Math.round(particle.getY());
        double dx = u / getSimulationManager().getDataset().getdxi(j, i) * dt;
        double dy = v / getSimulationManager().getDataset().getdeta(j, i) * dt;

        // Move the particle
        particle.increment(new double[]{dx, dy});
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
    private float getSpeed(IParticle particle) {
        float age = particle.getAge();
        for (int i = 0; i < ages.length - 1; i++) {
            if (ages[i] <= age && age < ages[i + 1]) {
                return speeds[i];
            }
        }
        return speeds[ages.length - 1];
    }

    /**
     * Random draw in {-1, 1}
     *
     * @return -1 or 1 randomly
     */
    private double randomDir() {
        return Math.random() < 0.5 ? -1.d : 1.d;
    }

}
