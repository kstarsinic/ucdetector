/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.OverrideIndicatorLabelDecorator;
import org.ucdetector.UCDetectorPlugin;

/**
 * Helper Class for javaElement's like:
 * <ul>
 * <li>methods</li>
 * <li>fields</li>
 * <li>types (=classes)</li>
 * </ul>
 * Calculates inheritance for methods
 * 
 */
public class JavaElementUtil {

  private JavaElementUtil() {
  };

  /**
   * Delegate to calculate if a method override or implements a methods
   */
  private static final OverrideIndicatorLabelDecorator OVERRIDE_OR_IMPLEMENT_DETECTOR//
  = new OverrideIndicatorLabelDecorator(null);

  /**
   * Delegate to calculate if a class as sub or super classes
   */
  private static final TypeHierarchyLifeCycle TYPE_HIERARCHY_LIFECYCLE //
  = new TypeHierarchyLifeCycle(false);

  /**
   * @return the package for an class, method, or field
   */
  public static IPackageFragment getPackageFor(IJavaElement javaElement) {
    IJavaElement parent = javaElement.getParent();
    while (true) {
      if (parent instanceof IPackageFragment || parent == null) {
        return (IPackageFragment) parent;
      }
      parent = parent.getParent();
    }
  }

  /**
   * @return the class for an class, method, or field
   */
  public static IType getTypeFor(IJavaElement javaElement) {
    IJavaElement parent = javaElement;
    while (true) {
      if (parent instanceof ICompilationUnit) {
        ICompilationUnit cu = (ICompilationUnit) parent;
        return cu.findPrimaryType();
      }
      if (parent instanceof IType || parent == null) {
        return (IType) parent;
      }
      parent = parent.getParent();
    }
  }

  /**
   * @return <code>true</code>, if the method is one of Object methods:<br>
   *         <ul>
   *         <li><code>hashCode()</code></li>
   *         <li><code>clone()</code></li>
   *         <li><code>toString()</code></li>
   *         <li><code>finalize()</code></li>
   *         <li><code>equals(Object)</code></li>
   *         </ul>
   *         Searching for this methods is very slow and in some cases it does
   *         make no sense to search for this methods
   */
  public static boolean isMethodOfJavaLangObject(IMethod method) {
    String methodName = method.getElementName();
    switch (method.getNumberOfParameters()) {
      case 0: {
        return "hashCode".equals(methodName) // //$NON-NLS-1$
            || "clone".equals(methodName) // //$NON-NLS-1$
            || "toString".equals(methodName)// //$NON-NLS-1$
            || "finalize".equals(methodName); //$NON-NLS-1$
      }
      case 1: {
        return "equals".equals(methodName);//$NON-NLS-1$
      }
      default:
        return false;
    }
  }

  /**
   * Ignore methods used for Object Serialization:
   * @return <code>true</code> if method is like
   * <ul>
   * <li><code>private void writeObject(ObjectOutputStream stream) throws IOException;</code></li>
   * <li><code>private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException;</code></li>
   * <li><code>ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;</code></li>
   * <li><code>ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;</code></li>
   * <li><code>private void readObjectNoData() throws ObjectStreamException;</code></li>
   * <li></li>
   * </ul>
   * @see http://java.sun.com/javase/6/docs/platform/serialization/spec/output.html
   */
  public static boolean isSerializationMethod(IMethod method) {
    String methodName = method.getElementName();
    switch (method.getNumberOfParameters()) {
      case 0: {
        return "writeReplace".equals(methodName) //$NON-NLS-1$
            || "readResolve".equals(methodName) //$NON-NLS-1$
            || "readObjectNoData".equals(methodName); //$NON-NLS-1$
      }
      case 1: {
        return "writeObject".equals(methodName) //$NON-NLS-1$
            || "readObject".equals(methodName);//$NON-NLS-1$
      }
      default:
        return false;
    }
  }

  /**
   * Ignore fields used for Object Serialization:
   * @return <code>true</code> if field is like
   * <ul>
   *         <li><code>static final long serialVersionUID</code></li>
   *         <li><code>private static final ObjectStreamField[] serialPersistentFields</code></li>
   * </ul>
   * @see http://java.sun.com/javase/6/docs/platform/serialization/spec/output.html
   */
  public static boolean isSerializationField(IField field)
      throws JavaModelException {
    if (isConstant(field)) {
      return "serialVersionUID".equals(field.getElementName()) //$NON-NLS-1$
          || "serialPersistentFields".equals(field.getElementName()); //$NON-NLS-1$
    }
    return false;
  }

