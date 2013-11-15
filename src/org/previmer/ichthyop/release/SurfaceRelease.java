/*
 * Copyright (C) 2011 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.arch.IParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class SurfaceRelease extends AbstractRelease {
    
    private int nParticles;
    private boolean is3D;

    @Override
    public void loadParameters() throws Exception {
        nParticles = Integer.valueOf(getParameter("number_particles"));
        is3D = getSimulationManager().getDataset().is3D();
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {
        
        int DROP_MAX = 2000;
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        int nx = getSimulationManager().getDataset().get_nx();
        int ny = getSimulationManager().getDataset().get_ny();
        System.out.println("nx " + nx + " ny " + ny);
        
        for (int p = 0; p < nParticles; p++) {
            IParticle particle = null;
            int counter = 0;
            while (null == particle) {

                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Release surface} Unable to release particle.");
                }
                double x = Math.random() * (nx - 1);
                double y = Math.random() * (ny - 1);
                particle = ParticleFactory.createSurfaceParticle(index, x, y, is3D);
            }
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
        }
        return index;
    }

    @Override
    public int getNbParticles() {
        return nParticles;
    }
    
}
