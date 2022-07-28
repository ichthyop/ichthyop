package org.previmer;

 

/** Enumeration that manages grid types. */
public enum GridType {
    
    
    NEMO("NEMO"),
    ROMS("ROMS"),
    MARS("MARS"),
    REGULAR("REGULAR");
    
    private String name;
    
    private GridType(String name) {
        this.name = name;   
    }
    
    public String getName() { 
        return this.name;   
    }

}
