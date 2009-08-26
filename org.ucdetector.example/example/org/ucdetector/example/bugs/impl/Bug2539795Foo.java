package org.ucdetector.example.bugs.impl;

/**
 * See classes: Bug2539795Consumer, Bug2539795Bar, Bug2539795Foo<br>
 * Changing Bug2539795Bar to private (suggested by UCDetector) causes a compile error in Bug2539795Consumer
 * <p>
 * https://sourceforge.net/tracker/?func=detail&aid=2539795&group_id=219599&atid=1046865
 */
public class Bug2539795Foo {
  public Bug2539795Bar getBar() {
    return new Bug2539795Bar();
  }
}