package org.ucdetector.example;
/**
 * This class should show all kind of UCDetector markers 
 */
public class MixedExample {

  private int useFinal = 0;

  // TODO 31.08.2008: Marker problems for final methods and make private
  protected void makePrivateMethod() {
  }

  protected void makeProtectedMethod() {
  }

  public void makeFinalMethod() {
  }

  public static void main(String[] args) {
    System.out.println(new MixedExample().useFinal);
    new MixedExample().makePrivateMethod();
  }
}
