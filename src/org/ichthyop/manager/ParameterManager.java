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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.ichthyop.Version;
import org.ichthyop.xml.XConfigurationFile;
import org.ichthyop.util.Separator;
import org.ichthyop.util.StringUtil;
import org.ichthyop.xml.XZoneFile;

/**
 *
 * @author pverley
 */
public class ParameterManager extends AbstractManager {

    private static final ParameterManager PARAMETER_MANAGER = new ParameterManager();
    private HashMap<String, Parameter> parameters = new HashMap();
    private String inputPathname;
    private String mainFilename;

    public static ParameterManager getInstance() {
        return PARAMETER_MANAGER;
    }

    public void setConfigurationFile(File file) throws Exception {
        File mainFile = file;
        inputPathname = mainFile.getParent();
        parameters.clear();
        // convert old XML format
        if (mainFile.getName().endsWith(".xml")) {
            mainFile = XMLToCFG(mainFile);
        }
        mainFilename = mainFile.getAbsolutePath();
        // load parameters
        loadParameters(mainFilename, 0);
        // check for old XML zone file
        zoneXMLToCFG();
    }

    private File XMLToCFG(File file) throws Exception {
        XConfigurationFile cfg = new XConfigurationFile(file);
        cfg.load();
        cfg.upgrade();
        HashMap<String, String> rawParamMap = cfg.toProperties(true);
        List<Parameter> cfgparam = new ArrayList();
        String filename = file.getAbsolutePath().replaceAll("xml$", "cfg");
        int i = 1;
        for (Entry<String, String> entry : rawParamMap.entrySet()) {
            Parameter parameter = new Parameter(i, filename);
            parameter.parse(entry.toString());
            cfgparam.add(parameter);
            debug(parameter.toString());
            i++;
        }
        warning("[Configuration] XML format deprecated. Configuration file {0} has been converted to CFG format {1}", new String[]{file.getName(), new File(filename).getName()});
        saveParameters(cfgparam, filename);
        return new File(filename);
    }

    private void zoneXMLToCFG() throws Exception {

        List<String> zkeys = new ArrayList();
        for (String key : parameters.keySet()) {
            if (key.toLowerCase().endsWith("zone_file") && getString(key).endsWith(".xml")) {
                zkeys.add(key);
            }
        }
        boolean mainHasChanged = false;
        for (String key : zkeys) {
            File zfile = new File(getFile(key));
            String filename = zfile.getAbsolutePath().replaceAll("xml$", "cfg");
            // add parameter zone_prefix
            String prefix = key.toLowerCase().substring(0, key.lastIndexOf("."));
            Parameter zonefileP = parameters.remove(key);
            warning("[configuration] Removed deprecated parameter " + key);
            Parameter prefixP = new Parameter(zonefileP.iline, zonefileP.source, zonefileP.keySeparator, zonefileP.valueSeparator);
            prefixP.key = prefix + ".zone_prefix";
            prefixP.value = prefix;
            parameters.put(prefixP.key, prefixP);
            warning("[configuration] Added new parameter " + prefixP);
            if (!isNull(prefix + ".parameters")) {
                String[] zparam = getArrayString(prefix + ".parameters");
                for (int k = 0; k < zparam.length; k++) {
                    if ("zone_file".equals(zparam[k])) {
                        zparam[k] = "zone_prefix";
                    }
                }
            }
            // configuration file has changed
            mainHasChanged = true;
            // XML zone file does not exist
            if (!zfile.exists()) {
                continue;
            }
            // add new parameter ichthyop.configuration.${prefix}
            Parameter zoneP = new Parameter(-1, prefixP.source, prefixP.keySeparator, prefixP.valueSeparator);
            zoneP.key = "ichthyop.configuration." + prefix;
            zoneP.value = filename;
            parameters.put(zoneP.key, zoneP);
            warning("[configuration] Added new parameter " + zoneP);
            // load xml zone definition
            HashMap<String, String> rawZoneMap = new XZoneFile(zfile).toProperties(prefix, true);
            int i = 1;
            for (Entry<String, String> entry : rawZoneMap.entrySet()) {
                Parameter parameter = new Parameter(i, filename);
                parameter.parse(entry.toString());
                if (parameter.key.equals("configuration.subsets")) {
                    parameters.get("configuration.subsets").value = parameter.value;
                    continue;
                }
                parameters.put(parameter.key, parameter);
                debug(parameter.toString());
                i++;
            }
            warning("[Configuration] XML format deprecated. Zone file {0} has been converted to CFG format {1}", new String[]{zfile.getName(), new File(filename).getName()});
        }
        if (mainHasChanged) {
            saveParameters();
        }
    }

