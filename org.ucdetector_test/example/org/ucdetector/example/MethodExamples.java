package org.ucdetector.example;

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
  @SuppressWarnings("unused")
  private void unusedPrivateMethod() {
  }

  void unusedDefaultMethod() { // Marker YES
  }

  protected void unusedProtectedMethod() {// Marker YES
  }

  void unusedPublicMethod1() {// Marker YES
  }

  void unusedPublicMethod2() {// Marker YES
  }

  void unusedPublicMethod3() {// Marker YES
  }

  void unusedPublicMethod5() {// Marker YES
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
