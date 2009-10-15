/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.action.ActionPool;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.release.*;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.event.ReleaseEvent;
import fr.ird.ichthyop.arch.IReleaseProcess;
import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ReleaseManager implements IReleaseManager {

    private static final ReleaseManager releaseManager = new ReleaseManager();
    private IReleaseProcess releaseProcess;
    private XBlock releaseBlock;

    public ReleaseManager() {
        getSchedule().addReleaseListener(this);
    }

    public static IReleaseManager getInstance() {
        return releaseManager;
    }

    public ReleaseSchedule getSchedule() {
        return ReleaseSchedule.getInstance();
    }

    private IReleaseProcess getReleaseProcess() {
        if (releaseProcess == null) {
            try {
                releaseBlock = findActiveReleaseProcess();
                if (releaseBlock != null) {
                    releaseProcess = (IReleaseProcess) Class.forName(releaseBlock.getParameter("class_name").getValue()).newInstance();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ActionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return releaseProcess;
    }

    public XBlock getXReleaseProcess(String key) {
        return ICFile.getInstance().getBlock(TypeBlock.RELEASE, key);
    }

    public String getParameter(String key) {
        return releaseBlock.getParameter(key).getValue();
    }

    private XBlock findActiveReleaseProcess() {
        List<XBlock> list = new ArrayList();
        for (XBlock block : ICFile.getInstance().getBlocks(TypeBlock.RELEASE)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.size() > 0 && list.size() < 2) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public void releaseTriggered(ReleaseEvent event) {
        getReleaseProcess().release(event);
    }

    public int getNbParticles() {
        return releaseProcess.getNbParticles();
    }
}
