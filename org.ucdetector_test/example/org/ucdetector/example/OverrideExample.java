package org.ucdetector.example;
/**
 *
 */
public class OverrideExample {
  public static final String UNUSED = null; // Marker YES: unused code

  public static void main(String[] args) {
    OverrideExample overrideExample = new OverrideExample();
    overrideExample.methodToOverride();
    overrideExample.methodToOverrideProtected();
    overrideExample.methodToOverridePrivate();
    overrideExample.makePrivate();
  }

  // NO marker here, because method is overridden!
  public void methodToOverride() {
  }

  // NO marker here, because method is overridden!
  protected void methodToOverrideProtected() {
  }

  private void methodToOverridePrivate() {
  }

  // TODO: 2 marker OK?
  public void makePrivate() { // Marker YES: use final
  }

  // TODO: 2 Marker OK?
  public void unused() { // Marker YES: use final
  }
}
