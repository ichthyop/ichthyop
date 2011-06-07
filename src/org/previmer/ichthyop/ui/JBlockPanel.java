/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import javax.swing.JPanel;
import org.previmer.ichthyop.io.XBlock;

/**
 *
 * @author pverley
 */
public class JBlockPanel extends JPanel {

    private XBlock block;

    public XBlock getBlock() {
        return block;
    }

    public void setBlock(XBlock block) {
        this.block = block;
    }
}
