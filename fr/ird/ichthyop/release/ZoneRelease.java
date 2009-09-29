/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.release;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.release.ReleaseEvent;
import fr.ird.ichthyop.release.AbstractReleaseProcess;
import fr.ird.ichthyop.arch.IBasicParticle;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractReleaseProcess {

    private int nbReleasedNow, nbReleaseZones, nbTotalToRelease;
    private int nbReleaseEvents;

    ZoneRelease() {
        loadParameters();
    }

    private void loadParameters() {
        nbTotalToRelease = Integer.valueOf(getParameter("number_particles"));
        nbReleaseZones = getSimulation().getZoneManager().getZones(TypeZone.RELEASE).size();
        nbReleaseEvents = getSimulation().getReleaseManager().getSchedule().getNbReleaseEvents();
    }

    public void release(ReleaseEvent event) throws IOException {

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
            upDepth = Math.min(upDepth, zone.getUpperDepth());
            lowDepth = Math.max(lowDepth, zone.getLowerDepth());
        }

        int index = Math.max(getSimulation().getPopulation().size() - 1, 0);
        for (int p = 0; p < nbReleasedNow; p++) {
            /** Instantiate a new Particle */
            IBasicParticle particle = null;// = ParticleFactory.createParticle(index, true, xmin, xmax, ymin, ymax, upDepth, lowDepth);
            getSimulation().getPopulation().add(particle);
            index++;
        }
    }
}
