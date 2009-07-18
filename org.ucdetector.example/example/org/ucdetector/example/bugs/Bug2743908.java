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
public class Bug2743908 {

  // [ 2804064 ] Access to enclosing type - make 2743908 configurable
	public void usedOnlyFromInnerClass() { // Marker YES: use protected

	}

	class MyMemberClass {
		void test() {
			usedOnlyFromInnerClass();
		}
	}

	public static void main(String[] args) {
		Bug2743908 bug = new Bug2743908();
		MyMemberClass myClass = bug.new MyMemberClass();
		myClass.test();
		new MyLocalClass();
	}
}

class MyLocalClass {
}

class MyLocalClassUnused { // Marker YES: unused code
}