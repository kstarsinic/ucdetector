package org.ucdetector.example.bugs;

/**
 * <b>Methods only called from inner class could be private - ID: 2743908</b>
 * <p>
 * Hi, i think ucdetector misses some methods which could be made private. If
 * the method is only called from an inner class, the method could be made
 * private.
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-04-08 11:44
 */
public class Bug2743908 {

  // TODO Bug 2743908
  public void usedOnlyFromInnerClass() { // Marker YES: use private

  }

  private class MyClass {
    private void test() {
      usedOnlyFromInnerClass();
    }
  }

  public static void main(String[] args) {
    Bug2743908 bug = new Bug2743908();
    MyClass myClass = bug.new MyClass();
    myClass.test();
  }
}
