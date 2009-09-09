/**
 * Markers in default package should not cause UCDetector problems!
 */
public class ClassInDefaultPackageUnusedExample { // Marker YES: unused code
	public void foo() {
		new ClassInDefaultPackageUsedExample();
	}
}
