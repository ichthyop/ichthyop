package org.previmer.ichthyop.manager;

/**
 *
 * @author mariem
 */

import org.previmer.ichthyop.event.SetupListener;
import java.io.IOException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;


public class EvolManager extends AbstractManager implements SetupListener{

    private static final EvolManager evolManager = new EvolManager();


    public static EvolManager getInstance() {
        return evolManager;
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        //traitement nécessaire;
        getLogger().info("Evol manager setup [OK]");
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getParameter(String evolkey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.EVOL, evolkey, key);
    }

    public boolean isEnabled(String evolKey) {
        return getSimulationManager().getParameterManager().isBlockEnabled(BlockType.EVOL, evolKey);
    }

    private XBlock findActiveReproductiveStrategy() throws Exception {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.EVOL)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.isEmpty()) {
            throw new NullPointerException("Could not find any enabled " + BlockType.EVOL.toString() + " block in the configuration file.");
        }
        if (list.size() > 1) {
            throw new IOException("Found several " + BlockType.RELEASE.toString() + " blocks enabled in the configuration file. Please only keep one enabled.");
        }
        return list.get(0);
    }

        private void instantiateReproductiveStrategy() throws Exception {

        XBlock strategyBlock = findActiveReproductiveStrategy();
        String className = getParameter(strategyBlock.getKey(), "class_name");
        if (strategyBlock != null) {
            try {
                
            } catch (Exception ex) {
                
            }
        }
    }


}
