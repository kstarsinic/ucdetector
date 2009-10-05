package org.ucdetector.example.bugs;

public class ProtectedReferenceHolder {
  public static void main(String[] args) {
    Bug2864046 bug = new Bug2864046();
    bug.used_1();

    bug.new Protected().used_2();

    new Bug2864046_2().used_5();
    //
    System.out.println(Bug2845133.class);
  }
}
