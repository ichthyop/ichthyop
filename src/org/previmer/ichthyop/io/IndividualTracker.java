package org.previmer.ichthyop.io;
/**
 *
 * @author mariem
 */
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

public class IndividualTracker extends AbstractTracker{

    public IndividualTracker() {
        super(DataType.INT);
    }

    //************* A compléter dans outputManager *************************
    @Override
    void setDimensions() {
        addAliveDimension();
    }

    @Override
    public Attribute[] attributes() {
        int nbParticle= getSimulationManager().getSimulation().getPopulation().size();  // avec -1 ou pas ?
        Attribute[] attr = new Attribute[nbParticle];
        for (int i=1; i<= nbParticle; i++) {
            attr[i] = new Attribute("individual", i);
        }
        return attr;
    }

    @Override
    Array createArray() {
        return new ArrayDouble.D1(1);
    }

    public void track() {
        getArray().setDouble(0, getSimulationManager().getTimeManager().getTime());
    }
}