package org.ucdetector.cycle;

import org.ucdetector.test.AImpl;

public class A {
  B b = new B();
  C c = new C();
  D d = new D();
  X x = new X();
  M m = new M();

  // -------------------------------------------------------------------------
  AImpl aImpl = new AImpl();

  public void getJavadoc() {
  };

  JavaDoc javaDoc = new JavaDoc();

  B getB() {
    return null;
  }

  D d2 = new D();
}
