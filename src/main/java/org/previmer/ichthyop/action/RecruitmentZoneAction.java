/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.io.RecruitmentZoneTracker;
import org.previmer.ichthyop.io.ZoneTracker;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.RecruitableParticleLayer;
import org.previmer.ichthyop.particle.ZoneParticleLayer;
import org.previmer.ichthyop.util.CheckGrowthParam;

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

    @Override
    public void loadParameters() throws Exception {

        timeInZone = 0;
        durationMinInRecruitArea = (int) (Float.valueOf(getParameter("duration_min")) * 24.f * 3600.f);
        isAgeCriterion = getParameter("criterion").equals("Age criterion");        
        boolean isGrowth = CheckGrowthParam.checkParams();
        if (!isGrowth && !isAgeCriterion) {
            throw new IllegalArgumentException("{Recruitment} Recruitment criterion cannot be based on particle length since the growth model is not activated. Activate the growth model or set a recruitment criterion based on particle age.");
        }
        if (isAgeCriterion) {
            ageMinAtRecruitment = Float.valueOf(getParameter("limit_age"));
        } else {
            lengthMinAtRecruitment = Float.valueOf(getParameter("limit_length"));
        }
        stopMovingOnceRecruited = Boolean.valueOf(getParameter("stop_moving"));
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.RECRUITMENT);
        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("recruited_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(RecruitmentZoneTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("zone_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
        }
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {

        //@todo
        // catch cast exception
        RecruitableParticleLayer rParticle = (RecruitableParticleLayer) particle.getLayer(RecruitableParticleLayer.class);
        if (stopMovingOnceRecruited && rParticle.isRecruited()) {
            particle.lock();
            return;
        }

        int numCurrentZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RECRUITMENT);
        if ((numCurrentZone != -1) && !rParticle.isRecruited(numCurrentZone)) {

            if (satisfyRecruitmentCriterion(particle)) {
                timeInZone = (rParticle.getNumRecruitmentZone() == numCurrentZone)
                        ? timeInZone + Math.abs(getSimulationManager().getTimeManager().get_dt())
                        : 0;
                rParticle.setNumRecruitmentZone(numCurrentZone);
                rParticle.setNewRecruited(timeInZone >= durationMinInRecruitArea);
                rParticle.setRecruited(numCurrentZone, rParticle.isNewRecruited());
            }
        }
    }

    private boolean satisfyRecruitmentCriterion(IParticle particle) {
        if (isAgeCriterion) {
            return ((float) particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class)).getLength() >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }
}
