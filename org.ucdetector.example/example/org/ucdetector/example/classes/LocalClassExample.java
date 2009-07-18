package org.ucdetector.example.classes;

public class LocalClassExample {

  public static void main(String[] args) {
    LocalClassExample localClassExample = new LocalClassExample();
    localClassExample.used();
    localClassExample.new MemberClass().toString();
  }

  private void used() {
    class LocalClass {

    }
    new LocalClass().getClass();
  }

  class MemberClass {

  }
}
