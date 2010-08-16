/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Simple logging API for UCDetector.
 * <p>
 * @see "http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in_%3F"
 */
@SuppressWarnings("nls")
public class Log {
  public enum LogLevel {
    DEBUG, INFO, WARN, ERROR, OFF,
  }

  /**
   * To activate debug traces add line
   * <pre>org.ucdetector/debug=true</pre>
   * to file ECLIPSE_INSTALL_DIR\.options
   * 
   * @see "http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F"
   */
  private static final LogLevel LOG_LEVEL_OPTIONS_FILE;
  private static MessageConsole console;
  private static PrintStream consoleStreamInfo;
  private static PrintStream consoleStreamWarn;

  static {
    activeLogLevel = LogLevel.INFO;
    LOG_LEVEL_OPTIONS_FILE = getLogLevelOption("org.ucdetector/logLevel");
    if (Log.getAcitveLogLevel().ordinal() > Log.LogLevel.INFO.ordinal()) {
      System.out.println("UCDetector Log level: " + Log.getAcitveLogLevel()); // we need to log to System.out
    }
    if (LOG_LEVEL_OPTIONS_FILE != null) {
      logWarn("Eclipse .options file overrides preferences log level. Log level is: " + LOG_LEVEL_OPTIONS_FILE);
    }
  }

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

  public static void logInfo(String format, Object... args) {
    logInfo(String.format(format, args));
  }

  public static void logSuccess(boolean success, String message) {
    if (success) {
      Log.logInfo("OK: " + message);
    }
    else {
      Log.logWarn("FAIL: " + message);
    }
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

  // LOG IMPL -------------------------------------------------------------------
  private static void logImpl(LogLevel level, String message) {
    logImpl(level, message, null);
  }

  /**
   * Very simple logging to System.out and System.err
   */
  private static void logImpl(LogLevel level, String message, Throwable ex) {
    if (level.ordinal() < getAcitveLogLevel().ordinal()) {
      return;
    }
    boolean isWarn = level.ordinal() > LogLevel.INFO.ordinal();
    String formattedMessage = String.format("%-5s: %s", level, message);
    logToStream(isWarn ? System.err : System.out, formattedMessage, ex);
    logToEclipseConsole(isWarn, formattedMessage, ex);
  }

  static boolean logToEclipse;

  private static void logToEclipseConsole(boolean isWarn, String formattedMessage, Throwable ex) {
    if (UCDetectorPlugin.isHeadlessMode() || !logToEclipse /*!Prefs.isLogToEclipse()*/) {
      return;
    }
    if (console == null) {
      initConsole();
    }
    logToStream(isWarn ? consoleStreamWarn : consoleStreamInfo, formattedMessage, ex);
  }

  private static void logToStream(PrintStream stream, String message, Throwable ex) {
    stream.println(message);
    if (ex != null) {
      ex.printStackTrace(stream);
    }
  }

  private static void initConsole() {
    console = new MessageConsole("UCDetector", null);
    ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
    consoleStreamInfo = new PrintStream(console.newMessageStream());
    MessageConsoleStream messageStream = console.newMessageStream();
    messageStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    consoleStreamWarn = new PrintStream(messageStream);
    try {
      UCDetectorPlugin.getActivePage().showView(IConsoleConstants.ID_CONSOLE_VIEW);
    }
    catch (PartInitException ex) {
      logError("Can't init console", ex);
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
    return (getAcitveLogLevel() == LogLevel.DEBUG);
  }

  static LogLevel activeLogLevel = LogLevel.INFO;

  protected static LogLevel getAcitveLogLevel() {
    return LOG_LEVEL_OPTIONS_FILE == null ? activeLogLevel : LOG_LEVEL_OPTIONS_FILE;
  }
}
