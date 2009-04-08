package org.ucdetector.example.bugs;
/**
 * <b>Don't check for constructors called only 1 time - ID: 2743872</b>
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-04-08 11:33
 * <p>
 * Hi, constructors should not be included in methods with a maximum of 1
 * reference. The reason is that even if you need to construct an object only
 * 1 time, you must do it anyway, so the warning cannot be fixed.
 * <p>
 * @see "http://sourceforge.net/tracker/?func=detail&atid=1046868&aid=2743872&group_id=219599"
 *
 */
public class Bug2743872 {

  public Bug2743872() {

  }

  public Bug2743872(int i) {

  }
}
