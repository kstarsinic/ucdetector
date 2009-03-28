package org.ucdetector.example;

public class ProtectedReferenceHolder {
  public static void main(String[] args) {
    System.out.println(new MixedExample());
    new MixedExample().makeProtectedMethod();

    QuickFixExample quickFixExample = new QuickFixExample();
    int i = quickFixExample.useProtectedField_1;
    i = quickFixExample.useProtectedField_2;
    i = quickFixExample.useProtectedField_3;
    i = quickFixExample.useProtectedField_4;
    i = quickFixExample.useProtectedField_5;
    System.out.println(i);

    Object class_1 = quickFixExample.new UseProtectedClass_1();
    class_1 = quickFixExample.new UseProtectedClass_2();
    class_1 = quickFixExample.new UseProtectedClass_3();
    class_1 = quickFixExample.new UseProtectedClass_4();
    class_1 = quickFixExample.new UseProtectedClass_5();
    System.out.println(class_1);

    quickFixExample.useProtected_1();
    quickFixExample.useProtected_2();
    quickFixExample.useProtected_3();
    quickFixExample.useProtected_4();
    quickFixExample.useProtected_5();

  }
}
