package org.previmer.ichthyop.io;

import java.util.Iterator;

import org.previmer.ichthyop.Population;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.particle.IParticle;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;

public class DensityTracker extends SimulationManagerAccessor {

    private float lonMin;
    private float lonMax;
    private float deltaLon;
    private float latMin;
    private float latMax;
    private float deltaLat;
    private int nLon, nLat;
    private float[] lonEdges, latEdges;
    private float[] lonCell, latCell;

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
        
        // Reconstructing longitude edges
        lonEdges = new float[nLon + 1];
        for (int i = 0; i < nLon + 1; i++) {
            lonEdges[i] = lonMin + i * deltaLon;
        }
        
        // reconstructing longitude centers
        lonCell = new float[nLon];
        for (int i = 0; i < nLon; i++) {
            lonCell[i] = 0.5f * (lonEdges[i] + lonEdges[i + 1]);
        }

        latEdges = new float[nLat + 1];
        for (int i = 0; i < nLat + 1; i++) {
            latEdges[i] = latMin + 1 + i * deltaLat;
        }

        // reconstructing latitude centers
        latCell = new float[nLat];
        for (int i = 0; i < nLat; i++) {
            latCell[i] = 0.5f * (latEdges[i] + latEdges[i + 1]);
        }

    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "app.output", key);
    }

    public Array getDensity() {

        int[][] density = new int[nLat][nLon];

        Population population = getSimulationManager().getSimulation().getPopulation();
        Iterator<IParticle> iter = population.iterator();
        IParticle particle;
        while (iter.hasNext()) {
            particle = iter.next();
            if (particle.isLiving()) {
                float lonPart = (float) particle.getLon();
                float latPart = (float) particle.getLat();
                // If particle is out of the density domain, nothing is done.
                if((lonPart > lonMax) || (lonPart < lonMin) || (latPart > latMax) || (latPart < latMin)) {
                    continue;
                }
                int indexLon = (int) Math.floor(nLon * (lonPart - lonMin) / (lonMax - lonMin));
                int indexLat = (int) Math.floor(nLat * (latPart - latMin) / (latMax - latMin));
                density[indexLat][indexLon] += 1;
            }
        }
        
        ArrayFloat.D2 output = new ArrayFloat.D2(nLat, nLon);
        Index index = output.getIndex();
        for(int j=0; j<nLat; j++) {
            for(int i=0; i<nLon; i++) {
                   index.set(j, i);
                   output.set(index, density[j][i]);
            }
        }
        
        return output;
    }

    public int getNLon() {
        return this.nLon;
    }

    public int getNLat() {
        return this.nLat;
    }

    public float[] getLonCells() {
        return this.lonCell;
    }

    public float[] getLatCells() {
        return this.latCell;
    }


}
