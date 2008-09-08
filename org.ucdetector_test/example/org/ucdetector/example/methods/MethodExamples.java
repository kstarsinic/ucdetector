package org.ucdetector.example.methods;
/**
 * {@link MethodExamples#usedByJavaDoc()}
 */
public class MethodExamples {

  /** nothing should happen here! */
  static {
    System.out.println("static");
  }

  /** nothing should happen here! */
  {
    System.out.println("class");
  }

  // -------------------------------------------------------------------------
  // UNUSED
  // -------------------------------------------------------------------------

  /**
   *  This method is referenced by javadoc
   **/
  public void usedByJavaDoc() { // Marker YES: unused code

  }

  /** should be ignored, because of method filter (ignore case) *test* */
  public void testMethod() {

  }

  /** should be ignored, because of method filter (ignore case) *test* */
  public void myMethodTest() {

  }

  @SuppressWarnings("unused")
  private void unusedPrivateMethod() {
  }

  void unusedDefaultMethod() { // Marker YES: unused code
  }

  protected void unusedProtectedMethod() {// Marker YES: unused code
  }

  void unusedPublicMethod1() {// Marker YES: unused code
  }

  void unusedPublicMethod2() {// Marker YES: unused code
  }

  void unusedPublicMethod3() {// Marker YES: unused code
  }

  void unusedPublicMethod5() {// Marker YES: unused code
  }

  // -------------------------------------------------------------------------
  // USED
  // -------------------------------------------------------------------------
  private void usedPrivateMethod() {
  }

  void usedDefaultMethod() {
  }

  protected void usedProtectedMethod() {
  }

  public void usedPublicMethod() {
  }

  // -------------------------------------------------------------------------
  // STRANGE
  // -------------------------------------------------------------------------
  // Problems in Eclipse 3.4M5 with a method called "start"
  /**
   *  slow!
   */
  public void start() {

  }

  /**
   *  slow!
   */
  public void run() {

  }

  /**
   *  slow!
   */
  public void append() {

  }

  @Override
  public String toString() {
    return super.toString();
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------
  public static void main(String[] args) {
    MethodExamples ex = new MethodExamples();
    ex.usedPrivateMethod();
  }
}
