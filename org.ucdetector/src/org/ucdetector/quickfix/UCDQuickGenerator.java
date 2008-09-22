/**
 * Copyright (c) 2008 Joerg Spieler
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
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 * Generates Marker Resolutions<br>
 * @see http://wiki.eclipse.org/FAQ_How_do_I_implement_Quick_Fixes_for_my_own_language%3F
 * @see extension point="org.eclipse.ui.ide.markerResolution" in plugin.xml
 */
public class UCDQuickGenerator implements IMarkerResolutionGenerator2 { // NO_UCD
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("UCDQuickFixer.getResolutions()" + marker.getType()); //$NON-NLS-1$
      }
      String problem = marker.getType();
      List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
      resolutions.add(new NoUcdTagQuickFix());
      add(resolutions, new NoUcdTagQuickFix());
      if (MarkerFactory.UCD_MARKER_UNUSED.equals(problem)) {
        resolutions.add(new DeleteQuickFix());
        resolutions.add(new LineCommentQuickFix());
      }
      else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(problem)
          || MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(problem)
          || MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(problem)) {
        resolutions.add(new VisibilityQuickFix(marker));
      }
      else if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(problem)) {
        resolutions.add(new UseFinalQuickFix());
      }
      return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
    }
    catch (CoreException e) {
      Log.logError("Can't get UCD resolutions", e); //$NON-NLS-1$
    }
    return new IMarkerResolution[0];
  }

  private void add(List<IMarkerResolution> resolutions,
      AbstractUCDQuickFix quickFix) {
    for (IMarkerResolution resolution : resolutions) {
      if (!(resolution instanceof NoUcdTagQuickFix)
          || !(quickFix instanceof NoUcdTagQuickFix)) {
        resolutions.add(quickFix);
      }
    }
  }

  public boolean hasResolutions(IMarker marker) {
    try {
      return !MarkerFactory.UCD_MARKER_USED_FEW.equals(marker.getType());
    }
    catch (CoreException e) {
      Log.logError("Can't get UCD resolutions", e); //$NON-NLS-1$
      return false;
    }
  }
}