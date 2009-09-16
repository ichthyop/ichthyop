/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Recruitment extends AbstractAction {

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

    public void loadParameters() {

        timeInZone = 0;
        durationMinInRecruitArea = Integer.valueOf(getParameter("recruitment.durationMinInRecruitArea"));
        isAgeCriterion = getParameter("recruitment.criterion").matches(getProperty("recruitment.criterion.age"));
        if (isAgeCriterion) {
            ageMinAtRecruitment = Float.valueOf(getParameter("recruitment.limit.age"));
        } else {
            lengthMinAtRecruitment = Float.valueOf(getParameter("recruitment.limit.length"));
        }
    }

    public void execute(IBasicParticle particle) {

        //@todo
        // catch cast exception
        IRecruitableParticle rParticle = (IRecruitableParticle)particle;
        int numCurrentZone = rParticle.getNumZone();
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
            return (float) (((IGrowingParticle) particle).getLength() / Constant.ONE_DAY) >= lengthMinAtRecruitment;
        }
    }
}
