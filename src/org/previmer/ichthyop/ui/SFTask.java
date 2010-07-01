/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public abstract class SFTask<T, V> extends Task<T, V> {

    SFTask(Application instance) {
        super(instance);
    }

    abstract void onSuccess(T result);

    abstract void onFailure(Throwable throwable);

    @Override
    protected void succeeded(T result) {
        firePropertyChange("succeeded", null, null);
        onSuccess(result);
    }

    @Override
    protected void failed(Throwable throwable) {
        firePropertyChange("failed", null, null);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        SimulationManager.getLogger().log(Level.SEVERE, throwable.getMessage(), throwable);
        super.setMessage(throwable.toString());
        firePropertyChange("reset", null, null);
        onFailure(throwable);
    }

    @Override
    public void setMessage(String message) {
        setMessage(message, false, Level.INFO);
    }

    public void setMessage(String message, boolean persistent, Level level) {
        super.setMessage(message);
        SimulationManager.getLogger().log(level, message);
        if (persistent) {
            firePropertyChange("persistent", null, null);
        }
    }
}
