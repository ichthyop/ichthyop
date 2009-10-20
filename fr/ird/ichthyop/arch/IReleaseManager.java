/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.event.ReleaseListener;
import fr.ird.ichthyop.io.XBlock;

/**
 *
 * @author pverley
 */
public interface IReleaseManager extends ReleaseListener, NextStepListener {
    
    public XBlock getXReleaseProcess(String key);

    //public ReleaseSchedule getSchedule();

    public String getParameter(String key);

    public int getNbParticles();

    public long getReleaseDuration();

    public int getNbReleaseEvents();

    public void setUp();
}
