/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.search.CountSearchRequestor;

/**
 * Helper Class for javaElement's like:
 * <ul>
 * <li>methods</li>
 * <li>fields</li>
 * <li>types (=classes)</li>
 * </ul>
 * Calculates inheritance for methods
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
@SuppressWarnings("nls")
public final class JavaElementUtil {
  /**
   * 
   */
  private static final String UNKNOWN_FIELD = "field?";
  private static final NullProgressMonitor NULL_MONITOR = new NullProgressMonitor();

  private JavaElementUtil() {
    //
  }

  // -------------------------------------------------------------------------
  // PACKAGE STUFF
  // -------------------------------------------------------------------------

  /**
   * @param javaElement to find source folder for
   * @return the PackageFragmentRoot for a javaElement.
   * In Eclipse IDE this is called a source folder
   *
   */
  public static IPackageFragmentRoot getPackageFragmentRootFor(IJavaElement javaElement) {
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
   * Bug [ 2715348 ] Filter on Source folders does not work.
   * Match project relative path, not only last path element
   * @param root source folder
   * @return "src/java/test" instead of only "test"
   */
  public static String getSourceFolderProjectRelativePath(IPackageFragmentRoot root) {
    IResource resource = root.getResource();
    if (resource != null && resource.getProjectRelativePath() != null) {
      return resource.getProjectRelativePath().toOSString();
    }
    return null;
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
  public static List<IPackageFragment> getSubPackages(IPackageFragment packageFragment) throws CoreException {
    List<IPackageFragment> subPackages = new ArrayList<IPackageFragment>();
    IJavaElement[] allPackages = ((IPackageFragmentRoot) packageFragment.getParent()).getChildren();
    for (IJavaElement javaElement : allPackages) {
      IPackageFragment pakage = (IPackageFragment) javaElement;
      String startPackagenName = packageFragment.getElementName() + ".";
      if (packageFragment.isDefaultPackage() || pakage.getElementName().startsWith(startPackagenName)) {
        subPackages.add(pakage);
      }
    }
    return subPackages;
  }

  // -------------------------------------------------------------------------
  // TYPE STUFF
  // -------------------------------------------------------------------------

  /**
   * @param javaElement to find class for
   * @param isPrimary type with the same name as the compilation unit, or the type of a class file
   * @return the class for an class, method, or field,
   * or return <code>null</code> if there is no type, for example a package
   * has no type.
   */
  public static IType getTypeFor(IJavaElement javaElement, boolean isPrimary) {
    IType result = null;
    IJavaElement parent = javaElement;
    while (parent != null) {
      if (parent instanceof IType) {
        result = (IType) parent;
        break;
      }
      if (parent instanceof ICompilationUnit) {
        result = ((ICompilationUnit) parent).findPrimaryType();
        break;
      }
      parent = parent.getParent();
    }
    if (isPrimary && result != null && result.getCompilationUnit() != null) {
      IType primaryType = result.getCompilationUnit().findPrimaryType();
      if (primaryType != null) {
        // Avoid return null for missing primary type.
        // Eg: Foo.java contains only class Bar
        result = primaryType;
      }
    }
    return result;
  }

  /**
   * @param resource usually a java file, but maybe others
   * @return type of the file, or null, if it is not a java file
   */
  public static IType getTypeFor(IResource resource) {
    if (isJavaFile(resource)) {
      IJavaElement javaElement = JavaCore.create((IFile) resource);
      return getTypeFor(javaElement, true);
    }
    // Log.debug("Resource %s is not a accessible java file", resource);
    return null;
  }

  private static boolean isJavaFile(IResource resource) {
    return resource instanceof IFile && "java".equalsIgnoreCase(resource.getFileExtension()) && resource.isAccessible();
  }

  /**
   * @param javaElement to find the last parent for
   * @return the class "before" the compilation unit in the package explorer
   */
  public static IType getRootTypeFor(IJavaElement javaElement) {
    IJavaElement parent = javaElement;
    IType lastType = null;
    while (parent != null) {
      if (parent instanceof IType) {
        lastType = (IType) parent;
      }
      else if (parent instanceof ICompilationUnit) {
        return lastType;
      }
      parent = parent.getParent();
    }
    return lastType;
  }

  /**
   * @param type check this type, if it is a primary type
   * @return <code>true</code>, when the class has the same name as the java file
   */
  public static boolean isPrimary(IType type) {
    ICompilationUnit cu = type.getCompilationUnit();
    if (cu == null) {
      return false;
    }
    IType primaryType = cu.findPrimaryType();
    if (primaryType == null) {
      return false;
    }
    return primaryType.equals(type);
  }

  /**
   * @param element1 method, class or field to check if it is in same class
   * @param element2 method, class or field to check if it is in same class
   * @return <code>true</code>, when the type of element1 and element2 is the same class
   */
  public static boolean isInSameType(IJavaElement element1, IJavaElement element2) {
    IType type1 = JavaElementUtil.getTypeFor(element1, false);
    IType type2 = JavaElementUtil.getTypeFor(element2, false);
    if (type1 == null || type2 == null) {
      return false;
    }
    return type1.equals(type2);
  }

  // -------------------------------------------------------------------------
  // INTERFACE STUFF
  // -------------------------------------------------------------------------

  /**
   * @param method method
   * @return true, when parent of field is an interface
   * @throws JavaModelException if a problem occurs
   */
  public static boolean isInterfaceMethod(IMethod method) throws JavaModelException {
    return isParentInterface(method);
  }

  /**
   * @param field field
   * @return true, when parent of field is an interface
   * @throws JavaModelException if a problem occurs
   */
  public static boolean isInterfaceField(IField field) throws JavaModelException {
    return isParentInterface(field);
  }

  private static boolean isParentInterface(IJavaElement element) throws JavaModelException {
    IJavaElement parent = element.getParent();
    return (parent instanceof IType && ((IType) parent).isInterface());
  }

  // -------------------------------------------------------------------------
  // CompilationUnit STUFF
  // -------------------------------------------------------------------------

  /**
   * @param resource usually a java file, but maybe others
   * @return top java element (CompilationUnit) of the marker
   */
  public static ICompilationUnit getCompilationUnitFor(IResource resource) {
    if (isJavaFile(resource)) {
      IJavaElement javaElement = JavaCore.create((IFile) resource);
      if (javaElement instanceof ICompilationUnit) {
        return (ICompilationUnit) javaElement;
      }
    }
    return null;
  }

  // -------------------------------------------------------------------------
  // SPECIAL METHODS
  // -------------------------------------------------------------------------
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
        return "hashCode".equals(methodName) //
            || "clone".equals(methodName) //
            || "toString".equals(methodName)//
            || "finalize".equals(methodName);
      }
      case 1: {
        return "equals".equals(methodName);
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
   * @throws JavaModelException when there is a problem
   * @see "http://java.sun.com/javase/6/docs/platform/serialization/spec/output.html"
   * @see org.eclipse.jdt.internal.compiler.problem.ProblemReporter#unusedPrivateMethod
   */
  public static boolean isSerializationMethod(IMethod method) throws JavaModelException {
    if (!Flags.isStatic(method.getFlags())) {
      String methodName = method.getElementName();
      if (method.getNumberOfParameters() == 0) {
        return "writeReplace".equals(methodName) // ANY-ACCESS-MODIFIER
            || "readResolve".equals(methodName) // ANY-ACCESS-MODIFIER
            || "readObjectNoData".equals(methodName);// private
      }
      else if (method.getNumberOfParameters() == 1) {
        return "writeObject".equals(methodName)// private
            || "readObject".equals(methodName);// private
      }
    }
    return false;
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
    if (!Flags.isPublic(method.getFlags()) || Flags.isStatic(method.getFlags())) {
      return false;
    }
    String name = method.getElementName();
    if (Signature.SIG_VOID.equals(method.getReturnType()) && name.startsWith("set") && name.length() > 3
        && Character.isUpperCase(name.charAt(3)) && method.getNumberOfParameters() == 1) {
      return true;
    }
    if (!Signature.SIG_VOID.equals(method.getReturnType()) && name.startsWith("get") && name.length() > 3
        && Character.isUpperCase(name.charAt(3)) && method.getNumberOfParameters() == 0) {
      return true;
    }
    if (Signature.SIG_BOOLEAN.equals(method.getReturnType()) && name.startsWith("is") && name.length() > 2
        && Character.isUpperCase(name.charAt(2)) && method.getNumberOfParameters() == 0) {
      return true;
    }
    return false;
  }

  /**
   * Check if a enum class is called by:
   * <ul>
   *   <li><code>values()</code> or</li>
   *   <li><code>valueOf(java.lang.String)</code></li>
   * </ul>
   * See bug 2900561: enum detection, or don't create "unnecessary marker" for enum constants
   * See: JavaSearchPage.performNewSearch()
   *  See: JavaSearchQuery.run()
   * @param enumType enum to check usage
   * @return <code>true</code>, when enum is used by values() or valueOf()
   * @throws CoreException when there are search problems
   */
  public static boolean isUsedBySpecialEnumMethods(IType enumType) throws CoreException {
    String[] stringPatterns = new String[] {
        // We need '.' as class name separator for search!
        enumType.getFullyQualifiedName('.') + ".values()",
        enumType.getFullyQualifiedName('.') + ".valueOf(java.lang.String)", };
    for (String stringPattern : stringPatterns) {
      SearchPattern pattern = SearchPattern.createPattern(stringPattern, IJavaSearchConstants.METHOD,
          IJavaSearchConstants.REFERENCES, SearchPattern.R_ERASURE_MATCH);
      CountSearchRequestor requestor = new CountSearchRequestor();
      runSearch(pattern, requestor);
      if (requestor.isFound()) {
        return true;
      }
    }
    return false;
  }

  // -------------------------------------------------------------------------
  // SPECIAL FIELDS
  // -------------------------------------------------------------------------

  /**
   * Ignore fields used for Object Serialization:
   * @param field to check
   * @return <code>true</code> if field is like
   * <ul>
   *         <li><code>ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;</code></li>
   *         <li><code>private static final ObjectStreamField[] serialPersistentFields</code></li>
   * </ul>
   * @throws JavaModelException if this element does not exist or if an
  *      exception occurs while accessing its corresponding resource.
   * @see "http://download.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html"
   * @see org.eclipse.jdt.internal.compiler.problem.ProblemReporter#unusedPrivateField
   */
  public static boolean isSerializationField(IField field) throws JavaModelException {
    if (isConstant(field)) {
      return "serialVersionUID".equals(field.getElementName())
          || "serialPersistentFields".equals(field.getElementName());
    }
    return false;
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

  public static IMethod getMainMethod(IType member) throws JavaModelException {
    IMethod[] methods = member.getMethods();
    for (IMethod method : methods) {
      if (method.isMainMethod()) {
        return method;
      }
    }
    return null;
  }

  public static boolean hasMainMethod(IType member) throws JavaModelException {
    return getMainMethod(member) != null;
  }

  // -------------------------------------------------------------------------
  // TO STRING
  // -------------------------------------------------------------------------
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
      return "null";
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
    if (element instanceof IPackageFragment && ((IPackageFragment) element).isDefaultPackage()) {
      return "default package";
    }
    if (element instanceof IImportContainer) {
      return "import declarations";
    }
    return element.getElementName();
  }

  /**
   * @param element to get a String representation. Should be a {@link IType}
   * @return typeName e.g. UCDetectorPlugin
   */
  public static String getTypeName(IJavaElement element) {
    if (element instanceof IType) {
      return ((IType) element).getTypeQualifiedName('.');
    }
    return "class?";
  }

  /**
   * @param type to get a String representation
   * @return full typeName e.g. org.ucdetector.UCDetectorPlugin
   */
  public static String getTypeNameFull(IType type) {
    IPackageFragment pakage = getPackageFor(type);
    if (pakage.isDefaultPackage()) {
      return getTypeName(type);
    }
    return pakage.getElementName() + '.' + getTypeName(type);
  }

  /**
   * @return Method name as String, or for constructors  "&lt;init>"
   */
  private static String getMethodName(IMethod method) {
    if (method == null) {
      return "method?";
    }
    return String.format("%s.%s(%s)", getTypeName(method.getParent()), getSimpleMethodName(method),
        parametersToString(method));
  }

  public static String getSimpleMethodName(IMethod method) {
    String methodName = (method == null) ? "method?" : method.getElementName();
    return ((methodName.length() == 0 ? "<init>" : methodName));
  }

  public static String getSimpleFieldName(IField field) {
    return (field == null) ? UNKNOWN_FIELD : field.getElementName();
  }

  private static String getFieldName(IField field) {
    if (field == null) {
      return UNKNOWN_FIELD;
    }
    return String.format("%s.%s", getTypeName(field.getParent()), field.getElementName());
  }

  /**
   * @return String presentation of the parameters:
   *         <code>public void foo(String text, int length)<code> will create <br>
   *         <code>"String,int"<code><br>
   *         if the parameter list is to long, a short form will be returned: <br><code>"String,int,*,*,*,"
   * @see org.eclipse.jdt.internal.core.SourceMethod#toStringInfo(int, StringBuffer)
   */
  private static String parametersToString(IMethod method) {
    StringBuilder sb = new StringBuilder();
    String[] typeParameters = method.getParameterTypes();
    for (int i = 0; i < typeParameters.length; i++) {
      String typeAsString = Signature.toString(typeParameters[i]);
      if (i == 0) {
        sb.append(typeAsString); // Always show first parameter
      }
      else {
        sb.append(',');
        sb.append(sb.length() > 30 ? "*" : typeAsString);
      }
    }
    return sb.toString();
  }

  public enum MemberInfo {
    // org.eclipse.jdt.ui/icons/full/obj16/field_public_obj.gif:
    Annotation("Annotation", "annotation_obj.gif"), //
    Class("Class", "class_obj.gif"), //
    ClassAnonymous("Anonymous class", "innerclass_public_obj.gif"), //
    ClassLocal("Local class", "innerclass_public_obj.gif"), //
    ClassMember("Member class", "innerclass_public_obj.gif"), //
    Constant("Constant", "field_public_obj.gif"), //
    Constructor("Constructor", "methpub_obj.gif"), //
    Enum("Enum", "enum_obj.gif"), //
    EnumConstant("EnumConstant", "field_public_obj.gif"), //
    Field("Field", "field_public_obj.gif"), //
    Initializer("Initializer", "methpub_obj.gif"), //
    Interface("Interface", "int_obj.gif"), //
    Method("Method", "methpub_obj.gif"), //
    ;

    private final String info;
    private final String icon;

    private MemberInfo(String info, String icon) {
      this.info = info;
      this.icon = icon;
    }

    @Override
    public String toString() {
      return info;
    }

    public String getIcon() {
      return icon;
    }
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
  * <li>"Initializer"</li>
  * </ul>
  */
  public static String getMemberTypeString(IMember member) {
    MemberInfo memberInfo = getMemberInfo(member);
    return memberInfo == null ? "???" : memberInfo.toString();
  }

  public static MemberInfo getMemberInfo(IMember member) {
    try {
      if (member instanceof IType) {
        IType type = (IType) member;
        if (type.isAnnotation()) {
          return MemberInfo.Annotation;
        }
        if (type.isAnonymous()) {
          return MemberInfo.ClassAnonymous;
        }
        if (type.isEnum()) {
          return MemberInfo.Enum;
        }
        if (type.isInterface()) {
          return MemberInfo.Interface;
        }
        if (type.isLocal()) {
          return MemberInfo.ClassLocal;
        }
        if (type.isMember()) {
          return MemberInfo.ClassMember;
        }
        return MemberInfo.Class;
      }
      if (member instanceof IMethod) {
        IMethod method = (IMethod) member;
        if (method.isConstructor()) {
          return MemberInfo.Constructor;
        }
        return MemberInfo.Method;
      }
      if (member instanceof IField) {
        IField field = (IField) member;
        if (field.isEnumConstant()) {
          return MemberInfo.EnumConstant;
        }
        if (JavaElementUtil.isConstant(field)) {
          return MemberInfo.Constant;
        }
        return MemberInfo.Field;
      }
    }
    catch (JavaModelException ex) {
      Log.error("Can't get memberTypeString for member: " + member, ex);
    }
    return null;
  }

  /**
   * @param member to crate a string for
   * @return "???" if unknown, otherwise:<p>
   * <ul>
   * <li>"Class"</li>
   * <li>"Method"</li>
   * <li>"Field"</li>
   * <li>"Initializer"</li>
   * </ul>
   */
  public static String getMemberTypeStringSimple(IMember member) {
    if (member instanceof IType) {
      return MemberInfo.Class.toString();
    }
    if (member instanceof IMethod) {
      return MemberInfo.Method.toString();
    }
    if (member instanceof IField) {
      return MemberInfo.Field.toString();
    }
    if (member instanceof IInitializer) {
      return MemberInfo.Initializer.toString();
    }
    return "???";
  }

  public static String getElementNames(Collection<? extends IJavaElement> javaElements) { // NO_UCD
    StringBuilder sb = new StringBuilder();
    for (IJavaElement javaElement : javaElements) {
      sb.append(getElementName(javaElement)).append("\n\t");
    }
    return sb.toString();
  }

  public static String getElementNameAndClassName(IJavaElement javaElement) { // NO_UCD
    return String.format("%s\t%s", getElementName(javaElement), getClassName(javaElement));
  }

  // -------------------------------------------------------------------------
  // INHERITANCE
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
    if (method.isConstructor() || Flags.isStatic(flags) || Flags.isPrivate(flags)) {
      return false;
    }
    int limitTo = IJavaSearchConstants.DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE
        | IJavaSearchConstants.IGNORE_RETURN_TYPE;
    SearchPattern pattern = SearchPattern.createPattern(method, limitTo);
    CountSearchRequestor requestor = new CountSearchRequestor();
    IType declaringType = method.getDeclaringType();
    IJavaSearchScope scope = SearchEngine.createHierarchyScope(declaringType);
    runSearch(pattern, requestor, scope);
    // Ignore 1 match: Declaring type!
    return requestor.getFoundCount() > 1;
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

  // -------------------------------------------------------------------------
  // SEARCH
  // -------------------------------------------------------------------------
  public static boolean runSearch(SearchPattern pattern, SearchRequestor requestor) throws CoreException {
    JavaSearchScopeFactory factory = JavaSearchScopeFactory.getInstance();
    IJavaSearchScope sourceScope = factory.createWorkspaceScope(IJavaSearchScope.SOURCES);
    // [PerformanceBug] Next line made UCDetector 2 times slower before version 1.4.0!!!
    // sourceScope = SearchEngine.createWorkspaceScope()
    return runSearch(pattern, requestor, sourceScope);
  }

  /**
   * Run a jdt (java development toolkit) search and handle Exceptions, <br>
   * the Search result is found in SearchRequestor
   * @param pattern search pattern
   * @param requestor contains result after search
   * @param scope scope to search
   * @return true, when a {@link Exception} happend
   * @throws CoreException when there is a OutOfMemoryError
   */
  private static boolean runSearch(SearchPattern pattern, SearchRequestor requestor, IJavaSearchScope scope)
      throws CoreException {
    boolean isSearchException = false;
    SearchEngine searchEngine = new SearchEngine();
    try {
      SearchParticipant[] participant = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
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
      String mes = "Java search problems. UCDetecor will ignore this exception. Maybe a 'org.eclipse.jdt.core.search' bug!";
      Log.error(mes, throwable);
    }
    return isSearchException;
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
  private static boolean hasXType(IType type, boolean isSupertype) throws JavaModelException {
    ITypeHierarchy hierarchy = type.newTypeHierarchy(NULL_MONITOR);
    if (hierarchy != null) {
      IType[] types = isSupertype ? hierarchy.getSupertypes(type) : hierarchy.getSubtypes(type);
      if (types == null || types.length == 0) {
        return false;
      }
    }
    return true;
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
    IType type = getTypeFor(javaElement, true);
    if (type != null && type.getElementName().endsWith("Test")) {
      return true;
    }
    // Check packageFragmentRoot -----------------------------------------------
    IPackageFragmentRoot pfr = getPackageFragmentRootFor(javaElement);
    if (pfr != null) {
      String sourceFolder = getSourceFolderProjectRelativePath(pfr);
      if (sourceFolder != null) {
        sourceFolder = sourceFolder.toLowerCase();
        if (sourceFolder.contains("test") || sourceFolder.contains("junit")) {
          return true;
        }
      }
    }
    // Check method ------------------------------------------------------------
    if (javaElement instanceof IMethod) {
      IMethod method = (IMethod) javaElement;
      try {
        if (Signature.SIG_VOID.equals(method.getReturnType()) && Flags.isPublic(method.getFlags())
            && !Flags.isStatic(method.getFlags()) && method.getNumberOfParameters() == 0) {
          // JUnit 3
          if (method.getElementName().startsWith("test")) {
            return true;
          }
          // NOT USED: JUnit 4 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          // if (false) {
          //   String annotation = getAnnotationFor(method);
          //   return "Test".equals(annotation)
          //       || "org.junit.Test".equals(annotation);
          // }
        }
      }
      catch (JavaModelException e) {
        Log.error("Can't run isTestCode() for method: " + method, e);
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
        // System.out.println("modifier=" + modifier + Log.getClassName(modifier));
        if (modifier instanceof Annotation) {
          Annotation ma = (Annotation) modifier;
          annotation = ma.getTypeName();
        }
      }
      return false;
    }
  }
   */

  /**
   * org.eclipse.jdt.internal.corext.dom.Bindings.findOverriddenMethod
   * org.eclipse.jdt.internal.ui.typehierarchy.SubTypeHierarchyViewer
   * @param type to find super types for
   * @return super types
   * @throws JavaModelException when there are problems creating hierarchy
   */
  public static IType[] getAllSupertypes(IType type) throws JavaModelException {
    ITypeHierarchy hierarchy = type.newTypeHierarchy(NULL_MONITOR);
    if (hierarchy != null) {
      return hierarchy.getAllSupertypes(type);
    }
    return new IType[0];
  }

  public static String getClassName(Object o) {
    return String.format("[%s]", o == null ? "?" : o.getClass().getName());
  }

  /**
   * Create link to java element, which can be used in Eclipse "Java Stack Trace Console"
   * 
   * @param javaElement class/method/field to create link for
   * @param lineNr line of javaElement 
   * @return java link eg: org.eclipse.swt.SWT.error(SWT.java:3634)
   */
  public static String createJavaLink(IMember javaElement, int lineNr) {
    IType type = getTypeFor(javaElement, true);
    String typeNameFull = getTypeNameFull(type);
    String member = (javaElement instanceof IType) ? "<init>" : javaElement.getElementName();
    String typeName = getElementName(type);
    return String.format("%s.%s(%s.java:%s)", typeNameFull, member, typeName, Integer.toString(lineNr));
  }

  /*
   * @param clazz to find super classes for
   * @return all super classes of clazz. All interfaces implemented by this class
  public static Set<Class<?>> getAllSuperClasses(Class<?> clazz) {
    Set<Class<?>> result = new LinkedHashSet<Class<?>>();
    addAllSuperClasses(clazz, result);
    return result;
  }

  private static void addAllSuperClasses(Class<?> clazz, Set<Class<?>> result) {
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> interfaze : interfaces) {
      result.add(interfaze);
      addAllSuperClasses(interfaze, result);
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null) {
      result.add(superclass);
      addAllSuperClasses(superclass, result);
    }
  }
   */
}
