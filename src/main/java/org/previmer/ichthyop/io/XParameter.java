/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.io;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

/**
 *
 * @author pverley
 */
public class XParameter extends org.jdom2.Element {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
    final public static String TEMPLATE = "template";
    private int index;
    private final ParamType param_type;
    private List<Element> values;
    private final boolean hidden;
    private final ParameterFormat param_format;

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
            return null;
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
        List<String> list = new ArrayList<>();
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
        return getValue(index);
    }

    public String getValue(int index) {
        return values.get(index).getTextNormalize();
    }

    public String getValues() {
        if (getType().equals(ParamType.SERIAL) && getLength() > 1) {
            StringBuilder strV = new StringBuilder();
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

    public String getTemplate() {
        if (null != getChildTextNormalize(TEMPLATE)) {
            return getChildTextNormalize(TEMPLATE);
        } else {
            return null;
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
