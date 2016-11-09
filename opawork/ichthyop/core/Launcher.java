/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.core;

import ichthyop.ui.MainFrame;
import ichthyop.util.Resources;
import java.awt.Toolkit;
import javax.swing.SwingUtilities;

/**
 *
 * @author pverley
 */
public class Launcher {

    public static void main(String[] args) throws Exception {

        /** prints application title in the console */
        System.out.println();
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();
        System.out.println(Resources.TITLE_LARGE);
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();

        if (args == null || args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Toolkit.getDefaultToolkit().setDynamicLayout(true);
                    new MainFrame().setVisible(true);
                }
            });

        } else {
            new RunBatch(args[0]);
        }
    }
}
