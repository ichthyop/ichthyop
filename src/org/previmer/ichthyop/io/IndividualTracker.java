package org.previmer.ichthyop.io;
/**
 *
 * @author mariem
 */
import java.util.Iterator;
import org.previmer.ichthyop.arch.IBasicParticle;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

public class IndividualTracker extends AbstractTracker{

    public IndividualTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addAliveDimension();
    }

  /*  @Override
    public Attribute[] attributes() {
        int nbParticle= getSimulationManager().getSimulation().getPopulation().size();  // avec -1 ou pas ?
        Attribute[] attr = new Attribute[nbParticle];
        for (int i=1; i<= nbParticle; i++) {
            attr[i] = new Attribute("individual", i);
        }
        return attr;
    }*/

    @Override
    Array createArray() {
        ArrayFloat.D2 array = new ArrayFloat.D2(1, dimensions().get(1).getLength());
        for (int i = 0; i < dimensions().get(1).getLength(); i++) {
            array.set(0, i, Float.NaN);
        }
        return array;
    }
    @Override
    public ArrayFloat.D2 getArray() {
        return (ArrayFloat.D2) super.getArray();
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().set(0, particle.getIndex(), (float) particle.getIndex());
        }
    }
}