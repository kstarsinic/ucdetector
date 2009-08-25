package org.ucdetector.example;

import org.ucdetector.util.UsedBy;

public class IgnoreAnnotationExample {

	// UsedBy
	// -------------------------------------------------------------------
	@UsedBy("reflection")
	public int unused = 0;

	@org.ucdetector.util.UsedBy("library")
	public void unused() {
	};

	// Filter
	// -------------------------------------------------------------------
	@FilterMeAnnotation()
	public int unused2 = 0;

	@org.ucdetector.example.FilterMeAnnotation()
	public void unused2() {
	};

	// Other -------------------------------------------------------------------
	// public int unused3 = 0;
	//  
	// public void unused3(){};

}
