/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

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
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StringBuffer message = new StringBuffer(throwable.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(throwable.getMessage());
        Logger.getLogger(SFTask.class.getName()).log(Level.SEVERE, message.toString());
        onFailure(throwable);
    }

    @Override
    public void setMessage(String message) {
        setMessage(message, false);
    }

    public void setMessage(String message, boolean persistent) {
        super.setMessage(message);
        if (!persistent) {
            firePropertyChange("reset", null, null);
        }
    }
}
