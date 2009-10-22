/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.event.ReleaseListener;

/**
 *
 * @author pverley
 */
public interface IReleaseManager extends ReleaseListener, NextStepListener {

    public int getNbParticles();

    public long getReleaseDuration();

    public int getNbReleaseEvents();

    public String getParameter(String releaseKey, String key);
}
