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
  // Class name to search: org.ucdetector.example.enums.Bug2922801.MakeMePrivate
  public enum MakeMePrivate {
    A, B, C, D
  }

  /**
   * Class name to search: org.ucdetector.example.enums.Bug2922801.MakeMePrivate2
   */
  protected static enum MakeMePrivate2 {
    E, F, G
  }

  /*
   * Class name to search: org.ucdetector.example.enums.Bug2922801.MakeMePrivate3
   */
  static enum MakeMePrivate3 {
    H, //
    I, //
    J, //
  }

  public static void main(String[] args) {
    System.out.println(MakeMePrivate.values());
    System.out.println(MakeMePrivate2.values());
    System.out.println(MakeMePrivate3.valueOf("Foo"));
  }
}
