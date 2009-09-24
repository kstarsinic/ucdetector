package org.ucdetector.example.bugs;

/**
 * <b>Methods only called from inner class could be private - ID: 2743908</b>
 * <p>
 * Hi, i think ucdetector misses some methods which could be made private. If
 * the method is only called from an inner class, the method could be made
 * private.
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-04-08 11:44
 */
public class Bug2743908_2804064 {
  // [ 2804064 ] Access to enclosing type - make 2743908 configurable
  public int usedOnlyFromInnerClassInt = 0; // Marker YES: use private

  // [ 2804064 ] Access to enclosing type - make 2743908 configurable
  public void usedOnlyFromInnerClass() { // Marker YES: use private

  }

  /**
   * primary type=Bug2743908_2804064<br>
   * isLocal=false<br>
   * isMember=false<br>
   */
  class MyMemberClass { // Marker YES: use private
    class MyMemberClass2 { // Marker YES: use private
      public void foo2() { // Marker YES: use private
        usedOnlyFromInnerClass();
      }
    }

    public void foo() { // Marker YES: use private
      usedOnlyFromInnerClass();
      System.out.println(usedOnlyFromInnerClassInt++);
    }
  }

  public static void main(String[] args) {
    Bug2743908_2804064 bug = new Bug2743908_2804064();
    MyMemberClass myClass = bug.new MyMemberClass();
    myClass.foo();
    new MyLocalClass().foo();
    new A();
    bug.new MyMemberClass().new MyMemberClass2().foo2();
  }

  public static class A { // Marker YES: use private
  };
}

/**
 * primary type=Bug2743908_2804064<br>
 * isLocal=false<br>
 * isMember=false<br>
 */
class MyLocalClass {
	// See Bug2864046: no more marker here!
	public void foo() {
  };
}

class MyLocalClassUnused { // Marker YES: unused code
}