package ichthyop.ui.param;

/** import java.util */
import java.util.EventListener;

/**
 * The ValueListener interface is the primary method for handling value
 * changed events thrown by {@link ichthyop.util.param.Parameter}.
 * Users implement the ValueListener interface and register
 * their listener on an EventListenerList using the addValueListener method.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.ValueCHangedEvent
 * @see ichthyop.util.param.Parameter for an example of implementation of the
 * listener.
 */
public interface ValueListener extends EventListener {

    /**
     * This method is called whenever an value changed event occurs.
     *
     * @param e the ValueChangedEvent does not contain any information. It just
     * notifies the listener the value of the source has changed.
     */
    public void valueChanged(ValueChangedEvent e);
}
