package ichthyop.ui;

/** import AWT */
import java.awt.Graphics;
import java.awt.Image;

/** import Swing */
import javax.swing.JPanel;

/**
 * This class is a panel that just displays images of the simulation and
 * resizes them when necessary.
 * It is used to replay the simulation at the end of a run.
 * The MainFrame records some snapshots of the simulation and feeds
 * the ReplayPanel at the end of the run with these images.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class ReplayPanel extends JPanel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The image to be displayed.
     */
    private Image image;
    /**
     * Current heigth of the component.
     */
    private int hi;
    /**
     * Current width of the component.
     */
    private int wi;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new replay panel.
     */
    public ReplayPanel() {

        hi = this.getHeight();
        wi = this.getWidth();
    }

///////////////////////////
// Definition of the method
///////////////////////////

    /**
     * Draws the <code>image</code> in the <code>Graphics</code> of the
     * component.
     *
     * @param g the Graphics of the component.
     */
    public void paintComponent(Graphics g) {

        if (image != null) {
            if (hi != this.getHeight() | wi != this.getWidth()) {
                image = image.getScaledInstance(this.getWidth(), this.getHeight(),
                                                Image.SCALE_FAST);
            }
            g.drawImage(image, 0, 0, null);
        }
    }

    /**
     * Sets the image to display
     */
    public void setImage(Image image) {
        this.image = image;
    }

    //---------- End of class
}
