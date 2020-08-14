/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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

package org.previmer.ichthyop.manager;

import java.io.IOException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.dataset.IDataset;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pverley
 */
public class DatasetManager extends AbstractManager {

    final private static DatasetManager datasetManager = new DatasetManager();
    private IDataset dataset;

    public static DatasetManager getInstance() {
        return datasetManager;
    }

    private void instantiateDataset() throws Exception {

        XBlock datasetBlock = findActiveDataset();
        String className = getParameter(datasetBlock.getKey(), "class_name");
        if (datasetBlock != null) {
            try {
            dataset = (IDataset) Class.forName(className).newInstance();
            } catch (Exception ex) {
                StringBuffer sb = new StringBuffer();
                sb.append("Dataset instantiation failed ==> ");
                sb.append(ex.toString());
                InstantiationException ieex = new InstantiationException(sb.toString());
                ieex.setStackTrace(ex.getStackTrace());
                throw ieex;
            }
        }
    }

    public IDataset getDataset() {
        return dataset;
    }

    public String getParameter(String datasetKey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.DATASET, datasetKey, key);
    }

    private XBlock findActiveDataset() throws Exception {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.DATASET)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.isEmpty()) {
            throw new NullPointerException("Could not find any " + BlockType.DATASET.toString() + " block in the configuration file.");
        }
        if (list.size() > 1) {
            throw new IOException("Found several " + BlockType.DATASET.toString() + " blocks enabled in the configuration file. Please only keep one enabled.");
        }
        return list.get(0);
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        instantiateDataset();
        getDataset().setUp();
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        getSimulationManager().getTimeManager().addNextStepListener(getDataset());
        getDataset().init();
    }
}
