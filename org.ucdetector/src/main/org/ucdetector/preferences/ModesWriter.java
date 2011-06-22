/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import static org.ucdetector.preferences.UCDetectorPreferencePage.GROUP_START;
import static org.ucdetector.preferences.UCDetectorPreferencePage.TAB_START;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Write modes file
 * <p>
 * @author Joerg Spieler
 * @since 22.06.2011
 */
public class ModesWriter {
  static final String MODES_FILE_TYPE = ".properties"; //$NON-NLS-1$
  private final List<String> extendedPreferences;

  public ModesWriter(List<String> extendedPreferences) {
    this.extendedPreferences = extendedPreferences;
  }

  /** Save it to a file in WORKSPACE/.metadata/.plugins/org.ucdetector/modes  */
  @SuppressWarnings("nls")
  void saveMode(String modeName) {
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    allPreferences.putAll(UCDetectorPlugin.getDeltaPreferences());
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    sb.append(String.format("###               UCDetector preference file for mode: '%s'%n", modeName));
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    sb.append(String.format("### Created by  : UCDetector %s%n", UCDetectorPlugin.getAboutUCDVersion()));
    sb.append(String.format("### Created date: %s%n", UCDetectorPlugin.getNow()));
    sb.append(String.format("### java.util.Properties.load() may fail to load this file%n"));
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    Map<String, String> groupPrefs = new LinkedHashMap<String, String>();
    for (String extendedPreference : extendedPreferences) {
      if (extendedPreference.startsWith(TAB_START)) {
        flushGroupPrefs(groupPrefs, sb);
        String tab = extendedPreference.substring(TAB_START.length());
        sb.append(String.format("%n## --------------------------------------------------------------------------%n"));
        sb.append(String.format("## Tab: %s%n", tab.startsWith("&") ? tab.substring(1) : tab));
        sb.append(String.format("## --------------------------------------------------------------------------%n"));
      }
      else if (extendedPreference.startsWith(GROUP_START)) {
        flushGroupPrefs(groupPrefs, sb);
        sb.append(String.format("%n# Group: %s%n", extendedPreference.substring(GROUP_START.length())));
      }
      else {
        groupPrefs.put(extendedPreference, allPreferences.get(extendedPreference));
        allPreferences.remove(extendedPreference);
      }
    }
    flushGroupPrefs(groupPrefs, sb);
    String fileText = sb.toString();
    if (Log.isDebug()) {
      Log.debug(fileText);
      Log.debug("Unhandled preferences :" + allPreferences);
    }
    File modesFile = getModesFile(modeName);
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(modesFile), UCDetectorPlugin.UTF_8);
      writer.write(fileText);
      Log.debug("Saved mode to: %s", modesFile.getAbsolutePath()); //$NON-NLS-1$
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_ModeFileCantSave, modesFile.getAbsolutePath());
      UCDetectorPlugin.logToEclipseLog(message, ex);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
  }

  /** Nice key value formatting only */
  private static void flushGroupPrefs(Map<String, String> groupPrefs, StringBuilder sb) {
    int maxKeyLength = 0;
    for (String key : groupPrefs.keySet()) {
      maxKeyLength = Math.max(maxKeyLength, key.length());
    }
    String format = MessageFormat.format("%-{0}s = %s%n", String.valueOf(maxKeyLength)); //$NON-NLS-1$
    for (Entry<String, String> entry : groupPrefs.entrySet()) {
      sb.append(String.format(format, entry.getKey(), entry.getValue()));
    }
    groupPrefs.clear();
  }

  static File getModesFile(String modeName) {
    return new File(UCDetectorPlugin.getModesDir(), modeName + MODES_FILE_TYPE);
  }
}
