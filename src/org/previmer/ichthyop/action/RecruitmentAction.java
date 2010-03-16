/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IRecruitableParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IZoneParticle;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.RecruitableParticleLayer;
import org.previmer.ichthyop.particle.ZoneParticleLayer;

/**
 *
 * @author pverley
 */
public class RecruitmentAction extends AbstractAction {

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
    private int dt;
    private float ageMinAtRecruitment;
    private float lengthMinAtRecruitment;
    private boolean isAgeCriterion;
    private boolean stopMovingOnceRecruited;

    public void loadParameters() {

        timeInZone = 0;
        durationMinInRecruitArea = Integer.valueOf(getParameter("duration_min"));
        isAgeCriterion = getParameter("recruitment_criterion").matches("age_criterion");
        if (isAgeCriterion) {
            ageMinAtRecruitment = Float.valueOf(getParameter("recruitment_limit_age"));
        } else {
            lengthMinAtRecruitment = Float.valueOf(getParameter("recruitment_limit_length"));
        }
        stopMovingOnceRecruited = Boolean.valueOf(getParameter("recruitment_stop_moving"));
    }

    public void execute(IBasicParticle particle) {

        //@todo
        // catch cast exception
        IRecruitableParticle rParticle = (IRecruitableParticle) particle.getLayer(RecruitableParticleLayer.class);
        if (stopMovingOnceRecruited && rParticle.isRecruited()) {
            particle.lock();
            return;
        }

        int numCurrentZone = ((IZoneParticle) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RECRUITMENT);
        if ((numCurrentZone != -1) && !rParticle.isRecruited(numCurrentZone)) {

            if (satisfyRecruitmentCriterion(particle)) {
                timeInZone = (rParticle.getNumRecruitmentZone() == numCurrentZone)
                        ? timeInZone + dt
                        : 0;
                rParticle.setNumRecruitmentZone(numCurrentZone);
                rParticle.setNewRecruited(timeInZone >= durationMinInRecruitArea);
                rParticle.setRecruited(numCurrentZone, rParticle.isNewRecruited());
            }
        }
    }

    private boolean satisfyRecruitmentCriterion(IBasicParticle particle) {
        if (isAgeCriterion) {
            return (float) (particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (((IGrowingParticle) particle.getLayer(GrowingParticleLayer.class)).getLength() >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }
}