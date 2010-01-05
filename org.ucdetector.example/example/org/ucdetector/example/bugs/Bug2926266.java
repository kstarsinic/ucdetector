package org.ucdetector.example.bugs;
/**
 * Bug 2926266: Method cannot be private if called on subclass
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2010-01-05 14:46
 * <p>
 * TEXT
 * <p>
 * @see https://sourceforge.net/tracker/?func=detail&aid=2926266&group_id=219599&atid=1046865
 * <p>
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/bugs/Bug2926266.java?view=markup
 */
public class Bug2926266 {
  void foo1() { // NO_UCD
    bar();
  }

  void foo2() { // NO_UCD
    bar();
  }

  // Bug 2926266: Method cannot be private if called on subclass
  void bar() {
    subField.bar();
  }

  private final Sub subField = new Sub();

  private class Sub extends Bug2926266 {
  }
}