package org.ucdetector.example;

import org.ucdetector.example.classes.AnnotationExample;

public class NoUcdAnnotationExample {

  @java.lang.SuppressWarnings("ucd")
  public int unused = 0;

  @SuppressWarnings( { "ucd", "unused" })
  /**
   * javadoc
   * 
   * 
   */
  // comment 1
  private final static String UNUSED_WITH_SINGLE_MEMBER_ANNOTATION = "1";

  /** javadoc */
  @SuppressWarnings( { "UCD", "test", "unused" })
  /*
   * comment
   */
  private final String unusedMember = "1";

  // comment
  @SuppressWarnings( { "UCD" })
  /**
   * javadoc
   */
  // comment
  // comment
  // comment
  // comment
  final String unusedMember2 = "1";

  // comment 2
  /** javadoc 1 */
  @java.lang.SuppressWarnings("ucd")
  public void unused() {

  }

  @AnnotationExample(parameterExmaple = "1")
  @SuppressWarnings("ucd")
  public static final String UNUSED_WITH_NORMAL_ANNOTATION = "2";

  // @SuppressWarnings("ucd")
  public static final String UNUSED_2 = "3"; // Marker YES: unused code

  @SuppressWarnings("hello")
  public static final String UNUSED_3 = "3"; // Marker YES: unused code

  @SuppressWarnings("NO_UCD")
  public static final String UNUSED_4 = "3"; // Marker YES: unused code

  public class Unused {// Marker YES: unused code

  }

  @SuppressWarnings("ucd")
  public class UnusedWithAnnotation {

  }

  public class UnusedWithOutAnnotation {// Marker YES: unused code

  }

  @SuppressWarnings("ucd")
  public enum EnumExample {
    UNUSED, //
    UNUSED_2
  }

  @java.lang.SuppressWarnings("ucd")
  public @interface LocalAnnotationExample {
  }
}
