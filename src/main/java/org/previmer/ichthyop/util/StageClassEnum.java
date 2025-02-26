package org.previmer.ichthyop.util;

public enum StageClassEnum {

    AGE(0, "age"),
    LENGTH(1, "length");

    private int code;
    private String name;

    StageClassEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {return code;}
    public String getName() {return name;}

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
