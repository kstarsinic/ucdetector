package org.ucdetector.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Java5Example {
  /**
   * The vararg parameter should not cause problems to UCDetector
   */
  public void varargExample(String s, int... vararg) {

  }

  /**
   * The vararg parameter should not cause problems to UCDetector
   */
  public void varargExampleUnused(String s, int... vararg) { // Marker YES

  }

  /**
   * The generic parameter list should not cause problems to UCDetector
   */
  public void genericExample(List<Map<Integer, String>> list) {

  }

  /**
   * The generic parameter list should not cause problems to UCDetector
   */
  public void genericExampleUnused(List<Map<Integer, String>> list) { // Marker YES

  }

  @SuppressWarnings("unchecked")
  public static List asList(Object[] array) { // Marker YES
    return Arrays.asList(array);
  }

  @Deprecated
  public void doNotUse() {

  }

  @AnnotationExample(parameterExmaple = "test")
  public void annotatedMethod2() {

  }

}
