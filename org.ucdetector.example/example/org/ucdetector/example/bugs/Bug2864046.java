package org.ucdetector.example.bugs;

/**
 * Bug 2864046: public methods of non-public classes
 * 
 * If I have a public class with a public method, UCDetector tells me if the
 * visibility of the method can be reduced. That's good.
 * 
 * But if the class is protected or default visibility, I don't really care if
 * the method is public or protected. Is there a way to suppress those warnings
 * but keep the other ones?
 */
class Bug2864046 {
  // no "use protected" marker here, because parent class is already "default"
  public void used_1() {

  }

  public void used_6() { // Marker YES: use private

  }

  public class Protected {
    public void used_2() {

    }
  }

  private class Private {
    void used_3() {

    }
  }

  static private class Static {
    public void used_4() {

    }
  }

  public static void main(String[] args) {
    Bug2864046 bug = new Bug2864046();
    bug.used_6();
    bug.new Private().used_3();
    bug.new Protected().used_2();

    new Bug2864046.Static().used_4();
  }

  public void unused() {// Marker YES: unused code
  }
}

class Bug2864046_2 {
  public void used_5() {

  }
}
