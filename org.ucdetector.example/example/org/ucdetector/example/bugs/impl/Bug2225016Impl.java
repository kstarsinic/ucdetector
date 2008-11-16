package org.ucdetector.example.bugs.impl;

import org.ucdetector.example.bugs.Bug2225016;

/**
 *
 */
public class Bug2225016Impl extends Bug2225016 {

  @Override
  protected void methodIsOverriden() {
  }

  public void unused() { // Marker YES: unused code

  }
}
