package org.ucdetector.example.inheritance;

import java.util.ArrayList;

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
    new MyList().toString();
  }

  // [ 2804064 ] Access to enclosing type - make 2743908 configurable
  static class MyList extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    @Override
    public int size() {
      return super.size();
    }
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
