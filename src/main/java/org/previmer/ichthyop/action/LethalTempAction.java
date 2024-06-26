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
import org.previmer.ichthyop.particle.StageParticleLayer;

/**
 *
 * @author pverley
 */
public class LethalTempAction extends AbstractAction {

    private float[] coldLethalTp, hotLethalTp;
    private float[] ages;
    private boolean FLAG_GROWTH, FLAG_LETHAL_TEMP_FUNCTION;
    private String temperature_field;

    @Override
    public void loadParameters() throws Exception {

        FLAG_GROWTH = getSimulationManager().getActionManager().isEnabled("action.growth");
        temperature_field = getParameter("temperature_field");
        if (!FLAG_GROWTH) {
            /*
             * Check whether there is a lethal temperature CSV file
             */
            String lethal_temp_file;
            try {
                lethal_temp_file = getParameter("lethal_temp_file");
            } catch (Exception ex) {
                lethal_temp_file = null;
            }
            if (null != lethal_temp_file && !lethal_temp_file.isEmpty()) {
                String pathname = IOTools.resolveFile(lethal_temp_file);
                File f = new File(pathname);
                if (!f.isFile()) {
                    throw new FileNotFoundException("Lethal temperature file " + pathname + " not found.");
                }
                if (!f.canRead()) {
                    throw new IOException("Lethal temperature file " + pathname + " cannot be read.");
                }
                loadLethalTemperatures(pathname);
                FLAG_LETHAL_TEMP_FUNCTION = true;
            } else {
                /*
                 * If not just load constant lethal temperature egg
                 */
                ages = new float[1];
                coldLethalTp = new float[]{Float.valueOf(getParameter("cold_lethal_temperature_egg"))};
                hotLethalTp = new float[]{Float.valueOf(getParameter("hot_lethal_temperature_egg"))};
                FLAG_LETHAL_TEMP_FUNCTION = false;
            }
        } else {
            coldLethalTp = new float[]{
                Float.valueOf(getParameter("cold_lethal_temperature_egg")),
                Float.valueOf(getParameter("cold_lethal_temperature_larva"))};
            hotLethalTp = new float[]{
                Float.valueOf(getParameter("hot_lethal_temperature_egg")),
                Float.valueOf(getParameter("hot_lethal_temperature_larva"))};
        }
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("temp_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addCustomTracker(temperature_field);
        }
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    private void loadLethalTemperatures(String csvFile) throws CsvException {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();

            // init arrays
            ages = new float[lines.size() - 1];
            coldLethalTp = new float[ages.length];
            hotLethalTp = new float[ages.length];

            // read ages (hours converted to seconds) and densities
            for (int i = 0; i < ages.length; i++) {
                String[] line = lines.get(i + 1);
                ages[i] = Float.valueOf(line[0]) * 3600.f;
                coldLethalTp[i] = Float.valueOf(line[1]);
                hotLethalTp[i] = Float.valueOf(line[2]);
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(IParticle particle) {

        if (FLAG_GROWTH) {
            checkTpGrowingParticle(particle);
        } else {
            checkTp(particle);
        }

    }

    private void checkTp(IParticle particle) {
        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        int iAge = ages.length - 1;
        if (FLAG_LETHAL_TEMP_FUNCTION) {
            float age = particle.getAge();
            for (int i = 0; i < ages.length - 1; i++) {
                if (ages[i] <= age && age < ages[i + 1]) {
                    iAge = i;
                    break;
                }
            }

        }
        //System.out.println("I am " + (particle.getAge() / 3600) + " hours old, lethal tp cold: " + coldLethalTp[iAge] + " & hot: " + hotLethalTp[iAge]);
        if (temperature <= coldLethalTp[iAge]) {
            particle.kill(ParticleMortality.DEAD_COLD);
        } else if (temperature >= hotLethalTp[iAge]) {
            particle.kill(ParticleMortality.DEAD_HOT);
        }
    }

    private void checkTpGrowingParticle(IParticle particle) {

        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        int stage = ((StageParticleLayer) particle.getLayer(StageParticleLayer.class)).getStage();
        // stage == 0 means egg, stage > 0 means larvae
        boolean frozen = ((stage == 0) && (temperature <= coldLethalTp[0])) || ((stage != 0) && (temperature <= coldLethalTp[1]));
        boolean heated = ((stage == 0) && (temperature >= hotLethalTp[0])) || ((stage != 0) && (temperature >= hotLethalTp[1]));
        if (frozen) {
            particle.kill(ParticleMortality.DEAD_COLD);
        } else if (heated) {
            particle.kill(ParticleMortality.DEAD_HOT);
        }
    }
}
