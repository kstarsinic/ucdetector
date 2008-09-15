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

  /** should be ignored, because of method filter *test* */
  public void testMethod() {

  }

  /** should be ignored, because of method filter *test* */
  public void myMethodTest() {

  }

  @SuppressWarnings("unused")
  private void unusedPrivateMethod() {
  }

  void unusedDefaultMethod() { // Marker YES: unused code
  }

  protected void unusedProtectedMethod() {// Marker YES: unused code
  }

  native protected void unusedNativeMethod();

  /** keyword "strictfp" should not cause problems to UCDetector */
  strictfp void unusedPublicMethod1() {// Marker YES: unused code
    /** keyword "assert" should not cause problems to UCDetector */
    assert true;
  }

  /** Arrays should not cause problems to UCDetector */
  void unusedPublicMethod2(int[] iArray, Object[][] o) {// Marker YES: unused code
  }

  /** Simple types should not cause problems to UCDetector */
  void unusedPublicMethod3(boolean b2, int i2, char c) {// Marker YES: unused code
  }

  void transientunusedPublicMethod3(Boolean b, Integer i, Character c) {// Marker YES: unused code
  }

  /** keyword "throws" should not cause problems to UCDetector */
  void unusedPublicMethod5() throws Exception {// Marker YES: unused code
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
