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
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.util.CheckGrowthParam;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public abstract class OrientationVelocity extends AbstractAction {

    private double secs_in_day = 86400;

    private double swimmingSpeedHatch; // cm / s
    private double swimmingSpeedSettle; // cm / s
    private double PLD; // days

    private double[] classCsv; // age array from CSV (seconds)
    private double[] speedCsv; // speed array (m/s);
    private double velocityPerLengthUnit;  // cm/s
    private boolean useCsv = false;
    private String method = "age";

    private interface GetValue {
        double getValue(IParticle particle);
    }

    @FunctionalInterface
    public interface InnerOrientationVelocity {
        double getVelocity(IParticle particle);
    }

    private InnerOrientationVelocity velocityMethod;

    @Override
    public void loadParameters() throws Exception {


        String key = "swimming.speed.mode";
        if(!isNull(key)) {
            method = getParameter(key);
        }

        // Allows for backward compatibility
        key = "swimming.speed.csv.enabled";
        if (!isNull(key)) {
            useCsv = Boolean.valueOf(getParameter(key));
        }

        boolean isGrowth = CheckGrowthParam.checkParams();  // check if growth or debgrowth is true (xor)
        if (!isGrowth && method.equals("length")) {
            throw new IllegalArgumentException("Velocity cannot be based on particle length since no growth model not activated.");
        }

        switch (method) {
            case "age":

                if(useCsv) {
                    velocityMethod = (IParticle particle) -> getVelocityCsv(particle, particle_temp -> (particle_temp.getAge() / secs_in_day));
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
                break;

            case "length":
                if (useCsv) {
                    velocityMethod = (IParticle particle) -> getVelocityCsv(particle, particle_temp -> particle_temp.getLength());
                    initVelocityCsv();
                } else {
                    velocityPerLengthUnit = Double.valueOf(getParameter("swimming.body.length.speed")) / 100;
                    velocityMethod = (IParticle particle) -> getVelocityLength(particle);
                }
                break;
            default:
                break;
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

    public double getVelocityCsv(IParticle particle, GetValue getValue) {

        double age = getValue.getValue(particle); // seconds
        for (int i = 0; i < classCsv.length - 1; i++) {
            if ((age >= classCsv[i]) && (age < classCsv[i + 1])) {
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
            classCsv = new double[lines.size() - 1];
            speedCsv = new double[classCsv.length];

            // read ageCsv (days converted to seconds) and
            for (int i = 0; i < classCsv.length; i++) {
                String[] line = lines.get(i + 1);
                classCsv[i] = Double.valueOf(line[0]); // age in days or length in cm
                speedCsv[i] = Double.valueOf(line[1]) / 100; // values in m/s
            }
        } catch (IOException ex) {
            Logger.getLogger(BuoyancyAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double getVelocityLength(IParticle particle) {
        return  particle.getLength() * velocityPerLengthUnit;
    }

}
