package org.ucdetector.example.bugs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Bug 2865051: public methods called only from inner class
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-09-23 15:03
 * <p>
 * UCDetector 1.2 does not tell me that the method "bar()" in the following
 * example could me made private:
 * <p>
 * Same as bug: 2804064, 2743908: Already fixed in svn
 * <p>
 * @see https://sourceforge.net/tracker/?func=detail&aid=2865051&group_id=219599&atid=1046865
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/bugs/Bug2865051.java&view=markup
 */
public class Bug2865051 {
  public void foo() { // Marker YES: use protected
    new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bar();
      }
    };
  }

  public void bar() {// Marker YES: use private
  }
}

class Use { // Marker YES: unused code
  public void foo() {
    new Bug2865051().foo();
  }
}