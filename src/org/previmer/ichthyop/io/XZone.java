/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.awt.Color;
import org.previmer.ichthyop.*;
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
    final public static String COLOR = "color";
    private XBathyMask bathyMask;

    public XZone(Element xzone) {
        super(BlockType.ZONE, xzone);
        if (xzone != null) {
            addContent(xzone.cloneContent());
            bathyMask = new XBathyMask(getChild(XBathyMask.BATHY_MASK));
        }
    }

    public TypeZone getTypeZone() {

        for (TypeZone type : TypeZone.values()) {
            if (type.toString().matches(getChildTextNormalize(TYPE_ZONE))) {
                return type;
            }
        }
        return null;
    }

    public XBathyMask getBathyMask() {
        return bathyMask;
    }

    public Color getColor() {
        if (null == getChild(COLOR)) {
            return Color.WHITE;
        }
        String strColor = this.getChildTextNormalize(COLOR);
        strColor = strColor.substring(1, strColor.length() - 1);
        String[] rgb = strColor.split(",");
        if (rgb.length != 3) {
            return Color.WHITE;
        }
        int red = Integer.valueOf(rgb[0].substring(rgb[0].indexOf("=") + 1));
        int green = Integer.valueOf(rgb[1].substring(rgb[1].indexOf("=") + 1));
        int blue = Integer.valueOf(rgb[2].substring(rgb[2].indexOf("=") + 1));
        return new Color(red, green, blue);

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
            return Integer.valueOf(getChildTextNormalize(LINE_INSHORE));
        }

        public int getOffshoreLine() {
            return Integer.valueOf(getChildTextNormalize(LINE_OFFSHORE));
        }

        public int getLowerDepth() {
            return Integer.valueOf(getChildTextNormalize(LOWER_DEPTH));
        }

        public int getUpperDepth() {
            return Integer.valueOf(getChildTextNormalize(UPPER_DEPTH));
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

        public GridPoint createRhoPoint() {
            GridPoint rhoPoint = new GridPoint(false);
            rhoPoint.setLat(getLat());
            rhoPoint.setLon(getLon());
            return rhoPoint;
        }
    }
}
