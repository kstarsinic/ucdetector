package org.ucdetector.example.impl;

import org.ucdetector.example.ReferencedByTestsExample;

public class JUnitReferenceHolder {
	private ReferencedByTestsExample example = new ReferencedByTestsExample();

	/**
	 * This IS a JUnit test method
	 */
	public void testHoldReference() {
		example.referencedByTestMethod();
	}

	/**
	 * This is NOT a JUnit test method
	 */
	public static void testHoldReferenceStatic() {
		new ReferencedByTestsExample().referencedByWrongTestMethodStatic();
	}

	/**
	 * This is NOT a JUnit test method
	 */
	public void testHoldReference(int i) {
		example.referencedByWrongTestMethod();
	}
}
