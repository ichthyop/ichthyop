/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import java.util.List;
import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XParameter extends org.jdom.Element {

    final public static String PARAMETER = "parameter";
    final public static String KEY = "key";
    final public static String VALUE = "value";
    final public static String TYPE = "type";
    private int index;
    private List<Element> values;

    XParameter(Element xparameter) {
        super(PARAMETER);
        if (xparameter != null) {
            addContent(xparameter.cloneContent());
            values = getChildren(VALUE);
        }
        reset();
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    public ParamType getType() {
        if (null != getAttribute(TYPE)) {
            return ParamType.getType(getAttribute(TYPE).getValue());
        } else {
            return ParamType.SINGLE;
        }
    }

    @Override
    public String getValue() {
        return values.get(index).getTextNormalize();
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
