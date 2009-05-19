package org.ucdetector.example.bugs;

import org.ucdetector.example.bugs.impl.Bug2539795Container;

/**
 * @see "https://sourceforge.net/tracker2/index.php?func=detail&aid=2539795&group_id=219599&atid=1046865"
 * 
 * Details:
 *
 *    with version 0.12.0, and the following 3 java files, ucdetector detect that
 *    the Field class could be set to default. But if i switch it to default,
 *    there is a compilation error in Main.foo :
 * 
 * 
 */
public class Bug2539795Main {
  public static void main(String[] args) {
    Bug2539795Container container = new Bug2539795Container();
    String s = container.field.getBar();
    System.out.println(s);
  }
}
