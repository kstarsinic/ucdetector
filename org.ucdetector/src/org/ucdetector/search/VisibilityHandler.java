/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import org.eclipse.core.runtime.CoreException;
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
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
class VisibilityHandler {
  private enum VISIBILITY {
    PRIVATE(0), DEFAULT(1), PROTECTED(2), PUBLIC(3);
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
    VISIBILITY vRootType = getVisibiliyRootType(startElement);
    VISIBILITY vStart = getVisibility(startElement);
    // Bug 2864046: public methods of non-public classes
    visibilityStart = vRootType.value < vStart.value ? vRootType : vStart;
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

  private VISIBILITY getVisibiliyRootType(IMember element) throws JavaModelException {
    IType rootType = JavaElementUtil.getRootTypeFor(element);
    return rootType == null ? VISIBILITY.PUBLIC : getVisibility(rootType);
  }

  /**
   * Tries to detect if it is possible to reduce visibility<br>
   * For example: Make a <code>public</code> member/method/class
   * <code>protected</code>.
   * @throws JavaModelException
   */
  void checkVisibility(IJavaElement foundElement) {
    if (!Prefs.isCheckReduceVisibility(startElement)) {
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
        // No modifier allowed for enum constants!
        return false;
      }
      if (JavaElementUtil.isInterfaceField(field)) {
        // fix bug [ 2269486 ] Constants in Interfaces Can't be Private
        // only public, static & final are permitted for interface fields
        return false; // default visibility means public!
      }
    }
    else if (startElement instanceof IMethod) {
      IMethod method = (IMethod) startElement;
      if (method.isMainMethod()) {
        return false;
      }
      // Bug [ 2269486 ] Constants in Interfaces Can't be Private
      if (JavaElementUtil.isInterfaceMethod(method)) {
        return false; // default visibility means public!
      }
      // Bug [ 2968753] protected abstract method cannot be made private
      if (Flags.isAbstract(method.getFlags())) {
        return false;
      }
    }
    else if (startElement instanceof IType) {
      IType type = (IType) startElement;
      if (type.isLocal() || JavaElementUtil.isPrimary(type)) {
        // "private" and "protected" are forbidden for primary types (classes, enums, annotations, interfaces)
        avoidPrivateProtected();
      }
      // TODO: Bug 2539795: Wrong default visibility marker for classes
      // Search all parent class methods and fields to resolve this bug?
      if (hasPublicChild((IType) startElement)) {
        // return false;
      }
    }
    String markerType;
    switch (visibilityMaxFound) {
      case PRIVATE:
        markerType = MarkerFactory.UCD_MARKER_USE_PRIVATE;
        break;
      case DEFAULT:
        markerType = MarkerFactory.UCD_MARKER_USE_DEFAULT;
        break;
      case PROTECTED:
        if (member instanceof IType) {
          markerType = MarkerFactory.UCD_MARKER_USE_DEFAULT;
        }
        else {
          markerType = MarkerFactory.UCD_MARKER_USE_PROTECTED;
        }
        break;
      default:
        return false;
    }
    return markerFactory.createVisibilityMarker(member, markerType, line);
  }

  private void avoidPrivateProtected() {
    if (visibilityMaxFound == VISIBILITY.PRIVATE || visibilityMaxFound == VISIBILITY.PROTECTED) {
      visibilityMaxFound = VISIBILITY.DEFAULT;
    }
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
    return found > 0 && decreaseVisibility && (//
        Prefs.isCheckReduceVisibilityProtected(member) && visibilityMaxFound == VISIBILITY.PROTECTED || //
        Prefs.isCheckReduceVisibilityToPrivate(member) && visibilityMaxFound == VISIBILITY.PRIVATE);
  }

  boolean isMaxVisibilityFoundPublic() {
    return visibilityMaxFound == VISIBILITY.PUBLIC;
  }
}
