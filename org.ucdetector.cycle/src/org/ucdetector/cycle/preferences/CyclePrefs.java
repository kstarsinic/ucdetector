/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.preferences;

import org.ucdetector.cycle.CyclePlugin;

public class CyclePrefs {
  // CYCLE ---------------------------------------------------------------------
  public static final String CYCLE_DEPTH //
  = CyclePlugin.ID + ".cycleDepth"; //$NON-NLS-1$
  public static final int CYCLE_DEPTH_MIN = 2; // // NO_UCD
  public static final int CYCLE_DEPTH_DEFAULT = 4; //
  public static final int CYCLE_DEPTH_MAX = 8; //

  // CYCLE ---------------------------------------------------------------------
  /**
   * @return Maximum depth of searching for class cycles
   */
  public static int getCycleDepth() {
    int cycleDepth = CyclePlugin.getDefault().getPreferenceStore().getInt(CYCLE_DEPTH);
    return cycleDepth < CYCLE_DEPTH_MIN ? CYCLE_DEPTH_MIN : cycleDepth > CYCLE_DEPTH_MAX ? CYCLE_DEPTH_MAX : cycleDepth;
  }
}
