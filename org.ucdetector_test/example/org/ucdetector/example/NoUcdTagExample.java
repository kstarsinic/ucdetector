package org.ucdetector.example;

/**
 * NoUcdTagExample
 */
public class NoUcdTagExample {
  // -------------------------------------------------------------------------
  // UNUSED
  // -------------------------------------------------------------------------
  public static String UNUSED = "UNUSED"; // NO_UCD

  public static String USED = "UNUSED"; // NO_UCD

  public static final String UNUSED2 = "UNUSED"; // Marker YES: unused code

  public static final String USED2 = "UNUSED";

  /**
   * javadoc
   */
  public static String unused() { // NO_UCD
    return "hello";
  }

  long unusedLong = 5L; // NO_UCD

  public static final String UNUSED_MARKER = "UNUSED"; // Marker YES: unused code

  public static String unused_Marker() {// Marker YES: unused code
    return "hello";
  }
}
