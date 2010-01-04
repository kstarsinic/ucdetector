package org.ucdetector.example.classes;

/**
 * Bug 2906950
 * 
 * IllegalArgumentException modifiers is not a property of type
 * 
 * Nobody/Anonymous ( nobody ) - 2009-12-01 15:50
 */
@AnnotationExampleUnusedMember(used = 1, needed = false, makeMePrivate = "forbidden")
public @interface AnnotationExampleUnusedMember {
  int used();

  boolean needed();

  String unused() default "[unused]";

  public String makeMePrivate() default "[public]";
}
