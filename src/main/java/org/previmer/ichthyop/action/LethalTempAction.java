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
 * temperature and temperature fields archived from oceanic models such as NEMO,
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
import org.previmer.ichthyop.util.CheckGrowthParam;

/**
 *
 * @author nbarrier
 */
public class LethalTempAction extends AbstractAction {

    private float[] coldLethalTemp, warmLethalTemp;
    private float[] classes;
    private String temperature_field;
    private boolean use_temperature_file = false;
    private double cold_lethal_temperature, warm_lethal_temperature;
    private double secs_in_day = 86400;

    @FunctionalInterface
    private interface GetValueInterface {
        double getValue(IParticle particle);
    }
    private GetValueInterface getValueInterface;

    @FunctionalInterface
    private interface KillInterface {
        void kill(IParticle particle);
    }
    private KillInterface killInterface;

    @Override
    public void loadParameters() throws Exception {

        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());

        String key = "temperature.file.enabled";
        if(!isNull(key) && (Boolean.valueOf(getParameter(key)))) {
            use_temperature_file = true;
        }

        if(use_temperature_file) {
            String lethal_temp_file = getParameter("lethal_temperature_file");
            String pathname = IOTools.resolveFile(lethal_temp_file);
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("Lethal temperature file " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("Lethal temperature file " + pathname + " cannot be read.");
            }
            loadLethaltemperature(pathname);

            String temperature_class = getParameter("temperature.class").toLowerCase();

            boolean isGrowth = CheckGrowthParam.checkParams();  // check if growth or debgrowth is true (xor)
            if (!isGrowth && temperature_class.equals("length")) {
                throw new IllegalArgumentException("Velocity cannot be based on particle length since no growth model not activated.");
            }

            if(temperature_class.equals("age")) {
                getValueInterface = (particle) -> (particle.getAge() / secs_in_day); // age of the particle in days
            } else {
                getValueInterface = (particle) -> (particle.getLength()); // lengh in cm
            }

            killInterface = (particle) -> killFiletemperature(particle);

        } else {
            cold_lethal_temperature =  Float.valueOf(getParameter("cold_lethal_temperature"));
            warm_lethal_temperature =  Float.valueOf(getParameter("warm_lethal_temperature"));
            killInterface = (particle) -> killConstanttemperature(particle);
        }


        boolean addTracker = true;
        key = "temperature_tracker";
        if(!isNull(key)) {
            addTracker = Boolean.valueOf(getParameter(key));
        }

        if (addTracker) {
            getSimulationManager().getOutputManager().addCustomTracker(temperature_field);
        }
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    private void loadLethaltemperature(String csvFile) throws CsvException {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();

            // init arrays
            classes = new float[lines.size() - 1];
            coldLethalTemp = new float[classes.length];
            warmLethalTemp = new float[classes.length];

            // read ages (hours converted to seconds) and densities
            for (int i = 0; i < classes.length; i++) {
                String[] line = lines.get(i + 1);
                classes[i] = Float.valueOf(line[0]);
                coldLethalTemp[i] = Float.valueOf(line[1]);
                warmLethalTemp[i] = Float.valueOf(line[2]);
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(IParticle particle) {

        if (!this.isActive(particle)) {
            return;
        }

        killInterface.kill(particle);
    }

    /**
     * Method for killing the particle when constant lethal salinities are provided.
     *
     */
    private void killConstanttemperature(IParticle particle) {

        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();

        // System.out.println("I am " + (particle.getAge() / 3600) + " hours old, lethal
        // tp cold: " + freshLethalSal[iAge] + " & hot: " + salineLethalSal[iAge]);
        if (temperature <= cold_lethal_temperature) {
            particle.kill(ParticleMortality.DEAD_COLD);
        } else if (temperature >= warm_lethal_temperature) {
            particle.kill(ParticleMortality.DEAD_HOT);
        }

    }

    private void killFiletemperature(IParticle particle) {

        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        double particleValue = getValueInterface.getValue(particle);

        int iAge = 0;
        if (particleValue < classes[0]) {
            iAge = 0;
        } else {
            iAge = classes.length - 1;
            for (int i = 0; i < classes.length - 1; i++) {
                if (classes[i] <= particleValue && particleValue < classes[i + 1]) {
                    iAge = i;
                    break;
                }
            }
        }

        if (temperature <= coldLethalTemp[iAge]) {
            particle.kill(ParticleMortality.DEAD_COLD);
        } else if (temperature >= warmLethalTemp[iAge]) {
            particle.kill(ParticleMortality.DEAD_HOT);
        }
    }
}
