package org.ucdetector.example;
/**
 * This class should show all kind of UCDetector markers 
 */
public class MixedExample {

  private int useFinal = 0; // Marker YES: use final

  // TODO 31.08.2008: Marker problems for final methods and make private
  protected void makePrivateMethod() { // Marker YES: use final
  }

  protected void makeProtectedMethod() { // Marker YES: use final
  }

  public void makeFinalMethod() { // Marker YES: use final
  }

  public static void main(String[] args) {
    System.out.println(new MixedExample().useFinal);
    new MixedExample().makePrivateMethod();
  }
}
