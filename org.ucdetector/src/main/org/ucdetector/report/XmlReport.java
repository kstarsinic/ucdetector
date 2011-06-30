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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.JavaElementUtil.MemberInfo;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates text report files like:
 * <ul>
 * <li>html</li>
 * <li>xml</li>
 * <li>text</li>
 * </ul>
 * This class uses xslt transformation.<br>
 * This class tries not to throw Exceptions.
 * @see "src/main/org/ucdetector/report/html.xslt"
 * <p>
 * @author Joerg Spieler
 * @since 2008-08-27
 */
@SuppressWarnings("nls")
public class XmlReport implements IUCDetectorReport {
  private static final String ICONS_DIR = ".icons";
  private static final String HTML_XSLT = "html.xslt";
  private static final String COPY_RIGHT = //
  /*<!-- */" ===========================================================================\n"
      + "     Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the\n"
      + "     accompanying materials are made available under the terms of the Eclipse\n"
      + "     Public License v1.0 which accompanies this distribution, and is available at\n"
      + "     http://www.eclipse.org/legal/epl-v10.html\n"
      + "     ======================================================================== ";
  private static final String XML_INFO = "\n" //
      + " - javaTypeSimple one of:\n"//
      + "   - Class, Method, Field, Initializer\n"//
      + " - javaType one of:\n"//
      + "   - Annotation, Anonymous class, Enumeration, Interface, Local class, Member class, Class\n"//
      + "   - Constructor, Method\n" //
      + "   - EnumConstant, Constant, Field\n"//
      + " - markerType one of:\n"//
      + "   - Reference, FewReference, VisibilityPrivate, VisibilityProtected, VisibilityDefault, Final, TestOnly\n";

  private Document doc;
  private Element markers;
  private Element problems;
  private Element statistcs;
  private int markerCount;
  private int detectionProblemCount;
  private Throwable initXMLException;
  private Element abouts;
  // NODES --------------------
  private Element nodeCreated;
  private Element nodeCreatedTS;
  private Element nodeDuration;
  private Element nodeDurationTS;
  private Element nodeFinished;
  private Element nodeWarnings;
  //
  private boolean isFirstStatistic = true;
  private boolean endReportCalled;
  //
  private IJavaElement[] objectsToIterate;
  private long startTime = System.currentTimeMillis();

