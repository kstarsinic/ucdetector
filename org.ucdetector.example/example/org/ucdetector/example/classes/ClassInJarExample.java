/* $CVSHeader$
 * Created on 15.07.2009
 * 
 * @author joerg
 *
 * Copyright ISMC GmbH Waldbronn, Germany
 */
package org.ucdetector.example.classes;

import org.ucdetector.jar.ClassInJar;

public class ClassInJarExample {
  public static void main(String[] args) {
    ClassInJar classInJar = new ClassInJar();
    System.out.println(classInJar.field);
  }

}
