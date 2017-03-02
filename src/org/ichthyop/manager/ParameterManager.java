/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.manager;

import java.io.BufferedWriter;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.io.BlockType;
import org.ichthyop.io.XBlock;
import org.ichthyop.io.XParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.ichthyop.Version;
import org.ichthyop.io.ConfigurationFile;
import org.ichthyop.util.Separator;

/**
 *
 * @author pverley
 */
public class ParameterManager extends AbstractManager {

    private static final ParameterManager MANAGER = new ParameterManager();
    private ConfigurationFile cfgFile;
    private HashMap<String, String> parameters;
    private String inputPathname;

    public static ParameterManager getInstance() {
        return MANAGER;
    }

    public void setConfigurationFile(File file) throws Exception {
        cfgFile = new ConfigurationFile(file);
        cfgFile.load();
        parameters = toProperties(cfgFile, false);
        inputPathname = file.getParent();
    }

    public ConfigurationFile getConfigurationFile() {
        return cfgFile;
    }

    public String getConfigurationDescription() {
        return cfgFile.getDescription();
    }

    public void setConfigurationDescription(String description) {
        cfgFile.setDescription(description);
    }

    public Version getConfigurationVersion() {
        return cfgFile.getVersion();
    }

    public void setConfigurationVersion(Version version) {
        cfgFile.setVersion(version);
    }

    public String getConfigurationTitle() {
        return cfgFile.getLongName();
    }

    public void setConfigurationTitle(String longName) {
        cfgFile.setLongName(longName);
    }

    public List<XParameter> getParameters() {
        return cfgFile.getParameters();
    }

    private String getParameter(String blockKey, String key) {
        return getParameter(BlockType.OPTION, blockKey, key);
    }