  public XmlReport() {
    initXML();
    if (UCDetectorPlugin.isHeadlessMode()) {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (!endReportCalled) {
            Log.warn("Process interrupted? Try to write reports in ShutdownHook");
            writeReports(true);
            Log.warn("Wrote reports in ShutdownHook");
          }
        }
      });
    }
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
      String stylesheet = String.format("type=\"text/xsl\" href=\"%s/%s\"", ICONS_DIR, HTML_XSLT);
      doc.insertBefore(doc.createProcessingInstruction("xml-stylesheet", stylesheet), doc.getDocumentElement());
      doc.insertBefore(doc.createComment(COPY_RIGHT), doc.getDocumentElement());
      Element root = doc.createElement("ucdetector");
      doc.appendChild(root);
      statistcs = doc.createElement("statistics");
      root.appendChild(statistcs);
      markers = doc.createElement("markers");
      root.appendChild(markers);
      problems = doc.createElement("problems");
      root.appendChild(problems);
      markers.appendChild(doc.createComment(XML_INFO));
    }
    catch (Throwable e) {
      Log.error("Can't create xml report: ", e);
      initXMLException = e;
    }
  }

  /**
   * @param startTimeIn time, when report is started
   */
  public void startReport(IJavaElement[] objectsToIterateArray, long startTimeIn) throws CoreException {
    this.objectsToIterate = objectsToIterateArray;
    this.startTime = startTimeIn;
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
      //markers.appendChild(doc.createComment(" === Marker number " + markerCount));
      marker = doc.createElement("marker");
      markers.appendChild(marker);
      String markerType = reportParam.getMarkerType();
      if (markerType.startsWith(MarkerFactory.UCD_MARKER)) {
        markerType = markerType.substring(MarkerFactory.UCD_MARKER.length());
      }
      IMember javaElement = reportParam.getJavaElement();
      IResource resource = javaElement.getResource();

      // ===== Attributes =====
      marker.setAttribute("nr", String.valueOf(markerCount));
      marker.setAttribute("level", String.valueOf(reportParam.getLevel()));// "Error", "Warning"
      marker.setAttribute("line", String.valueOf(reportParam.getLine()));
      // [ 3323078 ] Add line number start/end to markers
      marker.setAttribute("lineStart", String.valueOf(reportParam.getLineStart()));
      marker.setAttribute("lineEnd", String.valueOf(reportParam.getLineEnd()));
      marker.setAttribute("markerType", markerType);
      int iRefCount = reportParam.getReferenceCount();
      String sReferenceCount = (iRefCount == -1) ? "-" : "" + iRefCount;
      marker.setAttribute("referenceCount", sReferenceCount);

      // ===== Nodes =====
      appendChild(marker, "description", reportParam.getMessage());// NODE: Change visibility of MixedExample to default
      if (resource != null && resource.getRawLocation() != null) {
        // F:/ws/ucd/org.ucdetector.example/src/main/org/ucdetector/example/Bbb.java
        appendChild(marker, "file", resource.getRawLocation().toOSString());
      }
      if (javaElement.getJavaProject() != null) {
        IJavaProject project = javaElement.getJavaProject();
        Element projectElement = appendChild(marker, "project", null);
        projectElement.setAttribute("name", project.getElementName());// NODE:  org.ucdetector.example
        // [ 2762967 ] XmlReport: Problems running UCDetector NODE:  org.ucdetector.example - maybe different projectDir!
        IPath location = project.getProject().getLocation();
        String parentDir = location.removeLastSegments(1).toString();
        projectElement.setAttribute("parentDir", parentDir);// NODE:  F:/ws/ucd
        String projectDir = location.lastSegment();
        projectElement.setAttribute("dir", projectDir);
      }
      IPackageFragmentRoot sourceFolder = JavaElementUtil.getPackageFragmentRootFor(javaElement);
      if (sourceFolder != null && sourceFolder.getResource() != null) {
        IPath path = sourceFolder.getResource().getProjectRelativePath();
        if (path != null) {
          appendChild(marker, "sourceFolder", path.toString()); // NODE:  src/main
        }
      }
      IPackageFragment pack = JavaElementUtil.getPackageFor(javaElement);
      appendChild(marker, "package", pack.getElementName());
      IType type = JavaElementUtil.getTypeFor(javaElement, true);// NODE: UCDetectorPlugin
      appendChild(marker, "class", JavaElementUtil.getElementName(type));
      //
      Element javaTypeElement = appendChild(marker, "javaType", null);
      javaTypeElement.setAttribute("simple", JavaElementUtil.getMemberTypeStringSimple(javaElement));
      javaTypeElement.setAttribute("long", JavaElementUtil.getMemberTypeString(javaElement));
      MemberInfo memberInfo = JavaElementUtil.getMemberInfo(javaElement);
      if (memberInfo != null) {
        javaTypeElement.setAttribute("icon", memberInfo.getIcon());
      }

      if (javaElement instanceof IMethod) {
        IMethod method = (IMethod) javaElement;
        appendChild(marker, "method", JavaElementUtil.getSimpleMethodName(method)); // NODE: method
      }
      if (javaElement instanceof IField) {
        IField field = (IField) javaElement;
        appendChild(marker, "field", JavaElementUtil.getSimpleFieldName(field)); // NODE: field
      }
      if (reportParam.getAuthor() != null) {
        appendChild(marker, "author", reportParam.getAuthorTrimmed());
      }
      if (UCDetectorPlugin.isHeadlessMode() && markerCount % 50 == 0) {
        Log.info("Flush reports!");
        writeReports(false);
      }
    }
    catch (Throwable ex) {
      Log.error("XML problems", ex);
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
    appendChild(problem, "exception", UCDetectorPlugin.exceptionToString(status.getException()));
  }

  /** Create a <code>Status</code> and log it to the Eclipse log */
  private static void logEndReportMessage(String message, int iStatus, Throwable ex, String... parms) {
    String mes = NLS.bind(message, parms);
    Status status = new Status(iStatus, UCDetectorPlugin.ID, iStatus, mes, ex);
    UCDetectorPlugin.logToEclipseLog(status); // Create status in Error Log View
  }

  /** Append statistics like: date, searchDuration, searched elements   */
  private void appendStatistics(boolean isEndReport) {
    if (isFirstStatistic) {
      isFirstStatistic = false;
      abouts = appendChild(statistcs, "abouts", null);
      appendAbout("operatingSystem", "Operating system", UCDetectorPlugin.getAboutOS(), true, null);
      appendAbout("javaVersion", "Java", UCDetectorPlugin.getAboutJavaVersion(), true, null);
      appendAbout("eclipseVersion", "Eclipse", UCDetectorPlugin.getAboutEclipseVersion(), true, null);
      appendAbout("ucdetectorVersion", "UCDetector", UCDetectorPlugin.getAboutUCDVersion(), true, null);
      appendAbout("eclipseHome", "Eclipse home", UCDetectorPlugin.getAboutEclipseHome(), false, null);
      appendAbout("logfile", "Logfile", UCDetectorPlugin.getAboutLogfile(), false, null);
      appendAbout("workspace", "Workspace", UCDetectorPlugin.getAboutWorkspace(), false, null);
      appendAbout("mode", "Mode", Prefs.getModeName(), true, null);
      appendAbout("host", "Host", UCDetectorPlugin.getHostName(), false, null);
      appendAbout("headless", "headless", "" + UCDetectorPlugin.isHeadlessMode(), false, null);
      //
      Element searched = appendChild(statistcs, "searched", null);
      for (IJavaElement javaElement : objectsToIterate) {
        Element search = appendChild(searched, "search", JavaElementUtil.getElementName(javaElement));
        search.setAttribute("class", javaElement.getClass().getSimpleName());
      }
      Element preferencesNode = appendChild(statistcs, "preferences", null);
      Set<Entry<String, String>> preferencesSet = UCDetectorPlugin.getDeltaPreferences().entrySet();
      for (Entry<String, String> entry : preferencesSet) {
        Element preferenceNode = appendChild(preferencesNode, "preference", null);
        preferenceNode.setAttribute("key", entry.getKey());
        preferenceNode.setAttribute("value", entry.getValue());
      }
    }
    // Nodes change after each flush
    long now = System.currentTimeMillis();
    long duration = (now - startTime);
    String durationString = StopWatch.timeAsString(duration);
    nodeCreated = appendAbout("reportCreated", "Created report", UCDetectorPlugin.getNow(), true, nodeCreated);
    nodeCreatedTS = appendAbout("reportCreatedTS", "Created report", "" + now, false, nodeCreatedTS);
    nodeDuration = appendAbout("searchDuration", "Search duration", durationString, true, nodeDuration);
    nodeDurationTS = appendAbout("searchDurationTS", "Search duration", "" + duration, false, nodeDurationTS);
    nodeFinished = appendAbout("detectionFinished", "Detection Finished", "" + isEndReport, false, nodeFinished);
    nodeWarnings = appendAbout("warnings", "Warnings", String.valueOf(markerCount), true, nodeWarnings);
  }

  /**
   * <pre>
   *  &lt;about name="operatingSystem" show="true">
   *     &lt;key>Operating system&lt;/key>
   *     &lt;value>Linux-2.6.27.39-0.2-default&lt;/value>
   *  &lt;/about>
   * </pre>
   */
  private Element appendAbout(String nodeName, String nodeNiceName, String value, boolean show, Element alreadyCreated) {
    if (alreadyCreated != null) {
      alreadyCreated.getParentNode().removeChild(alreadyCreated);
    }
    Element about = appendChild(abouts, "about", null);
    about.setAttribute("name", nodeName);
    about.setAttribute("show", Boolean.toString(show));
    appendChild(about, "key", nodeNiceName);
    appendChild(about, "value", value);
    return about;
  }

  /** Append a child node and a text node  */
  private Element appendChild(Element parent, String tagName, String text) {
    Element childNode = doc.createElement(tagName);
    if (text != null) {
      childNode.appendChild(doc.createTextNode(text));
    }
    parent.appendChild(childNode);
    return childNode;
  }

  public void endReport() throws CoreException {
    endReportCalled = true;
    writeReports(true);
  }

  /** Write report to xml file, do xslt transformation to an html file or text file   */
  private void writeReports(boolean isEndReport) {
    if (!Prefs.isWriteReportFile()) {
      return;
    }
    long start = System.currentTimeMillis();
    File reportDir = new File(ReportNameManager.getReportDir(true));
    String reportPath = reportDir.getAbsolutePath();
    if (initXMLException != null) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, initXMLException, reportPath);
      return;
    }
    // 
    if (markerCount == 0 && detectionProblemCount == 0) {
      logEndReportMessage(Messages.XMLReport_WriteNoWarnings, IStatus.INFO, initXMLException);
      return;
    }
    appendStatistics(isEndReport);
    copyFilesToDotIconDir(reportDir);
    try {
      ArrayList<ReportExtension> xsltExtensions = ReportExtension.getXsltExtensions();
      for (ReportExtension xsltExtension : xsltExtensions) {
        if (Prefs.isCreateReport(xsltExtension)) {
          String reportName = ReportNameManager.getReportFileName(xsltExtension.getResultFile(), objectsToIterate);
          File resultFile = new File(reportDir, reportName);
          writeTextFile(resultFile, xsltExtension.getXslt());
        }
      }
      String reportName = ReportNameManager.getReportFileName(Prefs.getReportFile(), objectsToIterate);
      if (Prefs.isCreateReportXML()) {
        File xmlFile = new File(reportDir, reportName + ".xml");
        writeDocumentToFile(doc, xmlFile);
      }
      long duration = System.currentTimeMillis() - start;
      Log.info("Created reports in: %s", StopWatch.timeAsString(duration));
      if (isEndReport) {
        logEndReportMessage(Messages.XMLReport_WriteOk, IStatus.INFO, null, String.valueOf(markerCount), reportPath);
      }
    }
    catch (Exception e) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, e, reportPath);
    }
  }

  private void copyFilesToDotIconDir(File reportDir) {
    File iconsOutDir = new File(reportDir, ICONS_DIR);
    iconsOutDir.mkdirs();
    try {
      copyStylesheet(iconsOutDir);
      copyIconFiles(iconsOutDir);
    }
    catch (IOException ex) {
      Log.error("Problems copying files to: " + ICONS_DIR, ex);
    }
  }

  private void copyStylesheet(File iconsOutDir) throws IOException {
    InputStream inStream = getClass().getResourceAsStream(HTML_XSLT);
    copyStream(inStream, new FileOutputStream(new File(iconsOutDir, HTML_XSLT)));
  }

  private static final String[] ICONS = new String[] { //
  //  "ElementClass.gif", "ElementField.gif", "ElementMethod.gif",//
      "FewReference.gif", "Final.gif", "Reference.gif", "TestOnly.gif",//
      "ucd.gif", "ucdetector32.png", //
      "VisibilityDefault.gif", "VisibilityPrivate.gif", "VisibilityProtected.gif", //
  };

  private static void copyIconFiles(File iconsOutDir) throws IOException {
    for (String iconName : ICONS) {
      Path iconPath = new Path("icons");
      File outFile = new File(iconsOutDir, iconName);
      Bundle bundle = UCDetectorPlugin.getDefault().getBundle();
      copyToIconDir(iconPath, iconName, outFile, bundle);
    }
    for (JavaElementUtil.MemberInfo memberInfo : JavaElementUtil.MemberInfo.values()) {
      Path iconPath = new Path("icons/full/obj16/");
      File outFile = new File(iconsOutDir, memberInfo.getIcon());
      Bundle bundle = Platform.getBundle("org.eclipse.jdt.ui");
      copyToIconDir(iconPath, memberInfo.getIcon(), outFile, bundle);
    }
  }

  private static void copyToIconDir(Path iconPath, String iconName, File outFile, Bundle bundle) throws IOException {
    if (!outFile.exists()) {
      InputStream inStream = FileLocator.openStream(bundle, iconPath.append(iconName), false);
      copyStream(inStream, new FileOutputStream(outFile));
    }
  }

  private void writeTextFile(File file, String xslt) throws Exception, IOException {
    String text = transformToText(doc, xslt);
    OutputStreamWriter fileWriter = null;
    try {
      fileWriter = new OutputStreamWriter(new FileOutputStream(file), UCDetectorPlugin.UTF_8);
      fileWriter.write(text);
    }
    finally {
      UCDetectorPlugin.closeSave(fileWriter);
    }
    Log.info("Wrote file= " + UCDetectorPlugin.getCanonicalPath(file));
  }

  private static void writeDocumentToFile(Document docToWrite, File file) throws Exception {
    Source source = new DOMSource(docToWrite);
    Result result = new StreamResult(new OutputStreamWriter(new FileOutputStream(file), UCDetectorPlugin.UTF_8));
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer xformer = tf.newTransformer();
    try {
      xformer.setOutputProperty(OutputKeys.METHOD, "xml");
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      xformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
      tf.setAttribute("indent-number", Integer.valueOf(2));
    }
    catch (IllegalArgumentException ignore) {
      Log.warn("Can't change output format: " + ignore);
    }
    xformer.transform(source, result);
    Log.info("Wrote file= " + UCDetectorPlugin.getCanonicalPath(file));
  }

  /** Transform from xml to text using xslt transformation */
  private String transformToText(Document xmlDoc, String xslt) throws Exception {
    StringWriter stringWriter = new StringWriter();
    transform(xmlDoc, xslt, new StreamResult(stringWriter));
    return stringWriter.toString();
  }

  private void transform(Document xmlDoc, String xslt, Result result) throws TransformerException {
    InputStream xslIn = getClass().getClassLoader().getResourceAsStream(xslt);
    Templates template = TransformerFactory.newInstance().newTemplates(new StreamSource(xslIn));
    Transformer transformer = template.newTransformer();
    Source source = new DOMSource(xmlDoc);
    transformer.transform(source, result);
  }

  private static void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, read);
    }
  }

  public void setExtension(ReportExtension reportExtension) {
    //
  }
}
