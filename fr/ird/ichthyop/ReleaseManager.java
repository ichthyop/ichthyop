/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.IReleaseManager;
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

    private static ReleaseManager releaseManager = new ReleaseManager();
    private IReleaseProcess releaseProcess;

    public static IReleaseManager getInstance() {
        return releaseManager;
    }

    private IReleaseProcess getReleaseProcess() {
        if (releaseProcess == null) {
            try {
                XBlock block = findActiveReleaseProcess();
                if (block != null) {
                    releaseProcess = (IReleaseProcess) Class.forName(block.getChildTextNormalize("class_name")).newInstance();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ActionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return releaseProcess;
    }

    public XBlock getXReleaseProcess(String key) {
        return ICFile.getInstance().getBlock(TypeBlock.ZONE, key);
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
        try {
            getReleaseProcess().release(event);
        } catch (IOException ex) {
            Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
