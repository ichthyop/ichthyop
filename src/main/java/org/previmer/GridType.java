package org.previmer;

 

/** Enumeration that manages grid types. */
public enum GridType {
    
    
    NEMO("nemo"),
    ROMS("roms"),
    MARS("mars"),
    REGULAR("regular");
    
    private String name;
    
    private GridType(String name) {
        this.name = name;   
    }
    
    public String getName() { 
        return this.name;   
    }

}
