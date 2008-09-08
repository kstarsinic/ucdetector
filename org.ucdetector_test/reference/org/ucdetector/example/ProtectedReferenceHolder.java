package org.ucdetector.example;

public class ProtectedReferenceHolder {
  public static void main(String[] args) {
    System.out.println(new MixedExample());
    new MixedExample().makeProtectedMethod();
  }
}
