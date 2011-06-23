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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.util.FileUtils;
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
@SuppressWarnings("nls")
public class ModesWriter {
  private static final String HEADLESS_PROPERTIES = "headless.properties";
  static final String MODES_FILE_TYPE = ".properties";
  private static String headlessPropertiesContent;
  private final List<String> extendedPreferences;

  public ModesWriter(List<String> extendedPreferences) {
    this.extendedPreferences = extendedPreferences;
  }

  /** Save it to a file in WORKSPACE/.metadata/.plugins/org.ucdetector/modes  */
  void saveMode(String modeName) {
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    allPreferences.putAll(UCDetectorPlugin.getDeltaPreferences());
    StringBuilder text = new StringBuilder();
    text.append(String.format("### -------------------------------------------------------------------------%n"));
    text.append(String.format("###               UCDetector preference file for mode: '%s'%n", modeName));
    text.append(String.format("### -------------------------------------------------------------------------%n"));
    text.append(String.format("### Created by  : UCDetector %s%n", UCDetectorPlugin.getAboutUCDVersion()));
    text.append(String.format("### Created date: %s%n", UCDetectorPlugin.getNow()));
    text.append(String.format("### java.util.Properties.load() may fail to load this file%n"));
    text.append(String.format("### -------------------------------------------------------------------------%n"));
    Map<String, String> groupPrefs = new LinkedHashMap<String, String>();
    for (String extendedPreference : extendedPreferences) {
      if (extendedPreference.startsWith(TAB_START)) {
        flushGroupPrefs(groupPrefs, text);
        String tab = extendedPreference.substring(TAB_START.length());
        text.append(String.format("%n## --------------------------------------------------------------------------%n"));
        text.append(String.format("## Tab: %s%n", tab.startsWith("&") ? tab.substring(1) : tab));
        text.append(String.format("## --------------------------------------------------------------------------%n"));
      }
      else if (extendedPreference.startsWith(GROUP_START)) {
        flushGroupPrefs(groupPrefs, text);
        text.append(String.format("%n# Group: %s%n", extendedPreference.substring(GROUP_START.length())));
      }
      else {
        groupPrefs.put(extendedPreference, allPreferences.get(extendedPreference));
        allPreferences.remove(extendedPreference);
      }
    }
    flushGroupPrefs(groupPrefs, text);
    appendHeadlessProperties(text);
    String fileText = text.toString();
    if (Log.isDebug()) {
      Log.debug(fileText);
      Log.debug("Unhandled preferences :" + allPreferences);
    }
    File modesFile = getModesFile(modeName);
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(modesFile), UCDetectorPlugin.UTF_8);
      writer.write(fileText);
      Log.debug("Saved mode to: %s", modesFile.getAbsolutePath());
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_ModeFileCantSave, modesFile.getAbsolutePath());
      UCDetectorPlugin.logToEclipseLog(message, ex);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
  }

  private static void appendHeadlessProperties(StringBuilder sb) {
    try {
      if (headlessPropertiesContent == null) {
        InputStream in = ModesWriter.class.getResourceAsStream(HEADLESS_PROPERTIES);
        headlessPropertiesContent = FileUtils.readFully(new InputStreamReader(in, UCDetectorPlugin.UTF_8));
      }
    }
    catch (IOException ex) {
      headlessPropertiesContent = "";
      Log.error(ex, "Can't read %s", HEADLESS_PROPERTIES);
    }
    sb.append(headlessPropertiesContent);
  }

  /** Nice key value formatting only */
  private static void flushGroupPrefs(Map<String, String> groupPrefs, StringBuilder sb) {
    int maxKeyLength = 0;
    for (String key : groupPrefs.keySet()) {
      maxKeyLength = Math.max(maxKeyLength, key.length());
    }
    String format = MessageFormat.format("%-{0}s = %s%n", String.valueOf(maxKeyLength));
    for (Entry<String, String> entry : groupPrefs.entrySet()) {
      sb.append(String.format(format, entry.getKey(), entry.getValue()));
    }
    groupPrefs.clear();
  }

  static File getModesFile(String modeName) {
    return new File(UCDetectorPlugin.getModesDir(), modeName + MODES_FILE_TYPE);
  }
}
