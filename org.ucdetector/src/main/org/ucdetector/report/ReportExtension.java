/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * TODO: Describe class
 * <p>
 * @author Joerg Spieler
 * @since 01.04.2011
 */
public class ReportExtension {
  private static final String ATTRIBUTE_STYLESHEET = "stylesheet"; //$NON-NLS-1$
  private static final String ATTRIBUTE_CLASS = "class";//$NON-NLS-1$
  /** Simple identifier constant (value <code>"reports"</code>) for the UCDetector reports extension point. */
  private static final String EXTENSION_POINT_ID = UCDetectorPlugin.ID + ".reports"; //$NON-NLS-1$
  private static ArrayList<ReportExtension> xsltExtensions = null;
  private static ArrayList<ReportExtension> classExtensions = null;
  private final String resultFile;
  private final String name;
  private final String xslt;
  private final IUCDetectorReport report;

  public ReportExtension(String resultFile, String name, String xslt, IUCDetectorReport report) {
    this.resultFile = resultFile;
    this.name = name;
    this.xslt = xslt;
    this.report = report;
  }

  // org.eclipse.ant.core.AntCorePlugin.extractExtensions(String)
  private static void loadExtensions() throws CoreException {
    if (xsltExtensions == null) {
      xsltExtensions = new ArrayList<ReportExtension>();
      classExtensions = new ArrayList<ReportExtension>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IConfigurationElement[] reports = reg.getConfigurationElementsFor(EXTENSION_POINT_ID);
      for (IConfigurationElement report : reports) {
        String resultFile = report.getAttribute("resultFile"); //$NON-NLS-1$
        String name = report.getAttribute("name");//$NON-NLS-1$
        String xslt = report.getAttribute(ATTRIBUTE_STYLESHEET);
        String clazz = report.getAttribute(ATTRIBUTE_CLASS);
        if (xslt != null && clazz == null) {
          xsltExtensions.add(new ReportExtension(resultFile, name, xslt, null));
        }
        else if (xslt == null && clazz != null) {
          IUCDetectorReport reportObject = (IUCDetectorReport) WorkbenchPlugin.createExtension(report, ATTRIBUTE_CLASS);
          classExtensions.add(new ReportExtension(resultFile, name, null, reportObject));
        }
        else {
          Log.warn("One attribute needed: '%s' or '%s'", ATTRIBUTE_CLASS, ATTRIBUTE_STYLESHEET);//$NON-NLS-1$
        }
      }
    }
  }

  public String getResultFile() {
    return resultFile;
  }

  public String getName() {
    return name;
  }

  public String getXslt() {
    return xslt;
  }

  public IUCDetectorReport getReport() {
    return report;
  }

  public static ArrayList<ReportExtension> getXsltExtensions() throws CoreException {
    loadExtensions();
    return xsltExtensions;
  }

  public static ArrayList<ReportExtension> getClassExtensions() throws CoreException {
    loadExtensions();
    return classExtensions;
  }

  public static List<String> getNames() {
    List<String> result = new ArrayList<String>();
    try {
      loadExtensions();
      for (ReportExtension reportExtension : getXsltExtensions()) {
        result.add(reportExtension.getName());
      }
      for (ReportExtension reportExtension : getClassExtensions()) {
        result.add(reportExtension.getName());
      }
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public String toString() {
    return String.format("ReportExtension [resultFile=%s, name=%s, xslt=%s, report=%s]", //$NON-NLS-1$
        resultFile, name, xslt, report);
  }
}
