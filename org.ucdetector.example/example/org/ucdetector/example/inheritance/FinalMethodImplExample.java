package org.ucdetector.example.inheritance;
/**
 *
 */
public class FinalMethodImplExample extends FinalMethodExample {

  // NO final marker here, because method overrides an other method!
  @Override
  public void methodOverridden() {
  }

  // NO final marker here, because class has no sub classes
  public void noOverrideNoOverridden() {
  }
}
