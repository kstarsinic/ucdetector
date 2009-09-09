package org.ucdetector.example.classes;

public class LocalClassExample {

  public static void main(String[] args) {
    LocalClassExample localClassExample = new LocalClassExample();
    localClassExample.used();
    localClassExample.new MemberClass().toString();
  }

  private void used() {
	 // private is a forbidden keyword here!
    class LocalClass {

    }
    new LocalClass().getClass();
  }

  class MemberClass {// Marker YES: use private

  }
}
