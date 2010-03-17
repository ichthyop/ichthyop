/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.event.ReleaseEvent;
import java.io.IOException;
import org.previmer.ichthyop.arch.IBasicParticle;
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

    private String pathname;

    @Override
    void loadParameters() {
        pathname = getParameter("ncfile");
    }

    @Override
    void proceedToRelease(ReleaseEvent event) throws IOException {

        double time = event.getSource().getTime();
        int index = 0;

        NetcdfFile nc = NetcdfDataset.openFile(pathname, null);
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
        System.out.println(rank + " " + time);

        double x = 0.f;
        if (time != time_rank0) {
            x = (time - timeArr.get(rank))
                    / (timeArr.get(rank + 1) - timeArr.get(rank));
        }

        ArrayFloat.D2 lonArr = (ArrayFloat.D2) nc.findVariable("lon").read();
        ArrayFloat.D2 latArr = (ArrayFloat.D2) nc.findVariable("lat").read();
        //ArrayInt.D2 deathArr = (ArrayInt.D2) nc.findVariable("death").read();
        boolean bln3D = true;
        if (null != nc.findGlobalAttribute("transport_dimension")) {
            /* This test allows backward compatibility with ichthyop
             * output files anterior to v3.0, since the
             * "transport_dimension" attribute only exists from v3.0
             */
            bln3D = nc.findGlobalAttribute("transport_dimension").getStringValue().matches("three dimensions");
        } else {
            bln3D = (null != nc.findVariable("depth"));
        }
        ArrayFloat.D2 depthArr = null;
        if (bln3D) {
            depthArr = (ArrayFloat.D2) nc.findVariable("depth").read();
        }
        double lon, lat, depth = Double.NaN;
        IBasicParticle particle;
        int nb_particles = getNbParticles();

        boolean living;
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

            //living = (deathArr.get(rank, i) == Constant.DEAD_NOT);
            living = true;
            particle = ParticleFactory.createParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
        }

        lonArr = null;
        latArr = null;
        depthArr = null;
        //deathArr = null;
        nc.close();
    }

    public int getNbParticles() {

        try {
            return NetcdfDataset.open(pathname).findDimension("drifter").getLength();
        } catch (IOException ex) {
            Logger.getLogger(NcFileRelease.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
}
