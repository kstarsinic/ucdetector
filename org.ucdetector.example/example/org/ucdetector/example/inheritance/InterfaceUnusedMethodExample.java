package org.ucdetector.example.inheritance;

/**
 * Can't make interface methods final - ID: 2826205
 * <p>
 * UCDetector suggests to make interface methods which are not overridden
 * 'final'. This causes a compile error: <br>
 * Illegal modifier for the interface method MyInterface.notOverridden();<br>
 * only public & abstract are permitted<br>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-07-23 20:40<br>
 * 
 * <p>
 * Unused interface methods are not detected - ID: 2826216
 * <p>
 * Hi I was working through some very old code that has some very dodgy
 * programming practices and I found an edge case that UC-Detector did not
 * detect. Thanks for the good work, you've helped me clean up a ton of dead
 * code! Regards
 * 
 */
public interface InterfaceUnusedMethodExample {
  /**
   * No final marker here!
   * [2826205] Can't make interface methods final
   */
  void used();

  /**
   *  No final marker here!
   *  [2826205] Can't make interface methods final
   */
  void unused(); // Marker YES: unused code

  /**
   * [ 2826216 ] Unused interface methods are not detected<br>
   * [ 2153699 ] Find unused interface methods<br>
   */
  void implementedButNotUsed(); // Marker YES: unused code
}