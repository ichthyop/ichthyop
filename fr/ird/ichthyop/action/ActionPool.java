/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.arch.IActionPool;
import fr.ird.ichthyop.io.XAction;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IBasicParticle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ActionPool extends HashMap<String, AbstractAction> implements IActionPool, ISimulationAccessor {

    final private static ActionPool actionPool = new ActionPool();

    public ActionPool() {
        Iterator<XAction> it = getSimulation().getActionManager().getActions().iterator();
        while (it.hasNext()) {
            XAction xaction = it.next();
            if (xaction.isEnabled()) {
                try {
                    put(xaction.getKey(), getSimulation().getActionManager().createAction(xaction.getActionClass()));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ActionPool.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static ActionPool getInstance() {
        return actionPool;
    }

    public void execute(String actionName, IBasicParticle particle) {
        AbstractAction action = get(actionName);
        if (action != null) {
            action.execute(particle);
        }
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }
}
