package org.ucdetector.example;
/**
 * This class is used to check QuickFix refactorings
 */
public class QuickFixExample {

  public QuickFixExample() {
    System.out.println(usePrivateField_1);
    System.out.println(usePrivateField_2);
    System.out.println(usePrivateField_3);
    System.out.println(usePrivateField_4);
    System.out.println(usePrivateField_5);
  }

  public final int usePrivateField_1 = 0;// Marker YES: use private

  public final int usePrivateField_2 = 0;// Marker YES: use private

  public final int usePrivateField_3 = 0;// Marker YES: use private

  public final int usePrivateField_4 = 0;// Marker YES: use private

  public final int usePrivateField_5 = 0;// Marker YES: use private
  // -------------------------------------------------------------------------
  final int unusedField_1 = 0;// Marker YES: unused code

  final int unusedField_2 = 0;// Marker YES: unused code

  final int unusedField_3 = 0;// Marker YES: unused code

  final int unusedField_4 = 0;// Marker YES: unused code

  final int unusedField_5 = 0;// Marker YES: unused code

  // -------------------------------------------------------------------------

  void unusedPublicMethod_1() {// Marker YES: unused code
  }

  void unusedPublicMethod_2() {// Marker YES: unused code
  }

  void unusedPublicMethod_3() {// Marker YES: unused code
  }

  void unusedPublicMethod_4() {// Marker YES: unused code
  }

  void unusedPublicMethod_5() {// Marker YES: unused code
  }

}
