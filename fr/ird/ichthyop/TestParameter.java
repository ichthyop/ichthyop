/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XParameter;
import fr.ird.ichthyop.manager.ActionManager;
import fr.ird.ichthyop.manager.ParameterManager;
import fr.ird.ichthyop.manager.SimulationManager;
import fr.ird.ichthyop.manager.ZoneManager;
import java.io.File;
import java.util.Iterator;

/**
 *
 * @author pverley
 */
public class TestParameter {

    File file;

    public TestParameter() {

        String filename = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        file = new File(filename);

        ICFile.setFile(file);
        /*ActionManager actionPool = ActionManager.getInstance();
        Iterator<AbstractAction> itA = actionPool.values().iterator();
        while (itA.hasNext()) {
            AbstractAction action = itA.next();
            System.out.println("====================");
            System.out.println(action.getClass().getCanonicalName() + " " + action.isEnabled());
            for (XParameter param : action.getParameters()) {
                System.out.println("+++");
                System.out.println(param.getKey() + " " + param.getValue());
            }
        }*/
        System.out.println("-------------------------------------");
        
        System.out.println(ParameterManager.getInstance().getValue("app.time", "time_step"));

        System.out.println("-------------------------------------");

        /*Iterator<Zone> itZ = ZoneManager.getInstance().getZones(TypeZone.RELEASE).iterator();
        while(itZ.hasNext()) {
            Zone zone = itZ.next();
            System.out.println("====================");
            System.out.println(zone.toString());
        }*/


        System.out.println(SimulationManager.getInstance().getNumberOfSimulations());

    }

    public static void main(String[] arg) {
        new TestParameter();
    }
}
