/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.release;

import fr.ird.ichthyop.*;
import java.util.EventObject;

/**
 *
 * @author pverley
 */
public class ReleaseEvent extends EventObject {

    public ReleaseEvent(ReleaseSchedule source) {
        super(source);
    }

    @Override
    public ReleaseSchedule getSource() {
        return (ReleaseSchedule) source;
    }
}
