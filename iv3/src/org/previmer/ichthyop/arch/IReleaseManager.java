/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.event.ReleaseListener;

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
