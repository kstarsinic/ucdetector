package org.ucdetector.example.bugs;
/**
 * [ 2105379 ] Must check overloading before changing access
 * <p>
 * https://sourceforge.net/tracker/?func=detail&atid=1046865&aid=2105379&group_id=219599
 * <p>
 * In the following example, "new B().bar()" will call B.foo().
 * But if A.foo() is marked private, it will be called instead of B.foo().
 * <p>
 * Bug closed: User problem, not UCDetector problem!
 */
class Bug2105379 {
  public static void main(String[] args) {
    new Bug2105379();
    new B2();
    new C2();

  }

  public void foo() {
  }

  public void barA() { // Marker YES: unused code, use final
    foo();
  }
}

class B2 extends Bug2105379 {
  @Override
  public void foo() {
  }

  public void barB() { // Marker YES: unused code, use final
    foo();
  }
}

class C2 extends B2 {
  @Override
  public void foo() {

  }

  public void barC() { // Marker YES: unused code
    foo();
  }
}
