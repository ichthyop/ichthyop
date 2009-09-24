/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import fr.ird.ichthyop.*;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XZone extends XBlock {

    final public static String TYPE_ZONE = "type";
    final public static String INDEX = "index";
    final public static String POLYGON = "polygon";
    private XBathyMask bathyMask;

    public XZone(Element xzone) {
        super(TypeBlock.ZONE, xzone);
        if (xzone != null) {
            addContent(xzone.cloneContent());
            bathyMask = new XBathyMask(getChild(XBathyMask.BATHY_MASK));
        }
    }

    public TypeZone getTypeZone() {

        for (TypeZone type : TypeZone.values()) {
            if (type.name().matches(getChildTextNormalize(TYPE_ZONE))) {
                return type;
            }
        }
        return null;
    }

    public XBathyMask getBathyMask() {
        return bathyMask;
    }

    public ArrayList<XPoint> getPolygon() {

        List list = getChild(POLYGON).getChildren(XPoint.POINT);
        ArrayList<XPoint> polygon = new ArrayList(list.size());
        for (Object elt : list) {
            XPoint point = new XPoint((Element) elt);
            polygon.add(point.getIndex(), point);
        }
        return polygon;
    }

    public int getIndex() {
        return Integer.valueOf(getChildTextNormalize(INDEX));
    }

    public class XBathyMask extends org.jdom.Element {

        private static final String BATHY_MASK = "bathy_mask";
        private static final String LINE_INSHORE = "line_inshore";
        private static final String LINE_OFFSHORE = "line_offshore";
        private static final String LOWER_DEPTH = "lower_depth";
        private static final String UPPER_DEPTH = "upper_depth";

        XBathyMask(Element xBathyMask) {
            super(BATHY_MASK);
            if (xBathyMask != null) {
                addContent(xBathyMask.cloneContent());
            }
        }

        public int getInshoreLine() {
            return Integer.valueOf(LINE_INSHORE);
        }

        public int getOffshoreLine() {
            return Integer.valueOf(LINE_OFFSHORE);
        }

        public int getLowerDepth() {
            return Integer.valueOf(LOWER_DEPTH);
        }

        public int getUpperDepth() {
            return Integer.valueOf(UPPER_DEPTH);
        }
    }

    public class XPoint extends org.jdom.Element {

        private static final String POINT = "point";
        private static final String LON = "lon";
        private static final String LAT = "lat";
        final public static String INDEX = "index";

        XPoint(Element xpoint) {
            super(POINT);
            if (xpoint != null) {
                addContent(xpoint.cloneContent());
            }
        }

        public double getLon() {
            return Float.valueOf(getChildTextNormalize(LON));
        }

        public double getLat() {
            return Float.valueOf(getChildTextNormalize(LAT));
        }

        public int getIndex() {
            return Integer.valueOf(getChildTextNormalize(INDEX));
        }

        public RhoPoint createRhoPoint() {
            RhoPoint rhoPoint = new RhoPoint(false);
            rhoPoint.setLat(getLat());
            rhoPoint.setLon(getLon());
            return rhoPoint;
        }
    }
}
