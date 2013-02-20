/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.particle.ParticleMortality;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class NcFileRelease extends AbstractReleaseProcess {

    private String filename;

    @Override
    public void loadParameters() throws IOException {

        /* make sure file exits and can be read */
        filename = IOTools.resolveFile(getParameter("ncfile"));

        File file = new File(filename);
        if (!file.isFile()) {
            throw new FileNotFoundException("NetCDF file " + filename + " not found.");
        }
        if (!file.canRead()) {
            throw new IOException("NetCDF file " + file + " cannot be read");
        }
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        double time = event.getSource().getTime();
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);

        NetcdfFile nc = NetcdfDataset.openFile(filename, null);
        ArrayDouble.D1 timeArr = (ArrayDouble.D1) nc.findVariable("time").read();
        int rank = 0;
        int length = timeArr.getShape()[0];

        /** Find current rank */
        double time_rank0 = timeArr.get(0), time_rank1;
        while (rank < length) {
            rank++;
            time_rank1 = timeArr.get(rank);
            if ((time >= time_rank0 && time < time_rank1)
                    || (time <= time_rank0 && time > time_rank1)) {
                break;
            }
            if (time == time_rank1) {
                rank++;
                time_rank0 = time_rank1;
                break;
            }
            time_rank0 = time_rank1;
        }
        rank--;

        double x = 0.f;
        if (time != time_rank0) {
            x = (time - timeArr.get(rank))
                    / (timeArr.get(rank + 1) - timeArr.get(rank));
        }

        ArrayFloat.D2 lonArr = (ArrayFloat.D2) nc.findVariable("lon").read();
        ArrayFloat.D2 latArr = (ArrayFloat.D2) nc.findVariable("lat").read();
        ArrayInt.D2 mortalityArr = (ArrayInt.D2) nc.findVariable("mortality").read();
        boolean bln3D = getSimulationManager().getDataset().is3D();
        ArrayFloat.D2 depthArr = null;
        if (bln3D) {
            depthArr = (ArrayFloat.D2) nc.findVariable("depth").read();
        }
        double lon, lat, depth = Double.NaN;
        IBasicParticle particle;
        int nb_particles = lonArr.getShape()[1];

        for (int i = 0; i < nb_particles; i++) {

            if (x == 0.f) {
                lon = lonArr.get(rank, i);
                lat = latArr.get(rank, i);
                if (bln3D) {
                    depth = depthArr.get(rank, i);
                }
            } else {
                lon = x * lonArr.get(rank + 1, i)
                        + (1 - x) * lonArr.get(rank, i);
                lat = x * latArr.get(rank + 1, i)
                        + (1 - x) * latArr.get(rank, i);
                if (bln3D) {
                    depth = x * depthArr.get(rank + 1, i)
                            + (1 - x) * depthArr.get(rank, i);
                }
            }

            particle = ParticleFactory.createGeoParticle(index, lon, lat, depth, ParticleMortality.getMortality(mortalityArr.get(rank, i)));
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
        }

        nc.close();
        return index;
    }

    @Override
    public int getNbParticles() {

        try {
            return NetcdfDataset.open(filename).findDimension("drifter").getLength();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        return -1;
    }
}