/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.UCDetectorPlugin;

/**
 * Class used to initialize default preference values.
 * See: {@link  org.ucdetector.preferences.Prefs#LIST_SEPARATOR}
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer { // NO_UCD

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = UCDetectorPlugin.getDefault().getPreferenceStore();
    store.setDefault(CyclePrefs.CYCLE_DEPTH, CyclePrefs.CYCLE_DEPTH_DEFAULT);
  }
}
