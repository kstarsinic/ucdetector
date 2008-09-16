package org.ucdetector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @see http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in_%3F
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

  public static Status logErrorAndStatus(String message, Throwable ex) {
    Status status = new Status(IStatus.ERROR, UCDetectorPlugin.ID,
        IStatus.ERROR, message, ex);
    Log.logStatus(status);
    return status;
  }

  /**
   * @param status which is be logged to default log
   */
  public static void logStatus(IStatus status) {
    UCDetectorPlugin ucd = UCDetectorPlugin.getDefault();
    if (ucd != null && ucd.getLog() != null) {
      ucd.getLog().log(status);
    }
    if (status.getSeverity() == IStatus.ERROR) {
      logError(status.getMessage(), status.getException());
    }
    else if (status.getSeverity() == IStatus.WARNING) {
      logWarn(status.getMessage());
    }
    else if (status.getSeverity() == IStatus.INFO) {
      logInfo(status.getMessage());
    }
  }

  /**
   * Very simple logging to System.out and System.err
   */
  private static void logImpl(String level, String message, Throwable ex) {
    if (!UCDetectorPlugin.DEBUG) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(level).append(": ").append(message == null ? "" : message); //$NON-NLS-1$ //$NON-NLS-2$
    if (UCDetectorPlugin.DEBUG
        && (LOG_LEVEL_DEBUG.equals(level) || LOG_LEVEL_INFO.equals(level))) {
      System.out.println(sb.toString());
    }
    else if (LOG_LEVEL_WARN.equals(level) || LOG_LEVEL_ERROR.equals(level)) {
      System.err.println(sb.toString());
      if (ex != null) {
        ex.printStackTrace();
      }
    }
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
}
