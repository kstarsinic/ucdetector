/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

/**
 * Simple logging API for UCDetector.
 * <p>
 * @see "http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in_%3F"
 */
public class Log {
  /**
   * To activate debug traces add line
   * <pre>org.ucdetector/debug=true</pre>
   * to file ECLIPSE_INSTALL_DIR\.options
   * 
   * @see "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
   */
  // TODO 2010-05-06: Use log level to warn or error!
  public static final boolean DEBUG = isDebugOption("org.ucdetector/debug"); //$NON-NLS-1$

  enum LogLevel {
    @SuppressWarnings("hiding")
    DEBUG, INFO, WARN, ERROR,
  }

  private static final String LEVEL_SEPARATOR = ": "; //$NON-NLS-1$

  // DEBUG --------------------------------------------------------------------
  public static void logDebug(String message) {
    logImpl(LogLevel.DEBUG, message);
  }

  public static void logDebug(String format, Object... args) {
    logDebug(String.format(format, args));
  }

  // INFO ---------------------------------------------------------------------
  public static void logInfo(String message) {
    logImpl(LogLevel.INFO, message);
  }

  @SuppressWarnings("ucd")
  public static void logInfo(String format, Object... args) {
    logInfo(String.format(format, args));
  }

  // WARN ---------------------------------------------------------------------
  public static void logWarn(String message) {
    logImpl(LogLevel.WARN, message);
  }

  public static void logWarn(String format, Object... args) {
    logWarn(String.format(format, args));
  }

  // ERROR --------------------------------------------------------------------
  public static void logError(String message) {
    logImpl(LogLevel.ERROR, message);
  }

  public static void logError(String message, Throwable ex) {
    logImpl(LogLevel.ERROR, message, ex);
  }

  // STATUS -------------------------------------------------------------------
  public static void logStatus(IStatus status) {
    if (status.getSeverity() == IStatus.ERROR) {
      logError(status.getMessage(), status.getException());
    }
    else if (status.getSeverity() == IStatus.WARNING) {
      logWarn(status.getMessage());
    }
    else {
      logInfo(status.getMessage());
    }
  }

  private static void logImpl(LogLevel level, String message) {
    logImpl(level, message, null);
  }

  /**
   * Very simple logging to System.out and System.err
   */
  private static void logImpl(LogLevel level, String message, Throwable ex) {
    if ((DEBUG && level == LogLevel.DEBUG) || level == LogLevel.INFO) {
      System.out.println(createLogMessage(level, message));
    }
    else if (level == LogLevel.WARN || level == LogLevel.ERROR) {
      System.err.println(createLogMessage(level, message));
      if (ex != null) {
        ex.printStackTrace();
      }
    }
  }

  private static String createLogMessage(LogLevel level, String message) {
    int length = level.name().length() + LEVEL_SEPARATOR.length() + (message == null ? 0 : message.length());
    StringBuilder sb = new StringBuilder(length);
    sb.append(level).append(LEVEL_SEPARATOR).append(message);
    return sb.toString();
  }

  /**
   * @param key found in .options, for example "org.ucdetector/debug/search"
   * @return  <code>true</code> when debug option is set
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
    return String.format("[%s]", o == null ? "?" : o.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
