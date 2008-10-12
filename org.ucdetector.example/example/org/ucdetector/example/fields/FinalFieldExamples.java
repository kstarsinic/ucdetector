package org.ucdetector.example.fields;

public class FinalFieldExamples {
  /** no marker here */
  public static int DONT_USE_FINAL = 0;

  public static int USE_FINAL; // Marker YES: use final

  static {
    USE_FINAL = 3;
  }

  private final int finalField = 1;

  private int fieldNotSet = 1; // Marker YES: use final

  @SuppressWarnings("unused")
  private int fieldNotSet2; // Marker YES: use final

  /**
   * Using final here, causes a compile error BUT: It does make no sense to
   * set field in declaration AND constructor!!!!
   */
  private int fieldSetInConstructorAndDeclaration = 2; // Marker YES: use final

  private int fieldSetInConstructor; // Marker YES: use final

  private int fieldSetInInitilizer; // Marker YES: use final

  /**
   * Using final here, causes a compile error BUT: It does make no sense to
   * set field in declaration AND constructor!!!!
   */
  private int fieldSetInInitilizerAndDeclaration = 3; // Marker YES: use final

  private int fieldSetInStaticMethod = 2;

  private int fieldSetInSetter = 2;

  public int getFieldSetInSetter() {
    return fieldSetInSetter;
  }

  public void setFieldSetInSetter(int fieldSetInSetter) {
    this.fieldSetInSetter = fieldSetInSetter;
  }

  private static int staticFieldSetInSetter = 2;

  /**
   * 2008-10-12: volatile fields may not be final!
   */
  private volatile int volatitleField;

  public static int geStatictFieldSetInSetter() {
    return staticFieldSetInSetter;
  }

  public static void setStaticFieldSetInSetter(int fieldSetInSetter) {
    FinalFieldExamples.staticFieldSetInSetter = fieldSetInSetter;
  }

  // 
  {
    fieldSetInInitilizer = 6;
    fieldSetInInitilizerAndDeclaration = 6;
  }

  public FinalFieldExamples() {
    fieldSetInConstructor = 3;
    volatitleField = 0;
    // It does make no sense to set field in declaration AND constructor!!!!
    fieldSetInConstructorAndDeclaration = 4;
    DONT_USE_FINAL = 1;
    fieldNotSet2 = 3;
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------
  public static void main(String[] args) {
    FinalFieldExamples ex = new FinalFieldExamples();
    System.out.println(ex.finalField);
    System.out.println(ex.fieldSetInConstructor);
    System.out.println(ex.fieldSetInConstructorAndDeclaration);
    System.out.println(ex.fieldSetInStaticMethod);
    System.out.println(ex.fieldNotSet);
    System.out.println(ex.fieldSetInInitilizer);
    System.out.println(ex.fieldSetInInitilizerAndDeclaration);
    System.out.println(ex.volatitleField);
    ex.fieldSetInStaticMethod = 3;
  }
}
