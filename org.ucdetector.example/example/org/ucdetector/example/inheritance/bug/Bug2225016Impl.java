package org.ucdetector.example.inheritance.bug;

import org.ucdetector.example.inheritance.Bug2225016;

/**
 *
 */
public class Bug2225016Impl extends Bug2225016 {

	@Override
	protected void methodIsOverriden() {
	}

	public void unused() { // Marker YES: unused code

	}
}
