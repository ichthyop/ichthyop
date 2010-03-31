/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XParameter extends org.jdom.Element {

    final public static String PARAMETER = "parameter";
    final public static String KEY = "key";
    final public static String LONGNAME = "long_name";
    final public static String VALUE = "value";
    final public static String TYPE = "type";
    final public static String HIDDEN = "hidden";
    final public static String DESCRIPTION = "description";
    final public static String FORMAT = "format";
    final public static String ACCEPTED = "accepted";
    final public static String DEFAULT = "default";
    final public static String KEY_HEADER = "Key";
    final public static String INDEX_HEADER = "Index";
    final public static String NAME_HEADER = "Name";
    final public static String VALUE_HEADER = "Value";
    final public static String TYPE_HEADER = "Type";
    final public static String HIDDEN_HEADER = "Hidden";

    private int index;
    private final ParamType param_type;
    private List<Element> values;
    private boolean hidden;
    private ParameterFormat param_format;

    XParameter(Element xparameter) {
        super(PARAMETER);
        param_type = getType(xparameter);
        hidden = isHidden(xparameter);
        param_format = getFormat(xparameter);
        if (xparameter != null) {
            if (getType().equals(ParamType.SERIAL)) {
                this.setAttribute(TYPE, getType().toString());
            }
            if (isHidden()) {
                this.setAttribute(HIDDEN, String.valueOf(true));
            }
            addContent(xparameter.cloneContent());
            values = getChildren(VALUE);
        }
        reset();
    }

    public static String[] getHeaders() {
        return new String[] {KEY_HEADER, INDEX_HEADER, NAME_HEADER, VALUE_HEADER, TYPE_HEADER, HIDDEN_HEADER};
    }

    public String[] toTableRow() {
        String[] row;
        String longName = getLongName();
        if (this.getLength() > 1) {
            longName += " [" + String.valueOf(index + 1) + "]";
        }
        row = new String[] {getKey(), String.valueOf(index), longName, getValue(), param_type.toString(), Boolean.toString(hidden)};
        //System.out.println(getKey() + " - " +  String.valueOf(index) + " - " + longName + " - " + getValue() + " - " + param_type.toString() + " - " + Boolean.toString(hidden));
        return row;
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    public String getLongName() {
        return getChildTextNormalize(LONGNAME);
    }

    public ParamType getType() {
        return param_type;
    }

    public boolean isHidden() {
        return hidden;
    }

    private boolean isHidden(Element element) {
        if (null != element && null != element.getAttribute(HIDDEN)) {
            return Boolean.valueOf(element.getAttribute(HIDDEN).getValue());
        } else {
            return false;
        }
    }

    public String getDescription() {
        if (null != getChild(DESCRIPTION)) {
            return getChildTextNormalize(DESCRIPTION);
        } else {
            return "No description available";
        }
    }

    private ParamType getType(Element element) {
        if (null != element && null != element.getAttribute(TYPE)) {
            return ParamType.getType(element.getAttribute(TYPE).getValue());
        } else {
            return ParamType.SINGLE;
        }
    }

    public ParameterFormat getFormat() {
        return param_format;
    }

    public String[] getAcceptedValues() {
        List<String> list = new ArrayList();
        if (getFormat().equals(ParameterFormat.COMBO)) {
            try {
                for (Object elt : getChildren(ACCEPTED)) {
                    list.add(((Element) elt).getTextNormalize());
                }
            } catch (NullPointerException e) {
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private ParameterFormat getFormat(Element element) {
        if (null != element && null != element.getChild(FORMAT)) {
            return ParameterFormat.getFormat(element.getChild(FORMAT).getValue());
        } else {
            return ParameterFormat.TEXT;
        }
    }

    @Override
    public String getValue() {
        return values.get(index).getTextNormalize();
    }

    public String getValues() {
        if (getType().equals(ParamType.SERIAL) && getLength() > 1) {
            StringBuffer strV = new StringBuffer();
            for (Element elt : values) {
                strV.append('"');
                strV.append(elt.getTextNormalize());
                strV.append("\" ");
            }
            return strV.toString().trim();
        } else {
            return getValue();
        }
    }

    public void setValue(String value) {
        setValue(value, 0);
    }

    public void addValue() {
        indexOf(values.get(getLength() - 1));
        addContent(indexOf(values.get(getLength() - 1)) + 1, new Element(VALUE).setText(getDefault()));
        values = getChildren(VALUE);
    }

    public String getDefault() {
        if (null != getChildTextNormalize(DEFAULT)) {
            return getChildTextNormalize(DEFAULT);
        } else {
            return getFormat().getDefault();
        }
    }

    public void setValue(String value, int index) {
        if (index >= values.size()) {
            addContent(new Element(VALUE));
            values = getChildren(VALUE);
        }
        values.get(index).setText(value);
    }

    public void removeValue(int index) {
        values.remove(index);
    }

    public boolean isSerial() {
        return getType().equals(ParamType.SERIAL);
    }

    public int getLength() {
        return getChildren(VALUE).size();
    }

    public boolean hasNext() {
        return index < (getLength() - 1);
    }

    public int index() {
        return index;
    }

    public void reset() {
        index = 0;
    }

    public void increment() {
        index++;
    }
}
