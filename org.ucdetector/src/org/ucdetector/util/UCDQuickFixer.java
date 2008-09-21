/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * Generates Marker Resolutions<br>
 * @see http://wiki.eclipse.org/FAQ_How_do_I_implement_Quick_Fixes_for_my_own_language%3F
 * @see extension point="org.eclipse.ui.ide.markerResolution" in plugin.xml
 */
public class UCDQuickFixer implements IMarkerResolutionGenerator2 { // NO_UCD
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("UCDQuickFixer.getResolutions()" + marker); //$NON-NLS-1$
      }
      // we can use only use String, Boolean, Integer here,
      // no IJavaElements are permitted here!
      String javaElement = (String) marker
          .getAttribute(MarkerFactory.JAVA_ELEMENT_ATTRIBUTE);
      return new IMarkerResolution[] { //
      new UCDQuickFix(marker.getType(), javaElement),//
      };
    }
    catch (CoreException e) {
      Log.logError("Can't get UCD resolutions", e);
      e.printStackTrace();
    }
    return new IMarkerResolution[0];
  }

  public boolean hasResolutions(IMarker marker) {
    return true;
  }
}