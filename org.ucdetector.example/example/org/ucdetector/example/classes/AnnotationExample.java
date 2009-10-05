package org.ucdetector.example.classes;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * no markers here
 */
@Target({ TYPE, FIELD, METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface AnnotationExample {

  String parameterExmaple();

}
