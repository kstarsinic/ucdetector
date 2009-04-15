package org.ucdetector.example.bugs;

import org.ucdetector.example.bugs.impl.Bug2539795Foo;

public class Bug2539795Consumer { // NO_UCD -- main method

  public static void main(String[] args) {
    System.out.println("new Bug2539795Foo().getBar(): "
        + new Bug2539795Foo().getBar());
    new Bug2539795Foo().getBar();
    new Bug2539795Foo().getBar().toString();
  }
}