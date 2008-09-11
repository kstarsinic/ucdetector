package org.ucdetector.example.inheritance;
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

  public final void makePrivate() { // Marker YES: use private
  }

  public final void unused() { // Marker YES: unused code
  }
}
