package org.previmer.ichthyop.action.orientation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.action.BuoyancyAction;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public abstract class OrientationVelocity extends AbstractAction {

    private double secs_in_day = 86400;

    private double swimmingSpeedHatch; // cm
    private double swimmingSpeedSettle; // cm
    private double PLD; // days

    private double[] ageCsv; // age array from CSV (seconds)
    private double[] speedCsv; // speed array (m/s);

    @FunctionalInterface
    public interface InnerOrientationVelocity {
        double getVelocity(IParticle particle);
    }

    private InnerOrientationVelocity velocityMethod;

    @Override
    public void loadParameters() throws Exception {

        String key = "swimming.speed.csv.enabled";
        if (!isNull(key) && Boolean.valueOf(getParameter(key))) {
            velocityMethod = (IParticle particle) -> getVelocityCsv(particle);
            initVelocityCsv();
        } else {

            // values in cm/s
            swimmingSpeedHatch = Double.valueOf(getParameter("swimming.speed.hatch"));
            swimmingSpeedSettle = Double.valueOf(getParameter("swimming.speed.settle"));

            if (swimmingSpeedHatch > swimmingSpeedSettle) {
                getLogger().log(Level.WARNING, "Hatch and Settle velocity have been swapped");
                double temp = swimmingSpeedHatch;
                swimmingSpeedHatch = swimmingSpeedSettle;
                swimmingSpeedSettle = temp;
            }

            velocityMethod = (IParticle particle) -> getVelocityPLD(particle);

        }

    }

    @Override
    public void init(IParticle particle) {
        double timeMax = getSimulationManager().getTimeManager().getTransportDuration(); // seconds
        PLD = timeMax / (secs_in_day); // seconds / (seconds / day) = day
    }

    public double getVelocity(IParticle particle) {
        return velocityMethod.getVelocity(particle);
    }

    public double getVelocityPLD(IParticle particle) {

        // particle.getAge() in seconds
        // secs_in_day = 24 * 60 * 60 s.day^{-1}
        // age now in days
        double age = particle.getAge() / (secs_in_day);

        if (age == 0) {
            return 0;
        }

        // speed in cm/s
        double swimmingSpeed = swimmingSpeedHatch + Math.pow(10,
                (Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch));

        swimmingSpeed = swimmingSpeed / 100; // convert in m/s
        return swimmingSpeed;
    }

    public double getVelocityCsv(IParticle particle) {

        double age = particle.getAge(); // seconds
        for (int i = 0; i < ageCsv.length - 1; i++) {
            if ((age >= ageCsv[i]) && (age < ageCsv[i + 1])) {
                return speedCsv[i]; // value already in m/s
            }
        }

        return speedCsv[speedCsv.length - 1];

    }

    private void initVelocityCsv() throws CsvException, IOException {
        if (!isNull("swimming.speed.csv.file")) {
            String pathname = IOTools.resolveFile(getParameter("swimming.speed.csv.file"));
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("Density file " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("Density file " + pathname + " cannot be read.");
            }
            loadDensities(pathname);
        }
    }

    private void loadDensities(String csvFile) throws CsvException {
        Locale.setDefault(Locale.US);
        try {
            // open densities csv file
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();

            // init arrays
            ageCsv = new double[lines.size() - 1];
            speedCsv = new double[ageCsv.length];

            // read ageCsv (days converted to seconds) and
            for (int i = 0; i < ageCsv.length; i++) {
                String[] line = lines.get(i + 1);
                ageCsv[i] = Double.valueOf(line[0]) * secs_in_day; // age in seconds
                speedCsv[i] = Double.valueOf(line[1]) / 100; // values in m/s
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
