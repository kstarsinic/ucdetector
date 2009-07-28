/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates text report files like:
 * <ul>
 * <li>html-file</li>
 * <li>xml-file</li>
 * </ul>
 * This class uses xslt transformation.<br>
 * This class tries to not throw Exceptions.
 * @see "file://org.ucdetector/src/org/ucdetector/report/html.xslt"
 */
public class XmlReport implements IUCDetectorReport {
  private static final String COPY_RIGHT = //
  /*  */"Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the\n" //$NON-NLS-1$
      + "accompanying materials are made available under the terms of the Eclipse\n" //$NON-NLS-1$
      + "Public License v1.0 which accompanies this distribution, and is available at\n" //$NON-NLS-1$
      + "http://www.eclipse.org/legal/epl-v10.html\n";//$NON-NLS-1$
  //
  private static final String EXTENSION_XML = ".xml"; //$NON-NLS-1$
  private static final String EXTENSION_HTML = ".html"; //$NON-NLS-1$
  private static final String XSL_FILE = "org/ucdetector/report/html.xslt";//$NON-NLS-1$

  private static final DecimalFormat FORMAT_REPORT_NUMBER = new DecimalFormat(
      "000"); //$NON-NLS-1$

  private Document doc;
  private Element markers;
  private Element problems;
  private Element statistcs;
  private int markerCount;
  private int detectionProblemCount;
  private Throwable initXMLException;

  public XmlReport() {
    initXML();
  }

