package org.ucdetector.example;
/**
 * This class should show all kind of UCDetector markers 
 */
public class MixedExample {

  private int useFinal = 0; // Marker YES: use final

  public static final String UNUSED = "UNUSED"; // Marker YES: unused code

  public final void makePrivateMethod() { // Marker YES: use private
  }

  public final void makeProtectedMethod() { // Marker YES: use protected
  }

  public void makeFinalMethod() { // Marker YES: use final
  }

  public static void main(String[] args) {
    System.out.println(new MixedExample().useFinal);
    new MixedExample().makePrivateMethod();
  }
}
