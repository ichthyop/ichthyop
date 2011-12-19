/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.arch.IBasicParticle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 *
 * @author pverley
 */
public class TimeDrifterRelease extends AbstractReleaseProcess {

    //private File textFile;
    private boolean is3D;
    private int nbParticles;
    private HashMap<String, File> mapRelease;

    @Override
    public void loadParameters() throws IOException {

        File directory = getFile(getParameter("directory"));
        is3D = getSimulationManager().getDataset().is3D();
        mapRelease = createMapRelease(directory);
        nbParticles = 0;
        for (File file : mapRelease.values()) {
            nbParticles += readNbParticles(file);
        }

    }

    private File getFile(String filename) throws IOException {

        String pathname = IOTools.resolveFile(filename);
        File file = new File(pathname);
        if (!file.isDirectory()) {
            throw new FileNotFoundException("Drifter directory " + filename + " not found.");
        }
        if (null == file.list()) {
            throw new IOException("Drifter directory " + file + " is empty");
        }
        return file;
    }

    @Override
    public int release(ReleaseEvent event) throws IOException {

        int index = 0;
        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.US);

        File drifterFile = findReleaseFile(mapRelease, event.getSource().getTime());
        if (null != drifterFile) {
            getSimulationManager().getLogger().info("Drifter text file " + drifterFile.toString());
        } else {
            getSimulationManager().getLogger().warning("Could not find any drifter text file for release date " + getSimulationManager().getTimeManager().timeToString());
            return 0;
        }

        BufferedReader bfIn = new BufferedReader(new FileReader(drifterFile));
        String line;
        while ((line = bfIn.readLine()) != null) {
            if (!line.startsWith("#") & !(line.length() < 1)) {
                strCoord = line.trim().split(" ");
                coord = new double[strCoord.length];
                for (int i = 0; i < strCoord.length; i++) {
                    try {
                        coord[i] = nbFormat.parse(strCoord[i].trim()).doubleValue();
                    } catch (ParseException ex) {
                        IOException ioex = new IOException("{Drifter release} Failed to read drifter position at line " + (index + 1) + " ==> " + ex.getMessage());
                        ioex.setStackTrace(ex.getStackTrace());
                        throw ioex;
                    }
                }
                IBasicParticle particle;
                if (is3D) {
                    double depth = coord.length > 2
                            ? coord[2]
                            : 0.d;
                    if (depth > 0) {
                        depth *= -1;
                    }
                    particle = ParticleFactory.createGeoParticle(index, coord[0], coord[1], depth);
                } else {
                    particle = ParticleFactory.createGeoParticle(index, coord[0], coord[1]);
                }
                if (null != particle) {
                    //Logger.getAnonymousLogger().info("Adding new particle: " + particle.getLon() + " " + particle.getLat());
                    getSimulationManager().getSimulation().getPopulation().add(particle);
                    index++;
                } else {
                    throw new IOException("{Drifter release} Drifter at line " + (index + 1) + " is not in water");
                }
            }
        }        
        return index;
    }

    private int readNbParticles(File drifterFile) throws IOException {

        int index = 0;
        BufferedReader bfIn = new BufferedReader(new FileReader(drifterFile));
        String line;
        while ((line = bfIn.readLine()) != null) {
            if (!line.startsWith("#") & !(line.length() < 1)) {
                index++;
            }
        }
        return index;
    }

    public int getNbParticles() {
        return nbParticles;
    }

    private File findReleaseFile(HashMap<String, File> map, double time) {

        File file = null;
        String dateOn = null;
        for (String date : map.keySet()) {
            double match = time - dateStrToSeconds(date);
            if (match == 0) {
                dateOn = date;
                file = map.get(date);
            }
        }
        if (null != dateOn) {
            map.remove(dateOn);
        }
        return file;
    }

    /* 
     * Transforms a String such as y2010m05d13h1200 into seconds.
     * 
     * @param dateStr date formatted as "'y'yyyy'm'MM'd'dd'h'HHmm"
     * @return date into seconds elapsed since origin of time
     */
    private double dateStrToSeconds(String dateStr) {

        Calendar calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();

        SimpleDateFormat releaseDateFormat = new SimpleDateFormat("'y'yyyy'm'MM'd'dd'h'HHmm");
        releaseDateFormat.setCalendar(calendar);
        double seconds = Double.NaN;
        try {
            calendar.setTime(releaseDateFormat.parse(dateStr));
            seconds = calendar.getTimeInMillis() / 1000.d;
        } catch (ParseException ex) {
            Logger.getLogger(TimeDrifterRelease.class.getName()).log(Level.SEVERE, null, ex);
        }
        return seconds;
    }

    private HashMap<String, File> createMapRelease(File directory) {

        HashMap<String, File> map = new HashMap();

        for (File filename : directory.listFiles(new MetaFilenameFilter("*.txt"))) {
            String[] tokens = filename.getName().split("_");
            String filedate = tokens[tokens.length - 1].replace(".txt", "");
            map.put(filedate, filename);
        }
        return map;
    }
}