    public String[] getListParameter(BlockType blockType, String blockKey, String key) {
        String[] tokens = getParameter(blockType, blockKey, key).split("\"");
        List<String> list = new ArrayList();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                list.add(token.trim());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private String getParameter(BlockType blockType, String blockKey, String key) {

        XParameter xparam = cfgFile.getBlock(blockType, blockKey).getXParameter(key);
        if (xparam != null) {
            return xparam.getValue();
        } else {
            throw new NullPointerException("Could not retrieve parameter " + blockKey + "/" + key);
        }
    }

    private boolean isBlockEnabled(BlockType type, String key) {
        return cfgFile.getBlock(type, key).isEnabled();
    }

    public Iterable<XBlock> getBlocks(BlockType type) {
        return cfgFile.getBlocks(type);
    }

    public Collection<XBlock> readBlocks() throws IOException {
        return cfgFile.readBlocks();
    }

    public void cleanup() {
        cfgFile.removeAllBlocks();
    }

    public void save() throws IOException, FileNotFoundException {
        cfgFile.write(new FileOutputStream(cfgFile.getFile()));
    }

    public void addBlock(XBlock block) {
        cfgFile.addBlock(block);
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
//        toCSV(toProperties(cfgFile, true), "=");
//        toJson(toProperties(cfgFile, true));
//        for (Entry<String, String> parameter : toProperties(cfgFile, false).entrySet()) {
//            System.out.println(parameter);
//        }
    }

    @Override
    public void initializePerformed(InitializeEvent e) {
        // does nothing
    }

    /**
     * Check whether parameter 'key' has 'null' value. The function returns
     * {@code true} in several cases: the parameter does not exist, the value of
     * the parameter is empty or the value of the parameter is set to "null".
     *
     * @param key, the key of the parameter
     * @return {@code true} if the parameter is either null, empty or does not
     * exist
     */
    public boolean isNull(String key) {
        String param = parameters.get(key.toLowerCase());
        return (null == param)
                || param.isEmpty()
                || param.equalsIgnoreCase("null");
    }

    /**
     * Check whether the parameter exists, no matter what the value is.
     *
     * @param key, the key of the parameter
     * @return {@code true} if the parameter exists.
     */
    public final boolean canFind(String key) {
        return parameters.containsKey(key.toLowerCase());
    }

    /**
     * Find all the parameters whose key matches the filter given as argument.
     * The filter accepts the following meta-character: "?" for any single
     * character and "*" for any String.
     *
     * @see fr.ird.osmose.util.Properties#getKeys(java.lang.String) for details
     * about how the filter works.
     * @param filter
     * @return
     */
    public List<String> findKeys(String filter) {

        // Add \Q \E around substrings of fileMask that are not meta-characters
        String regexpPattern = filter.replaceAll("[^\\*\\?]+", "\\\\Q$0\\\\E");
        // Replace all "*" by the corresponding java regex meta-characters
        regexpPattern = regexpPattern.replaceAll("\\*", ".*");
        // Replace all "?" by the corresponding java regex meta-characters
        regexpPattern = regexpPattern.replaceAll("\\?", ".");

        // List the keys and select the ones that match the filter
        List<String> filteredKeys = new ArrayList();
        for (String key : parameters.keySet()) {
            if (key.matches(regexpPattern)) {
                filteredKeys.add(key);
            }
        }
        return filteredKeys;
    }

    /**
     * Returns the value of the specified parameter as a {@code String}
     *
     * @param key, the key of the parameter
     * @throws NullPointerException if the parameter is not found.
     * @return the value of the parameter as a {@code String}
     */
    public String getString(String key) {
        String lkey = key.toLowerCase();
        if (parameters.containsKey(lkey)) {
            return parameters.get(lkey).trim().replaceAll("^\"|\"$", "");
        } else {
            error("Could not find parameter " + key, new NullPointerException("Parameter " + key + " not found "));
        }
        return null;
    }

    /**
     * Returns the specified parameter as a path resolved again the main
     * configuration file.
     *
     * @see #resolveFile(java.lang.String)
     * @param key, the key of the parameter
     * @return, the parameter as a path resolved again the main configuration
     * file.
     */
    public String getFile(String key) {
        return resolve(getString(key), inputPathname);
    }

    /**
     * Returns the specified parameter as an array of strings, {@code String[]}.
     *
     * @param key, the key of the parameter
     * @return the parameter as a {@code String[]}
     */
    public String[] getArrayString(String key) {
        String value = getString(key);
        value = value.replaceAll("^\\[|\\]$", "");
        String[] values = value.split(Separator.guess(value, Separator.SEMICOLON).toString());
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim().replaceAll("^\"|\"$", "");
        }
        return values;
    }

    /**
     * Returns the specified parameter as an integer.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the value of the parameter cannot be
     * parsed as an integer.
     * @return the parameter as an integer
     */
    public int getInt(String key) {
        String s = getString(key);
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            error("Could not convert to Integer parameter " + getString(key), ex);
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Returns the specified parameter as a float.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the value of the parameter cannot be
     * parsed as a float.
     * @return the parameter as a float
     */
    public float getFloat(String key) {
        String s = getString(key);
        try {
            return Float.valueOf(s);
        } catch (NumberFormatException ex) {
            error("Could not convert to Float parameter " + getString(key), ex);
        }
        return Float.NaN;
    }

    /**
     * Returns the specified parameter as a double.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the value of the parameter cannot be
     * parsed as a double.
     * @return the parameter as a double
     */
    public double getDouble(String key) {
        String s = getString(key);
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException ex) {
            error("Could not convert to Double parameter " + getString(key), ex);
        }
        return Double.NaN;
    }

    /**
     * Returns the specified parameter as a boolean.
     *
     * @param key, the key of the parameter
     * @param warning, send a warning if the key cannot be found
     * @throws NumberFormatException if the value of the parameter cannot be
     * parsed as a boolean.
     * @return the parameter as a boolean
     */
    public boolean getBoolean(String key, boolean warning) {
        if (canFind(key)) {
            try {
                return Boolean.valueOf(getString(key));
            } catch (NumberFormatException ex) {
                error("Could not convert to Boolean parameter " + getString(key), ex);
            }
        } else if (warning) {
            warning("Could not find Boolean parameter " + key + ". Osmose assumes it is false.");
        }

        return false;
    }

