/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.cycle.CyclePlugin;

/**
 * Class used to initialize default preference values.
 */
public class CyclePreferenceInitializer extends AbstractPreferenceInitializer { // NO_UCD

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = CyclePlugin.getDefault().getPreferenceStore();
    store.setDefault(CyclePrefs.CYCLE_DEPTH, CyclePrefs.CYCLE_DEPTH_DEFAULT);
  }
}
