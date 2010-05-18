/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.Map.Entry;

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
@SuppressWarnings("nls")
public class XmlReport implements IUCDetectorReport {
  private static final String COPY_RIGHT = "\n" //
      + "    Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the\n"
      + "    accompanying materials are made available under the terms of the Eclipse\n"
      + "    Public License v1.0 which accompanies this distribution, and is available at\n"
      + "    http://www.eclipse.org/legal/epl-v10.html\n";
  //
  private static final String XML_INFO = "\n" //
      + " - javaTypeSimple one of:\n"//
      + "   - Class, Method, Field, Initializer\n"//
      + " - javaType one of:\n"//
      + "   - Annotation, Anonymous class, Enumeration, Interface, Local class, Member class, Class\n"//
      + "   - Constructor, Method\n" //
      + "   - EnumConstant, Constant, Field\n"//
      + " - markerType one of:\n"//
      + "   - Reference, FewReference, VisibilityPrivate, VisibilityProtected, VisibilityDefault, Final, TestOnly\n";
  //
  private static final String HTML_XSL_FILE = "org/ucdetector/report/html.xslt";
  private static final String TEXT_XSL_FILE = "org/ucdetector/report/text.xslt";

  private static final DecimalFormat FORMAT_REPORT_NUMBER = new DecimalFormat("000");

