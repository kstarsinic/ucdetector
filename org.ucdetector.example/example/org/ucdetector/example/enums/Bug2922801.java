package org.ucdetector.example.enums;
/**
 * Bug 2922801: Quick fix eception on enum declaration
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-12-29 13:20
 * <p>
 * TEXT
 * <p>
 * @see https://sourceforge.net/tracker/?func=detail&aid=2922801&group_id=219599&atid=1046865
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/enums/Bug2922801.java?view=markup
 */

public class Bug2922801 {
  public enum MakeMePrivate {
    A, B, C, D
  }

  public static void main(String[] args) {
    System.out.println(MakeMePrivate.values());
  }
}
