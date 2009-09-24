package org.ucdetector.example.classes.text;

/**
 * Full qualified class name exists in
 * /org.ucdetector.example/example/org/ucdetector/example/no_java/reference.xml
 * Detection depends on "File name patterns"
 */
public class ClassNameInXmlFile {

  /**
   * There should be no marker here!
   * <p>
   * see also [ 2373808 ] Classes found by text search should have no markers:<br>
   * https://sourceforge.net/tracker2/?func=detail&aid=2373808&group_id=219599&atid=1046865
   */
  public static final int UNUSED = -1;

  /**
   * There should be no marker here! see also [ 2373808 ] Classes found by
   * <p>
   * text search should have no markers:<br>
   * https://sourceforge.net/tracker2/?func=detail&aid=2373808&group_id=219599&atid=1046865
   */
  public void unused() {

  }
}
