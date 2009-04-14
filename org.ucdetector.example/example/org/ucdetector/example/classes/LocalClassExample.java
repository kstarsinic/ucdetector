package org.ucdetector.example.classes;

public class LocalClassExample {

  public static void main(String[] args) {
    new LocalClassExample().used();
  }

  private void used() {
    class LocalClass {

    }
    new LocalClass().getClass();
  }
}
