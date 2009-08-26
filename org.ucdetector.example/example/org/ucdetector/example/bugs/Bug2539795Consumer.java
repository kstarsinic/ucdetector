package org.ucdetector.example.bugs;

import org.ucdetector.example.bugs.impl.Bug2539795Foo;

/**
 * See classes: Bug2539795Consumer, Bug2539795Bar, Bug2539795Foo<br>
 * Changing Bug2539795Bar to private (suggested by UCDetector) causes a compile error in Bug2539795Consumer
 * <p>
 * https://sourceforge.net/tracker/?func=detail&aid=2539795&group_id=219599&atid=1046865
 */
public class Bug2539795Consumer { // NO_UCD -- main method

  public static void main(String[] args) {

    // NO compile error here, when changing visibility Bug2539795Bar to default
    new Bug2539795Foo().getBar();

    // COMPILE ERROR HERE   , when changing visibility Bug2539795Bar to default
    new Bug2539795Foo().getBar().toString();
  }
}