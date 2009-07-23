package org.ucdetector.example.inheritance;

/**
 * Can't make interface methods final - ID: 2826205<br>
 * UCDetector suggests to make interface methods which are not overridden
 * 'final'. This causes a compile error: <br>
 * Illegal modifier for the interface method MyInterface.notOverridden();<br>
 * only public & abstract are permitted<br>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-07-23 20:40<br>
 */
public interface InterfaceUnusedMethodExample {
	// No marker here!
	// [2826205] Can't make interface methods final
	void used();

	// No marker here!
	// [2826205] Can't make interface methods final
	void unused();

	void implementedButNotUsed();
}