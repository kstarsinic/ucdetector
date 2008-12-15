/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import org.ucdetector.Log;
import org.ucdetector.Messages;
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
   * @return the class for an class, method, or field,
   * or return <code>null</code> if there is no type, for example a package
   * has no type.
   */
  public static IType getTypeFor(IJavaElement javaElement) {
    IJavaElement parent = javaElement;
    while (true) {
      if (parent == null) {
        return null;
      }
      if (parent instanceof IType) {
        return (IType) parent;
      }
      if (parent instanceof ICompilationUnit) {
        ICompilationUnit cu = (ICompilationUnit) parent;
        return cu.findPrimaryType();
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
   * @return <ul>
  * <li>For classes: <code>ClassName</code></li>
  * <li>For methods: <code>ClassName.methodName(String, int, double )</code></li>
  * <li>For fields: <code>ClassName.fieldName</code></li>
  * </ul>
  * @see org.eclipse.jdt.internal.core.JavaElement.readableName()
   */
  public static String getElementName(IJavaElement element) {
    if (element == null) {
      return "null"; //$NON-NLS-1$
    }
    if (element instanceof IMethod) {
      return getMethodName((IMethod) element);
    }
    if (element instanceof IField) {
      return getFieldName((IField) element);
    }
    if (element instanceof IType) {
      return getTypeName(element);
    }
    if (element instanceof IPackageFragment
        && ((IPackageFragment) element).isDefaultPackage()) {
      return "default package"; //$NON-NLS-1$
    }
    if (element instanceof IImportContainer) {
      return "import declarations"; //$NON-NLS-1$
    }
    return element.getElementName();
  }

  public static String getTypeName(IJavaElement element) { // NO_UCD
    if (element instanceof IType) {
      return ((IType) element).getTypeQualifiedName();
    }
    return "class?"; //$NON-NLS-1$
  }

  /**
   * @return Method name as String, or for constructors  "<init>"
   */
  private static String getMethodName(IMethod method) {
    if (method == null) {
      return "method?"; //$NON-NLS-1$
    }
    StringBuffer info = new StringBuffer();
    info.append(getTypeName(method.getParent()));
    info.append('.');
    info.append(getSimpleMethodName(method));
    info.append('(');
    info.append(parametersToString(method));
    info.append(')');
    return info.toString();
  }

  public static String getSimpleMethodName(IMethod method) {
    String methodName = method == null ? "method?" : method.getElementName(); //$NON-NLS-1$
    return ((methodName.length() == 0 ? "<init>" : methodName)); //$NON-NLS-1$
  }

  private static String getFieldName(IField field) {
    if (field == null) {
      return "field?"; //$NON-NLS-1$
    }
    StringBuffer info = new StringBuffer();
    info.append(getTypeName(field.getParent()));
    info.append('.').append(field.getElementName());
    return info.toString();
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
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // OVERRIDE
  // -------------------------------------------------------------------------

  /**
   * @see org.eclipse.jdt.ui.actions.FindDeclarationsAction
   * @see http://help.eclipse.org/stable/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_search.htm
   * @return <code>true</code> if a method is overridden<br>
   * it is very expensive to call this method!!!
   */
  public static boolean isOverriddenMethod(IMethod method) throws CoreException {
    if (method.isConstructor()) {
      return false;
    }
    int limitTo = IJavaSearchConstants.DECLARATIONS
        | IJavaSearchConstants.IGNORE_DECLARING_TYPE
        | IJavaSearchConstants.IGNORE_RETURN_TYPE;
    SearchPattern pattern = SearchPattern.createPattern(method, limitTo);
    CountRequestor requestor = new CountRequestor();
    IType declaringType = method.getDeclaringType();
    IJavaSearchScope scope = SearchEngine.createHierarchyScope(declaringType);
    runSearch(pattern, requestor, scope);
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
      Log.logError("Java search problems", rte); //$NON-NLS-1$
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
      // System.out.println("~~~~~~~~~~~acceptSearchMatch=" + match.getElement());
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
      if (name.length() > 3
          && name.startsWith("set") //$NON-NLS-1$
          && Character.isUpperCase(name.charAt(3))
          && Signature.SIG_VOID.equals(method.getReturnType())
          && method.getNumberOfParameters() == 1) {
        return true;
      }
      if (name.length() > 3
          && name.startsWith("get") //$NON-NLS-1$
          && Character.isUpperCase(name.charAt(3))
          && !Signature.SIG_VOID.equals(method.getReturnType())
          && method.getNumberOfParameters() == 0) {
        return true;
      }
      if (name.length() > 2
          && name.startsWith("is") //$NON-NLS-1$
          && Character.isUpperCase(name.charAt(2))
          && Signature.SIG_BOOLEAN.equals(method.getReturnType())
          && method.getNumberOfParameters() == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * If we call <code>IPackageFragment.getChildren()</code>
   * we do NOT get sub packages!<br>
   * This is a workaround. We calculate sub packages by going to the
   * parent of code>IPackageFragment</code> 
   * which is a <code>IPackageFragmentRoot</code> and call
   * <code>IPackageFragmentRoot.getChildren()</code>
   * When a name of a subpackage starts with package name + "." it is a
   * subpackage
   * 
   * @return a list of all sub packages for the input package. For example input 
   * <pre>org.ucdetector.cycle</pre>
   * return a list with:
   * <ul>
   * <li><code>org.ucdetector.cycle.model</code></li>
   * <li><code>org.ucdetector.cycle.search</code></li>
   * </ul>
   */
  public static List<IPackageFragment> getSubPackages(
      IPackageFragment packageFragment) throws CoreException {
    List<IPackageFragment> subPackages = new ArrayList<IPackageFragment>();
    IJavaElement[] allPackages = ((IPackageFragmentRoot) packageFragment
        .getParent()).getChildren();
    for (IJavaElement javaElement : allPackages) {
      IPackageFragment pakage = (IPackageFragment) javaElement;
      String startPackagenName = packageFragment.getElementName() + "."; //$NON-NLS-1$
      if (packageFragment.isDefaultPackage()
          || pakage.getElementName().startsWith(startPackagenName)) {
        subPackages.add(pakage);
      }
    }
    return subPackages;
  }

  /**
   * @return "???" if unknown, otherwise:<p>
   * For classes:
   * <ul>
  * <li>"Annotation"</li>
  * <li>"Anonymous class"</li>
  * <li>"Enumeration"</li>
  * <li>"Interface"</li>
  * <li>"Local class"</li>
  * <li>"Class"</li>
  * </ul>
  * For methods:
  * <ul>
  * <li>"Constructor"</li>
  * <li>"Method"</li>
  * </ul>
  * For IField:
  * <ul>
  * <li>"EnumConstant"</li>
  * <li>"Constant"</li>
  * <li>"Field"</li>
  * </ul>
   */
  public static String getMemberTypeString(IMember member)
      throws JavaModelException {
    if (member instanceof IType) {
      IType type = (IType) member;
      if (type.isAnnotation()) {
        return "Annotation"; //$NON-NLS-1$
      }
      if (type.isAnonymous()) {
        return "Anonymous class"; //$NON-NLS-1$
      }
      if (type.isEnum()) {
        return "Enumeration"; //$NON-NLS-1$
      }
      if (type.isInterface()) {
        return "Interface"; //$NON-NLS-1$
      }
      // TODO 25.11.2008: no detection of local classes
      if (type.isLocal()) {
        return "Local class"; //$NON-NLS-1$
      }
      return Messages.SearchManager_Class;
    }
    if (member instanceof IMethod) {
      IMethod method = (IMethod) member;
      if (method.isConstructor()) {
        return Messages.SearchManager_Constructor;
      }
      return Messages.SearchManager_Method;
    }
    if (member instanceof IField) {
      IField field = (IField) member;
      if (field.isEnumConstant()) {
        return "EnumConstant"; //$NON-NLS-1$
      }
      if (JavaElementUtil.isConstant(field)) {
        return Messages.SearchManager_Constant;
      }
      return Messages.SearchManager_Field;
    }
    return "???"; //$NON-NLS-1$
  }
}
