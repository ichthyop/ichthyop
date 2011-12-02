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
import org.previmer.ichthyop.arch.IEvol;
import org.previmer.ichthyop.evol.InitialSpawn;
//import org.previmer.ichthyop.evol.Stray;

public class EvolManager extends AbstractManager implements SetupListener {

    private static final EvolManager evolManager = new EvolManager();
    private IEvol evolStrategy;
    //private Stray str;

    public static EvolManager getInstance() {
        return evolManager;
    }
    
    public void setupPerformed(SetupEvent e) throws Exception {
        InitialSpawn init_spawn = new InitialSpawn();
        do {
            init_spawn.InitialSpawnSetUp();
        } while (InitialSpawn.last_spawn <= SimulationManager.getInstance().getTimeManager().get_tO() + 31536000);
        getLogger().info("Evol manager initial spawn setup [OK]");
        
        //début de la deuxième génération
        int nb_spawn = SimulationManager.getInstance().getIOSpawnManager().countSpawn(SimulationManager.getInstance().getIOSpawnManager().getLast_name());
        
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
        //  float lost;

        if (strategyBlock != null) {
            try {
                evolStrategy = (IEvol) Class.forName(className).newInstance();
                getEvolStrategy().loadParameters();

                // A partir de la première ponte     
                //      str.loadParameters();
                //      lost=str.getRateStray();
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder();
                sb.append("Evol process instantiation failed ==> ");
                sb.append(ex.toString());
                InstantiationException ieex = new InstantiationException(sb.toString());
                ieex.setStackTrace(ex.getStackTrace());
                throw ieex;

            }
        }
    }

    /**
     * @return the evolStrategy
     */
    private IEvol getEvolStrategy() {
        return evolStrategy;
    }
}
