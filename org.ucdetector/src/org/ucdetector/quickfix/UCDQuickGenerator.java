/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.ucdetector.Log;
import org.ucdetector.util.MarkerFactory;

/**
 * Generates Marker Resolutions<br>
 * 
 * @see "http://wiki.eclipse.org/FAQ_How_do_I_implement_Quick_Fixes_for_my_own_language%3F"
 * See extension point="org.eclipse.ui.ide.markerResolution" in plugin.xml
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-22
 */
public class UCDQuickGenerator implements IMarkerResolutionGenerator2 {

  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      String markerType = marker.getType();
      if (Log.isDebug()) {
        Log.logDebug("UCDQuickFixer.getResolutions() for: " + markerType); //$NON-NLS-1$
      }
      List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
      resolutions.add(new TodoQuickFix(marker));
      if (MarkerFactory.UCD_MARKER_UNUSED.equals(markerType)) {
        resolutions.add(new DeleteQuickFix(marker));
        resolutions.add(new LineCommentQuickFix(marker));
      }
      else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)
          || MarkerFactory.UCD_MARKER_USE_PROTECTED.equals(markerType)
          || MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(markerType)) {
        resolutions.add(new VisibilityQuickFix(marker));
      }
      else if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(markerType)) {
        resolutions.add(new UseFinalQuickFix(marker));
      }
      resolutions.add(new NoUcdTagQuickFix(marker));
      resolutions.add(new UseSuppressWarningsQuickFix(marker));
      return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
    }
    catch (CoreException e) {
      Log.logError("Can't get UCDetector resolutions for marker: " + marker, e); //$NON-NLS-1$
    }
    return new IMarkerResolution[0];
  }

  public boolean hasResolutions(IMarker marker) {
    try {
      return !MarkerFactory.UCD_MARKER_USED_FEW.equals(marker.getType());
    }
    catch (CoreException e) {
      Log.logError("Can't get UCD resolutions for marker: " + marker, e); //$NON-NLS-1$
      return false;
    }
  }
}