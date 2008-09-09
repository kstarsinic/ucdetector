/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.UCDetectorPlugin;

/**
 * Class used to initialize default preference values.
 * @see org.ucdetector.preferences.Prefs.LIST_SEPARATOR
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer { // NO_UCD

  private static final String SOURCE_FOLDER_FILTER //
  = "*test*,generated,"; //$NON-NLS-1$
  private static final String PACKAGE_FILTER //
  = "*test*,"; //$NON-NLS-1$
  private static final String CLASS_FILTER //
  = "Test*,*Test"; //$NON-NLS-1$
  private static final String METHOD_FILTER //
  = "test*,*Test"; //$NON-NLS-1$
  private static final String FIELD_FILTER //
  = "test*,*Test"; //$NON-NLS-1$
  private static final String FILE_PATTERN_LITERAL_SEARCH //
  = "*.xml,"; //$NON-NLS-1$

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = UCDetectorPlugin.getDefault().getPreferenceStore();
    // Analyze -----------------------------------------------------------------
    store.setDefault(Prefs.ANALYZE_CLASSES, WarnLevel.WARNING.toString());
    store.setDefault(Prefs.ANALYZE_MEHTODS, WarnLevel.WARNING.toString());
    store.setDefault(Prefs.ANALYZE_FIELDS, WarnLevel.WARNING.toString());
    store.setDefault(Prefs.ANALYZE_VISIBILITY, WarnLevel.WARNING.toString());
    store.setDefault(Prefs.ANALYZE_LITERALS_CHECK, Boolean.TRUE);
    store.setDefault(Prefs.ANALYZE_LITERALS, FILE_PATTERN_LITERAL_SEARCH);
    // Filter ------------------------------------------------------------------
    store.setDefault(Prefs.FILTER_SOURCE_FOLDER, SOURCE_FOLDER_FILTER);
    store.setDefault(Prefs.FILTER_PACKAGE, PACKAGE_FILTER);
    store.setDefault(Prefs.FILTER_CLASS, CLASS_FILTER);
    store.setDefault(Prefs.FILTER_BEAN_METHOD, true);
    store.setDefault(Prefs.FILTER_METHOD, METHOD_FILTER);
    store.setDefault(Prefs.FILTER_FIELD, FIELD_FILTER);
    // Limit -------------------------------------------------------------------
    store.setDefault(Prefs.WARN_LIMIT, 0);
    // final -------------------------------------------------------------------
    store.setDefault(Prefs.ANALYZE_FINAL_FIELD, WarnLevel.IGNORE.toString());
    store.setDefault(Prefs.ANALYZE_FINAL_METHOD, WarnLevel.IGNORE.toString());
    // Cycles ------------------------------------------------------------------
    store.setDefault(Prefs.CYCLE_DEPTH, Prefs.CYCLE_DEPTH_DEFAULT);
    // Report ------------------------------------------------------------------
    store.setDefault(Prefs.REPORT_FILE, ""); //$NON-NLS-1$
  }
}
