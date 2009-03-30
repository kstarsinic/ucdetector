package org.ucdetector.example.bugs.impl;

/**
 * Bug: Bug2539795Field may not be default, because Bug2539795Field.getBar() is
 * used outside of this package!
 */
// TODO: Fix bug (no marker here, because public method is used)
public class Bug2539795Field { // Marker YES: use default
	public String getBar() {
		return "Bar";
	}
}
