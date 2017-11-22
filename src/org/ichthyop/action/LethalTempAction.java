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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ichthyop.util.IOTools;
import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.ParticleMortality;
import org.ichthyop.particle.StageParticle;

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
    public String getKey() {
        return "action.growth";
    }

    @Override
    public void loadParameters() throws Exception {

        FLAG_GROWTH = getConfiguration().getBoolean("action.growth.enabled");
        temperature_field = getConfiguration().getString("action.lethal_temp.temperature_field");
        if (!FLAG_GROWTH) {
            /*
             * Check whether there is a lethal temperature CSV file
             */
            String lethal_temp_file;
            try {
                lethal_temp_file = getConfiguration().getString("action.lethal_temp.lethal_temp_file");
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
                coldLethalTp = new float[]{getConfiguration().getFloat("action.lethal_temp.cold_lethal_temperature_egg")};
                hotLethalTp = new float[]{getConfiguration().getFloat("action.lethal_temp.hot_lethal_temperature_egg")};
                FLAG_LETHAL_TEMP_FUNCTION = false;
            }
        } else {
            coldLethalTp = new float[]{
                getConfiguration().getFloat("action.lethal_temp.cold_lethal_temperature_egg"),
                getConfiguration().getFloat("action.lethal_temp.cold_lethal_temperature_larva")};
            hotLethalTp = new float[]{
                getConfiguration().getFloat("action.lethal_temp.hot_lethal_temperature_egg"),
                getConfiguration().getFloat("action.lethal_temp.hot_lethal_temperature_larva")};
        }
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        boolean addTracker = true;
        try {
            addTracker = getConfiguration().getBoolean("action.lethal_temp.temp_tracker");
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

    private void loadLethalTemperatures(String csvFile) {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
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
        double temperature = getSimulationManager().getDataset().getDouble(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime());
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

        double temperature = getSimulationManager().getDataset().getDouble(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime());
        int stage = StageParticle.getStage(particle);
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