  /**
   * initialize some xml stuff, and xml root elements
   */
  private void initXML() {
    if (!Prefs.isWriteReportFile()) {
      return;
    }
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .newDocument();
      Element root = doc.createElement("ucdetector");//$NON-NLS-1$
      root.appendChild(doc.createComment(COPY_RIGHT));
      doc.appendChild(root);
      //
      markers = doc.createElement("markers");//$NON-NLS-1$
      root.appendChild(markers);
      //
      problems = doc.createElement("problems");//$NON-NLS-1$
      root.appendChild(problems);
      //
      statistcs = doc.createElement("statistics");//$NON-NLS-1$
      root.appendChild(statistcs);
    }
    catch (Throwable e) {
      Log.logError("XML problems", e);//$NON-NLS-1$
      initXMLException = e;
    }
  }

  /**
   * creates for each marker a xml element and its children
   */
  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    if (initXMLException != null || !Prefs.isWriteReportFile()) {
      return true;
    }
    return reportMarkerImpl(reportParam);
  }

  /**
   * If there are problem creating xml report, ignore all exceptions!
   */
  private boolean reportMarkerImpl(ReportParam reportParam) {
    Element marker = null;
    try {
      markerCount++;
      markers.appendChild(doc
          .createComment(" === Marker number " + markerCount));//$NON-NLS-1$
      marker = doc.createElement("marker"); //$NON-NLS-1$
      markers.appendChild(marker);
      IMember javaElement = reportParam.getJavaElement();
      IResource resource = javaElement.getResource();

      // NODE: "Error", "Warning"
      appendChild(marker, "level", reportParam.getLevel().toString());//$NON-NLS-1$

      // NODE: org.ucdetector
      IPackageFragment pack = JavaElementUtil.getPackageFor(javaElement);
      String packageName = pack.getElementName();
      appendChild(marker, "package", packageName);//$NON-NLS-1$

      IType type = JavaElementUtil.getTypeFor(javaElement, true);
      // NODE: UCDetectorPlugin
      appendChild(marker, "class", JavaElementUtil.getElementName(type));//$NON-NLS-1$
      // NODE: Class, Annotation, Constructor...
      appendChild(
          marker,
          "javaTypeSimple", JavaElementUtil.getMemberTypeStringSimple(javaElement));//$NON-NLS-1$
      appendChild(marker,
          "javaType", JavaElementUtil.getMemberTypeString(javaElement));//$NON-NLS-1$

      if (javaElement instanceof IMethod) {
        // NODE: method
        IMethod method = (IMethod) javaElement;
        appendChild(marker,
            "method", JavaElementUtil.getSimpleMethodName(method));//$NON-NLS-1$
      }
      if (javaElement instanceof IField) {
        // NODE: field
        IField field = (IField) javaElement;
        appendChild(marker, "field", JavaElementUtil.getSimpleFieldName(field));//$NON-NLS-1$
      }

      // NODE: 123
      appendChild(marker, "line", String.valueOf(reportParam.getLine()));//$NON-NLS-1$

      String markerType = reportParam.getMarkerType();
      if (markerType.startsWith(MarkerFactory.UCD_MARKER)) {
        markerType = markerType.substring(MarkerFactory.UCD_MARKER.length());
      }
      appendChild(marker, "markerType", markerType);//$NON-NLS-1$
      // NODE: Change visibility of MixedExample to default
      appendChild(marker, "description", reportParam.getMessage());//$NON-NLS-1$
      String sReferenceCount = (reportParam.getReferenceCount() == -1) ? "-" : "" //$NON-NLS-1$ //$NON-NLS-2$
              + reportParam.getReferenceCount();
      appendChild(marker, "referenceCount", sReferenceCount);//$NON-NLS-1$

      if (resource != null) {
        // F:/ws/ucd/org.ucdetector.example/src/main/org/ucdetector/example/Bbb.java
        if (resource.getRawLocation() != null) {
          appendChild(marker,
              "resourceRawLocation", resource.getRawLocation().toString());//$NON-NLS-1$
        }
        IProject project = resource.getProject();
        if (project != null && project.getLocation() != null) {
          IPath location = project.getLocation();
          // [ 2762967 ] XmlReport: Problems running UCDetector
          // NODE:  org.ucdetector.example - maybe different projectName!
          appendChild(marker, "projectDir", location.lastSegment());//$NON-NLS-1$
          // NODE:  org.ucdetector.example - maybe different projectDir!
          appendChild(marker, "projectName", project.getName());//$NON-NLS-1$
          // NODE:  F:/ws/ucd
          String parentDir = location.removeLastSegments(1).toString();
          appendChild(marker, "projectLocation", parentDir);//$NON-NLS-1$
        }

        IPackageFragmentRoot sourceFolder = JavaElementUtil
            .getPackageFragmentRootFor(javaElement);
        if (sourceFolder != null && sourceFolder.getResource() != null) {
          IPath path = sourceFolder.getResource().getProjectRelativePath();
          if (path != null) {
            // NODE:  example
            appendChild(marker, "sourceFolder", path.toString());//$NON-NLS-1$
          }
        }

        IContainer parent = resource.getParent();
        if (parent != null && parent.getProjectRelativePath() != null) {
          // NODE:  org/ucdetector/example
          appendChild(marker, "resourceLocation", packageName.replace('.', '/'));//$NON-NLS-1$
        }
        // NODE: NoReferenceExample.java
        appendChild(marker, "resourceName", resource.getName());//$NON-NLS-1$
      }
      appendChild(marker, "nr", String.valueOf(markerCount));//$NON-NLS-1$
    }
    catch (Throwable ex) {
      Log.logError("XML problems", ex);//$NON-NLS-1$
      if (marker != null) {
        appendChild(marker, "ExceptionForCreatingMarker", ex.getMessage());//$NON-NLS-1$
      }
    }
    return true;
  }

  public void reportDetectionProblem(IStatus status) {
    detectionProblemCount++;
    Element problem = doc.createElement("problem"); //$NON-NLS-1$
    problems.appendChild(problem);
    appendChild(problem, "status", status.toString());//$NON-NLS-1$
    Throwable ex = status.getException();
    String exString = ""; //$NON-NLS-1$
    if (ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));
      exString = writer.toString().replace("\r\n", "\n"); //$NON-NLS-1$//$NON-NLS-2$
    }
    appendChild(problem, "exception", exString);//$NON-NLS-1$
  }

  /**
   * Append a child node and a text node
   */
  private Element appendChild(Element parent, String child, String text) {
    Element childNode = doc.createElement(child);
    if (text != null) {
      childNode.appendChild(doc.createTextNode(text));
    }
    parent.appendChild(childNode);
    return childNode;
  }

  /**
   * Write report to xml file, do xslt transformation to an html file
   */
  public void endReport(Object[] selected, long start) throws CoreException {
    if (!Prefs.isWriteReportFile()) {
      return;
    }
    String htmlFileName = appendFreeNumber(Prefs.getReportFile());
    if (initXMLException != null) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR,
          initXMLException, htmlFileName);
      return;
    }
    if (markerCount == 0 && detectionProblemCount == 0) {
      logEndReportMessage(Messages.XMLReport_WriteNoWarnings, IStatus.INFO,
          initXMLException);
      return;
    }
    appendStatistics(selected, start);
    String xmlFileName;
    if (htmlFileName.endsWith(EXTENSION_HTML)) {
      xmlFileName = htmlFileName.replace(EXTENSION_HTML, EXTENSION_XML);
    }
    else {
      xmlFileName = htmlFileName + EXTENSION_XML;
    }
    try {
      File xmlFile = writeDocumentToFile(doc, xmlFileName);
      Document htmlDocument = transformXSLT(xmlFile);
      File htmlFile = writeDocumentToFile(htmlDocument, htmlFileName);
      logEndReportMessage(Messages.XMLReport_WriteOk, IStatus.INFO, null,
          String.valueOf(markerCount), htmlFile.getAbsoluteFile().toString());

    }
    catch (Exception e) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, e,
          htmlFileName);
    }
  }

  /**
   * @return File name, with does not exist, containing a number.
   * UCDetetorReport.html -&gt; UCDetetorReport_001.html
   */
  // Fix [2811049]  Html report is overridden each run
  private String appendFreeNumber(String reportFile) {
    int posDot = reportFile.lastIndexOf('.');
    posDot = (posDot == -1) ? reportFile.length() : posDot;
    String nameStart = reportFile.substring(0, posDot);
    String nameEnd = reportFile.substring(posDot);
    for (int i = 1; i < 1000; i++) {
      StringBuilder numberName = new StringBuilder();
      numberName.append(nameStart).append('_');
      numberName.append(FORMAT_REPORT_NUMBER.format(i)).append(nameEnd);
      String sNumberName = numberName.toString();
      if (!new File(sNumberName).exists()) {
        return sNumberName;
      }
    }
    return reportFile;
  }

  /**
   * Create a <code>Status</code> and log it to the Eclipse log
   */
  private static void logEndReportMessage(String message, int iStatus,
      Throwable ex, String... parms) {
    String mes = NLS.bind(message, parms);
    Status status = new Status(iStatus, UCDetectorPlugin.ID, iStatus, mes, ex);
    if (iStatus == IStatus.ERROR) {
      UCDetectorPlugin.logStatus(status); // Create status in Error Log View
      return;
    }
    Log.logStatus(status);
  }

  /**
   * Append statistics like: date, searchDuration, searched elements
   */
  private void appendStatistics(Object[] selected, long start) {
    appendChild(statistcs,
        "dateStarted", UCDetectorPlugin.DATE_FORMAT.format(new Date(start)));//$NON-NLS-1$
    appendChild(statistcs, "dateFinished", UCDetectorPlugin.getNow());//$NON-NLS-1$
    long millis = (System.currentTimeMillis() - start);
    appendChild(statistcs, "searchDuration", StopWatch.timeAsString(millis));//$NON-NLS-1$
    Element searched = appendChild(statistcs, "searched", null);//$NON-NLS-1$
    for (Object selection : selected) {
      if (selection instanceof IJavaElement) {
        IJavaElement javaElement = (IJavaElement) selection;
        appendChild(searched,
            "search", JavaElementUtil.getElementName(javaElement));//$NON-NLS-1$
      }
    }
  }

  /**
   * writes an document do a file
   */
  private static File writeDocumentToFile(Document docToWrite, String fileName)
      throws Exception {
    Source source = new DOMSource(docToWrite);
    File file = new File(fileName);
    Result result = new StreamResult(file);
    Transformer xformer = TransformerFactory.newInstance().newTransformer();
    xformer.setOutputProperty(OutputKeys.INDENT, "yes");//$NON-NLS-1$
    xformer.setOutputProperty(OutputKeys.METHOD, "xml");//$NON-NLS-1$
    xformer.transform(source, result);
    return file;
  }

  /**
   * Do an xslt transformation
   */
  private Document transformXSLT(File file) throws Exception {
    InputStream xmlIn = null;
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      InputStream xslIn = getClass().getClassLoader().getResourceAsStream(
          XSL_FILE);
      Templates template = factory.newTemplates(new StreamSource(xslIn));
      Transformer xformer = template.newTransformer();
      xmlIn = new FileInputStream(file);
      Source source = new StreamSource(xmlIn);
      DocumentBuilder builder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      Document transformedDoc = builder.newDocument();
      Result result = new DOMResult(transformedDoc);
      xformer.transform(source, result);
      xmlIn.close();
      return transformedDoc;
    }
    finally {
      if (xmlIn != null) {
        xmlIn.close();
      }
    }
  }
}
