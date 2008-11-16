package org.ucdetector.example.inheritance;
/**
 * [ 2139142 ] Classes used through interface<p>
 * If there is class that implements some interface and is used only through
 * that interface, then that class is marked as having 0 references. Which is
 * formally true, but not very useful for the purpose of finding unused code
 * :) Is it possible to somehow handle that situation? May be an additional
 * option: "check for interfaces"?
 * <p>
 * Date: 2008-10-01 22:45<br>
 * Sender: spj<br>
 * This is an interesting problem.<br>
 * <ul>
 * <li>How do you create an instance of this class? Do you use
 *   Class.forName("org.test.ClassName")?
 *   Solution: Use Class.forName(org.test.ClassName.getName())</li>
 * <li>Is the class name declared in a xml file?
 *   Solution: Use option "search class names in text files"</li>
 * <li>What about using a comment: public class ClassName extends MyInterface { // NO_UCD</li>
 * </ul>
 * How could discover a new developer in your team, that this class is used?
 * I think searching for references does not work.
 * @see https://sourceforge.net/tracker/index.php?func=detail&aid=2139142&group_id=219599&atid=1046865
 */
public class Bug2139142Class implements Bug2139142Interface { // Marker YES: unused code

}
