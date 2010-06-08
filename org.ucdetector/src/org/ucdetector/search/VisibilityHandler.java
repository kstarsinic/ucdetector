/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * This class helps to find the minimum necessary visibility. If class is public
 * and has only references in the same package, this class will tell to use the
 * modifier "protected" instead of "public"
 */
class VisibilityHandler {
  private enum VISIBILITY {
    PRIVATE(0), PROTECTED(1), PUBLIC(2);
    final int value;

    private VISIBILITY(int value) {
      this.value = value;
    }
  }

  private final VISIBILITY visibilityStart;
  private VISIBILITY visibilityMaxFound = VISIBILITY.PRIVATE;
  //
  private final IMember startElement;
  private final MarkerFactory markerFactory;

  VisibilityHandler(MarkerFactory markerFactory, IMember startElement) throws JavaModelException {
    this.markerFactory = markerFactory;
    this.startElement = startElement;
    VISIBILITY vParent = getVisibiliyParent(startElement);
    VISIBILITY vStart = getVisibility(startElement);
    // Bug 2864046: public methods of non-public classes
    visibilityStart = vParent.value < vStart.value ? vParent : vStart;
  }

  private VISIBILITY getVisibility(IMember element) throws JavaModelException {
    int flags = element.getFlags();
    if (Flags.isPublic(flags)) {
      return VISIBILITY.PUBLIC;
    }
    if (Flags.isProtected(flags) || Flags.isPackageDefault(flags)) {
      return VISIBILITY.PROTECTED;
    }
    return VISIBILITY.PRIVATE;
  }

  private VISIBILITY getVisibiliyParent(IMember element) throws JavaModelException {
    IJavaElement parent = element.getParent();
    VISIBILITY result = VISIBILITY.PUBLIC;
    while (true) {
      if (!(parent instanceof IType)) {
        return result;
      }
      VISIBILITY vParent = getVisibility((IType) parent);
      result = vParent.value < result.value ? vParent : result;
      parent = parent.getParent();
    }
  }

  /**
   * Tries to detect if it is possible to reduce visibility<br>
   * For example: Make a <code>public</code> member/method/class
   * <code>protected</code>.
   * @throws JavaModelException
   */
  void checkVisibility(IJavaElement foundElement, int found, int foundTest) {
    if (!Prefs.isCheckReduceVisibilityProtected(startElement) && !Prefs.isCheckReduceVisibilityToPrivate(startElement)) {
      return;
    }
    IType startRootType = JavaElementUtil.getRootTypeFor(startElement);
    IType foundRootType = JavaElementUtil.getRootTypeFor(foundElement);
    //
    if (startRootType == null || foundRootType == null) {
      // reference in xml file found!
      setMaxVisibilityFound(VISIBILITY.PUBLIC);
      return;
    }
    // [ 2743908 ] Methods only called from inner class could be private
    if (startRootType.equals(foundRootType)) {
      // [ 2804064 ] Access to enclosing type - make 2743908 configurable
      setMaxVisibilityFound(Prefs.isIgnoreSyntheticAccessEmulationWarning() ? VISIBILITY.PRIVATE : VISIBILITY.PROTECTED);
      return;
    }
    IPackageFragment startPackage = JavaElementUtil.getPackageFor(startElement);
    IPackageFragment foundPackage = JavaElementUtil.getPackageFor(foundElement);
    if (startPackage.getElementName().equals(foundPackage.getElementName())) {
      setMaxVisibilityFound(VISIBILITY.PROTECTED);
      return;
    }
    setMaxVisibilityFound(VISIBILITY.PUBLIC);
    if (Prefs.isDetectTestOnly() && (found == foundTest)) {
      // continue searching, because all matches are matches in test code
      return;
    }
    if (found > Prefs.getWarnLimit()) {
      throw new OperationCanceledException("Cancel Search: public found"); //$NON-NLS-1$
    }
  }

  /**
   * Create a marker: "Change visibility of ClassName to private"
   * @return <code>true</code>, if a marker was created
   */
  boolean createMarker(IMember member, int line, int found) throws CoreException {
    if (!needVisibilityMarker(member, found)) {
      return false;
    }
    if (startElement instanceof IField) {
      IField field = (IField) startElement;
      if (field.isEnumConstant()) {
        // EnumConstant can not be private
        return false;
      }
      if (JavaElementUtil.isInterfaceField(field)) {
        // fix bug [ 2269486 ] Constants in Interfaces Can't be Private
        return false;
      }
    }
    else if (startElement instanceof IMethod) {
      IMethod method = (IMethod) startElement;
      if (method.isMainMethod()) {
        return false;
      }
      // Bug [ 2269486 ] Constants in Interfaces Can't be Private
      if (JavaElementUtil.isInterfaceMethod(method)) {
        return false;
      }
      // TODO - Bug [ 2968753] protected abstract method cannot be made private, see also Bug2968753
      //      - review all references from JavaElementUtil.isInterfaceMethod(method)
      //      - review all marker creation of methods, check for isAbstract()
      if (Flags.isAbstract(method.getFlags())) {
        return false;
      }
    }
    else if (startElement instanceof IType) {
      IType type = (IType) startElement;
      if (type.isLocal()) {
        // protected or private are forbidden for local classes
        return false;
      }
      // TODO: Search all parent class methods and fields to resolve this bug?
      if (hasPublicChild((IType) startElement)) {
        // The return line does not resolve
        // Bug 2539795: Wrong default visibility marker for classes
        // return false;
      }
    }
    String type;
    switch (visibilityMaxFound) {
      case PRIVATE:
        type = MarkerFactory.UCD_MARKER_USE_PRIVATE;
        break;
      case PROTECTED:
        if (member instanceof IType) {
          type = MarkerFactory.UCD_MARKER_USE_DEFAULT;
        }
        else {
          type = MarkerFactory.UCD_MARKER_USE_PROTECTED;
        }
        break;
      default:
        return false;
    }
    return markerFactory.createVisibilityMarker(member, type, line);
  }

  private boolean hasPublicChild(IType type) throws JavaModelException {
    for (IJavaElement element : type.getChildren()) {
      if (element instanceof IMember) {
        if (Flags.isPublic(((IMember) element).getFlags())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Set the maximum visibility found. Order is:
   * <ul>
   * <li>VISIBILITY_PRIVATE = 0</li>
   * <li>VISIBILITY_PROTECTED = 1</li>
   * <li>VISIBILITY_PUBLIC = 2</li>
   * </ul>
   */
  private void setMaxVisibilityFound(VISIBILITY visibility) {
    if (visibility.value > visibilityMaxFound.value) {
      visibilityMaxFound = visibility;
    }
  }

  /**
   * @return <code>true</code> when the member visibility can be decreased
   *         to private or protected
   */
  private boolean needVisibilityMarker(IMember member, int found) {
    boolean decreaseVisibility = visibilityStart.value > visibilityMaxFound.value;
    return found > 0
        && decreaseVisibility
        && (Prefs.isCheckReduceVisibilityProtected(member) && visibilityMaxFound == VISIBILITY.PROTECTED || Prefs
            .isCheckReduceVisibilityToPrivate(member)
            && visibilityMaxFound == VISIBILITY.PRIVATE);
  }

  boolean isMaxVisibilityFoundPublic() {
    return visibilityMaxFound == VISIBILITY.PUBLIC;
  }
}
