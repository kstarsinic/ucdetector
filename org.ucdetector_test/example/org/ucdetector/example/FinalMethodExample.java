package org.ucdetector.example;
/**
 *
 */
public class FinalMethodExample {

  public static void main(String[] args) {
    FinalMethodExample finalMethodExample = new FinalMethodExample();
    finalMethodExample.methodOverridden();
    finalMethodExample.methodNotOverridden();
    finalMethodExample.privateMethod();
  }

  /** NO final marker here, because method is overridden! */
  public void methodOverridden() {
  }

  public void methodNotOverridden() { // Marker YES: use final
  }

  /** NO final marker here, because method is final! */
  public final void finalMethod() {
  }

  /** NO final marker here, because method is static! */
  public static void staticMethod() {
  }

  /** NO final marker here, because method is private! */
  private void privateMethod() {
  }
}
