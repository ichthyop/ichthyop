/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class PatchyRelease extends AbstractReleaseProcess {

    private int nb_patches;
    private int nb_agregated;
    private double radius_patch, thickness_patch;
    private int nbPatchesNow, nbReleaseZones;
    private int nbReleaseEvents;
    private boolean is3D;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    @Override
    public void loadParameters() throws Exception {
        
        /* retrieve patches parameters */
        nb_patches = Integer.valueOf(getParameter("number_patches"));
        nb_agregated = Integer.valueOf(getParameter("number_agregated"));
        radius_patch = Float.valueOf(getParameter("radius_patch"));
        thickness_patch = Float.valueOf(getParameter("thickness_patch"));
        
        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().is3D();

        /* Load release zones*/
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.RELEASE);
        nbReleaseZones = (null != getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE))
                ? getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).size()
                : 0;
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        int indexEvent = event.getSource().getIndexEvent();
        nbReleaseEvents = getSimulationManager().getReleaseManager().getNbReleaseEvents();

        nbPatchesNow = nb_patches / nbReleaseEvents;
        int mod = nb_patches % nbReleaseEvents;
        nbPatchesNow += (indexEvent < mod) ? 1 : 0;

        double xmin, xmax, ymin, ymax;
        double upDepth = Double.MAX_VALUE, lowDepth = 0.d;
        /** Reduces the release area function of the user-defined zones */
        xmin = Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        xmax = 0.d;
        ymax = 0.d;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
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

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size() - 1, 0);
        for (int p = 0; p < nbPatchesNow; p++) {
            /** Instantiate a new Particle */
            IBasicParticle particle = ParticleFactory.createParticle(index, xmin, xmax, ymin, ymax, upDepth, lowDepth);
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
            for (int f = 0; f < nb_agregated - 1; f++) {
                double lat = particle.getLat() + radius_patch * (Math.random() - 0.5d) / ONE_DEG_LATITUDE_IN_METER;
                double one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * particle.getLat() / 180.d);
                double lon = particle.getLon() + radius_patch * (Math.random() - 0.5d) / one_deg_longitude_meter;
                double depth = Double.NaN;
                if (is3D) {
                    depth = particle.getDepth() + thickness_patch * (Math.random() - 0.5d);
                }
                IBasicParticle particlePatch = ParticleFactory.createParticle(index, lon, lat, depth);
                getSimulationManager().getSimulation().getPopulation().add(particlePatch);
                index++;
            }
        }
        return index;
    }

    public int getNbParticles() {
        return nb_patches * nb_agregated;
    }
}
