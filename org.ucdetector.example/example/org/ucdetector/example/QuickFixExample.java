package org.ucdetector.example;
/**
 * This class is used to check QuickFix refactorings
 */
public class QuickFixExample {

  public static int USE_FINAL = 1;

  public static int USE_FINAL2 = 1;

  public static int USE_FINAL3 = 1;

  public static int USE_FINAL4 = 1;

  // -------------------------------------------------------------------------

  public class UseProtectedClass_1 {
  }

  public class UseProtectedClass_2 {
  }

  public class UseProtectedClass_3 {
  }

  public class UseProtectedClass_4 {
  }

  class UseProtectedClass_5 {
  }

  public final int usePrivateField_1 = 0;// Marker YES: use private

  public final int usePrivateField_2 = 0;// Marker YES: use private

  public final int usePrivateField_3 = 0;// Marker YES: use private

  public final int usePrivateField_4 = 0;// Marker YES: use private

  public final int usePrivateField_5 = 0;// Marker YES: use private

  // -------------------------------------------------------------------------

  public final int useProtectedField_1 = 0;// Marker YES: use protected

  public final int useProtectedField_2 = 0;// Marker YES: use protected

  public final int useProtectedField_3 = 0;// Marker YES: use protected

  public final int useProtectedField_4 = 0;// Marker YES: use protected

  public final int useProtectedField_5 = 0;// Marker YES: use protected

  // -------------------------------------------------------------------------
  public void useProtected_1() {
  };

  public void useProtected_2() {
  };

  public void useProtected_3() {
  };

  public void useProtected_4() {
  };

  public void useProtected_5() {
  };

  // -------------------------------------------------------------------------
  public void usePrivate_1() {
  }

  public void usePrivate_2() {
  }

  public void usePrivate_3() {
  }

  public void usePrivate_4() {
  }

  public void usePrivate_5() {
  }

  // -------------------------------------------------------------------------
  final int unusedField_1 = 0;// Marker YES: unused code

  final int unusedField_2 = 0;// Marker YES: unused code

  final int unusedField_3 = 0;// Marker YES: unused code

  final int unusedField_5 = 0;// Marker YES: unused code

  // -------------------------------------------------------------------------

  public void unusedPublicMethod_2() {// Marker YES: unused code
  }

  public void unusedPublicMethod_5() {// Marker YES: unused code
  }

  public QuickFixExample() {
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
}
