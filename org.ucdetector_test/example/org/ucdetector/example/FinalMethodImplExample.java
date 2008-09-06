package org.ucdetector.example;
/**
 *
 */
public class FinalMethodImplExample extends FinalMethodExample {

    // NO final marker here, because method overrides an other method!
    public void methodOverridden() {
    }

    // NO final marker here, because class has no sub classes
    public void noOverrideNoOverridden() {
    }
}
