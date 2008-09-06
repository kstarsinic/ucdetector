package org.ucdetector.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * no markers here
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AnnotationExample {

  String parameterExmaple();

}
