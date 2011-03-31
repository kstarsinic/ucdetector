/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.ucdetector.preferences.Prefs;

/**
 * Simple logging API for UCDetector.
 * <p>
 * @see "http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in_%3F"
 * @author Joerg Spieler
 * @since 2008-09-13
 */
@SuppressWarnings("nls")
public class Log {
  public enum LogLevel {
    DEBUG, INFO, WARN, ERROR, OFF,
  }

  private static boolean isLogInited;
  /**
   * To activate debug traces add line
   * <pre>org.ucdetector/debug=true</pre>
   * to file ECLIPSE_INSTALL_DIR\.options
   *
   * @see "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
   */
  private static LogLevel LOG_LEVEL_OPTIONS_FILE;

  //  private static MessageConsole console;
  //  private static PrintStream consoleStreamInfo;
  //  private static PrintStream consoleStreamWarn;

  private static void initLog() {
    if (isLogInited) {
      return;
    }
    isLogInited = true;
    setActiveLogLevel(Prefs.getLogLevel());
    setLogToEclipse(Prefs.isLogToEclipse());
    LOG_LEVEL_OPTIONS_FILE = getLogLevelOption("org.ucdetector/logLevel");
    if (getActiveLogLevel().ordinal() > LogLevel.INFO.ordinal()) {
      System.out.println("UCDetector Log level: " + getActiveLogLevel()); // we need to log to System.out
    }
    if (LOG_LEVEL_OPTIONS_FILE != null) {
      warn("Eclipse .options file overrides preferences log level. Log level is: " + LOG_LEVEL_OPTIONS_FILE);
    }
  }

  // DEBUG --------------------------------------------------------------------
  public static void debug(String format, Object... args) {
    if (isDebug()) {
      log(LogLevel.DEBUG, args.length == 0 ? format : String.format(format, args));
    }
  }

  // INFO ---------------------------------------------------------------------
  public static void info(String format, Object... args) {
    if (isInfo()) {
      log(LogLevel.INFO, args.length == 0 ? format : String.format(format, args));
    }
  }

  // WARN ---------------------------------------------------------------------
  public static void warn(String format, Object... args) {
    log(LogLevel.WARN, args.length == 0 ? format : String.format(format, args));
  }

  // ERROR --------------------------------------------------------------------
  public static void error(String message) {
    logImpl(LogLevel.ERROR, message, null);
  }

  public static void error(String message, Throwable ex) {
    logImpl(LogLevel.ERROR, message, ex);
  }

  // SUCCESS ------------------------------------------------------------------
  public static void success(boolean success, String message) {
    log(success ? LogLevel.INFO : LogLevel.WARN, success ? "OK: " : "FAIL: " + message);
  }

  // LOG ------------------------------------------------------------------------
  public static void log(LogLevel level, String message) { // NO_UCD
    logImpl(level, message, null);
  }

  // STATUS -------------------------------------------------------------------
  public static void status(IStatus status) { // NO_UCD
    if (status.getSeverity() == IStatus.ERROR) {
      error(status.getMessage(), status.getException());
    }
    else if (status.getSeverity() == IStatus.WARNING) {
      warn(status.getMessage());
    }
    else {
      info(status.getMessage());
    }
  }

  // LOG IMPL -------------------------------------------------------------------
  /**
   * Very simple logging to System.out and System.err
   */
  private static void logImpl(LogLevel level, String message, Throwable ex) {
    initLog();
    if (level.ordinal() < getActiveLogLevel().ordinal()) {
      return;
    }
    boolean isWarn = level.ordinal() > LogLevel.INFO.ordinal();
    String formattedMessage = String.format("%-5s: %s", level, message);
    logImplStream(message, ex, isWarn);
    if (!UCDetectorPlugin.isHeadlessMode() && logToEclipse) {
      UCDetectorConsole.log(isWarn, formattedMessage, ex);
    }
  }

  private static void logImplStream(String message, Throwable ex, boolean isWarn) {
    PrintStream stream = isWarn ? System.err : System.out;
    if (message != null) {
      stream.println(message);
    }
    if (ex != null) {
      ex.printStackTrace(stream);
    }
  }

  /**
   * @param key found in .options, for example "org.ucdetector/debug/search"
   * @return  <code>true</code> when debug option is set
   */
  public static boolean isDebugOption(String key) {
    String option = Platform.getDebugOption(key);
    return "true".equalsIgnoreCase(option);
  }

  private static LogLevel getLogLevelOption(String key) {
    String option = Platform.getDebugOption(key);
    for (LogLevel logLevel : LogLevel.values()) {
      if (logLevel.toString().equals(option)) {
        return logLevel;
      }
    }
    return null;
  }

  public static boolean isDebug() {
    return (getActiveLogLevel() == LogLevel.DEBUG);
  }

  public static boolean isInfo() { // NO_UCD
    return getActiveLogLevel() == LogLevel.DEBUG || getActiveLogLevel() == LogLevel.INFO;
  }

  private static LogLevel activeLogLevel = LogLevel.INFO;

  protected static void setActiveLogLevel(LogLevel logLevel) {
    //    System.out.println("NEW LOG LEVEL: " + logLevel);
    Log.activeLogLevel = logLevel;
  }

  protected static LogLevel getActiveLogLevel() {
    return LOG_LEVEL_OPTIONS_FILE == null ? activeLogLevel : LOG_LEVEL_OPTIONS_FILE;
  }

  protected static String getCanonicalPath(File file) {
    if (file == null) {
      return null;
    }
    try {
      return file.getCanonicalPath();
    }
    catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  private static boolean logToEclipse;

  static void setLogToEclipse(boolean log) {
    //    System.out.println("NEW LOG TO ECLIPSE: " + log);
    Log.logToEclipse = log;
  }
}
