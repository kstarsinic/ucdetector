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
  public final int usedByJavadoc = 1;// Marker YES: unused code

  /** This kind of field declaration should not cause problems for UCDetector! */
  private final int usedFieldList1 = 1, //
      usedFieldList2 = 2,//
      usedFieldList3 = 4;//

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

  protected final int protectedUnusedField = 0; // Marker YES: unused code

  /** keyword "transient" should not cause UCDetector problems */
  public transient final int publicUnusedField = 0; // Marker YES: unused code

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
  // HELPER
  // -------------------------------------------------------------------------
  public static void main(String[] args) {
    FieldExamples ex = new FieldExamples();
    System.out.println(ex.privateUsedField);
    System.out.println(ex.usedFieldList1);
    System.out.println(ex.usedFieldList2);
    System.out.println(ex.usedFieldList3);
    ex.privateUsedField = 1;
    ex.defaultUsedField = 1;
    ex.protectedUsedField = 1;
    ex.publicUsedField = 1;
  }
}
