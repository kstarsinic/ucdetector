/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Read modes file
 * <p>
 * @author Joerg Spieler
 * @since 23.06.2011
 */
@SuppressWarnings("nls")
public class ModesReader {
  private static final String MULTILINE_END = "\\";

  /**
   * 
   * [ 3025571 ] Exception loading modes: Malformed  &#92;uxxxx encoding
   * <p>
   * java.util.Properties.load() fails, because of file names containing Strings (file names)
   *  which are similar to unicode signs
   * <p>
   * @param isFile <code>true</code>, when it is a file, <code>false</code>, when it is a resource in classpath
   * @param modeFileName name of mode file or mode resource
   * @return Map containing key value pairs loaded from modeFileName
   */
  public static Map<String, String> loadModeFile(boolean isFile, String modeFileName) {
    Map<String, String> result = new HashMap<String, String>();
    InputStream inStream = null;
    try {
      inStream = getInputStream(isFile, modeFileName);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, UCDetectorPlugin.UTF_8));
      String line = null;
      boolean isInsideMultiLine = false;
      String key = null;
      String value = "";
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        int indexEquals = line.indexOf('=');
        boolean isCommentLine = line.startsWith("#");
        if (isCommentLine || line.length() == 0) {
          continue;
        }
        boolean isMultiLine = line.endsWith(MULTILINE_END);
        line = line.substring(0, line.length() - (isMultiLine ? 1 : 0));

        if (isInsideMultiLine) {
          value += line;
          if (!isMultiLine && key != null) {
            result.put(key.trim(), value.trim());
          }
        }
        else {
          key = (indexEquals == -1) ? line : line.substring(0, indexEquals);
          value = (indexEquals == -1) ? "" : line.substring(indexEquals + 1);
          result.put(key.trim(), value.trim());
        }
        isInsideMultiLine = isMultiLine;
        // # org.ucdetector.internal.headless.resourcesToIterate = \
        // #  org.ucdetector/src/main,\
        // #  org.ucdetector.example
      }
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_CantSetPreferences, modeFileName);
      UCDetectorPlugin.logToEclipseLog(message, ex);
    }
    finally {
      UCDetectorPlugin.closeSave(inStream);
    }
    return result;
  }

  private static InputStream getInputStream(boolean isFile, String modeFileName) throws FileNotFoundException {
    if (isFile) {
      return new FileInputStream(modeFileName);
    }
    return ModesReader.class.getResourceAsStream("modes/" + modeFileName);
  }

  //  public static void main(String[] args) throws FileNotFoundException, IOException {
  //    Map<String, String> map = loadModeFile(true, "/home/js/aaa.properties");
  //    dumpMap(map);
  //    System.out.println();
  //    Properties map2 = new Properties();
  //    map2.load(new FileInputStream(new File("/home/js/aaa.properties")));
  //    dumpMap(map2);
  //  }

  //  private static boolean isValidFileChar(char c) {
  //    if (c < 32 || c > 127) {
  //      return false;
  //    }
  //    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
  //      return true;
  //    }
  //    return " !#$%&'()+,-.=?@[]^_".indexOf(c) > -1;
  //  }
  //  private static void dumpMap(Map<String, String> map) {
  //    for (Entry<String, String> entry : map.entrySet()) {
  //      System.out.printf("%s=%s%n", entry.getKey(), entry.getValue());
  //    }
  //  }
  //
  //  private static void dumpMap(Properties map) {
  //    for (Entry<?, ?> entry : map.entrySet()) {
  //      System.out.printf("%s=%s%n", entry.getKey(), entry.getValue());
  //    }
  //  }
}
