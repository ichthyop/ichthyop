/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.event;

import fr.ird.ichthyop.manager.ReleaseManager;
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
