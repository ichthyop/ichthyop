/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author pverley
 */
public class ICFile {

    private static ICFile icfile  = new ICFile();
    private static File file;
    private static ICStructure icstructure;


    public static void setFile(File sfile) {
        file = sfile;
        icfile.load();
    }

    public static ICFile getInstance() {
        return icfile;
    }

    public void load() {

        SAXBuilder sxb = new SAXBuilder();
        try {
            Element racine = sxb.build(file).getRootElement();
            racine.detach();
            icstructure = new ICStructure(racine);
            icstructure.createMaps();
        } catch (Exception e) {
            Logger.getLogger(ICFile.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public XParameter getParameter(String key) {
        return icstructure.getParameter(key);
    }
    
    public XAction getAction(String key) {
        return icstructure.getAction(key);
    }

    public ArrayList<Zone> getZones() {
        return null;
    }

    public class ICStructure extends org.jdom.Document {

        public final static String PARAMETERS = "parameters";
        public final static String ACTIONS = "actions";
        private HashMap<String, XParameter> mapParameters;
        private HashMap<String, XAction> mapActions;

        public ICStructure(Element root) {
            super(root);
        }

        private ICStructure(String root) {
            this(new Element(root));
        }

        private XParameter getParameter(String key) {
            return mapParameters.get(key);
        }

        private XAction getAction(String key) {
            return mapActions.get(key);
        }
        
        private Element get(String arg) {
            return getRootElement().getChild(arg);
        }

        private List<XParameter> readParameters() {

            List list = get(PARAMETERS).getChildren();
            ArrayList<XParameter> listvar = new ArrayList();
            Iterator<Element> it = list.iterator();
            int i = 0;
            while (it.hasNext()) {
                listvar.add(new XParameter(it.next()));
            }
            return listvar;
        }

        private List<XAction> readActions() {

            List list = get(ACTIONS).getChildren();
            ArrayList<XAction> listvar = new ArrayList();
            Iterator<Element> it = list.iterator();
            int i = 0;
            while (it.hasNext()) {
                listvar.add(new XAction(it.next()));
            }
            return listvar;
        }

        private void createMaps() {

            mapParameters = new HashMap();
            Iterator<XParameter> itP = readParameters().iterator();
            while (itP.hasNext()) {
                XParameter param = itP.next();
                mapParameters.put(param.getKey(), param);
            }

            mapActions = new HashMap();
            Iterator<XAction> itA = readActions().iterator();
            while (itA.hasNext()) {
                XAction action = itA.next();
                mapActions.put(action.getKey(), action);
            }
        }
    }

}
