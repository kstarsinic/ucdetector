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

  public int usePrivateField_1 = 0;// Marker YES

  public int usePrivateField_2 = 0;// Marker YES

  public int usePrivateField_3 = 0;// Marker YES

  public int usePrivateField_4 = 0;// Marker YES

  public int usePrivateField_5 = 0;// Marker YES
  // -------------------------------------------------------------------------
  int unusedField_1 = 0;// Marker YES

  int unusedField_2 = 0;// Marker YES

  int unusedField_3 = 0;// Marker YES

  int unusedField_4 = 0;// Marker YES

  int unusedField_5 = 0;// Marker YES

  // -------------------------------------------------------------------------

  void unusedPublicMethod_1() {// Marker YES
  }

  void unusedPublicMethod_2() {// Marker YES
  }

  void unusedPublicMethod_3() {// Marker YES
  }

  void unusedPublicMethod_4() {// Marker YES
  }

  void unusedPublicMethod_5() {// Marker YES
  }

}
