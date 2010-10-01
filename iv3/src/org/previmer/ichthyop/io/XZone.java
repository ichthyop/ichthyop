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
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

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
    private static final String THICKNESS = "thickness";
    private static final String LOWER_DEPTH = "lower_depth";
    private static final String UPPER_DEPTH = "upper_depth";
    private static final String BATHY_MASK = "bathy_mask";
    private static final String LINE_INSHORE = "line_inshore";
    private static final String LINE_OFFSHORE = "line_offshore";

    public XZone(Element xzone) {
        super(ZONE);
        if (xzone != null) {
            addContent(xzone.cloneContent());
        }
    }

    public XZone(String key) {
        super(ZONE);
        setKey(key);
        setEnabled(true);
        setType(TypeZone.RELEASE);
        addPoint(0, "0.0", "0.0");
        addPoint(1, "0.0", "0.0");
        addPoint(2, "0.0", "0.0");
        addPoint(3, "0.0", "0.0");
        addBathyMask();
        addThickness();
    }

    private void addBathyMask() {
        if (null == getChild(BATHY_MASK)) {
            addContent(new Element(BATHY_MASK));
        }
        setBathyMaskEnabled(true);
        setInshoreLine(0);
        setOffshoreLine(12000);
    }

    private void addThickness() {
        if (null == getChild(THICKNESS)) {
            addContent(new Element(THICKNESS));
        }
        setThicknessEnabled(true);
        setUpperDepth(0.f);
        setLowerDepth(50.f);
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

    public Element getBathyMask() {
        return getChild(BATHY_MASK);
    }

    public Element getThickness() {
        return getChild(THICKNESS);
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

    public void addPoint(int index, String lon, String lat) {
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

    /*
     * Thickness 
     */
    public float getLowerDepth() {
        return Float.valueOf(getThickness().getChildTextNormalize(LOWER_DEPTH));
    }

    public void setLowerDepth(float depth) {
        if (null == getThickness().getChild(LOWER_DEPTH)) {
            getThickness().addContent(new Element(LOWER_DEPTH));
        }
        getThickness().getChild(LOWER_DEPTH).setText(String.valueOf(depth));
    }

    public float getUpperDepth() {
        return Float.valueOf(getThickness().getChildTextNormalize(UPPER_DEPTH));
    }

    public void setUpperDepth(float depth) {
        if (null == getThickness().getChild(UPPER_DEPTH)) {
            getThickness().addContent(new Element(UPPER_DEPTH));
        }
        getThickness().getChild(UPPER_DEPTH).setText(String.valueOf(depth));
    }

    public boolean isThicknessEnabled() {
        return Boolean.valueOf(getThickness().getChildTextNormalize(ENABLED));
    }

    public void setThicknessEnabled(boolean enabled) {
        if (null == getThickness().getChild(ENABLED)) {
            getThickness().addContent(new Element(ENABLED));
        }
        getThickness().getChild(ENABLED).setText(String.valueOf(enabled));
    }

    /*
     * BathyMask
     */
    public float getInshoreLine() {
        return Float.valueOf(getBathyMask().getChildTextNormalize(LINE_INSHORE));
    }

    public void setInshoreLine(float depth) {
        if (null == getBathyMask().getChild(LINE_INSHORE)) {
            getBathyMask().addContent(new Element(LINE_INSHORE));
        }
        getBathyMask().getChild(LINE_INSHORE).setText(String.valueOf(depth));
    }

    public float getOffshoreLine() {
        return Float.valueOf(getBathyMask().getChildTextNormalize(LINE_OFFSHORE));
    }

    public void setOffshoreLine(float depth) {
        if (null == getBathyMask().getChild(LINE_OFFSHORE)) {
            getBathyMask().addContent(new Element(LINE_OFFSHORE));
        }
        getBathyMask().getChild(LINE_OFFSHORE).setText(String.valueOf(depth));
    }

    public boolean isBathyMaskEnabled() {
        return Boolean.valueOf(getBathyMask().getChildTextNormalize(ENABLED));
    }

    public void setBathyMaskEnabled(boolean enabled) {
        if (null == getBathyMask().getChild(ENABLED)) {
            getBathyMask().addContent(new Element(ENABLED));
        }
        getBathyMask().getChild(ENABLED).setText(String.valueOf(enabled));
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

        XPoint(int index, String lon, String lat) {
            super(POINT);
            setIndex(index);
            setLon(lon);
            setLat(lat);
        }

        public String getLon() {
            return getChildTextNormalize(LON);
        }

        public void setLon(String lon) {
            if (null == getChild(LON)) {
                addContent(new Element(LON));
            }
            getChild(LON).setText(lon);
        }

        public String getLat() {
            return getChildTextNormalize(LAT);
        }

        public void setLat(String lat) {
            if (null == getChild(LAT)) {
                addContent(new Element(LAT));
            }
            getChild(LAT).setText(lat);
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
            double lat = Double.valueOf(LonLatConverter.convert(getLat(), LonLatFormat.DecimalDeg));
            double lon = Double.valueOf(LonLatConverter.convert(getLon(), LonLatFormat.DecimalDeg));
            rhoPoint.setLat(lat);
            rhoPoint.setLon(lon);
            return rhoPoint;
        }
    }
}
