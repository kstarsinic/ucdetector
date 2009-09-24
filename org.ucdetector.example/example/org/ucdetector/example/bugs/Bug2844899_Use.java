package org.ucdetector.example.bugs;

/**
 * Bug 2844899: protected instead of private
 */
public class Bug2844899_Use {

  void foo() {// Marker YES: unused code
    Bug2844899_FieldFromInnerClass o = new Bug2844899_FieldFromInnerClass();
    System.out.print(o);
  }
}
