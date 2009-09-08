package ichthyop.util;

/** import Swing */
import javax.swing.JOptionPane;

/** import java.util */
import java.util.concurrent.ExecutionException;
import org.jdesktop.swingworker.SwingWorker;

/**
 * <p>This SwingWorker handles exceptions thrown by
 * the <code>doInBackground()</code> method. It divides the <code>done()</code>
 * method in two submethods <code>onSuccess()</code> and
 * <code>onFailure()</code>.</p>
 *
 * @see the <a href="http://java.sun.com/javase/6/docs/api/javax/swing/SwingWorker.html">
 * SwingWorker</a> javadoc for more details.
 *
 * @author P.Verley
 */

public abstract class SafeSwingWorker<T, V> extends SwingWorker<T, V> {

////////////////////////////
// Definition of the methods
////////////////////////////

    /**The done() method is declared as final so it is not reused by inheritance.
     * The programmer would rather define the onSucces() and onFailure() methods.
     */
    @Override
            protected final void done() {

        try {
            onSuccess(get());
        } catch (InterruptedException e) {
            onFailure(e);
        } catch (ExecutionException e) {
            onFailure(e.getCause());
        }
    }

    /**
     * This method prints an error message in the console and shows an error
     * dialog box giving details about the exception.
     *
     * @param t a Throwable, the exception thrown.
     * @param errTitle a String, the title of the error dialog box.
     */
    public void printErr(Throwable t, String errTitle) {

        t.printStackTrace(); // to be deleted

        /** Print first element of StackTrace in the Java console */
        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(t.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());

        /** Shows error message dialog box */
        JOptionPane.showMessageDialog(null,
                                      errTitle + "\n" +
                                      t.getClass().getSimpleName() + " : " +
                                      t.getMessage(),
                                      "Error " + t.getClass().getSimpleName(),
                                      JOptionPane.ERROR_MESSAGE);
    }

//////////////////////////////////////
// Declaration of the abstract methods
//////////////////////////////////////

    /**
     * This method is called within the Event Dispatch Thread when the
     * doInBackGround() method successfully complete.
     *
     * @param The computed value returned by the doInBackGround() method.
     */
    protected abstract void onSuccess(T result);

    /**
     * This method is called within the EDT when the doInBackground() method
     * throws an exception.
     *
     * @param Exception thrown by doInBackground()
     */
    protected abstract void onFailure(Throwable t);

    //---------- End of class

}
