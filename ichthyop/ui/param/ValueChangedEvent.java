package ichthyop.ui.param;

/** import AWT */
import java.awt.Component;

/**java.util */
import java.util.EventObject;

/**
 * An event which indicates that a value change occurred in a
 * {@link ichthyop.util.param.Parameter}. A value change is considered to occur
 * in a particular component if and only if the value effectively changed.
 * A value changed event type is enabled by adding a ValueListener to
 * the Parameter. The value change event does not provide any extra information
 * about the source: it only notifies a change occured.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.ValueListener
 */
public class ValueChangedEvent extends EventObject {

///////////////
// Constructors
///////////////

    /**
     * Constructs a ValueChangedEvent object with the specified source
     * component
     *
     * @param source the Component that originated the event
     */
    public ValueChangedEvent(Component source) {
        super(source);
    }

}
