/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.io;

import fr.ird.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;

/**
 *
 * @author pverley
 */
public class DepthTracker extends AbstractTracker {

    public DepthTracker() {
        super(DataType.FLOAT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            Index index = Index.factory(new int[]{0, particle.getIndex()});
            getArray().setFloat(index, (float) particle.getDepth());
        }
    }

    @Override
    Array createArray() {
        return new ArrayFloat.D2(1, dimensions().get(1).getLength());
    }
}
