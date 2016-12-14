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
import org.ichthyop.io.ParamType;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ichthyop.Version;
import org.ichthyop.io.ConfigurationFile;

/**
 *
 * @author pverley
 */
public class ParameterManager extends AbstractManager {

    private static ParameterManager parameterManager = new ParameterManager();
    private ConfigurationFile cfgFile;

    public static ParameterManager getInstance() {
        return parameterManager;
    }

    public void setConfigurationFile(File file) throws Exception {
        cfgFile = new ConfigurationFile(file);
        cfgFile.load();
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

    public List<XParameter> getParameters(ParamType paramType) {
        return cfgFile.getParameters(paramType);
    }

    public String getParameter(String blockKey, String key) {
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

    public String getParameter(BlockType blockType, String blockKey, String key) {

        XParameter xparam = cfgFile.getBlock(blockType, blockKey).getXParameter(key);
        if (xparam != null) {
            return xparam.getValue();
        } else {
            throw new NullPointerException("Could not retrieve parameter " + blockKey + "/" + key);
        }
    }

    public boolean isBlockEnabled(BlockType type, String key) {
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

    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
        asProperties();
    }

    public void initializePerformed(InitializeEvent e) {
        // does nothing
    }

    private void asProperties() throws IOException {

        boolean extended = true;

        String file = cfgFile.getFile().toString().replaceAll("xml$", "cfg");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String newline = System.getProperty("line.separator");

            writer.write("# " + newline);
            writer.write("# " + cfgFile.getLongName() + newline);
            if (extended) {
                writer.write("# " + cfgFile.getDescription() + newline);
            }
            writer.write("# " + newline);
            writer.write("ichthyop.version=" + cfgFile.getVersion().toString() + newline);
            for (XBlock block : readBlocks()) {
                writer.write("#" + newline);
                if (extended) {
                    writer.write("# " + block.getKey().replaceAll("[\\._]", " ").toUpperCase() + newline);
                    writer.write("#" + newline);
                }
                StringBuilder str = new StringBuilder();
                str.append(block.getKey());
                str.append(".description=");
                str.append(block.getDescription());
                str.append(newline);
                if (extended) {
                    writer.write(str.toString());
                }

                str = new StringBuilder();
                str.append(block.getKey());
                str.append(".enabled=");
                str.append(block.isEnabled());
                str.append(newline);
                writer.write(str.toString());

                str = new StringBuilder();
                str.append(block.getKey());
                str.append(".treepath=");
                str.append(block.getTreePath());
                if (extended) {
                    writer.write(str.toString());
                }

                str = new StringBuilder();
                str.append(block.getKey());
                str.append(".type=");
                str.append(block.getType());
                str.append(newline);
                if (block.getType() != BlockType.OPTION) {
                    writer.write(str.toString());
                }

                for (XParameter parameter : block.getXParameters()) {
                    if (extended) {
                        writer.write("#" + newline);
                    }
                    str.append(newline);
                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append(".long_name=");
                    str.append(nullify(parameter.getLongName()));
                    str.append(newline);
                    if (extended) {
                        writer.write(str.toString());
                    }

                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append(".format=");
                    str.append(nullify(parameter.getFormat().toString()));
                    str.append(newline);
                    if (extended) {
                        writer.write(str.toString());
                    }

                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append(".description=");
                    str.append(nullify(parameter.getDescription()));
                    str.append(newline);
                    if (extended) {
                        writer.write(str.toString());
                    }

                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append(".default=");
                    str.append(nullify(parameter.getDefault()));
                    str.append(newline);
                    if (extended) {
                        writer.write(str.toString());
                    }

                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append(".accepted=");
                    str.append(Arrays.toString(parameter.getAcceptedValues()));
                    str.append(newline);
                    if (extended) {
                        writer.write(str.toString());
                    }

                    str = new StringBuilder();
                    str.append(block.getKey());
                    str.append(".");
                    str.append(parameter.getKey());
                    str.append("=");
                    str.append(nullify(parameter.getValue()));
                    str.append(newline);
                    writer.write(str.toString());
                }
            }
        }
    }

    private String nullify(String str) {
        return str.isEmpty() ? "null" : str;
    }
}
