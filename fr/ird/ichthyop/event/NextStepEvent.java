/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.event;

import fr.ird.ichthyop.arch.IStep;
import java.util.EventObject;

/**
 *
 * @author pverley
 */
public class NextStepEvent extends EventObject {

    public NextStepEvent(IStep source) {
        super(source);
    }

    @Override
    public IStep getSource() {
        return (IStep) source;
    }

}
