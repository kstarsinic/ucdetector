package org.ucdetector.example.bugs;

/**
 * Bug 2864967: References for recursive methods
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-09-23 12:05
 * <p>
 * UCDetector does not tag the recursive method has having 1 reference because
 * of the recursive call. Could you "not count" recursive call for the
 * "1 reference" marker ?
 * <p>
 * @see https://sourceforge.net/tracker/?func=detail&aid=2864967&group_id=219599&atid=1046865
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/bugs/Bug2864967.java&view=markup
 */
public class Bug2864967 {
  public Bug2864967() {
    recursive(5);
    recursivePublic(5);
  }

  /**
   * This method should have a marker: Unused code, because it is only used by
   * itself!
   */
  public void recursiveUnused(int n) { // Marker YES: unused code
    if (n > 0) {
      recursiveUnused(n - 1);
    }
  }

  /**
   * There are no markers here, because method is private
   */
  private void recursive(int n) {
    if (n > 0) {
      recursive(n - 1);
    }
  }

  /**
   * 2 Markers here:
   * <ul>
   * <li>Method "Bug2864967.recursivePublic(int)" has 1 references - <b>the
   * recursive call is not counted!</b></li>
   * <li>Change visibility of Method "Bug2864967.recursivePublic(int)" to
   * private</li>
   * <ul>
   */
  public void recursivePublic(int n) {// Marker YES: use private
    if (n > 0) {
      recursivePublic(n - 1);
    }
  }
}
