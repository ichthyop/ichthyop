/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.event;

import java.util.EventListener;

/**
 *
 * @author pverley
 */
public interface ReleaseListener extends EventListener {

    public void releaseTriggered(ReleaseEvent e) throws Exception;

}
