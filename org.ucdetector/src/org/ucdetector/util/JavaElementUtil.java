/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
 */
public class JavaElementUtil {
  private final static NullProgressMonitor monitor = new NullProgressMonitor();

  private JavaElementUtil() {
    //
  }

  /**
   * @param javaElement element to find package for
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
   * @param javaElement to find class for
   * @param isPrimary type with the same name as the compilation unit, or the type of a class file
   * @return the class for an class, method, or field,
   * or return <code>null</code> if there is no type, for example a package
   * has no type.
   */
  // TODO 2009-04-12: review all isPrimary
  public static IType getTypeFor(IJavaElement javaElement, boolean isPrimary) {
    IJavaElement parent = javaElement;
    while (true) {
      if (parent == null) {
        return null;
      }
      if (parent instanceof IType) {
        IType type = (IType) parent;
        if (isPrimary && type.getCompilationUnit() != null) {
          return type.getCompilationUnit().findPrimaryType();
        }
        return type;
      }
      if (parent instanceof ICompilationUnit) {
        ICompilationUnit cu = (ICompilationUnit) parent;
        return cu.findPrimaryType();
      }
      parent = parent.getParent();
    }
  }

  /**
   * @param element1 method, class or field to check if it is in same class
   * @param element2 method, class or field to check if it is in same class
   * @return <code>true</code>, when the type of element1 and element2 is the same class
   */
  public static boolean isInSameType(IJavaElement element1,
      IJavaElement element2) {
    IType type1 = JavaElementUtil.getTypeFor(element1, false);
    IType type2 = JavaElementUtil.getTypeFor(element2, false);
    if (type1 == null || type2 == null) {
      return false;
    }
    return type1.equals(type2);
  }

  /**
   * @param method method to check
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
   * @param method java method to check
   * @return <code>true</code> if method is like
   * <ul>
   * <li><code>private void writeObject(ObjectOutputStream stream) throws IOException;</code></li>
   * <li><code>private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException;</code></li>
   * <li><code>ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;</code></li>
   * <li><code>ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;</code></li>
   * <li><code>private void readObjectNoData() throws ObjectStreamException;</code></li>
   * <li></li>
   * </ul>
   * @see "http://java.sun.com/javase/6/docs/platform/serialization/spec/output.html"
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
   * @param field to check
   * @return <code>true</code> if field is like
   * <ul>
   *         <li><code>static final long serialVersionUID</code></li>
   *         <li><code>private static final ObjectStreamField[] serialPersistentFields</code></li>
   * </ul>
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
   * @see "http://java.sun.com/javase/6/docs/platform/serialization/spec/output.html"
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
   * @param element to get name for
   * @return <ul>
  * <li>For classes: <code>ClassName</code></li>
  * <li>For methods: <code>ClassName.methodName(String, int, double )</code></li>
  * <li>For fields: <code>ClassName.fieldName</code></li>
  * </ul>
  * @see org.eclipse.jdt.internal.core.JavaElement#readableName()
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
    String methodName = (method == null) ? "method?" : method.getElementName(); //$NON-NLS-1$
    return ((methodName.length() == 0 ? "<init>" : methodName)); //$NON-NLS-1$
  }

  public static String getSimpleFieldName(IField field) {
    return (field == null) ? "field?" : field.getElementName(); //$NON-NLS-1$
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
   * @see org.eclipse.jdt.internal.core.SourceMethod#toStringInfo(int, StringBuffer)
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
   * @param method  to check if it is overridden
   * @see org.eclipse.jdt.ui.actions.FindDeclarationsAction
   * @see "http://help.eclipse.org/stable/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_search.htm"
   * @return <code>true</code> if a method is overridden<br>
   * it is very expensive to call this method!!!
   * @throws CoreException when problems found during search
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
   */
  public static boolean isOverriddenMethod(IMethod method) throws CoreException {
    int flags = method.getFlags();
    if (method.isConstructor() || Flags.isStatic(flags)
        || Flags.isPrivate(flags)) {
      return false;
    }
    int limitTo = IJavaSearchConstants.DECLARATIONS
        | IJavaSearchConstants.IGNORE_DECLARING_TYPE
        | IJavaSearchConstants.IGNORE_RETURN_TYPE;
    SearchPattern pattern = SearchPattern.createPattern(method, limitTo);
    CountOverridingRequestor requestor = new CountOverridingRequestor();
    IType declaringType = method.getDeclaringType();
    IJavaSearchScope scope = SearchEngine.createHierarchyScope(declaringType);
    runSearch(pattern, requestor, scope);
    // Ignore 1 match: Declaring type! 
    // TODO: check search matches: OverrideImplExample
    return requestor.found > 1;
  }

