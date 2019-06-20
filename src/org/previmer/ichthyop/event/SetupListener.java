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
public interface SetupListener extends EventListener {

    public void setupPerformed(SetupEvent e) throws Exception;

}