  private Document doc;
  private Element markers;
  private Element problems;
  private Element statistcs;
  private int markerCount;
  private int detectionProblemCount;
  private Throwable initXMLException;
  private Element abouts;

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
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement("ucdetector");
      root.appendChild(doc.createComment(COPY_RIGHT));
      doc.appendChild(root);
      //
      statistcs = doc.createElement("statistics");
      root.appendChild(statistcs);
      //
      markers = doc.createElement("markers");
      root.appendChild(markers);
      //
      problems = doc.createElement("problems");
      root.appendChild(problems);
      //
      markers.appendChild(doc.createComment(XML_INFO));
    }
    catch (Throwable e) {
      Log.logError("Can't create xml report: ", e);
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
      markers.appendChild(doc.createComment(" === Marker number " + markerCount));
      marker = doc.createElement("marker");
      markers.appendChild(marker);
      IMember javaElement = reportParam.getJavaElement();
      IResource resource = javaElement.getResource();

      // NODE: "Error", "Warning"
      appendChild(marker, "level", reportParam.getLevel().toString());

      // NODE: org.ucdetector
      IPackageFragment pack = JavaElementUtil.getPackageFor(javaElement);
      String packageName = pack.getElementName();
      appendChild(marker, "package", packageName);

      IType type = JavaElementUtil.getTypeFor(javaElement, true);
      // NODE: UCDetectorPlugin
      appendChild(marker, "class", JavaElementUtil.getElementName(type));
      // NODE: Class, Annotation, Constructor...
      appendChild(marker, "javaTypeSimple", JavaElementUtil.getMemberTypeStringSimple(javaElement));
      appendChild(marker, "javaType", JavaElementUtil.getMemberTypeString(javaElement));

      if (javaElement instanceof IMethod) {
        // NODE: method
        IMethod method = (IMethod) javaElement;
        appendChild(marker, "method", JavaElementUtil.getSimpleMethodName(method));
      }
      if (javaElement instanceof IField) {
        // NODE: field
        IField field = (IField) javaElement;
        appendChild(marker, "field", JavaElementUtil.getSimpleFieldName(field));
      }

      // NODE: 123
      appendChild(marker, "line", String.valueOf(reportParam.getLine()));

      String markerType = reportParam.getMarkerType();
      if (markerType.startsWith(MarkerFactory.UCD_MARKER)) {
        markerType = markerType.substring(MarkerFactory.UCD_MARKER.length());
      }
      appendChild(marker, "markerType", markerType);
      // NODE: Change visibility of MixedExample to default
      appendChild(marker, "description", reportParam.getMessage());
      String sReferenceCount = (reportParam.getReferenceCount() == -1) ? "-" : "" //$NON-NLS-2$
          + reportParam.getReferenceCount();
      appendChild(marker, "referenceCount", sReferenceCount);

      if (resource != null) {
        // F:/ws/ucd/org.ucdetector.example/src/main/org/ucdetector/example/Bbb.java
        if (resource.getRawLocation() != null) {
          appendChild(marker, "resourceRawLocation", resource.getRawLocation().toString());
        }
        IProject project = resource.getProject();
        if (project != null && project.getLocation() != null) {
          IPath location = project.getLocation();
          // [ 2762967 ] XmlReport: Problems running UCDetector
          // NODE:  org.ucdetector.example - maybe different projectName!
          appendChild(marker, "projectDir", location.lastSegment());
          // NODE:  org.ucdetector.example - maybe different projectDir!
          appendChild(marker, "projectName", project.getName());
          // NODE:  F:/ws/ucd
          String parentDir = location.removeLastSegments(1).toString();
          appendChild(marker, "projectLocation", parentDir);
        }

        IPackageFragmentRoot sourceFolder = JavaElementUtil.getPackageFragmentRootFor(javaElement);
        if (sourceFolder != null && sourceFolder.getResource() != null) {
          IPath path = sourceFolder.getResource().getProjectRelativePath();
          if (path != null) {
            // NODE:  example
            appendChild(marker, "sourceFolder", path.toString());
          }
        }

        IContainer parent = resource.getParent();
        if (parent != null && parent.getProjectRelativePath() != null) {
          // NODE:  org/ucdetector/example
          appendChild(marker, "resourceLocation", packageName.replace('.', '/'));
        }
        // NODE: NoReferenceExample.java
        appendChild(marker, "resourceName", resource.getName());
      }
      appendChild(marker, "nr", String.valueOf(markerCount));
    }
    catch (Throwable ex) {
      Log.logError("XML problems", ex);
      if (marker != null) {
        appendChild(marker, "ExceptionForCreatingMarker", ex.getMessage());
      }
    }
    return true;
  }

  public void reportDetectionProblem(IStatus status) {
    detectionProblemCount++;
    Element problem = doc.createElement("problem");
    problems.appendChild(problem);
    appendChild(problem, "status", status.toString());
    Throwable ex = status.getException();
    String exString = "";
    if (ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));
      exString = writer.toString().replace("\r\n", "\n");//$NON-NLS-2$
    }
    appendChild(problem, "exception", exString);
  }

  /**
   * Append a child node and a text node
   */
  private Element appendChild(Element parent, String tagName, String text) {
    Element childNode = doc.createElement(tagName);
    if (text != null) {
      childNode.appendChild(doc.createTextNode(text));
    }
    parent.appendChild(childNode);
    return childNode;
  }

  /**
   * @return File name, with does not exist, containing a number.
   * eg: UCDetetorReport_001
   */
  // Fix [2811049]  Html report is overridden each run
  private String getReportNumberName(File reportDir) {
    String[] files = reportDir.list();
    files = (files == null) ? new String[0] : files;
    for (int i = 1; i < 1000; i++) {
      String number = FORMAT_REPORT_NUMBER.format(i);
      boolean found = false;
      for (String file : files) {
        if (file.contains(number)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return "UCDetetorReport_" + number;
      }
    }
    return "UCDetetorReport";
  }

  /** Create a <code>Status</code> and log it to the Eclipse log */
  private static void logEndReportMessage(String message, int iStatus, Throwable ex, String... parms) {
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
    long now = System.currentTimeMillis();
    long duration = (now - start);
    abouts = appendChild(statistcs, "abouts", null);
    appendAbout("reportCreated", "Created report", UCDetectorPlugin.getNow(), true);
    appendAbout("reportCreatedTS", "Created report", "" + now, false);
    appendAbout("operatingSystem", "Operating system", UCDetectorPlugin.getAboutOS(), true);
    appendAbout("javaVersion", "Java", UCDetectorPlugin.getAboutJavaVersion(), true);
    appendAbout("eclipseVersion", "Eclipse", UCDetectorPlugin.getAboutEclipseVersion(), true);
    appendAbout("ucdetectorVersion", "UCDetector", UCDetectorPlugin.getAboutUCDVersion(), true);
    appendAbout("searchDuration", "Search duration", StopWatch.timeAsString(duration), true);
    appendAbout("searchDurationTS", "Search duration", "" + duration, false);
    appendAbout("eclipseHome", "Eclipse home", UCDetectorPlugin.getAboutEclipseHome(), false);
    appendAbout("logfile", "Logfile", UCDetectorPlugin.getAboutLogfile(), false);
    appendAbout("workspace", "Workspace", UCDetectorPlugin.getAboutWorkspace(), false);
    appendAbout("warnings", "Warnings", String.valueOf(markerCount), true);
    appendAbout("host", "Host", UCDetectorPlugin.getHostName(), false);
    //
    Element searched = appendChild(statistcs, "searched", null);
    for (Object selection : selected) {
      if (selection instanceof IJavaElement) {
        IJavaElement javaElement = (IJavaElement) selection;
        Element search = appendChild(searched, "search", JavaElementUtil.getElementName(javaElement));
        search.setAttribute("class", javaElement.getClass().getSimpleName());
      }
    }
    Element preferencesNode = appendChild(statistcs, "preferences", null);
    Set<Entry<String, String>> preferencesSet = UCDetectorPlugin.getDeltaPreferences().entrySet();
    for (Entry<String, String> entry : preferencesSet) {
      Element preferenceNode = appendChild(preferencesNode, "preference", null);
      preferenceNode.setAttribute("key", entry.getKey());
      preferenceNode.setAttribute("value", entry.getValue());
    }
  }

  /**
   * <pre>
   *  &lt;about name="operatingSystem" show="true">
   *     &lt;key>Operating system&lt;/key>
   *     &lt;value>Linux-2.6.27.39-0.2-default&lt;/value>
   *  &lt;/about>
   * </pre>
   */
  private Element appendAbout(String nodeName, String nodeNiceName, String value, boolean show) {
    Element about = appendChild(abouts, "about", null);
    about.setAttribute("name", nodeName);
    about.setAttribute("show", Boolean.toString(show));
    appendChild(about, "key", nodeNiceName);
    appendChild(about, "value", value);
    return about;
  }

  /**
   * Write report to xml file, do xslt transformation to an html file
   */
  public void endReport(Object[] selected, long start) throws CoreException {
    if (!Prefs.isWriteReportFile()) {
      return;
    }
    File reportDir = new File(Prefs.getReportDir());
    reportDir.mkdirs();
    String baseFileName = getReportNumberName(reportDir);
    File htmlFile = new File(reportDir, baseFileName + ".html");
    String reportPath = reportDir.getAbsolutePath();
    if (initXMLException != null) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, initXMLException, reportPath);
      return;
    }
    if (markerCount == 0 && detectionProblemCount == 0) {
      logEndReportMessage(Messages.XMLReport_WriteNoWarnings, IStatus.INFO, initXMLException);
      return;
    }
    appendStatistics(selected, start);

    try {
      if (Prefs.isCreateReportHTML()) {
        Document htmlDocument = transformToHTML(doc);
        writeDocumentToFile(htmlDocument, htmlFile);
      }
      if (Prefs.isCreateReportXML()) {
        writeDocumentToFile(doc, new File(reportDir, baseFileName + ".xml"));
      }
      if (Prefs.isCreateReportTXT()) {
        writeTextFile(new File(reportDir, baseFileName + ".txt"));
      }
      logEndReportMessage(Messages.XMLReport_WriteOk, IStatus.INFO, null, String.valueOf(markerCount), reportPath);
    }
    catch (Exception e) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, e, reportPath);
    }
  }

  private void writeTextFile(File file) throws Exception, IOException {
    String text = transformToText(doc);
    FileWriter fileWriter = new FileWriter(file);
    fileWriter.write(text);
    fileWriter.close();
    Log.logInfo("Wrote file= " + file.getCanonicalPath());
  }

  /**
   * writes an document do a file
   */
  private static void writeDocumentToFile(Document docToWrite, File file) throws Exception {
    Source source = new DOMSource(docToWrite);
    Result result = new StreamResult(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer xformer = tf.newTransformer();
    try {
      xformer.setOutputProperty(OutputKeys.METHOD, "xml");
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
      tf.setAttribute("indent-number", Integer.valueOf(2));
    }
    catch (IllegalArgumentException ignore) {
      Log.logWarn("Can't change output format: " + ignore);
    }
    xformer.transform(source, result);
    Log.logInfo("Wrote file= " + file.getCanonicalPath());
  }

  /** Transform from xml to html using xslt transformation */
  private Document transformToHTML(Document xmlDoc) throws Exception {
    InputStream xslIn = getClass().getClassLoader().getResourceAsStream(HTML_XSL_FILE);
    Templates template = TransformerFactory.newInstance().newTemplates(new StreamSource(xslIn));
    Transformer xformer = template.newTransformer();
    Source source = new DOMSource(xmlDoc);
    Document transformedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Result result = new DOMResult(transformedDoc);
    xformer.transform(source, result);
    return transformedDoc;
  }

  /** Transform from xml to text using xslt transformation */
  private String transformToText(Document xmlDoc) throws Exception {
    InputStream xslIn = getClass().getClassLoader().getResourceAsStream(TEXT_XSL_FILE);
    Templates template = TransformerFactory.newInstance().newTemplates(new StreamSource(xslIn));
    Transformer xformer = template.newTransformer();
    Source source = new DOMSource(xmlDoc);
    StringWriter stringWriter = new StringWriter();
    Result result = new StreamResult(stringWriter);
    xformer.transform(source, result);
    return stringWriter.toString();
  }
}
