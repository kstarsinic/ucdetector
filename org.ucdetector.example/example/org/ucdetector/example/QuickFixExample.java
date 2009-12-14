package org.ucdetector.example;

/**
 * This class is used to apply/check QuickFix refactorings
 */
public class QuickFixExample {
  // ---------------------------------------------------------------------------
  // CONSTANTS
  // ---------------------------------------------------------------------------
  public static int USE_FINAL1 = 1;// Marker YES: use final,unused code

  /**
   * javadoc
   */
  public static int USE_FINAL2 = 2;// Marker YES: use final,unused code

  // comment
  public static int USE_FINAL3 = 3;// Marker YES: use final,unused code

  /*
   * comment_2
   */
  public static int USE_FINAL4 = 4;// Marker YES: use final,unused code

  public static int USE_FINAL5 = 5;// Marker YES: use final,unused code

  // ---------------------------------------------------------------------------
  // CONSTUCTORS
  // ---------------------------------------------------------------------------
  public QuickFixExample() {
    this(true);
    System.out.println(usePrivateField_1);
    System.out.println(usePrivateField_2);
    System.out.println(usePrivateField_3);
    System.out.println(usePrivateField_4);
    System.out.println(usePrivateField_5);

    usePrivate_1();
    usePrivate_2();
    usePrivate_3();
    usePrivate_4();
    usePrivate_5();
  }

  public QuickFixExample(String s) {// Marker YES: use protected
    this(Boolean.TRUE);
  }

  /**
   * javadoc
   */
  public QuickFixExample(int i) {// Marker YES: use protected
  }

  // comment
  public QuickFixExample(boolean privateOnly) {// Marker YES: use private
  }

  /*
   * comment_2
   */
  public QuickFixExample(Boolean privateOnly) {// Marker YES: use private
  }

  public QuickFixExample(char c) {
  }

  // ---------------------------------------------------------------------------
  // CLASSES DEFAULT
  // ---------------------------------------------------------------------------
  public class UseProtectedClass_1 {// Marker YES: use default
  }

  /**
   * javadoc
   */
  public class UseProtectedClass_2 {// Marker YES: use default
  }

  // comment
  public class UseProtectedClass_3 {// Marker YES: use default
  }

  /*
   * comment_2
   */
  public class UseProtectedClass_4 {// Marker YES: use default
  }

  public class UseProtectedClass_5 {// Marker YES: use default
  }

  // ---------------------------------------------------------------------------
  // FIELDS PRIVATE
  // ---------------------------------------------------------------------------
  public final int usePrivateField_1 = 0;// Marker YES: use private

  /**
   * javadoc
   */
  public final int usePrivateField_2 = 0;// Marker YES: use private

  // comment
  public final int usePrivateField_3 = 0;// Marker YES: use private

  /*
   * comment_2
   */
  public final int usePrivateField_4 = 0;// Marker YES: use private

  public final int usePrivateField_5 = 0;// Marker YES: use private

  // ---------------------------------------------------------------------------
  // FIELDS PROTECTED
  // ---------------------------------------------------------------------------
  public final int useProtectedField_1 = 0;// Marker YES: use protected

  /**
   * javadoc
   */
  public final int useProtectedField_2 = 0;// Marker YES: use protected

  // comment
  public final int useProtectedField_3 = 0;// Marker YES: use protected

  /*
   * comment_2
   */
  public final int useProtectedField_4 = 0;// Marker YES: use protected

  public final int useProtectedField_5 = 0;// Marker YES: use protected

  // ---------------------------------------------------------------------------
  // METHODS PROTECTED
  // ---------------------------------------------------------------------------
  public void useProtected_1() {// Marker YES: use protected
  }

  /**
   * javadoc
   */
  public void useProtected_2() {// Marker YES: use protected
  }

  // comment
  public void useProtected_3() {// Marker YES: use protected
  }

  /*
   * comment_2
   */
  public void useProtected_4() {// Marker YES: use protected
  }

  public void useProtected_5() {// Marker YES: use protected
  }

  // ---------------------------------------------------------------------------
  // METHODS PRIVATE
  // ---------------------------------------------------------------------------
  public void usePrivate_1() {// Marker YES: use private
  }

  /**
   * javadoc
   */
  public void usePrivate_2() {// Marker YES: use private
  }

  // comment
  public void usePrivate_3() {// Marker YES: use private
  }

  /*
   * comment_2
   */
  public void usePrivate_4() {// Marker YES: use private
  }

  public void usePrivate_5() {// Marker YES: use private
  }

  // ---------------------------------------------------------------------------
  // FIELDS UNUSED
  // ---------------------------------------------------------------------------
  final int unusedField_1 = 1;// Marker YES: unused code

  /**
   * javadoc
   */
  final int unusedField_2 = 2;// Marker YES: unused code

  // comment
  final int unusedField_3 = 3;// Marker YES: unused code

  /*
   * comment_2
   */
  final int unusedField_4 = 4;// Marker YES: unused code

  final int unusedField_5 = 5;// Marker YES: unused code

  // ---------------------------------------------------------------------------
  //[ 2721955 ] On QuickFix the direct sibling marker gets deleted too
  // ---------------------------------------------------------------------------
  public final int UNUSED_FIELD_10 = 10; // Marker YES: unused code
  public final int UNUSED_FIELD_11 = 11; // Marker YES: unused code
  public final int UNUSED_FIELD_12 = 12; // Marker YES: unused code
  public final int UNUSED_FIELD_13 = 13; // Marker YES: unused code
  public final int UNUSED_FIELD_14 = 14; // Marker YES: unused code
  public final int UNUSED_FIELD_15 = 15; // Marker YES: unused code
  public final int UNUSED_FIELD_16 = 16; // Marker YES: unused code

  // ---------------------------------------------------------------------------
  // METHODS UNUSED
  // ---------------------------------------------------------------------------

  public void unusedMethod_1() {// Marker YES: unused code
  }

  /**
   * javadoc
   */
  public void unusedMethod_2() {// Marker YES: unused code
    // comment
    System.out.println("hello ");
    // comment
  }

  // comment
  public void unusedMethod_3() {// Marker YES: unused code
    System.out.println("hello ");
    /* -- comment */
    System.out.println("hello ");
    // comment
  }

  /*
   * comment_2
   */
  public void unusedMethod_4() {// Marker YES: unused code
  }

  public void unusedMethod_5() {// Marker YES: unused code
  }

  public void unusedMethod_10() {// Marker YES: unused code
  }

  // ---------------------------------------------------------------------------
  //[ 2721955 ] On QuickFix the direct sibling marker gets deleted too
  // ---------------------------------------------------------------------------
  public void unusedMethod_11() {// Marker YES: unused code
  }

  public void unusedMethod_12() {// Marker YES: unused code
  }

  public void unusedMethod_13() {// Marker YES: unused code
  }

  public void unusedMethod_14() {// Marker YES: unused code
  }

  // ---------------------------------------------------------------------------
  // CLASSES UNUSED
  // ---------------------------------------------------------------------------

  public class UnusedClass_1 {// Marker YES: unused code
  }

  /**
   * javadoc
   */
  public class UnusedClass_2 {// Marker YES: unused code
  }

  // comment
  public class UnusedClass_3 {// Marker YES: unused code
  }

  /*
   * comment_2
   */
  public class UnusedClass_4 {// Marker YES: unused code
    {
      System.out.println("hello ");
      /* -- comment */
      System.out.println("hello ");
      // comment
    }
  }

  public class UnusedClass_5 {// Marker YES: unused code
  }
}
