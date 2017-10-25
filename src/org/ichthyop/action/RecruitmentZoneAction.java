/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.action;

import org.ichthyop.util.Constant;
import org.ichthyop.particle.IParticle;
import org.ichthyop.output.RecruitmentZoneTracker;
import org.ichthyop.output.ZoneTracker;
import org.ichthyop.particle.LengthParticle;
import org.ichthyop.particle.RecruitableParticle;

/**
 *
 * @author pverley
 */
public class RecruitmentZoneAction extends AbstractAction {

    /**
     * Minimum duration [second] a particle has to spend within the same zone
     * before being recruited.
     */
    private static int durationMinInRecruitArea;
    /**
     * Duration [second] presently spent by the particle within the current
     * zone.
     */
    private int timeInZone;
    private float ageMinAtRecruitment;
    private float lengthMinAtRecruitment;
    private boolean isAgeCriterion;
    private boolean stopMovingOnceRecruited;
    private String zonePrefix;

    @Override
    public String getKey() {
        return "action.recruitment.zone";
    }

    @Override
    public void loadParameters() throws Exception {

        timeInZone = 0;
        durationMinInRecruitArea = (int) (getConfiguration().getFloat("action.recruitment.zone.duration_min") * 24.f * 3600.f);
        isAgeCriterion = getConfiguration().getString("action.recruitment.zone.criterion").equals("Age criterion");
        boolean isGrowth = getConfiguration().getBoolean("action.growth.enabled");
        if (!isGrowth && !isAgeCriterion) {
            throw new IllegalArgumentException("{Recruitment} Recruitment criterion cannot be based on particle length since the growth model is not activated. Activate the growth model or set a recruitment criterion based on particle age.");
        }
        if (isAgeCriterion) {
            ageMinAtRecruitment = getConfiguration().getFloat("action.recruitment.zone.limit_age");
        } else {
            lengthMinAtRecruitment = getConfiguration().getFloat("action.recruitment.zone.limit_length");
        }
        stopMovingOnceRecruited = getConfiguration().getBoolean("action.recruitment.zone.stop_moving");
        zonePrefix = getConfiguration().getString("action.recruitment.zone.zone_prefix");
        getSimulationManager().getZoneManager().loadZones(zonePrefix);
        boolean addTracker = true;
        try {
            addTracker = getConfiguration().getBoolean("action.recruitment.zone.recruited_tracker");
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(RecruitmentZoneTracker.class);
        }
        addTracker = true;
        try {
            addTracker = getConfiguration().getBoolean("action.recruitment.zone.zone_tracker");
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
        }
    }

    @Override
    public void init(IParticle particle) {
        RecruitableParticle.init(particle);
    }

    @Override
    public void execute(IParticle particle) {

        if (stopMovingOnceRecruited && RecruitableParticle.isRecruited(particle)) {
            particle.lock();
            return;
        }

        Float[] indexes = getSimulationManager().getZoneManager().findZones(particle, zonePrefix);
        for (float index : indexes) {
            if (!RecruitableParticle.isRecruited(particle, index)) {
                if (satisfyRecruitmentCriterion(particle)) {
                    timeInZone = (RecruitableParticle.getCurrentRecruimentZone(particle) == index)
                            ? timeInZone + getSimulationManager().getTimeManager().get_dt()
                            : 0;
                    RecruitableParticle.setCurrentRecruimentZone(particle, index);
                    if (timeInZone >= durationMinInRecruitArea) {
                        RecruitableParticle.recruit(particle, index);
                    }
                }
            }
        }

    }

    private boolean satisfyRecruitmentCriterion(IParticle particle) {
        if (isAgeCriterion) {
            return ((float) particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (LengthParticle.getLength(particle) >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }
}