  /*
   * @see OverrideIndicatorLabelDecorator, MethodOverrideTester
   * @return <code>true</code>, when a method override or implements another method.
  public static boolean isOverrideOrImplements(IMethod method)
      throws JavaModelException {
    int flags = method.getFlags();
    if (method.isConstructor() || Flags.isStatic(flags)
        || Flags.isPrivate(flags)) {
      return false;
    }
    IType type = method.getDeclaringType();
    MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache
        .getMethodOverrideTester(type);
    IMethod defining = methodOverrideTester.findOverriddenMethod(method, true);
    //    if (JdtFlags.isAbstract(defining)) { return JavaElementImageDescriptor.IMPLEMENTS;
    //    else {return JavaElementImageDescriptor.OVERRIDES;
    return defining != null;
  }
   */

  /**
   * Run a jdt (java development toolkit) search and handle Exceptions, <br>
   * the Search result is found in SearchRequestor
   * @param pattern search pattern
   * @param requestor contains result after search
   * @param scope scope to search
   * @return true, when a {@link Exception} happend
   * @throws CoreException when there is a OutOfMemoryError
   */
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
    catch (OutOfMemoryError e) {
      isSearchException = true;
      UCDetectorPlugin.handleOutOfMemoryError(e);
    }
    catch (Throwable throwable) {
      isSearchException = true;
      // Java Search throws an NullPointerException in Eclipse 3.4M5
      Log.logError("Java search problems." //$NON-NLS-1$
          + "UCDetecor will ignore this exception. " //$NON-NLS-1$
          + "Maybe a 'org.eclipse.jdt.core.search' bug!", throwable); //$NON-NLS-1$
    }
    return isSearchException;
  }

  /**
   * Count number of matches
   */
  private static final class CountOverridingRequestor extends SearchRequestor {
    private int found = 0;

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      //      System.out.println("~~~~~~~~acceptSearchMatch=" + match.getElement());
      this.found++;
    }

    @Override
    public String toString() {
      return "found: " + found; //$NON-NLS-1$
    }
  }

  // -------------------------------------------------------------------------
  // SUB, SUPER CLASSES
  // -------------------------------------------------------------------------
  /**
   * @param type to check for subclasses
   * @return <code>true</code>, when a type has sub classes
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
   */
  public static boolean hasSubClasses(IType type) throws JavaModelException {
    return hasXType(type, false);
  }

  /**
   * @param type to check for super classes
   * @return <code>true</code>, when a type has super classes
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
   */
  public static boolean hasSuperClasses(IType type) throws JavaModelException { // NO_UCD
    return hasXType(type, true);
  }

  /**
   * org.eclipse.jdt.internal.corext.dom.Bindings.findOverriddenMethod
   * org.eclipse.jdt.internal.ui.typehierarchy.SubTypeHierarchyViewer
   */
  private static boolean hasXType(IType type, boolean isSupertype)
      throws JavaModelException {
    ITypeHierarchy hierarchy = type.newTypeHierarchy(monitor);
    if (hierarchy != null) {
      IType[] types = isSupertype ? hierarchy.getSupertypes(type) : hierarchy
          .getSubtypes(type);
      if (types == null || types.length == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param field to check if it is a constant
   * @return <code>true</code>, when a field is static and final
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource.
   */
  public static boolean isConstant(IField field) throws JavaModelException { // NO_UCD
    return Flags.isStatic(field.getFlags()) && Flags.isFinal(field.getFlags());
  }

  /**
   * @param method to check for java bean conventions
   * @return <code>true</code>, when a method matches the java bean conventions,
   * for example:
   *         <ul>
   *         <li><code>public void setName(String name)</code></li>
   *         <li><code>public String getName()</code></li>
   *         <li><code>public boolean isValid()</code></li>
   *         </ul>
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
   */
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
   * When a name of a sub package starts with package name + "." it is a
   * sub package
   * @param packageFragment package to find sub packages
   * 
   * @return a list of all sub packages for the input package. For example input 
   * <pre>org.ucdetector</pre>
   * return a list with:
   * <ul>
   * <li><code>org.ucdetector.action</code></li>
   * <li><code>org.ucdetector.iterator</code></li>
   * </ul>
   * @throws CoreException when there are problems finding sub packages
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
   * @param member to crate a string for
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
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource. 
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
      if (type.isLocal()) {
        return "Local class"; //$NON-NLS-1$
      }
      if (type.isMember()) {
        return "Member class"; //$NON-NLS-1$
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

  public static String dumpJavaElement(IJavaElement javaElement) { // NO_UCD
    if (javaElement == null) {
      return "null"; //$NON-NLS-1$
    }
    return javaElement.getElementName() + '\t' + '['
        + javaElement.getClass().getName() + ']';
  }

  /**
   * @param javaElement to find source folder for
   * @return the PackageFragmentRoot for a javaElement.
   * In Eclipse IDE this is called a source folder
   * 
   */
  public static IPackageFragmentRoot getPackageFragmentRootFor(
      IJavaElement javaElement) {
    IJavaElement parent = javaElement.getParent();
    while (true) {
      // System.out.println("parent =\t" + dumpJavaElement(parent));
      if (parent == null || parent instanceof IPackageFragmentRoot) {
        return (IPackageFragmentRoot) parent;
      }
      parent = parent.getParent();
    }
  }

  /**
   * Bug [ 2715348 ] Filter on Source folders does not work.
   * Match project relative path, not only last path element 
   * @param root source folder
   * @return "src/java/test" instead of only "test"
   */
  public static String getSourceFolderProjectRelativePath(
      IPackageFragmentRoot root) {
    IResource resource = root.getResource();
    if (resource != null && resource.getProjectRelativePath() != null) {
      return resource.getProjectRelativePath().toOSString();
    }
    return null;
  }

  /**
   * @param javaElement to check, if it is test code
   * @return <code>true</code>, when:
   * <ul>
   * <li>the class name of the javaElement ends with "Test"</li>
   * <li>the source folder name of the javaElement contains "test" or "junit"</li>
   * <li>the javaElement is a JUnit 3 test method: <code>public void testName()</code></li>
   * </ul>
   * NOTE: The @org.junit.Test annotations are ignored
   * <p>
   * See also feature request 2409697 "Detect code only called from tests" 
   * https://sourceforge.net/tracker2/?func=detail&aid=2409697&group_id=219599&atid=1046868
   */
  public static boolean isTestCode(IJavaElement javaElement) {
    // Check type -------------------------------------------------------------
    IType type = getTypeFor(javaElement, false);
    if (type != null) {
      if (type.getElementName().endsWith("Test")) { //$NON-NLS-1$
        return true;
      }
    }
    // Check packageFragmentRoot -----------------------------------------------
    IPackageFragmentRoot pfr = getPackageFragmentRootFor(javaElement);
    if (pfr != null) {
      String sourceFolder = getSourceFolderProjectRelativePath(pfr);
      if (sourceFolder != null) {
        String sf = sourceFolder.toLowerCase();
        if (sf.contains("test") || sf.contains("junit")) { //$NON-NLS-1$  //$NON-NLS-2$
          return true;
        }
      }
    }
    // Check method ------------------------------------------------------------
    if (javaElement instanceof IMethod) {
      IMethod method = (IMethod) javaElement;
      try {
        if (Flags.isPublic(method.getFlags()) //
            && Signature.SIG_VOID.equals(method.getReturnType())//
            && method.getNumberOfParameters() == 0 //
            && !Flags.isStatic(method.getFlags())) {
          // JUnit 3
          if (method.getElementName().startsWith("test")) { //$NON-NLS-1$
            return true;
          }
          // NOT USED: JUnit 4 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          // if (false) {
          //   String annotation = getAnnotationFor(method);
          //   return "Test".equals(annotation) //$NON-NLS-1$
          //       || "org.junit.Test".equals(annotation); //$NON-NLS-1$
          // }
        }
      }
      catch (JavaModelException e) {
        Log.logError("Can't run isTestCode()", e); //$NON-NLS-1$
      }
    }
    return false;
  }

  /*
   * @return the annotation for a method like @org.junit.Test
   * This method seems to be slow, because it needs to parse
   * the code of the method!
  private static String getAnnotationFor(IMethod method)
      throws JavaModelException {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(method.getSource().toCharArray());
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
    parser.setResolveBindings(true);
    ASTNode createAST = parser.createAST(null);
    FindAnnotationVisitor visitor = new FindAnnotationVisitor();
    createAST.accept(visitor);
    //    System.out.println(" annotation for " + method.getElementName() + "->"
    //        + visitor.annotation.getFullyQualifiedName());
    return visitor.annotation.getFullyQualifiedName();
  }

  private static class FindAnnotationVisitor extends ASTVisitor {
    private Name annotation;

    @Override
    public boolean visit(MethodDeclaration node) {
      for (Object modifier : node.modifiers()) {
        // System.out.println("modifier=" + modifier + " [" + modifier.getClass().getName() + "]");
        if (modifier instanceof Annotation) {
          Annotation ma = (Annotation) modifier;
          annotation = ma.getTypeName();
        }
      }
      return false;
    }
  }
   */
}
