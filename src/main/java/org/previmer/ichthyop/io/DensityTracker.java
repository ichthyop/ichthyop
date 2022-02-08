package org.previmer.ichthyop.io;

import java.util.Iterator;

import com.sleepycat.je.config.IntConfigParam;

import org.previmer.ichthyop.Population;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.particle.IParticle;

public class DensityTracker extends SimulationManagerAccessor {

    private int[][] density;
    private float lonMin;
    private float lonMax;
    private float deltaLon;
    private float latMin;
    private float latMax;
    private float deltaLat;
    private int nLon, nLat;
    private float[] lonOut, latOut;

    public void init() {

        // Provides the minimum and maximum longitudes
        // of output grid edge.
        lonMin = Float.valueOf("output.density.lonmin");
        lonMax = Float.valueOf("output.density.lonmax");
        deltaLon = Float.valueOf("output.density.dlon");
        // swap arrays if bad definition
        if (lonMin > lonMax) {
            float temp = lonMin;
            lonMin = lonMax;
            lonMax = temp;
        }

        latMin = Float.valueOf("output.density.latmin");
        latMax = Float.valueOf("output.density.latmax");
        deltaLat = Float.valueOf("output.density.dlat");
        // swap arrays if bad definition
        if (latMin > latMax) {
            float temp = latMin;
            latMin = latMax;
            latMax = temp;
        }

        // Output grid will contain nLon, nLat values
        // but edges will have nLon+1, nLat+1 elements
        nLon = (int) ((lonMax - lonMin) / deltaLon);
        nLat = (int) ((latMax - latMin) / deltaLat);

        lonOut = new float[nLon + 1];
        for (int i = 0; i < nLon + 1; i++) {
            lonOut[i] = lonMin + i * deltaLon;
        }

        latOut = new float[nLat + 1];
        for (int i = 0; i < nLon; i++) {
            latOut[i] = latMin + 1 + i * deltaLat;
        }

        density = new int[nLat][nLon];

    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "app.output", key);
    }

    public void incrementDensity() {

        density = new int[nLat][nLon];

        Population population = getSimulationManager().getSimulation().getPopulation();
        Iterator<IParticle> iter = population.iterator();
        IParticle particle;
        while (iter.hasNext()) {
            particle = iter.next();
            if (particle.isLiving()) {
                float lonPart = (float) particle.getLon();
                float latPart = (float) particle.getLat();
                int indexLon = (int) Math.floor(nLon * (lonPart - lonMin) / (lonMax - lonMin));
                int indexLat = (int) Math.floor(nLat * (latPart - latMin) / (latMax - latMin));
                density[indexLat][indexLon] += 1;
            }
        }
    }

}
