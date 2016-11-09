package ichthyop.util;

/**
 * This enum lists all the fields of the netcdf input file that the application
 * might have to access while running.
 * Each field provides the following information:
 * <ul>
 * <li> the type of field (dimension / variable / global attribute)
 * <li> the description of the field
 * <li> whether the field is required for MARS simulation
 * <li> whether the filed is requiered for ROMS simulation
 * <li> the default field name for ROMS (null if not required for ROMS)
 * <li> the default filed name for MARS (null if not required for MARS)
 * <li> the current name for ROMS if different from default name
 * <li> the current name for MARS if different from default name
 * <li> whether the field is required for two-dimensions simulations
 * <li> whether the field is required for three-dimensions simulations
 * </ul>
 * The enum provides a few methods to manipulate easily the values within
 * the application.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public enum NCField {

    xiDim(Constant.DIMENSION, Resources.LBL_XI_DIM, "x"),
    etaDim(Constant.DIMENSION, Resources.LBL_ETA_DIM, "y"),
    zDim(Constant.DIMENSION, Resources.LBL_Z_DIM, "z"),
    timeDim(Constant.DIMENSION, Resources.LBL_TIME_DIM, "time_counter"),
    lon(Constant.VARIABLE, Resources.LBL_LONGITUDE, "nav_lon"),
    lat(Constant.VARIABLE, Resources.LBL_LATITUDE, "nav_lat"),
    mask(Constant.VARIABLE, Resources.LBL_MASK, "fmask"),
    bathy(Constant.VARIABLE, Resources.LBL_BATHYMETRY, "hdepw"),
    gdept(Constant.VARIABLE, "", "gdept"),
    gdepw(Constant.VARIABLE, "", "gdepw"),
    u3d(Constant.VARIABLE, Resources.LBL_U + " 3D", "vozocrtx"),
    v3d(Constant.VARIABLE, Resources.LBL_V + " 3D", "vomecrty"),
    temp(Constant.VARIABLE, Resources.LBL_TEMPERATURE, "votemper"),
    sal(Constant.VARIABLE, Resources.LBL_SAL, "vosaline"),
    time(Constant.VARIABLE, Resources.LBL_TIME, "time_counter"),
    zeta(Constant.VARIABLE, Resources.LBL_ZETA, null),
    e1t(Constant.VARIABLE, "e1t", "e1t"),
    e2t(Constant.VARIABLE, "e2t", "e2t"),
    e1v(Constant.VARIABLE, "e1v", "e1v"),
    e2u(Constant.VARIABLE, "e2u", "e2u"),
    e3t(Constant.VARIABLE, "e3t", "e3t_ps"),
    kv(Constant.VARIABLE, Resources.LBL_KV, null);

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    protected String defaultName;

    protected String name;

    protected String description;

    protected int type;

///////////////
// Constructors
///////////////

    NCField(int type, String description, String defaultName) {
        
        this.type = type;
        this.description = description;
        this.defaultName = defaultName;
    }
    /**
     * Constructs a new item of the enum and sets default values. The names for
     * MARS and ROMS take the default value.
     *
     * @param the type of field (dimension / variable / global attribute).
     * See {@link Constant} for details about the type labels.
     * @param description a String that describes the field in a few words.
     * @param defaultRoms a String, the default name for ROMS simulation
     * @param defaultMars a String, the default name for ROMS simulation
     * @param required2D a boolean, <code>true</code> if required in 2D
     * simulations.
     * @param required3D a boolean, <code>true</code> if required in 3D
     * simulations.
     */
    @Deprecated
    NCField(int type, String description, String defaultRoms,
            String defaultMars,
            boolean required2D,
            boolean required3D) {}

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Determines whether the field id required for the specified configuration.
     * The terms configuration just refers to the type of model (Roms or Mars)
     * and the dimension of the simulation (2D or 3D).
     *
     * @param config, an int, <code>config = model + dimension</code> where
     * model is an integer characterizing the type of model (ROMS/MARS) and
     * dimension an integer characterizing the dimension (2D/3D).
     * @return <code>true</code> if required for the specified configuration,
     * <code>false</code> otherwise.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the simulation.
     */
    public boolean isRequired() {

        return true;
    }

    /**
     * Gets the default field name for the specified model (Roms or Mars).
     * @param model an int, characterizing the model (Roms or Mars)
     * @return the default name of the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the model.
     */
    public String getDefaultName() {

        return defaultName;
    }

    /**
     * Gets the name of the field for the specified model (Roms or Mars).
     * @param model an int, characterizing the model (Roms or Mars)
     * @return the name of the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the model.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name of the field for the specified model with the given String.
     *
     * @param model an int, characterizing the model (Roms or Mars)
     * @param name a String, the new name for the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the model.
     */
    public void setName( String name) {

        this.name = name;
    }

    /**
     * Gets the description of the field.
     * @return the String that provides a brief description of the field.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the type of the field (dimension / variable / global attribute).
     * @return the type of the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of NetCDF fields.
     */
    public int getType() {
        return type;
    }

    /**
    * Sets the type of the field (dimension / variable / global attribute).
    * @param the type of the field.
    * @see ichthyop.util.Constant for details about the labels characterizing
    * the type of NetCDF fields.
    */

    public void setType(int type) {
        this.type = type;
    }

    //---------- End of class
}
