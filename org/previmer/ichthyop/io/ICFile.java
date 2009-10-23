/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.io.File;
import java.util.ArrayList;
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

    public XBlock getBlock(BlockType type, String key) {
        return icstructure.getBlock(type, key);
    }

    public List<XBlock> getBlocks(BlockType type) {
        return icstructure.getBlocks(type);
    }

    public class ICStructure extends org.jdom.Document {

        public ICStructure(Element root) {
            super(root);
        }

        private ICStructure(String root) {
            this(new Element(root));
        }

        private XBlock getBlock(final BlockType type, final String key) {
            List<XBlock> list = new ArrayList();
            for (XBlock block : getBlocks(type)) {
                if (block.getKey().matches(key)) {
                    list.add(block);
                }
            }
            if (list.size() > 0 && list.size() < 2) {
                return list.get(0);
            } else {
                return null;
            }
        }

        private List<XBlock> getBlocks(final BlockType type) {
            Filter filtre = new Filter() {

                public boolean matches(Object obj) {
                    if (!(obj instanceof Element)) {
                        return false;
                    }
                    Element element = (Element) obj;
                    if (element.getAttributeValue(XBlock.TYPE).matches(type.toString())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            List<XBlock> list = new ArrayList();
            for (Object elt : getRootElement().getContent(filtre)) {
                list.add(new XBlock(type, (Element) elt));
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
