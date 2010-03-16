/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.event.ReleaseEvent;

/**
 *
 * @author pverley
 */
public interface IReleaseProcess {

    public void release(ReleaseEvent event);

    public int getNbParticles();

}
