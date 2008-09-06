/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
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
  private static final int VISIBILITY_PRIVATE = 0;
  private static final int VISIBILITY_PROTECTED = 1;
  private static final int VISIBILITY_PUBLIC = 2;

  private final int visibilityStart;
  private final IMember startElement;
  private final MarkerFactory markerFactory;

  private int maxVisibilityFound = VISIBILITY_PRIVATE;

  VisibilityHandler(MarkerFactory markerFactory, IMember startElement)
      throws JavaModelException {
    this.markerFactory = markerFactory;
    this.startElement = startElement;
    int flags = startElement.getFlags();
    IJavaElement parent = startElement.getParent();
    // handle enum member like protected, they have no "public"
    boolean isEnum = (parent instanceof IType && ((IType) parent).isEnum());
    if (Flags.isPublic(flags)) {
      visibilityStart = VISIBILITY_PUBLIC;
    }
    else if (Flags.isProtected(flags) || Flags.isPackageDefault(flags)
        || isEnum) {
      visibilityStart = VISIBILITY_PROTECTED;
    }
    else {
      visibilityStart = VISIBILITY_PRIVATE;
    }
  }

  /**
   * Tries to detect if it is possible to increase visibility<br>
   * For example: Make a <code>public</code> member/method/class
   * <code>protected</code>.
   */
  void checkVisibility(IJavaElement foundElement, int found) {
    if (!Prefs.isCheckIncreaseVisibility()) {
      return;
    }
    IType startType = JavaElementUtil.getTypeFor(startElement);
    IType foundType = JavaElementUtil.getTypeFor(foundElement);
    if (startType == null || foundType == null) {
      // reference in xml file found!
      setMaxVisibilityFound(VISIBILITY_PUBLIC);
      return;
    }
    if (startType.equals(foundType)) {
      setMaxVisibilityFound(VISIBILITY_PRIVATE);
      return;
    }
    IPackageFragment startPackage = JavaElementUtil.getPackageFor(startElement);
    IPackageFragment foundPackage = JavaElementUtil.getPackageFor(foundElement);
    if (startPackage.getElementName().equals(foundPackage.getElementName())) {
      setMaxVisibilityFound(VISIBILITY_PROTECTED);
      return;
    }
    setMaxVisibilityFound(VISIBILITY_PUBLIC);
    if (found > Prefs.getWarnLimit()) {
      throw new OperationCanceledException("Cancel Search: public found"); //$NON-NLS-1$
    }
  }

  /**
   * Create a marker: "Change visibility of ClassName to private"
   * @return <code>true</code>, if a marker was created
   */
  boolean createMarker(IMember member, int line, int found)
      throws CoreException {
    if (!needVisibilityMarker(member, found)) {
      return false;
    }
    // Fix for BUG 1925549
    // Exclude overridden methods from visibility detection
    if (member instanceof IMethod) {
      // TODO 01.09.2008: This is already done SearchManager.searchMethods(List<IMethod>)
      if (JavaElementUtil.isOverriddenMethod((IMethod) member)) {
        return false;
      }
    }
    boolean isPrivate = maxVisibilityFound == VISIBILITY_PRIVATE;
    // classes can't be protected!
    String type = isPrivate ? MarkerFactory.ANALYZE_MARKER_VISIBILITY_PRIVATE
        : (member instanceof IType) ? MarkerFactory.ANALYZE_MARKER_VISIBILITY_DEFAULT
            : MarkerFactory.ANALYZE_MARKER_VISIBILITY_PROETECTED;
    return markerFactory.createVisibilityMarker(member, type, line);
  }

  /**
   * Set the maximum visibility found. Order is:
   * <ul>
   * <li>VISIBILITY_PRIVATE = 0</li>
   * <li>VISIBILITY_PROTECTED = 1</li>
   * <li>VISIBILITY_PUBLIC = 2</li>
   * </ul>
   */
  private void setMaxVisibilityFound(int visibility) {
    maxVisibilityFound = Math.max(maxVisibilityFound, visibility);
  }

  /**
   * @return <code>true</code> when the member visibility can be decreased
   *         to private or protected
   */
  private boolean needVisibilityMarker(IMember member, int found) {
    boolean decreaseVisibility = visibilityStart > maxVisibilityFound;
    return (found > 0 && Prefs.isCheckIncreaseVisibility() && decreaseVisibility);
  }

  boolean isMaxVisibilityFoundPublic() {
    return maxVisibilityFound == VISIBILITY_PUBLIC;
  }
}
