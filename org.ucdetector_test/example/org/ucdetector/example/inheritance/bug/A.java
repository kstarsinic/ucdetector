package org.ucdetector.example.inheritance.bug;
/**
 * https://sourceforge.net/tracker/?func=detail&atid=1046865&aid=2105379&group_id=219599
 * In the following example, "new B().bar()" will call B.foo().
 * But if A.foo() is marked private, it will be called instead of B.foo().
 */
public class A {
  public void foo() {
  }

  public void barA() { // Marker YES: unused code
    foo();
  }
}