    public String getMainFile() {
        return mainFilename;
    }

    public String getConfigurationDescription() {
        return getString("configuration.description");
    }

    public void setConfigurationDescription(String description) {
        setString("configuration.description", description);
    }

    public Version getConfigurationVersion() {
        return new Version(getString("configuration.version"));
    }

    public void setConfigurationVersion(Version version) {
        setString("configuration.version", version.toString());
    }

    public String getConfigurationTitle() {
        return getString("configuration.title");
    }

    public void setConfigurationTitle(String longName) {
        setString("configuration.title", longName);
    }

    public void updateSource(String source) {

        for (Parameter parameter : parameters.values()) {
            if (parameter.source.equals(mainFilename)) {
                parameter.source = source;
            }
        }
    }

    /**
     * Save all the parameters in their corresponding file sources.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void saveParameters() throws IOException, FileNotFoundException {

        // find sources
        HashMap<String, List<Parameter>> parametersBySource = new HashMap();
        for (Parameter parameter : parameters.values()) {
            if (!parametersBySource.containsKey(parameter.source)) {
                parametersBySource.put(parameter.source, new ArrayList());
            }
            parametersBySource.get(parameter.source).add(parameter);
        }

        // save parameters for every source
        for (String source : parametersBySource.keySet()) {
            saveParameters(parametersBySource.get(source), source);
        }
    }

    private void saveParameters(List<Parameter> parameters, String filename) throws IOException, FileNotFoundException {

        boolean JSON = filename.endsWith(".json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            String newline = System.getProperty("line.separator");
            if (JSON) {
                StringBuilder str = new StringBuilder();
                str.append("{").append(newline);
                writer.write(str.toString());
            }
            Collections.sort(parameters, (Parameter p1, Parameter p2) -> p1.key.compareTo(p2.key));
            int i = 0;
            for (Parameter parameter : parameters) {
                i++;
                StringBuilder str = new StringBuilder();
                if (JSON) {
                    str.append("    ");
                }
                str.append("\"").append(parameter.key).append("\"");
                str.append(JSON ? ": " : parameter.keySeparator);
                if (StringUtil.isNotString(parameter.value) | StringUtil.isQuoted(parameter.value)) {
                    str.append(parameter.value);
                } else {
                    str.append("\"").append(parameter.value).append("\"");
                }
                if (JSON && (i != parameters.size())) {
                    str.append(",");
                }
                str.append(newline);
                writer.write(str.toString());
            }
            if (JSON) {
                StringBuilder str = new StringBuilder();
                str.append("}").append(newline);
                writer.write(str.toString());
            }
        }
        info("[Configuration] Configuration file {0} has been saved.", filename);
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
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
        Parameter param = parameters.get(key.toLowerCase());
        return (null == param)
                || param.value.isEmpty()
                || param.value.equalsIgnoreCase("null");
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
        String regexpPattern = filter.toLowerCase().replaceAll("[^\\*\\?]+", "\\\\Q$0\\\\E");
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

    public void setString(String key, String value) {
        getParameter(key).value = value;
    }

    /**
     * Returns the parameter designated by its key.
     *
     * @param key, the key of the parameter
     * @throws NullPointerException if the parameter is not found.
     * @return the parameter as a {@link Parameter}
     */
    private Parameter getParameter(String key) {
        String lkey = key.toLowerCase();
        if (parameters.containsKey(lkey)) {
            return parameters.get(lkey);
        } else {
            error("[Configuration] Could not find parameter " + key, new NullPointerException("Parameter " + key + " not found "));
        }
        return null;
    }

