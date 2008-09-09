package org.ucdetector.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** new in java 5: static imports! */
import static java.lang.Math.PI;

import org.ucdetector.example.classes.AnnotationExample;

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
  public void varargExampleUnused(String s, int... vararg) { // Marker YES: unused code

  }

  /**
   * The generic parameter list should not cause problems to UCDetector
   */
  public void genericExample(List<Map<Integer, String>> list) {

  }

  /**
   * The generic parameter list should not cause problems to UCDetector
   */
  public void genericExampleUnused(List<Map<Integer, String>> list) { // Marker YES: unused code

  }

  @SuppressWarnings("unchecked")
  public static List asMyList(Object[] array) { // Marker YES: unused code
    return Arrays.asList(array);
  }

  @Deprecated
  public void doNotUse() {

  }

  @AnnotationExample(parameterExmaple = "test")
  public void annotatedMethod2() {
    System.out.println(PI);
  }
}