    /**
     * Returns the specified parameter as a boolean.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the value of the parameter cannot be
     * parsed as a boolean.
     * @return the parameter as a boolean
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    /**
     * Returns the specified parameter as an array of integers, {@code int[]}.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the values of the parameter cannot be
     * parsed as an integer.
     * @return the parameter as a {@code int[]}
     */
    public int[] getArrayInt(String key) {
        String[] as = getArrayString(key);
        try {
            int[] ai = new int[as.length];
            for (int i = 0; i < ai.length; i++) {
                ai[i] = Integer.valueOf(as[i]);
            }
            return ai;
        } catch (NumberFormatException ex) {
            error("Could not convert to array of Integer parameter " + getString(key), ex);
        }
        return null;
    }

    /**
     * Returns the specified parameter as an array of floats, {@code float[]}.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the values of the parameter cannot be
     * parsed as a float.
     * @return the parameter as a {@code float[]}
     */
    public float[] getArrayFloat(String key) {
        String[] as = getArrayString(key);
        try {
            float[] af = new float[as.length];
            for (int i = 0; i < af.length; i++) {
                af[i] = Float.valueOf(as[i]);
            }
            return af;
        } catch (NumberFormatException ex) {
            error("Could not convert to array of Float parameter " + getString(key), ex);
        }
        return null;
    }

    /**
     * Returns the specified parameter as an array of doubles, {@code double[]}.
     *
     * @param key, the key of the parameter
     * @throws NumberFormatException if the values of the parameter cannot be
     * parsed as a double.
     * @return the parameter as a {@code double[]}
     */
    public double[] getArrayDouble(String key) {
        String[] as = getArrayString(key);
        try {
            double[] ad = new double[as.length];
            for (int i = 0; i < ad.length; i++) {
                ad[i] = Double.valueOf(as[i]);
            }
            return ad;
        } catch (NumberFormatException ex) {
            error("Could not convert to array of Double parameter " + getString(key), ex);
        }
        return null;
    }

    /**
     * Resolves a file path against the the input path. If filename is a
     * directory the function ensures the path ends with a separator.
     *
     * @param filename, the file path to resolve
     * @param relativeTo, the path against the file must be resolved
     * @return the resolved file path
     */
    private String resolve(String filename, String relativeTo) {
        String pathname = filename;
        try {
            File file = new File(relativeTo);
            pathname = new File(file.toURI().resolve(filename)).getCanonicalPath();
        } catch (Exception ex) {
            // do nothing, just return the argument
        }
        if (new File(pathname).isDirectory() && !pathname.endsWith(File.separator)) {
            pathname += File.separator;
        }
        return pathname;
    }

