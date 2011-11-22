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

    public void loadParameters() throws Exception;

    public int release(ReleaseEvent event) throws Exception;

    public int getNbParticles();

}
