/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author pverley
 */
public class ICFile {

    private static ICFile icfile = new ICFile();
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
            //icstructure.createMaps();
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

    public Collection<XAction> getActions() {
        return icstructure.getActions();
    }

    public Collection<XZone> getZones() {
        return icstructure.getZones();
    }

    public class ICStructure extends org.jdom.Document {

        public final static String PARAMETERS = "parameters";
        public final static String ACTIONS = "actions";
        public final static String ZONES = "zones";
        public final static String RELEASE_PROCESSES = "release_processes";
        //private HashMap<String, XParameter> mapParameters;
        //private HashMap<String, XAction> mapActions;
        //private HashMap<String, XZone> mapZones;

        public ICStructure(Element root) {
            super(root);
        }

        private ICStructure(String root) {
            this(new Element(root));
        }

        private XParameter getParameter(final String key) {
            Filter filtre = new Filter() {

                public boolean matches(Object obj) {
                    if (!(obj instanceof Element)) {
                        return false;
                    }
                    Element element = (Element) obj;
                    if (element.getChildTextNormalize(XParameter.KEY).matches(key)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            List searchResult = getRootElement().getChild(PARAMETERS).getContent(filtre);
            if (searchResult != null && searchResult.size() < 2) {
                return new XParameter((Element) searchResult.get(0));
            } else {
                return null;
            }
        }

        private XAction getAction(final String key) {
            Filter filtre = new Filter() {

                public boolean matches(Object obj) {
                    if (!(obj instanceof Element)) {
                        return false;
                    }
                    Element element = (Element) obj;
                    if (element.getChildTextNormalize(XParameter.KEY).matches(key)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            List searchResult = getRootElement().getChild(PARAMETERS).getContent(filtre);
            if (searchResult != null && searchResult.size() < 2) {
                return new XAction((Element) searchResult.get(0));
            } else {
                return null;
            }
        }

        private ArrayList<XAction> getActions() {
            ArrayList<XAction> list = new ArrayList();
            try {
                for (Object elt : getRootElement().getChild(ACTIONS).getChildren(XParameter.PARAMETER)) {
                    list.add(new XAction((Element) elt));
                }
            } catch (java.lang.NullPointerException ex) {
            }
            return list;
        }

        private Collection<XZone> getZones() {
            ArrayList<XZone> list = new ArrayList();
            try {
                for (Object elt : getRootElement().getChild(ZONES).getChildren(XZone.ZONE)) {
                    list.add(new XZone((Element) elt));
                }
            } catch (java.lang.NullPointerException ex) {
            }
            return list;
        }

        private Element get(String arg) {
            return getRootElement().getChild(arg);
        }

        /*private List<XParameter> readParameters() {

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

        private List<XZone> readZones() {

            List list = get(ZONES).getChildren();
            ArrayList<XZone> listvar = new ArrayList();
            Iterator<Element> it = list.iterator();
            int i = 0;
            while (it.hasNext()) {
                listvar.add(new XZone(it.next()));
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

            mapZones = new HashMap();
            Iterator<XZone> itZ = readZones().iterator();
            while (itZ.hasNext()) {
                XZone zone = itZ.next();
                mapZones.put(zone.getKey(), zone);
            }
        }*/
    }
}
