/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * Provid access to the org.ucdetector.reports extension point
 * <p>
 * @author Joerg Spieler
 * @since 2011-04.01
 */
public final class ReportExtension {
  /** Simple identifier constant (value <code>"reports"</code>) for the UCDetector reports extension point. */
  private static final String EXTENSION_POINT_ID = UCDetectorPlugin.ID + ".reports"; //$NON-NLS-1$
  //
  private static final String ATTRIBUTE_STYLESHEET = "stylesheet"; //$NON-NLS-1$
  private static final String ATTRIBUTE_CLASS = "class";//$NON-NLS-1$
  private static final String ATTRIBUTE_REPORT_ID = "id";//$NON-NLS-1$
  private static boolean isInitialized = false;
  private static List<ReportExtension> xsltExtensions;
  private static List<ReportExtension> classExtensions;
  private static List<ReportExtension> allExtensions;

  private final String resultFile;
  private final String description;
  private final String xslt;
  private final IUCDetectorReport report;
  private final String id;

  private ReportExtension(String resultFile, String description, String xslt, IUCDetectorReport report, String id) {
    this.resultFile = resultFile;
    this.description = description;
    this.xslt = xslt;
    this.report = report;
    this.id = id;
  }

  public String getResultFile() {
    return resultFile;
  }

  public String getDescription() {
    return description;
  }

  public String getXslt() {
    return xslt;
  }

  public IUCDetectorReport getReport() {
    return report;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return String.format("ReportExtension [resultFile=%s, description=%s, xslt=%s, report=%s, id=%s]", //$NON-NLS-1$
        resultFile, description, xslt, report, id);
  }

  // STATIC -------------------------------------------------------------------
  // Similar to: org.eclipse.ant.core.AntCorePlugin.extractExtensions(String)
  private static void loadExtensions() {
    if (!isInitialized) {
      isInitialized = true;
      xsltExtensions = new ArrayList<>();
      classExtensions = new ArrayList<>();
      allExtensions = new ArrayList<>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IConfigurationElement[] reports = reg.getConfigurationElementsFor(EXTENSION_POINT_ID);
      for (IConfigurationElement report : reports) {
        String resultFile = report.getAttribute("resultFile"); //$NON-NLS-1$
        String name = report.getAttribute("description");//$NON-NLS-1$
        String xslt = report.getAttribute(ATTRIBUTE_STYLESHEET);
        String clazz = report.getAttribute(ATTRIBUTE_CLASS);
        String id = report.getAttribute(ATTRIBUTE_REPORT_ID);
        if (xslt != null && clazz == null) {
          boolean xsltFound = ReportExtension.class.getClassLoader().getResourceAsStream(xslt) != null;
          if (xsltFound) {
            xsltExtensions.add(new ReportExtension(resultFile, name, xslt, null, id));
          }
          else if (xslt.endsWith("custom.xslt")) { //$NON-NLS-1$
            Log.info("Tip: To create custom reports rename file to custom.xslt: org.ucdetector_x.y.z.jar/org/ucdetector/report/__custom.xslt"); //$NON-NLS-1$
          }
        }
        else if (xslt == null && clazz != null) {
          try {
            //IUCDetectorReport reportObject = (IUCDetectorReport) WorkbenchPlugin.createExtension(report, ATTRIBUTE_CLASS);
            IUCDetectorReport reportObject = (IUCDetectorReport) report.createExecutableExtension(ATTRIBUTE_CLASS);// This line fixes headless exception
            classExtensions.add(new ReportExtension(resultFile, name, null, reportObject, id));
          }
          // Catch Throwable here because of headless problem here: java.lang.UnsatisfiedLinkError: Could not load SWT library. R
          catch (Throwable ex) {
            UCDetectorPlugin.logToEclipseLog("Can't load ReportExtension", ex); //$NON-NLS-1$
          }
        }
        else {
          Log.warn("One attribute needed: '%s' or '%s'", ATTRIBUTE_CLASS, ATTRIBUTE_STYLESHEET);//$NON-NLS-1$
        }
      }
      allExtensions.addAll(classExtensions);
      allExtensions.addAll(xsltExtensions);
    }
  }

  public static List<ReportExtension> getXsltExtensions() { // NO_UCD
    loadExtensions();
    return xsltExtensions;
  }

  public static List<ReportExtension> getClassExtensions() {
    loadExtensions();
    return classExtensions;
  }

  public static List<ReportExtension> getAllExtensions() {
    loadExtensions();
    return allExtensions;
  }
}