  /**
   * @return a list of IJavaElement as string
   */
  public static String asStringSimple(Collection<? extends IJavaElement> list) { // NO_UCD
    StringBuilder sb = new StringBuilder();
    for (Iterator<? extends IJavaElement> iterator = list.iterator(); iterator
        .hasNext();) {
      IJavaElement element = iterator.next();
      sb.append(element == null ? "null" : element.getElementName());//$NON-NLS-1$
      sb.append(',');
    }
    return sb.toString();
  }

  /**
   * @return <ul>
  * <li>For classes: <code>ClassName</code></li>
  * <li>For methods: <code>ClassName.methodName(String, int, double )</code></li>
  * <li>For fields: <code>ClassName.fieldName</code></li>
  * </ul>
   */
  public static String asString(IJavaElement element) {
    if (element == null) {
      return "null"; //$NON-NLS-1$
    }
    StringBuffer info = new StringBuffer();
    if (element instanceof IMethod) {
      IMethod method = (IMethod) element;
      String className = element.getParent().getElementName();
      className = (className == null ? "" : className); //$NON-NLS-1$
      // hack for anonymous classes
      info.append(className.length() == 0 ? "$" : className); //$NON-NLS-1$
      info.append('.');
      info.append(getMethodName(method));
      info.append('(');
      info.append(parametersToString(method));
      info.append(')');
    }
    else if (element instanceof IField) {
      info.append(element.getParent().getElementName());
      info.append('.').append(element.getElementName());
    }
    else {
      info.append(element.getElementName());
    }
    return info.toString();
  }

  /**
   * @return <ul>
  * <li>For classes: <code>org.ucdetector.ClassName</code></li>
  * <li>For methods: <code>org.ucdetector.ClassName.methodName</code></li>
  * <li>For constructors: <code>org.ucdetector.ClassName.&lt;init&gt;</code></li>
  * <li>For fields: <code>org.ucdetector.ClassName.fieldName</code></li>
  * </ul>
   */
  public static String asStringWithPackage(IJavaElement element) { // NO_UCD
    if (element == null) {
      return "null"; //$NON-NLS-1$
    }
    StringBuffer info = new StringBuffer();
    IPackageFragment pack = getPackageFor(element);
    info.append(pack.getElementName()).append('.');
    IType type = getTypeFor(element);
    info.append(type.getElementName());
    if (element instanceof IMethod) {
      info.append('.');
      IMethod method = (IMethod) element;
      info.append(getMethodName(method));
    }
    else if (element instanceof IField) {
      info.append('.').append(element.getElementName());
    }
    return info.toString();
  }

  /**
   * @return Method name as String, or for constructors  "<init>"
   */
  public static String getMethodName(IMethod method) {
    boolean isConstructor;
    try {
      isConstructor = method.isConstructor();
    }
    catch (JavaModelException e) {
      isConstructor = false;
    }
    return isConstructor ? "<init>" : method.getElementName();//$NON-NLS-1$
  }

