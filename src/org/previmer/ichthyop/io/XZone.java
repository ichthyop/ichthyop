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
public class XZone extends org.jdom.Element {

    final public static String ZONE = "zone";
    final public static String TYPE_ZONE = "type";
    final public static String INDEX = "index";
    final public static String POLYGON = "polygon";
    final public static String COLOR = "color";
    final public static String KEY = "key";
    final public static String ENABLED = "enabled";
    private XBathyMask bathyMask;
    private XThickness thickness;

    public XZone(Element xzone) {
        super(ZONE);
        if (xzone != null) {
            addContent(xzone.cloneContent());
            bathyMask = new XBathyMask(getChild(XBathyMask.BATHY_MASK));
            thickness = new XThickness(getChild(XThickness.THICKNESS));
        }
    }

    public XZone(String key) {
        super(ZONE);
        setKey(key);
        setEnabled(true);
        setType(TypeZone.RELEASE);
        addPoint(0, 0.f, 0.f);
        addPoint(1, 0.f, 0.f);
        addPoint(2, 0.f, 0.f);
        addPoint(3, 0.f, 0.f);
        addBathyMask();
        addThickness();
    }

    private void addBathyMask() {
        bathyMask = new XBathyMask();
        addContent(bathyMask);
    }

    private void addThickness() {
        thickness = new XThickness();
        addContent(thickness);
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    public void setKey(String key) {
        if (null == getChild(KEY)) {
            addContent(new Element(KEY));
        }
        getChild(KEY).setText(key);
    }

    public boolean isEnabled() {

        if (null != getChild(ENABLED)) {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        } else {
            return true;
        }
    }

    public void setEnabled(boolean enabled) {
        if (null == getChild(ENABLED)) {
            addContent(new Element(ENABLED));
        }
        getChild(ENABLED).setText(String.valueOf(enabled));
    }

    public TypeZone getTypeZone() {

        for (TypeZone type : TypeZone.values()) {
            if (type.toString().matches(getChildTextNormalize(TYPE_ZONE))) {
                return type;
            }
        }
        return null;
    }

    public void setType(TypeZone type) {
        if (null == getChild(TYPE_ZONE)) {
            addContent(new Element(TYPE_ZONE));
        }
        getChild(TYPE_ZONE).setText(type.toString());
    }

    public XBathyMask getBathyMask() {
        return bathyMask;
    }

    public XThickness getThickness() {
        return thickness;
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

    public void setColor(Color color) {
        if (null == getChild(COLOR)) {
            addContent(new Element(COLOR));
        }
        String scolor = color.toString();
        scolor = scolor.substring(scolor.lastIndexOf("["));
        getChild(COLOR).setText(scolor);
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

    public void cleanupPolygon() {
        getChild(POLYGON).removeContent();
    }

    public void addPoint(int index, float lon, float lat) {
        if (null == getChild(POLYGON)) {
            addContent(new Element(POLYGON));
        }
        getChild(POLYGON).addContent(new XPoint(index, lon, lat));
    }

    public int getIndex() {
        return Integer.valueOf(getChildTextNormalize(INDEX));
    }

    public void setIndex(int index) {
        if (null == getChild(INDEX)) {
            addContent(new Element(INDEX));
        }
        getChild(INDEX).setText(String.valueOf(index));
    }

    public class XThickness extends org.jdom.Element {

        private static final String THICKNESS = "thickness";
        private static final String LOWER_DEPTH = "lower_depth";
        private static final String UPPER_DEPTH = "upper_depth";

        XThickness(Element xBathyMask) {
            super(THICKNESS);
            if (xBathyMask != null) {
                addContent(xBathyMask.cloneContent());
            }
        }

        XThickness() {
            super(THICKNESS);
            setEnabled(true);
            setUpperDepth(0);
            setLowerDepth(50);
        }

        public float getLowerDepth() {
            return Float.valueOf(getChildTextNormalize(LOWER_DEPTH));
        }

        public void setLowerDepth(float depth) {
            if (null == getChild(LOWER_DEPTH)) {
                addContent(new Element(LOWER_DEPTH));
            }
            getChild(LOWER_DEPTH).setText(String.valueOf(depth));
        }

        public float getUpperDepth() {
            return Float.valueOf(getChildTextNormalize(UPPER_DEPTH));
        }

        public void setUpperDepth(float depth) {
            if (null == getChild(UPPER_DEPTH)) {
                addContent(new Element(UPPER_DEPTH));
            }
            getChild(UPPER_DEPTH).setText(String.valueOf(depth));
        }

        public boolean isEnabled() {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        }

        public void setEnabled(boolean enabled) {
            if (null == getChild(ENABLED)) {
                addContent(new Element(ENABLED));
            }
            getChild(ENABLED).setText(String.valueOf(enabled));
        }
    }

    public class XBathyMask extends org.jdom.Element {

        private static final String BATHY_MASK = "bathy_mask";
        private static final String LINE_INSHORE = "line_inshore";
        private static final String LINE_OFFSHORE = "line_offshore";

        XBathyMask(Element xBathyMask) {
            super(BATHY_MASK);
            if (xBathyMask != null) {
                addContent(xBathyMask.cloneContent());
            }
        }

        XBathyMask() {
            super(BATHY_MASK);
            setEnabled(false);
            setInshoreLine(0);
            setOffshoreLine(12000);
        }

        public float getInshoreLine() {
            return Float.valueOf(getChildTextNormalize(LINE_INSHORE));
        }

        public void setInshoreLine(float depth) {
            if (null == getChild(LINE_INSHORE)) {
                addContent(new Element(LINE_INSHORE));
            }
            getChild(LINE_INSHORE).setText(String.valueOf(depth));
        }

        public float getOffshoreLine() {
            return Float.valueOf(getChildTextNormalize(LINE_OFFSHORE));
        }

        public void setOffshoreLine(float depth) {
            if (null == getChild(LINE_OFFSHORE)) {
                addContent(new Element(LINE_OFFSHORE));
            }
            getChild(LINE_OFFSHORE).setText(String.valueOf(depth));
        }

        public boolean isEnabled() {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        }

        public void setEnabled(boolean enabled) {
            if (null == getChild(ENABLED)) {
                addContent(new Element(ENABLED));
            }
            getChild(ENABLED).setText(String.valueOf(enabled));
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

        XPoint(int index, float lon, float lat) {
            super(POINT);
            setIndex(index);
            setLon(lon);
            setLat(lat);
        }

        public double getLon() {
            return Float.valueOf(getChildTextNormalize(LON));
        }

        public void setLon(float lon) {
            if (null == getChild(LON)) {
                addContent(new Element(LON));
            }
            getChild(LON).setText(String.valueOf(lon));
        }

        public double getLat() {
            return Float.valueOf(getChildTextNormalize(LAT));
        }

        public void setLat(float lat) {
            if (null == getChild(LAT)) {
                addContent(new Element(LAT));
            }
            getChild(LAT).setText(String.valueOf(lat));
        }

        public int getIndex() {
            return Integer.valueOf(getChildTextNormalize(INDEX));
        }

        public void setIndex(int index) {
            if (null == getChild(INDEX)) {
                addContent(new Element(INDEX));
            }
            getChild(INDEX).setText(String.valueOf(index));
        }

        public GridPoint createRhoPoint() {
            GridPoint rhoPoint = new GridPoint(false);
            rhoPoint.setLat(getLat());
            rhoPoint.setLon(getLon());
            return rhoPoint;
        }
    }
}
