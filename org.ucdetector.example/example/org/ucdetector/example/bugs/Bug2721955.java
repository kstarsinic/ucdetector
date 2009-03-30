package org.ucdetector.example.bugs;
/**
 * i had two constants, both with UCD Marker QuickFix "Delete Code"
 * <pre>
 * public static final String CON1 = "1";
 * public static final String CON2 = "2";
 * </pre>
 * I ran the quick fix on the first one and the both UDC Marker was deleted.
 * The code was only changed for the first quick fix.
 */
public class Bug2721955 {
  public static final String UNUSED_1 = "1";
  public static final String UNUSED_2 = "2";
  public static final String UNUSED_3 = "2";
  public static final String UNUSED_4 = "2";
}
