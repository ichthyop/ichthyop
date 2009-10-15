/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.release;

import fr.ird.ichthyop.event.ReleaseEvent;
import fr.ird.ichthyop.particle.ParticleFactory;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IBasicParticle;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractReleaseProcess {

    private int nbReleasedNow, nbReleaseZones, nbTotalToRelease;
    private int nbReleaseEvents;
    private boolean is3D;

    public void loadParameters() {
        nbTotalToRelease = Integer.valueOf(getParameter("number_particles"));
        nbReleaseZones = getSimulation().getZoneManager().getZones(TypeZone.RELEASE).size();
        nbReleaseEvents = getSimulation().getReleaseManager().getSchedule().getNbReleaseEvents();
        is3D = Boolean.valueOf(getSimulation().getParameterManager().getValue("app.transport", "three_dimension"));
    }

    public void proceedToRelease(ReleaseEvent event) throws IOException {

        int indexEvent = event.getSource().getIndexEvent();

        nbReleasedNow = nbTotalToRelease / nbReleaseEvents;
        int mod = nbTotalToRelease % nbReleaseEvents;
        nbReleasedNow += (indexEvent < mod) ? 1 : 0;

        double xmin, xmax, ymin, ymax;
        double upDepth = Double.MAX_VALUE, lowDepth = 0.d;
        /** Reduces the release area function of the user-defined zones */
        xmin = Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        xmax = 0.d;
        ymax = 0.d;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulation().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            xmin = Math.min(xmin, zone.getXmin());
            xmax = Math.max(xmax, zone.getXmax());
            ymin = Math.min(ymin, zone.getYmin());
            ymax = Math.max(ymax, zone.getYmax());
            if (is3D) {
                upDepth = Math.min(upDepth, zone.getUpperDepth());
                lowDepth = Math.max(lowDepth, zone.getLowerDepth());
            } else {
                upDepth = lowDepth = Double.NaN;
            }
        }

        int index = Math.max(getSimulation().getPopulation().size() - 1, 0);
        for (int p = 0; p < nbReleasedNow; p++) {
            /** Instantiate a new Particle */
            IBasicParticle particle = ParticleFactory.createParticle(index, xmin, xmax, ymin, ymax, upDepth, lowDepth);
            getSimulation().getPopulation().add(particle);
            index++;
        }
    }

    public int getNbParticles() {
        return Integer.valueOf(getParameter("number_particles"));
    }
}
