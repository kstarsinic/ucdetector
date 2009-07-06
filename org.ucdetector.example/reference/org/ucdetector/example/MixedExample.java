package org.ucdetector.example;

/**
 * This class should show all kind of UCDetector markers and is used for
 * UCDetector screenhots for help.
 */

public class MixedExample {
  public final String localField = "local";
	
  public String readOnlyField = "local";
	
  public static final int UNUSED = 0;
	
  public final void localMethod() {}

  public static void usedOnceMethod() {}
	
  public static void usedOnlyByTests() {}
	
  public void helper() {}
  
  public class UnusedClass{}

  public int unusedIgnore = 0; // NO_UCD

	@SuppressWarnings("unused")
	private static final String UNUSED_PRIVAT = "UNUSED";

	public static void main(String[] args) {
		System.out.println(new MixedExample().localField);
		System.out.println(new MixedExample().localField);
		new MixedExample().localMethod();
		new MixedExample().localMethod();
	}
}
