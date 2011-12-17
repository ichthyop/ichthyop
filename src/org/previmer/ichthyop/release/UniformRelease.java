package org.previmer.ichthyop.release;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author mariem
 */
public class UniformRelease extends AbstractReleaseProcess {

    private int nb_particles;
    private float depth_min;
    private float depth_max;

    public void loadParameters() throws Exception {
        nb_particles = Integer.valueOf(getParameter("number_particles"));
        depth_min = Float.valueOf(getParameter("depth_min"));
        depth_max = Float.valueOf(getParameter("depth_max"));
    }

    public int release(ReleaseEvent event) throws Exception {

        int DROP_MAX = 2000;
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size() - 1, 0);
        int nx = getSimulationManager().getDataset().get_nx();
        int ny = getSimulationManager().getDataset().get_ny();
       
        double max_bathy = getSimulationManager().getDataset().getDepthMax();
        
        for (int p = 0; p < nb_particles; p++) {
            IBasicParticle particle = null;
            int counter = 0, buf;
            double x, y, z;
            while (null == particle) {

                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Uniform Release} Unable to release particle.");
                }
                do{
                x = Math.random() * (nx - 1);
                y = Math.random() * (ny - 1);
                buf= (int) getSimulationManager().getDataset().getBathy((int) x, (int) y);
                }while (buf< depth_min);
                
                do{
                z = depth_min + (Math.random() * (depth_max - depth_min));
                } while (depth_max > buf);
               
                particle = ParticleFactory.createUniformParticle(index, x, y, z);
            }
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
        }
        return index;
    }

    public int getNbParticles() {
        return nb_particles;
    }
}