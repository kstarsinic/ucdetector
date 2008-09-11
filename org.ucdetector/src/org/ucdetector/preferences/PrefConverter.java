/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.UCDetectorPlugin;

/**
 * Converts old versions of Preferences
 */
class PrefConverter {
  private static final String LIST_SEPARATOR_OLD = ";"; //$NON-NLS-1$
  //
  private static final String[] LIST_SEPARATORS_TO_REPLACE = new String[] {
      Prefs.FILTER_SOURCE_FOLDER, Prefs.FILTER_PACKAGE, Prefs.FILTER_CLASS,
      Prefs.FILTER_METHOD, Prefs.FILTER_FIELD, Prefs.ANALYZE_LITERALS };
  private static final String[] WARN_LEVEL_TO_REPLACE = new String[] {
      Prefs.ANALYZE_CLASSES, Prefs.ANALYZE_MEHTODS, Prefs.ANALYZE_FIELDS,
      Prefs.ANALYZE_VISIBILITY_PROTECTED, Prefs.ANALYZE_FINAL_FIELD,
      Prefs.ANALYZE_FINAL_METHOD };

  private static final String UCD_PREFERENCE_VERSION //
  = UCDetectorPlugin.ID + ".preference.version"; //$NON-NLS-1$
  private static final int UCD_PREFERENCE_VERSION_VALUE = 10;

  /**
   * From UCDetector 0.9 to 0.10 the list separator changed from ";" to ",".
   * So it behaves like file name field in eclipse text search!<p>
   * From UCDetector 0.9 to 0.10 WarnLevel changes from Error to ERROR
   */
  static void convert09to10(IPreferenceStore store) {
    if (store.getInt(UCD_PREFERENCE_VERSION) == UCD_PREFERENCE_VERSION_VALUE) {
      return;
    }
    store.setValue(UCD_PREFERENCE_VERSION, UCD_PREFERENCE_VERSION_VALUE);
    // "*.xml;*.java;" -> "*.xml;*.java;"
    for (String name : PrefConverter.LIST_SEPARATORS_TO_REPLACE) {
      String value = store.getString(name);
      if (value.indexOf(LIST_SEPARATOR_OLD) > 0) {
        value = value.replace(LIST_SEPARATOR_OLD, Prefs.LIST_SEPARATOR);
        store.setValue(name, value);
      }
    }
    // "Warning" -> "WARNING"
    for (String name : PrefConverter.WARN_LEVEL_TO_REPLACE) {
      String value = store.getString(name);
      if (value.length() > 0 && Character.isLowerCase(value.charAt(1))) {
        store.setValue(name, value.toUpperCase());
      }
    }
  }
}
