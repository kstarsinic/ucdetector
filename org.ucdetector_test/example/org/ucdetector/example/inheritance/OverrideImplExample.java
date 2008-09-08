package org.ucdetector.example.inheritance;
/**
 *
 */
public class OverrideImplExample extends OverrideExample {
  public static void main(String[] args) {
    OverrideImplExample overrideImplExample = new OverrideImplExample();
    overrideImplExample.methodToOverridePrivate();
  }

  // @Override
  public void methodToOverride() {
  }

  public void methodToOverrideProtected() {
  }

  private void methodToOverridePrivate() {
  }

  public void makePrivateImpl() { // Marker YES: unused code
  }
}
