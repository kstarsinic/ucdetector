/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.core.runtime.Platform;

/**
 * @see "http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in_%3F"
 */
public class Log {

  private static final String LOG_LEVEL_DEBUG = "DEBUG"; //$NON-NLS-1$
  private static final String LOG_LEVEL_INFO = "INFO "; //$NON-NLS-1$
  private static final String LOG_LEVEL_WARN = "WARN "; //$NON-NLS-1$
  private static final String LOG_LEVEL_ERROR = "ERROR"; //$NON-NLS-1$

  public static void logDebug(String message) {
    Log.logImpl(LOG_LEVEL_DEBUG, message, null);
  }

  public static void logInfo(String message) {
    Log.logImpl(LOG_LEVEL_INFO, message, null);
  }

  public static void logWarn(String message) {
    Log.logImpl(LOG_LEVEL_WARN, message, null);
  }

  public static void logError(String message) {
    Log.logImpl(LOG_LEVEL_ERROR, message, null);
  }

  public static void logError(String message, Throwable ex) {
    Log.logImpl(LOG_LEVEL_ERROR, message, ex);
  }

  /**
   * Very simple logging to System.out and System.err
   */
  private static void logImpl(String level, String message, Throwable ex) {
    if (Log.DEBUG
        && (LOG_LEVEL_DEBUG.equals(level) || LOG_LEVEL_INFO.equals(level))) {
      System.out.println(createLogMessage(level, message));
    }
    else if (LOG_LEVEL_WARN.equals(level) || LOG_LEVEL_ERROR.equals(level)) {
      System.err.println(createLogMessage(level, message));
      if (ex != null) {
        ex.printStackTrace();
      }
    }
  }

  private static StringBuilder createLogMessage(String level, String message) {
    String mes = (message == null) ? "" : message; //$NON-NLS-1$
    StringBuilder sb = new StringBuilder(level.length() + 2 + mes.length());
    sb.append(level).append(": ").append(mes); //$NON-NLS-1$
    return sb;
  }

  /**
   * "org.ucdetector/debug/search"
   */
  public static boolean isDebugOption(String key) {
    String option = Platform.getDebugOption(key);
    return "true".equalsIgnoreCase(option); //$NON-NLS-1$
  }

  public static int getDebugOption(String key, int defaultValue) {
    String option = Platform.getDebugOption(key);
    if (option == null || option.length() == 0) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(option);
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static String getClassName(Object o) {
    StringBuilder sb = new StringBuilder();
    sb.append('[').append(o == null ? "?" : o.getClass().getName()).append(']');//$NON-NLS-1$
    return sb.toString();
  }

  /**
   * To activate debug traces add line
   * <pre>org.ucdetector/debug=true</pre>
   * to file ECLIPSE_INSTALL_DIR\.options
   * 
   * @see "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
   */
  public static final boolean DEBUG = isDebugOption("org.ucdetector/debug"); //$NON-NLS-1$
}
