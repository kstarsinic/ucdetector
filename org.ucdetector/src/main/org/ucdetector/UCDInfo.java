/**
 * Copyright (c) 2013 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.ProductProperties;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.osgi.framework.Bundle;
import org.ucdetector.Log.LogLevel;

/**
 * Get usefull information about eclipse, ucdetector, memory, files...
 * <p>
 * @author Joerg Spieler
 * @since 12.04.2013
 */
@SuppressWarnings("nls")
public class UCDInfo {
  private static final int MEGA_BYTE = 1024 * 1024;

  public static String getJavaVersion() {
    return System.getProperty("java.runtime.version");
  }

  public static String getOS() {
    return System.getProperty("os.name") + " - " + System.getProperty("os.version");
  }

  /** @return the version of the platform plugin (= version of Eclipse about dialog or splash screen) */
  public static String getEclipseVersion() {
    //System.getProperty("osgi.framework.version");// values is 3.8.0 for eclipse 4.2.0
    return getBundelVersion(Platform.getBundle("org.eclipse.platform"));
  }

  public static String getUCDVersion() {
    return getBundelVersion(UCDetectorPlugin.getDefault().getBundle());
  }

  private static String getBundelVersion(Bundle bundle) {
    return String.valueOf(bundle.getHeaders().get("Bundle-Version"));
  }

  public static String getEclipseHome() {
    String eclipseHome = System.getProperty("osgi.install.area");
    if (eclipseHome != null && eclipseHome.startsWith("file:")) {
      return eclipseHome.substring("file:".length());
    }
    return eclipseHome;
  }

  public static String getEclipseProduct() {
    IProduct product = Platform.getProduct();
    if (product == null) {
      // see: org.eclipse.ui.internal.dialogs.AboutDialog.productName
      return WorkbenchMessages.AboutDialog_defaultProductName;
    }
    if (ProductProperties.getAboutText(product) != null) {
      // Eg: org.eclipse.epp.package.java_1.4.0.20110609-1120/plugin.xml
      // Eg: org.eclipse.sdk_3.7.1.v201109091335/plugin.properties
      String aboutText = ProductProperties.getAboutText(product);
      String[] aboutLines = aboutText.split("\r\n|\r|\n");
      StringBuffer result = new StringBuffer();
      int foundLines = 0;
      for (String aboutLine : aboutLines) {
        aboutLine = aboutLine.trim();
        if (aboutLine.length() > 0) {
          foundLines++;
          result.append(aboutLine);
          if (foundLines >= 3) {
            return result.toString();// Usually the first 3 line contain useful information
          }
          result.append(", ");
        }
      }
    }
    return product.getName();
  }

  public static String getLogfile() {
    return System.getProperty("osgi.logfile");
  }

  public static String getWorkspace() {
    IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
    return location == null ? System.getProperty("osgi.instance.area") : location.toOSString();
  }

  public static String getHostName() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (Exception e) {
      return "?";
    }
  }

  public static void logMemoryInfo() {
    long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE;
    long max = Runtime.getRuntime().maxMemory() / MEGA_BYTE;
    long percentUsed = (100 * used) / max;
    String message = String.format("Memory: %s MB max, %s MB used (%s %%)", //
        String.valueOf(max),//
        String.valueOf(used),//
        String.valueOf(percentUsed));
    Log.log(percentUsed > 80 ? LogLevel.WARN : LogLevel.INFO, message);
  }
}