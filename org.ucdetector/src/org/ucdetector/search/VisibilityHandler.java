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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
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
    private final int value;

    VISIBILITY(int value) {
      this.value = value;

    }
  }

  private final VISIBILITY visibilityStart;
  private VISIBILITY visibilityMaxFound = VISIBILITY.PRIVATE;
  //
  private final IMember startElement;
  private final MarkerFactory markerFactory;

  VisibilityHandler(MarkerFactory markerFactory, IMember startElement)
      throws JavaModelException {
    this.markerFactory = markerFactory;
    this.startElement = startElement;
    int flags = startElement.getFlags();
    if (Flags.isPublic(flags)) {
      visibilityStart = VISIBILITY.PUBLIC;
    }
    else if (Flags.isProtected(flags) || Flags.isPackageDefault(flags)) {
      visibilityStart = VISIBILITY.PROTECTED;
    }
    else {
      visibilityStart = VISIBILITY.PRIVATE;
    }
  }

  /**
   * Tries to detect if it is possible to increase visibility<br>
   * For example: Make a <code>public</code> member/method/class
   * <code>protected</code>.
   */
  void checkVisibility(IJavaElement foundElement, int found) {
    if (!Prefs.isCheckIncreaseVisibilityProtected() &&
            !Prefs.isCheckIncreaseVisibilityToPrivate()) {
      return;
    }
    IType startType = JavaElementUtil.getTypeFor(startElement);
    IType foundType = JavaElementUtil.getTypeFor(foundElement);
    if (startType == null || foundType == null) {
      // reference in xml file found!
      setMaxVisibilityFound(VISIBILITY.PUBLIC);
      return;
    }
    if (startType.equals(foundType)) {
      setMaxVisibilityFound(VISIBILITY.PRIVATE);
      return;
    }
    IPackageFragment startPackage = JavaElementUtil.getPackageFor(startElement);
    IPackageFragment foundPackage = JavaElementUtil.getPackageFor(foundElement);
    if (startPackage.getElementName().equals(foundPackage.getElementName())) {
      setMaxVisibilityFound(VISIBILITY.PROTECTED);
      return;
    }
    setMaxVisibilityFound(VISIBILITY.PUBLIC);
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
    if (startElement instanceof IField) {
      IField field = (IField) startElement;
      if (field.isEnumConstant()) {
        return false;
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
          type = MarkerFactory.UCD_MARKER_USE_PROETECTED;
        }
        break;
      default:
        return false;
    }
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
    return found > 0 && decreaseVisibility &&
            (Prefs.isCheckIncreaseVisibilityProtected() && visibilityMaxFound == VISIBILITY.PROTECTED
            || Prefs.isCheckIncreaseVisibilityToPrivate() && visibilityMaxFound == VISIBILITY.PRIVATE);
  }

  boolean isMaxVisibilityFoundPublic() {
    return visibilityMaxFound == VISIBILITY.PUBLIC;
  }
}
