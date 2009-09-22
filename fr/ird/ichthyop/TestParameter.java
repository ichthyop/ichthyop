/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XParameter;
import java.io.File;
import java.util.Iterator;

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
        ActionPool actionPool = new ActionPool();
        Iterator<AbstractAction> it = actionPool.values().iterator();
        while (it.hasNext()) {
            AbstractAction action = it.next();
            System.out.println("====================");
            System.out.println(action.getClass().getCanonicalName() + " " + action.isActivated());
            for (XParameter param : action.getParameters()) {
                System.out.println("+++");
                System.out.println(param.getKey() + " " + param.getValue());
            }
        }


    }

    public static void main(String[] arg) {
        new TestParameter();
    }
}
