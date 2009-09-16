/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import java.util.Iterator;

/**
 *
 * @author pverley
 */
public class Recruitment extends AbstractAction {

    private int numCurrentZone;
    /**
     * Number of the zone where the particle has been recruited.
     */
    private int numRecruitZone;
    /**
     * Recruitment status for each recruitment zone.
     */
    private boolean[] isRecruited;
    /**
     * Inform whether a particle is newly recruited.
     */
    private boolean isNewRecruited;
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

        numRecruitZone = -1;
        timeInZone = 0;
        durationMinInRecruitArea = Integer.valueOf(getParameter("recruitment.durationMinInRecruitArea"));
        isRecruited = new boolean[ICFile.getInstance().getZones().size()];
        isNewRecruited = false;
        isAgeCriterion = getParameter("recruitment.criterion").matches(getProperty("recruitment.criterion.age"));
        if (isAgeCriterion) {
            ageMinAtRecruitment = Float.valueOf(getParameter("recruitment.limit.age"));
        } else {
            lengthMinAtRecruitment = Float.valueOf(getParameter("recruitment.limit.length"));
        }
    }

    public void execute(IBasicParticle particle) {

        numCurrentZone = getNumZone(particle);
        if ((numCurrentZone != -1) && !isRecruited[numCurrentZone]) {

            if (satisfyRecruitmentCriterion(particle)) {
                timeInZone = (numRecruitZone == numCurrentZone)
                        ? timeInZone + dt
                        : 0;
                numRecruitZone = numCurrentZone;
                isNewRecruited = (timeInZone >= durationMinInRecruitArea);
                isRecruited[numCurrentZone] = isNewRecruited;
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

    private int getNumZone(IBasicParticle particle) {

        int nZone = -1;
        boolean foundZone = false;
        Iterator iter = ICFile.getInstance().getZones().iterator();
        while (!foundZone && iter.hasNext()) {
            Zone znTmp = (Zone) iter.next();
            if (znTmp.isXYInZone(particle.getX(), particle.getY())) {
                nZone = znTmp.getIndex();
                foundZone = true;
            }
        }
        return nZone;
    }
}
