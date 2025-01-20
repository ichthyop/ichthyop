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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author nbarrier
 */
public class LethalSaltAction extends AbstractAction {

    private float[] freshLethalSal, salineLethalSal;
    private float[] ages;
    private boolean FLAG_LETHAL_SALT_FUNCTION;
    private String salinity_field;

    @Override
    public void loadParameters() throws Exception {

        salinity_field = getParameter("salinity_field");

        /*
             * Check whether there is a lethal temperature CSV file
         */
        String lethal_salt_file;
        try {
            lethal_salt_file = getParameter("lethal_salt_file");
        } catch (Exception ex) {
            lethal_salt_file = null;
        }
        if (null != lethal_salt_file && !lethal_salt_file.isEmpty()) {
            String pathname = IOTools.resolveFile(lethal_salt_file);
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("Lethal salinity file " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("Lethal salinity file " + pathname + " cannot be read.");
            }
            loadLethalSalinity(pathname);
            FLAG_LETHAL_SALT_FUNCTION = true;
        } else {
            /*
                 * If not just load constant lethal temperature egg
             */
            ages = new float[1];
            freshLethalSal = new float[]{Float.valueOf(getParameter("fresh_lethal_salinity"))};
            salineLethalSal = new float[]{Float.valueOf(getParameter("saline_lethal_salinity"))};
            FLAG_LETHAL_SALT_FUNCTION = false;
        }

        getSimulationManager().getDataset().requireVariable(salinity_field, getClass());
        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("salt_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addCustomTracker(salinity_field);
        }
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    private void loadLethalSalinity(String csvFile) throws CsvException {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();

            // init arrays
            ages = new float[lines.size() - 1];
            freshLethalSal = new float[ages.length];
            salineLethalSal = new float[ages.length];

            // read ages (hours converted to seconds) and densities
            for (int i = 0; i < ages.length; i++) {
                String[] line = lines.get(i + 1);
                ages[i] = Float.valueOf(line[0]) * 3600.f;
                freshLethalSal[i] = Float.valueOf(line[1]);
                salineLethalSal[i] = Float.valueOf(line[2]);
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(IParticle particle) {
        checkTp(particle);
    }

    private void checkTp(IParticle particle) {
        double salinity = getSimulationManager().getDataset().get(salinity_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        int iAge = ages.length - 1;
        if (FLAG_LETHAL_SALT_FUNCTION) {
            float age = particle.getAge();
            for (int i = 0; i < ages.length - 1; i++) {
                if (ages[i] <= age && age < ages[i + 1]) {
                    iAge = i;
                    break;
                }
            }
        }
        //System.out.println("I am " + (particle.getAge() / 3600) + " hours old, lethal tp cold: " + freshLethalSal[iAge] + " & hot: " + salineLethalSal[iAge]);
        if (salinity <= freshLethalSal[iAge]) {
            particle.kill(ParticleMortality.DEAD_FRESH);
        } else if (salinity >= salineLethalSal[iAge]) {
            particle.kill(ParticleMortality.DEAD_SALINE);
        }
    }
}
