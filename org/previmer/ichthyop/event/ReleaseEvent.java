/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.event;

import org.previmer.ichthyop.manager.ReleaseManager;
import java.util.EventObject;

/**
 *
 * @author pverley
 */
public class ReleaseEvent extends EventObject {

    public ReleaseEvent(ReleaseManager source) {
        super(source);
    }

    @Override
    public ReleaseManager getSource() {
        return (ReleaseManager) source;
    }
}
