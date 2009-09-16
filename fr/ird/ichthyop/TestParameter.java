/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import java.io.File;

/**
 *
 * @author pverley
 */
public class TestParameter {
    
    File file;

    public TestParameter() {

        String filename = System.getProperty("user.dir") + File.separator + "cfg1.xic";
        file = new File(filename);

        ICFile.setFile(file);
        AbstractAction action = new Advection();
        System.out.println(action.isActivated());
        //action.loadParameters();
        
    }
    
    public static void main(String[] arg) {
        new TestParameter();
    }

}
