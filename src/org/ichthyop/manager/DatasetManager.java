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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ichthyop.dataset.BathymetryDataset;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.dataset.IDataset;

/**
 *
 * @author pverley
 */
public class DatasetManager extends AbstractManager {

    // static instance of the dataset manager
    final private static DatasetManager DATASET_MANAGER = new DatasetManager();
    // key of the ocean dataset
    private String oceandataset;
    // map of all the datasets
    private final HashMap<String, IDataset> datasets = new HashMap();

    public static DatasetManager getInstance() {
        return DATASET_MANAGER;
    }

    private String findOceanDataset() throws IOException {

        String datasetKey = null;
        int n = 0;
        for (String key : getConfiguration().getParameterSubsets()) {
            if (!getConfiguration().isNull(key + ".type")
                    && getConfiguration().getString(key + ".type").equalsIgnoreCase("ocean_dataset")) {
                if (getConfiguration().getBoolean(key + ".enabled")) {
                    datasetKey = key;
                    n++;
                }
            }
        }
        if (n == 0) {
            throw new NullPointerException("Could not find any enabled OCEAN_DATASET subset in the configuration file.");
        }
        if (n > 1) {
            throw new IOException("Found several enabled OCEAN_DATASET subsets in the configuration file. One only please.");
        }
        return datasetKey;
    }

    private List<String> findDatasets() {

        List<String> keys = new ArrayList();
        for (String key : getConfiguration().getParameterSubsets()) {
            if (!getConfiguration().isNull(key + ".type")
                    && getConfiguration().getString(key + ".type").equalsIgnoreCase("dataset")) {
                if (getConfiguration().getBoolean(key + ".enabled")) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    private IDataset instantiateDataset(String key) {

        String className = getConfiguration().getString(key + ".class_name");
        try {
            return (IDataset) Class.forName(className).getConstructor(String.class).newInstance(key);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            error("[dataset] Dataset instantiation failed " + key, ex);
        }
        return null;
    }
    
    public IDataset getDataset(String key) {
        return datasets.get(key);
    }

    public IDataset getOceanDataset() {
        return datasets.get(oceandataset);
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {

        // find datasets and instantiate them
        List<String> datasetkeys = new ArrayList();
        datasetkeys.add(oceandataset = findOceanDataset());
        datasetkeys.addAll(findDatasets());
        for (String key : datasetkeys) {
            datasets.put(key, instantiateDataset(key));
        }

        // setup dataset
        for (IDataset dataset : datasets.values()) {
            dataset.setUp();
        }
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {
        // setup dataset
        for (IDataset dataset : datasets.values()) {
            getSimulationManager().getTimeManager().addNextStepListener(dataset);
            dataset.init();
        }
    }
}