    /**
     * Returns the value of the specified parameter as a {@code String}
     *
     * @param key, the key of the parameter
     * @throws NullPointerException if the parameter is not found.
     * @return the value of the parameter as a {@code String}
     */
    final public String getString(String key) {
        return getParameter(key).value.trim().replaceAll("^\"|\"$", "");
    }

    /**
     * Returns the path of the configuration file that contains the specified
     * parameter.
     *
     * @param key, the key of the parameter
     * @return the path of the configuration file that contains the parameter.
     */
    final public String getSource(String key) {
        return parameters.get(key.toLowerCase()).source;
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
            error("[Configuration] Could not convert to Integer parameter " + getString(key), ex);
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
            error("[Configuration] Could not convert to Float parameter " + getString(key), ex);
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
            error("[Configuration] Could not convert to Double parameter " + getString(key), ex);
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
                error("[Configuration] Could not convert to Boolean parameter " + getString(key), ex);
            }
        } else if (warning) {
            warning("[Configuration] Could not find Boolean parameter " + key + ". Ichthyop assumes it is false.");
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
            error("[Configuration] Could not convert to array of Integer parameter " + getString(key), ex);
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
            error("[Configuration] Could not convert to array of Float parameter " + getString(key), ex);
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
            error("[Configuration] Could not convert to array of Double parameter " + getString(key), ex);
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

    /**
     * Loads recursively the parameters from the configuration file. The
     * function scans one by one the lines of the configuration file. A line is
     * discarded when it matches any of these criteria: it is empty, it contains
     * only blank and/or tab characters<br>
     * Any other lines are expected to be parameters formed as
     * <i>"key":"value"</i>. Refer to the documentation at the beginning of the
     * class for details about the parameters.<br>
     * A parameter whose key start with <i>ichthyop.configuration.</i> means the
     * value designate an other configuration file that has to be loaded in the
     * current {@code Configuration}. The function {@code loadProperties} is
     * called recursively.
     *
     * @param filename, the configuration file to be loaded
     * @param depth, an integer that reflects the level of recursivity of the
     * function. Zero for the main configuration file, one for a file loaded
     * from the main configuration file, etc.
     */
    private void loadParameters(String filename, int depth) {

        boolean JSON = filename.endsWith(".json");
        BufferedReader bfIn = null;
        // Open the buffer
        try {
            bfIn = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            error("[Configuration] Could not find Ichthyop configuration file: " + filename, ex);
        }
        StringBuilder msg = new StringBuilder();
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            space.append(". ");
        }
        msg.append(space);
        msg.append("[configuration] loading parameters from ");
        msg.append(filename);
        debug(msg.toString());
        space.append(". ");

        // Read it
        String line = null;
        int iline = 1;
        try {
            while ((line = bfIn.readLine()) != null) {
                line = line.trim();
                if (!(line.length() <= 1)) {
                    Parameter entry = JSON
                            ? new Parameter(iline, filename, ":", ",")
                            : new Parameter(iline, filename);
                    entry.parse(line);
                    if (parameters.containsKey(entry.key)) {
                        warning("{0}Ichthyop will ignore parameter {1}", new Object[]{space, entry});
                        warning("{0}Parameter already defined {1}", new Object[]{space, parameters.get(entry.key)});

                    } else {
                        parameters.put(entry.key, entry);
                        debug("[configuration] " + space + entry.toString());
                        if (entry.key.startsWith("ichthyop.configuration")) {
                            loadParameters(getFile(entry.key), depth + 1);
                        }
                    }
                }
                iline++;
            }
        } catch (IOException ex) {
            error("[Configuration] Error loading parameters from " + filename + " at line " + iline + " " + line, ex);
        }
    }

    public String[] getParameterSubsets() {
        return getArrayString("configuration.subsets");
    }

    /**
     * Inner class that represents a parameter in the configuration file.
     * {@code Configuration} parses the configuration file line by line. When
     * the line is not discarded (refer to function
     * {@link #loadParameters(java.lang.String, int)} for details about
     * discarded lines), it assumes it is a parameter (formed as <i>key
     * separator value</i> or <i>key separator1 value1 separator2 value2
     * separator2 value3 separator2 value4</i>) and creates a new
     * {@code Parameter} object.
     */
    private class Parameter {

        /**
         * Path of the configuration file containing the parameter.
         */
        private String source;
        /**
         * The line of the parameter in the configuration file.
         */
        private final int iline;
        /**
         * The key of the parameter.
         */
        private String key;
        /**
         * The value of the parameter.
         */
        private String value;
        /**
         * The separator between key and value. <i>key keySeparator value</i>
         */
        private String keySeparator;
        /**
         * The separator between the values of the parameter. <i>key
         * keySeparator value1 valueSeparator value2 valueSeparator value3</i>
         */
        private String valueSeparator;

        /**
         * Create a new parameter out of the given line.
         *
         * @param iline, the line of the parameter in the configuration file
         * @param source, the path of the configuration file
         */
        Parameter(int iline, String source, String keySeparator, String valueSeparator) {
            this.iline = iline;
            this.source = source;
            this.keySeparator = keySeparator;
            this.valueSeparator = valueSeparator;
        }

        /**
         * Create a new parameter out of the given line.
         *
         * @param iline, the line of the parameter in the configuration file
         * @param source, the path of the configuration file
         */
        Parameter(int iline, String source) {
            this.iline = iline;
            this.source = source;
        }

        /**
         * Create a new parameter from the command line
         *
         * @param key, the key of the parameter
         * @param value, the value of the parameter
         */
        Parameter(String key, String value) {
            this.key = key;
            this.value = value;
            this.source = null;
            this.iline = -1;
        }

        /**
         * Parse the line as a parameter. It follows the following steps: guess
         * the separator between key and value. Splits the line into a key and a
         * value. Guess the value separator in case it is actually an array of
         * values.
         *
         * @param line, the line to be parsed as a parameter
         */
        private void parse(String line) {
            key = value = null;
            // key separator
            if (null == keySeparator) {
                keySeparator = Separator.guess(line, Separator.COMA).toString();
            }
            // make sure the line contains at least one semi-colon (key;value)
            if (!line.contains(keySeparator)) {
                error("[Configuration] Failed to split line " + iline + " " + line + " as key" + keySeparator + "value (from " + source + ")", null);
            }
            // extract the key and remove leading and trailing double quotes
            key = line.substring(0, line.indexOf(keySeparator)).toLowerCase().trim().replaceAll("^\"|\"$", "");
            // extract the value
            try {
                value = line.substring(line.indexOf(keySeparator) + 1).trim();
            } catch (StringIndexOutOfBoundsException ex) {
                // set value to "null"
                value = "null";
            }
            // set empty value to "null"
            if (value.isEmpty()) {
                value = "null";
            }
            // value separator
            if (null == valueSeparator) {
                valueSeparator = Separator.guess(value, Separator.COMA).toString();
            }
            value = clean(value);
        }

        /**
         * Cleans the value of the parameter. Trims the value (removes leading
         * and trailing blank characters), and removes any trailing separators.
         *
         * @param value, the value to be cleaned
         * @return a copy of the value, trimmed and without any trailing
         * separator.
         */
        private String clean(String value) {
            String cleanedValue = value.trim();
            if (cleanedValue.endsWith(valueSeparator)) {
                cleanedValue = cleanedValue.substring(0, cleanedValue.lastIndexOf(valueSeparator));
                return clean(cleanedValue);
            } else {
                return cleanedValue;
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(key);
            str.append(" = ");
            str.append(value);
            str.append(" (from ");
            str.append((null != source) ? source : "runtime");
            if (iline >= 0) {
                str.append(" line ");
                str.append(iline);
            }
            str.append(")");
            return str.toString();
        }
    }
}
