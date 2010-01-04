package org.ucdetector.example.classes;

/**
 * Bug 2906950
 * 
 * IllegalArgumentException modifiers is not a property of type
 * 
 * Nobody/Anonymous ( nobody ) - 2009-12-01 15:50
 * 
 * Hi, i got this exception today with UCDetector. I'm using eclipse 3.6M3 and
 * ucdetector 1.3.0. I don't know what i was doing when i got it, sorry.
 * 
 * java.lang.IllegalArgumentException: modifiers is not a property of type
 * org.eclipse.jdt.core.dom.MethodDeclaration
 * <p>
 * See: http://java.sun.com/j2se/1.5.0/docs/guide/language/annotations.html
 */
@AnnotationExampleUnusedMember(used = 1, needed = false, makeMePrivate = "forbidden")
public @interface AnnotationExampleUnusedMember {
  int used();

  boolean needed();

  String unused() default "[unused]";// Marker YES: unused code

  public String makeMePrivate() default "[false]";
}
