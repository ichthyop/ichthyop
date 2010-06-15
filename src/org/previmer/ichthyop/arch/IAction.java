/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.arch;
import org.previmer.ichthyop.action.ActionPriority;

/**
 *
 * @author pverley
 */
public interface IAction {

    public void loadParameters() throws Exception;

    public void execute(IBasicParticle particle);

    public boolean isEnabled();

    public ActionPriority getPriority();
}
