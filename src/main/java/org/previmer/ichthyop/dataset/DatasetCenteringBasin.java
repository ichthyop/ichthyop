package org.previmer.ichthyop.dataset;

public enum DatasetCenteringBasin {
    
    /** If the dataset is Pacific centerred, i.e lon=[0, 360] */
    PACIFIC(0),
    ATLANTIC(1);
    
    private int code;
    
    DatasetCenteringBasin(int code) {
        this.code = code;
    }
    
    public int getCode() {return code;}

    @Override
    public String toString() {
        return name().toLowerCase();
    }


}
