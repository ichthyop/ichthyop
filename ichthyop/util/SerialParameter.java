package ichthyop.util;

import ichthyop.io.Configuration;

/**
 * The enum is used to list all the the parameters that are
 * allowed to have several values in the SERIAL simulation mode.
 * A field Parameter provides two constants:
 * <ul>
 * <li>the length, meaning the number of values of the parameter
 * <li>the index of the value used in the current set of parameters
 * </ul>
 */

public enum SerialParameter {

    REPLICA(Configuration.getNbReplica()),
            PATCHINESS(Configuration.getRadiusPatchi().length),
            PULSATION(Configuration.getReleaseDt().length),
            BUOYANCY(Configuration.getEggDensity().length),
            RECRUIT_AGE(Configuration.getAgeRecruitment().length),
            RECRUIT_LENGTH(Configuration.getLengthRecruitment().length),
            LETHAL_TP_LARVAE(Configuration.getLethalTpLarvae().length),
            LETHAL_TP_EGG(Configuration.getLethalTpEgg().length),
            RELASE_DEPTH(Configuration.getDepthReleaseMin().length),
            TO(Configuration.get_t0().length),
            DVM(Configuration.getDepthDay().length);

    ///////////////////////////////
    // Declaration of the variables
    ///////////////////////////////

    /**
     * The index of the value used in the current set of parameters
     */
    private int index;
    /**
     * The number of values of the parameter
     */
    private final int length;

    ///////////////
    // Constructors
    ///////////////

    /**
     * Constructs a new field of the enum with the specified length and
     * initializes the index to zero.
     *
     * @param length The number of values of the parameter
     */
    SerialParameter(int length) {
        this.length = length;
        reset();
    }

    ////////////////////////////
    // Definition of the methods
    ////////////////////////////

    /**
     * Sets the index to zero
     */
    public void reset() {
        index = 0;
    }

    /**
     * Increments the index by one
     */
    public void increment() {
        index++;
    }

    /**
     * Gets the length of the Parameter
     * @return the length of the Parameter
     */
    public int length() {
        return length;
    }

    /**
     * Gets the index of the Parameter
     * @return the index of the value used in the current set of parameters.
     */
    public int index() {
        return index;
    }

    /**
     * Checks whether the Parameter has more values
     * @return <code>true</code> if the Parameter has more values
     */
    public boolean hasNext() {
        return index < (length - 1);
    }

    //---------- End of the enum Parameter
}
