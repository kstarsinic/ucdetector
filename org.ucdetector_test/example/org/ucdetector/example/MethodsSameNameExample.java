package org.ucdetector.example;

/**
 * javadoc
 */
public class MethodsSameNameExample {
    /**
     * javadoc
     */
    public void sameName() {
    }

    /**
     * javadoc
     */
    public void sameName(int i) {// Marker YES: unused code
    }

    /**
     * javadoc
     */
    public void sameName(String s, int i) {// Marker YES: unused code
    }

    /**
     * javadoc
     */
    //
    public void sameName(Long s, int i) {
    }

    /**
     * javadoc
     */
    public void sameName(Float f, String s, int i) {// Marker YES: unused code
    }
}
