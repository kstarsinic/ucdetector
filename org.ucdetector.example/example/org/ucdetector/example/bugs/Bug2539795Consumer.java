package org.ucdetector.example.bugs;

import org.ucdetector.example.bugs.impl.Bug2539795Foo;

public class Bug2539795Consumer { // NO_UCD -- main method

	public static void main(String[] args) {
		// With the UCDetector suggestions, the following error is issued:
		// The type Bar is not visible.
		Bug2539795Foo foo = new Bug2539795Foo();
		foo.getBar();
	}
}