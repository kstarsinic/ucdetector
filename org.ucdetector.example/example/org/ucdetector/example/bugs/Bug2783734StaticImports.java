package org.ucdetector.example.bugs;
/**
 * Static imports are not recognized - ID: 2783734
 * <p>
 * We have a class which only defines some static final's (icons). These
 * constants are import by the static import mechanism.
 * Unfortunately, UCDetector detects no references to these constants and
 * says that the class itself has no references (which just is not true).
 * <p>
 * @see "http://sourceforge.net/tracker/?func=detail&atid=1046865&aid=2783734&group_id=219599"
 */
public class Bug2783734StaticImports {
  public static final int STATIC_IMPORT_USED = 1;

  public static final String STATIC_IMPORT_UNUSED = "2"; // Marker YES: unused code

  public static void staticImportMethod() {
  }
}
