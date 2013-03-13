/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.RecruitmentZoneTracker;
import org.previmer.ichthyop.io.ZoneTracker;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.RecruitableParticleLayer;
import org.previmer.ichthyop.particle.ZoneParticleLayer;

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
     * Duration [second] presently spent by the particule within the
     * current zone.
     */
    private int timeInZone;
    private float ageMinAtRecruitment;
    private float lengthMinAtRecruitment;
    private boolean isAgeCriterion;
    private boolean stopMovingOnceRecruited;

    public void loadParameters() throws Exception {

        timeInZone = 0;
        durationMinInRecruitArea = (int) (Float.valueOf(getParameter("duration_min")) * 24.f * 3600.f);
        isAgeCriterion = getParameter("criterion").equals(Criterion.AGE.toString());
        boolean isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
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

    public void execute(IBasicParticle particle) {

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
                        ? timeInZone + getSimulationManager().getTimeManager().get_dt()
                        : 0;
                rParticle.setNumRecruitmentZone(numCurrentZone);
                rParticle.setNewRecruited(timeInZone >= durationMinInRecruitArea);
                rParticle.setRecruited(numCurrentZone, rParticle.isNewRecruited());
            }
        }
    }

    private boolean satisfyRecruitmentCriterion(IBasicParticle particle) {
        if (isAgeCriterion) {
            return ((float) particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getLength() >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }

    public enum Criterion {

        LENGTH("Length criterion"),
        AGE("Age criterion");
        private String name;

        Criterion(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