    public HashMap<String, String> toProperties(ConfigurationFile cfg, boolean extended) throws IOException {
        HashMap<String, String> map = new LinkedHashMap();

        map.put("configuration.longname", cfgFile.getLongName());
        map.put("configuration.description", clean(cfgFile.getDescription()));
        map.put("ichthyop.version", cfgFile.getVersion().toString());
        map.put("configuration.blocks", listBlocks(cfgFile));
        for (XBlock block : cfg.readBlocks()) {
            if (block.getType() != BlockType.OPTION) {
                map.put(block.getKey() + ".enabled", String.valueOf(block.isEnabled()));
                map.put(block.getKey() + ".type", block.getType().toString());
            }
            if (extended) {
                map.put(block.getKey() + ".description", clean(block.getDescription()));
                map.put(block.getKey() + ".treepath", block.getTreePath());
            }
            block.getXParameters().forEach((parameter) -> {
                StringBuilder key;
                if (extended) {
                    key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey()).append(".long_name");
                    map.put(key.toString(), parameter.getLongName());
                    key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey()).append(".format");
                    map.put(key.toString(), nullify(parameter.getFormat().toString()));
                    key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey()).append(".description");
                    map.put(key.toString(), clean(nullify(parameter.getDescription())));
                    key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey()).append(".default");
                    map.put(key.toString(), handleArray(nullify(parameter.getDefault())));
                    key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey()).append(".accepted");
                    map.put(key.toString(), handleArray(parameter.getAcceptedValues()));
                }
                key = new StringBuilder(block.getKey()).append(".").append(parameter.getKey());
                map.put(key.toString(), handleArray(nullify(parameter.getValue())));
                key = new StringBuilder(block.getKey()).append(".parameters");
                map.put(key.toString(), listParameters(block));
            });
        }

        return map;
    }

    private void toCSV(HashMap<String, String> parameters, String separator) throws IOException {

        String file = cfgFile.getFile().toString().replaceAll("xml$", "cfg");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String newline = System.getProperty("line.separator");

            for (Entry<String, String> parameter : parameters.entrySet()) {
                StringBuilder str = new StringBuilder();
                str.append(parameter.getKey());
                str.append(separator);
                String value = parameter.getValue();
                str.append(isNotString(value) ? value : "\"" + value + "\"");
                str.append(newline);
                writer.write(str.toString());
            }
        }
    }

    private void toJson(HashMap<String, String> parameters) throws IOException {

        String file = cfgFile.getFile().toString().replaceAll("xml$", "json");
        String newline = System.getProperty("line.separator");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("{" + newline);
            int k = 0;
            int size = parameters.size();
            for (Entry<String, String> parameter : parameters.entrySet()) {
                StringBuilder str = new StringBuilder();
                str.append("\"");
                str.append(parameter.getKey());
                str.append("\"");
                str.append(":");
                String value = parameter.getValue();
                str.append(isNotString(value) ? value : "\"" + value + "\"");
                if (++k < size) {
                    str.append(",");
                }
                str.append(newline);
                writer.write(str.toString());
            }
            writer.write("}" + newline);
        }
    }

    private String clean(String str) {
        return str.replaceAll("[\"'\u2018\u2019\u201c\u201d\u201f]", "");
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isBoolean(String str) {
        return Boolean.TRUE.toString().equalsIgnoreCase(str) || Boolean.FALSE.toString().equalsIgnoreCase(str);
    }

    private boolean isArray(String str) {
        return str.startsWith("[") && str.endsWith("]");
    }

    private boolean isNotString(String str) {
        return isNumeric(str) || isBoolean(str) || isArray(str) || str.equalsIgnoreCase("null");
    }

    private String nullify(String str) {
        return str.isEmpty() ? "null" : str;
    }

    private String handleArray(String[] values) {

        String[] array = new String[values.length];
        for (int k = 0; k < array.length; k++) {
            array[k] = isNotString(values[k]) ? values[k] : "\"" + values[k] + "\"";
        }
        return Arrays.toString(array);
    }

    private String handleArray(String value) {
        String[] tokens = value.split("\"");
        List<String> list = new ArrayList();
        for (String token : tokens) {
            String str = token.trim();
            if (!str.isEmpty()) {
                list.add(isNotString(str) ? str : "\"" + str + "\"");
            }
        }
        if (list.size() > 1) {
            return Arrays.toString(list.toArray(new String[list.size()]));
        } else {
            return value;
        }
    }

    private String listBlocks(ConfigurationFile cfg) throws IOException {
        List<String> list = new ArrayList();
        for (XBlock block : cfg.readBlocks()) {
            list.add(block.getKey());
        }
        return handleArray(list.toArray(new String[list.size()]));
    }

    private String listParameters(XBlock block) {
        List<String> list = new ArrayList();
        for (XParameter param : block.getXParameters()) {
            list.add(param.getKey());
        }
        return handleArray(list.toArray(new String[list.size()]));
    }
}
