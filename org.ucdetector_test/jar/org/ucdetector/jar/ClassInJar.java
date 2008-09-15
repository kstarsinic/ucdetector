package org.ucdetector.jar;
/**
 * Example for org.ucdetector.example plugin
 * <pre>
 * ClassInJar        =org.eclipse.jdt.internal.core.BinaryType
 * ClassInJar.class  =org.eclipse.jdt.internal.core.ClassFile
 * org.ucdetector.jar=org.eclipse.jdt.internal.core.JarPackageFragment
 * example.jar       =org.eclipse.jdt.internal.core.JarPackageFragmentRoot
 * <pre>
 * 
 * Java5Example          =org.eclipse.jdt.internal.core.SourceType
 * Java5Example.java     =org.eclipse.jdt.internal.core.CompilationUnit
 * org.ucdetector.example=org.eclipse.jdt.internal.core.PackageFragment
 * example               =org.eclipse.jdt.internal.core.PackageFragmentRoot
 */
public class ClassInJar {
  public static final String CONSTANT = "CONSTANT";

  public int field = 0;

  public static void main(String[] args) {
  }

  public void method() {

  }
}