package org.ucdetector.example;

public interface InterfaceExample {
    // -------------------------------------------------------------------------
    // UNUSED
    // -------------------------------------------------------------------------
    public static final String UNUSED = "UNUSED"; // Marker YES: unused code

    // -------------------------------------------------------------------------
    // USED
    // -------------------------------------------------------------------------
    public static final String USED = "USED";

    void unusedMethod();

    String overridenMethod();
}
