package org.ucdetector.example;

public class ProtectedReferenceHolder extends MixedExample {
  public static void main(String[] args) {
    FieldExamples fieldExamples = new FieldExamples();
    System.out.println(fieldExamples.defaultUsedField);
    System.out.println(fieldExamples.protectedUsedField);
    // ---------------------------------------------------------------------
    MethodExamples methodExamples = new MethodExamples();
    methodExamples.usedDefaultMethod();
    methodExamples.usedProtectedMethod();
    // ---------------------------------------------------------------------
    System.out.println(EnumExample.CHANGE_TO_PROTECTED);
    // ---------------------------------------------------------------------
    System.out.println(new MixedExample());
    new MixedExample().makeProtectedMethod();

    // ---------------------------------------------------------------------

    // ---------------------------------------------------------------------

    // ---------------------------------------------------------------------

    // ---------------------------------------------------------------------
  }
}
