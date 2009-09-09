package org.ucdetector.example.bugs;

/**
 * Bug 2844899: protected instead of private
 * <p>
 * Hi, iv'e found an example where ucdetector propose protected even if it could
 * propose private. Here are the 2 java files to test:
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-08-26 14:18
 * <p>
 * see"https://sourceforge.net/tracker/?func=detail&atid=1046865&aid=2844899&group_id=219599"
 * <p>
 * See also bug: [ 2804064 ] 
 * See also bug: [ 2743908 ] 
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/bugs/Bug2844899_FieldFromInnerClass.java?view=markup
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/bugs/Bug2844899_Use.java?view=markup
 * <p>
 */
public class Bug2844899_FieldFromInnerClass {
  /**
   * Changing i to private causes a warning:<br>
   * "Read access to enclosing field Bug2844899_FieldFromInnerClass.i is emulated by a synthetic accessor"
   */
  public int i = 0;// Marker YES: use private

  void foo() {// Marker YES: unused code
    i = i + 5;
    Object o = new Object() {
      @Override
      public int hashCode() {
        return i + 1;
      }
    };
    System.out.print(o);
  }
}
