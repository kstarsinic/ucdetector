package org.ucdetector.example.inheritance.bug;
/**
 * https://sourceforge.net/tracker/?func=detail&atid=1046865&aid=2105379&group_id=219599
 * In the following example, "new B().bar()" will call B.foo().
 * But if A.foo() is marked private, it will be called instead of B.foo().
 */
class InOneFile {
  public static void main(String[] args) {
    new InOneFile();
    new B2();
    new C2();

  }

  public void foo() {
  }

  public void barA() { // Marker YES: unused code
    foo();
  }
}

class B2 extends InOneFile {
  public void foo() {
  }

  public void barB() { // Marker YES: unused code
    foo();
  }
}

class C2 extends B2 {
  public void foo() {

  }

  public void barC() { // Marker YES: unused code
    foo();
  }
}
