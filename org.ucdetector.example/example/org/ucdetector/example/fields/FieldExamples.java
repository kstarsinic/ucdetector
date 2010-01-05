package org.ucdetector.example.fields;

/**
 * {@link FieldExamples#usedByJavadoc}
 */
public class FieldExamples {

  /** should be ignored, because of field filter *test* */
  public static final int test_field = 0;

  /** should be ignored, because of field filter *test* */
  public static final int myTest = 0;

  /** should be ignored, because of field filter *test* */
  public static final int my_field_Test = 0;

  /**
   *  This field is referenced by javadoc
   **/
  public int usedByJavadoc = 1;// Marker YES: unused code,use final

  /** This kind of field declaration should not cause problems for UCDetector! */
  private int usedFieldList1 = 1, // Marker YES: use final
      usedFieldList2 = 2,// Marker YES: use final
      usedFieldList3 = 4;// Marker YES: use final

  // -------------------------------------------------------------------------
  // UNUSED
  // -------------------------------------------------------------------------
  @SuppressWarnings("unused")
  /**
   * no UCDetector reference marker here,
   * because UCDetector ignores private fields
   */
  private final int privateUnusedField = 0;

  final int defaultUnusedField = 0;// Marker YES: unused code

  protected int protectedUnusedField = 0; // Marker YES: unused code,use final

  /** keyword "transient" should not cause UCDetector problems */
  public transient int publicUnusedField = 0; // Marker YES: unused code,use final

  /** javadoc */
  public static final String UNUSED_FIELD = "UNUSED_FIELD"; // Marker YES: unused code
  // (private)

  // -------------------------------------------------------------------------
  // USED
  // -------------------------------------------------------------------------
  private int privateUsedField = 0;

  int defaultUsedField = 0;

  /** keyword "volatile" should not cause UCDetector problems */
  protected volatile int protectedUsedField = 0;

  public int publicUsedField = 0;

  /** javadoc */
  public static final String USED_FIELD = "UNUSED_FIELD";

  // -------------------------------------------------------------------------
  // MULTI LINE
  // -------------------------------------------------------------------------
  private int oneLine1 = 1, noMarker = 5, oneLine2 = 2, oneLine3 = 4;// Marker YES: use final

  public static final String UNUSED_1 = "1", USED = "4", UNUSED_2 = "2", UNUSED_3 = "3"; // Marker YES: unused code

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------
  public static void main(String[] args) {
    FieldExamples ex = new FieldExamples();
    System.out.println(ex.privateUsedField);
    //
    System.out.println(ex.usedFieldList1);
    System.out.println(ex.usedFieldList2);
    System.out.println(ex.usedFieldList3);
    //
    System.out.println(ex.oneLine1);
    System.out.println(ex.oneLine2);
    System.out.println(ex.oneLine3);
    System.out.println(ex.noMarker);
    ex.noMarker = 6;
    //
    ex.privateUsedField = 1;
    ex.defaultUsedField = 1;
    ex.protectedUsedField = 1;
    ex.publicUsedField = 1;
  }
}