  /**
   * @return String presentation of the parameters:
   *         <code>public void foo(String text, int length)<code> will create <br>
   *         <code>"String,int"<code><br>
   *         if the parameter list is to long, a short form will be returned: <br><code>"String,int,*,*,*,"
   * @see org.eclipse.jdt.internal.core.SourceMethod.toStringName(StringBuffer, int)
   */
  private static String parametersToString(IMethod method) {
    StringBuffer sb = new StringBuffer();
    try {
      String[] typeParameters = method.getParameterTypes();
      for (int i = 0; i < typeParameters.length; i++) {
        String typeAsString = Signature.toString(typeParameters[i]);
        if (i == 0) {
          sb.append(typeAsString); // Always show first parameter
        }
        else {
          sb.append(',');
          sb.append(sb.length() > 30 ? "*" : typeAsString); //$NON-NLS-1$
        }
      }
    }
    catch (Exception e) {
      sb.append("???");
    }
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // OVERRIDE
  // -------------------------------------------------------------------------

  /**
   * @return <code>true</code> if a method is overridden<br>
   * it is very expensive to call this method!!!
   */
  public static boolean isOverriddenMethod(IMethod method) throws CoreException {
    StopWatch stop = new StopWatch(method);
    SearchPattern pattern = SearchPattern.createPattern(method,
        IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH
    // | IJavaSearchConstants.IGNORE_DECLARING_TYPE
        );
    CountRequestor requestor = new CountRequestor();
    runSearch(pattern, requestor, SearchEngine.createWorkspaceScope());
    stop.end("isOverriddenMethod");
    return requestor.found > 1;
  }

  public static boolean runSearch(SearchPattern pattern,
      SearchRequestor requestor, IJavaSearchScope scope) throws CoreException {
    boolean isSearchException = false;
    SearchEngine searchEngine = new SearchEngine();
    try {
      SearchParticipant[] participant = new SearchParticipant[] { SearchEngine
          .getDefaultSearchParticipant() };
      searchEngine.search(pattern, participant, scope, requestor, null);
    }
    catch (OperationCanceledException e) {
      // ignore
    }
    catch (RuntimeException rte) {
      isSearchException = true;
      // Java Search throws an NullPointerException in Eclipse 3.4M5
      UCDetectorPlugin.logError("Java search problems", rte); //$NON-NLS-1$
    }
    catch (OutOfMemoryError e) {
      isSearchException = true;
      UCDetectorPlugin.handleOutOfMemoryError(e);
    }
    return isSearchException;
  }

  private static final class CountRequestor extends SearchRequestor {
    private int found = 0;

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      this.found++;
    }
  }

  public static boolean isOverrideOrImplements(IMethod method) {
    int flags = OVERRIDE_OR_IMPLEMENT_DETECTOR.computeAdornmentFlags(method);
    boolean isOverride = (flags == JavaElementImageDescriptor.IMPLEMENTS //
    || flags == JavaElementImageDescriptor.OVERRIDES);
    return isOverride;
  }

  // -------------------------------------------------------------------------
  // SUB, SUPER CLASSES
  // -------------------------------------------------------------------------
  public static boolean hasSubClasses(IType type) throws JavaModelException {
    return hasXType(type, false);
  }

  public static boolean hasSuperClasses(IType type) throws JavaModelException { // NO_UCD
    return hasXType(type, true);
  }

  /**
   * org.eclipse.jdt.internal.corext.dom.Bindings.findOverriddenMethod
   * org.eclipse.jdt.internal.ui.typehierarchy.SubTypeHierarchyViewer
   */
  private static boolean hasXType(IType type, boolean isSupertype)
      throws JavaModelException {
    TYPE_HIERARCHY_LIFECYCLE.doHierarchyRefresh(type, null);
    ITypeHierarchy hierarchy = TYPE_HIERARCHY_LIFECYCLE.getHierarchy();
    if (hierarchy != null) {
      IType[] types = isSupertype ? hierarchy.getSupertypes(type) : hierarchy
          .getSubtypes(type);
      if (types == null || types.length == 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean isConstant(IField field) throws JavaModelException {
    return Flags.isStatic(field.getFlags()) && Flags.isFinal(field.getFlags());
  }

  public static boolean isBeanMethod(IMethod method) throws JavaModelException {
    if (Flags.isPublic(method.getFlags()) && !Flags.isStatic(method.getFlags())) {
      String name = method.getElementName();
      if (name.length() > 3 && name.startsWith("set")
          && Character.isUpperCase(name.charAt(3))
          && Signature.SIG_VOID.equals(method.getReturnType())
          && method.getNumberOfParameters() == 1) {
        return true;
      }
      if (name.length() > 3 && name.startsWith("get")
          && Character.isUpperCase(name.charAt(3))
          && !Signature.SIG_VOID.equals(method.getReturnType())
          && method.getNumberOfParameters() == 0) {
        return true;
      }
      if (name.length() > 2 && name.startsWith("is")
          && Character.isUpperCase(name.charAt(2))
          && Signature.SIG_BOOLEAN.equals(method.getReturnType())
          && method.getNumberOfParameters() == 0) {
        return true;
      }
    }
    return false;
  }
}
