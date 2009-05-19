package org.ucdetector.example.bugs;

public class Bug2776029FinalField extends Bug2776029FinalFieldBase {
  private int field; // Marker YES: use final

  public Bug2776029FinalField() {
    initializedBySuper = 1;
    initializedBySuperAndLocal = 1;
    field = 1;
    System.out.println(field);
  }
}