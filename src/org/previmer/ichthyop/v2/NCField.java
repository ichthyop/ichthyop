package org.previmer.ichthyop.v2;

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

    xiDim(Constant.DIMENSION, Resources.LBL_XI_DIM, Constant.XI_DIM_R,
          Constant.XI_DIM_M, true, true),
            etaDim(Constant.DIMENSION, Resources.LBL_ETA_DIM,
                   Constant.ETA_DIM_R, Constant.ETA_DIM_M, true, true),
            zDim(Constant.DIMENSION, Resources.LBL_Z_DIM,
                 Constant.Z_DIM_R, Constant.Z_DIM_M, false, true),
            timeDim(Constant.DIMENSION, Resources.LBL_TIME_DIM,
                    Constant.TIME_DIM_R, Constant.TIME_DIM_M, true, true),
            lon(Constant.VARIABLE, Resources.LBL_LONGITUDE,
                Constant.LON_R, Constant.LON_M, true, true),
            lat(Constant.VARIABLE, Resources.LBL_LATITUDE, Constant.LAT_R,
                Constant.LAT_M, true, true),
            mask(Constant.VARIABLE, Resources.LBL_MASK, Constant.MASK_R, null, true, true),
            bathy(Constant.VARIABLE, Resources.LBL_BATHYMETRY,
                  Constant.BATHY_R, Constant.BATHY_M, true, true),
            u2d(Constant.VARIABLE, Resources.LBL_U + " 2D",
                Constant.U2D_R, Constant.U2D_M, true, false),
            v2d(Constant.VARIABLE, Resources.LBL_V + " 2D",
                Constant.V2D_R, Constant.V2D_M, true, false),
            u3d(Constant.VARIABLE, Resources.LBL_U + " 3D",
                Constant.U3D_R, Constant.U3D_M, false, true),
            v3d(Constant.VARIABLE, Resources.LBL_V + " 3D",
                Constant.V3D_R, Constant.V3D_M, false, true),
            temp(Constant.VARIABLE, Resources.LBL_TEMPERATURE,
                 Constant.TP_R, Constant.TP_M, false, true),
            sal(Constant.VARIABLE, Resources.LBL_SAL, Constant.SAL_R,
                Constant.TP_M, false, true),
            time(Constant.VARIABLE, Resources.LBL_TIME, Constant.TIME_R,
                 Constant.TIME_M, true, true),
            zeta(Constant.VARIABLE, Resources.LBL_ZETA, Constant.ZETA_R,
                 Constant.ZETA_M, false, true),
            pm(Constant.VARIABLE, Resources.LBL_PM, Constant.PM, null, true, true),
            pn(Constant.VARIABLE, Resources.LBL_PN, Constant.PN, null, true, true),
            thetaS(Constant.ATTRIBUTE, Resources.LBL_THETAS,
                   Constant.THETA_S, null, false, true),
            thetaB(Constant.ATTRIBUTE, Resources.LBL_THETAB,
                   Constant.THETA_B, null, false, true),
            hc(Constant.ATTRIBUTE, Resources.LBL_HC, Constant.HC, null, false, true),
            sigma(Constant.VARIABLE, Resources.LBL_SIGMA, null,
                  Constant.SIGMA, false, true),
            kv(Constant.VARIABLE, Resources.LBL_KV, Constant.KV_R,
               Constant.KV_M, false, true);

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    protected String defaultRoms, defaultMars;

    protected String nameRoms, nameMars;

    protected String description;

    protected boolean requiredRoms, requiredMars, required2D, required3D;

    protected int type;

///////////////
// Constructors
///////////////

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
    NCField(int type, String description, String defaultRoms,
            String defaultMars,
            boolean required2D,
            boolean required3D) {

        this.type = type;
        this.description = description;
        this.defaultMars = defaultMars;
        this.defaultRoms = defaultRoms;
        this.required2D = required2D;
        this.required3D = required3D;
        nameRoms = defaultRoms;
        nameMars = defaultMars;
        requiredRoms = (defaultRoms != null);
        requiredMars = (defaultMars != null);
    }

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
    public boolean isRequired(int config) {

        boolean required = false;
        switch (config) {
        case (Constant.ROMS + Constant.SIMU_2D):
            required = requiredRoms && required2D;
            break;
        case (Constant.ROMS + Constant.SIMU_3D):
            required = requiredRoms && required3D;
            break;
        case (Constant.MARS + Constant.SIMU_2D):
            required = requiredMars && required2D;
            break;
        case (Constant.MARS + Constant.SIMU_3D):
            required = requiredMars && required3D;
            break;
        }
        return required;
    }

    /**
     * Gets the default field name for the specified model (Roms or Mars).
     * @param model an int, characterizing the model (Roms or Mars)
     * @return the default name of the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the model.
     */
    public String getDefaultName(int model) {

        String name = "";
        switch (model) {
        case (Constant.ROMS):
            name = defaultRoms;
            break;
        case (Constant.MARS):
            name = defaultMars;
            break;
        }
        return name;
    }

    /**
     * Gets the name of the field for the specified model (Roms or Mars).
     * @param model an int, characterizing the model (Roms or Mars)
     * @return the name of the field.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the model.
     */
    public String getName(int model) {

        String name = "";
        switch (model) {
        case (Constant.ROMS):
            name = nameRoms;
            break;
        case (Constant.MARS):
            name = nameMars;
            break;
        }
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
    public void setName(int model, String name) {

        switch (model) {
        case (Constant.ROMS):
            nameRoms = name;
            break;
        case (Constant.MARS):
            nameMars = name;
            break;
        }
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
