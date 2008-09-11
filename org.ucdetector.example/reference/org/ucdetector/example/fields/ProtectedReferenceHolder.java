package org.ucdetector.example.fields;

public class ProtectedReferenceHolder {
  public static void main(String[] args) {
    FieldExamples fieldExamples = new FieldExamples();
    System.out.println(fieldExamples.defaultUsedField);
    System.out.println(fieldExamples.protectedUsedField);
  }
}
