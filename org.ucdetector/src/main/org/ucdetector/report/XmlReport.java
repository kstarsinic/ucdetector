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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
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
import org.ucdetector.UCDInfo;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.JavaElementUtil.MemberInfo;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
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
  private static final String DTD_FILE = "ucdetector.dtd";
  private static final String COPY_RIGHT = //
      /*<!-- */" ===========================================================================\n"
          + "     Copyright (c) 2016 Joerg Spieler All rights reserved. This program and the\n"
          + "     accompanying materials are made available under the terms of the Eclipse\n"
          + "     Public License v1.0 which accompanies this distribution, and is available at\n"
          + "     http://www.eclipse.org/legal/epl-v10.html\n"
          + "     ========================================================================\n";

  private Document doc;

  private Element statistcs;
  private Element markers;
  private Element problems;
  private Element abouts;

  private int markerCount;
  private int detectionProblemCount;
  private Throwable initXMLException;

  private final Map<String, Element> aboutNodes = new HashMap<String, Element>();
  private boolean endReportCalled;

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
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      DOMImplementation domImpl = builder.getDOMImplementation();
      DocumentType docType = null;// domImpl.createDocumentType("ucdetector", null, dtdFile);
      doc = domImpl.createDocument(null, "ucdetector", docType);
      Element root = doc.getDocumentElement();

      String stylesheet = String.format("type=\"text/xsl\" href=\"%s/%s\"", ICONS_DIR, HTML_XSLT);
      doc.insertBefore(doc.createProcessingInstruction("xml-stylesheet", stylesheet), root);
      doc.insertBefore(doc.createComment(COPY_RIGHT), root);
      doc.insertBefore(doc.createComment("\n"), root);

      statistcs = appendChild(root, "statistics");
      markers = appendChild(root, "markers");
      problems = appendChild(root, "problems");
      abouts = appendChild(statistcs, "abouts", null);
    }
    catch (Throwable e) {
      Log.error("Can't create xml report: ", e);
      initXMLException = e;
    }
  }

  /**
   * @param startTimeIn time, when report is started
   */
  @Override
  public void startReport(IJavaElement[] objectsToIterateArray, long startTimeIn) throws CoreException {
    this.objectsToIterate = objectsToIterateArray;
    this.startTime = startTimeIn;
  }

  /**
   * creates for each marker a xml element and its children
   */
  @Override
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
      marker = appendChild(markers, "marker");
      setMarkerAttributes(reportParam, marker);

      IMember javaElement = reportParam.getJavaElement();
      IResource resource = javaElement.getResource();
      // ===== Nodes =====
      appendChild(marker, "description", reportParam.getMessage());// NODE: Change visibility of MixedExample to default
      if (resource != null && resource.getRawLocation() != null) {
        // F:/ws/ucd/org.ucdetector.example/src/main/org/ucdetector/example/Bbb.java
        appendChild(marker, "file", resource.getRawLocation().toOSString());
      }
      appendProject(marker, javaElement);
      IPackageFragmentRoot sourceFolder = JavaElementUtil.getPackageFragmentRootFor(javaElement);
      if (sourceFolder != null && sourceFolder.getResource() != null) {
        IPath path = sourceFolder.getResource().getProjectRelativePath();
        if (path != null) {
          appendChild(marker, "sourceFolder", path.toString()); // NODE:  src/main
        }
      }
      IPackageFragment pack = JavaElementUtil.getPackageFor(javaElement);
      if (pack != null) {
        appendChild(marker, "package", pack.getElementName());
        IType type = JavaElementUtil.getTypeFor(javaElement, true);// NODE: UCDetectorPlugin
        appendChild(marker, "class", JavaElementUtil.getElementName(type));
      }
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

  private void setMarkerAttributes(ReportParam reportParam, Element marker) {
    String markerType = reportParam.getMarkerType();
    if (markerType.startsWith(MarkerFactory.UCD_MARKER_TYPE_PREFIX)) {
      markerType = markerType.substring(MarkerFactory.UCD_MARKER_TYPE_PREFIX.length());
    }
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
  }

  private void appendProject(Element marker, IMember javaElement) {
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
  }

  @Override
  public void reportDetectionProblem(IStatus status) {
    detectionProblemCount++;
    Element problem = appendChild(problems, "problem");
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
  @SuppressWarnings("boxing")
  private void appendStatistics(boolean isEndReport) {
    if (aboutNodes.isEmpty()) {
      // First time called
      // @formatter:off
      appendAbout("operatingSystem"  , "Operating system" , UCDInfo.getOS()                  , true );
      appendAbout("javaVersion"      , "Java"             , UCDInfo.getJavaVersion()         , true );
      appendAbout("eclipseVersion"   , "Eclipse"          , UCDInfo.getEclipseVersion()      , true );
      appendAbout("eclipseHome"      , "Eclipse home"     , UCDInfo.getEclipseHome()         , false);
      appendAbout("eclipseProduct"   , "Eclipse product"  , UCDInfo.getEclipseProduct()      , true );
      appendAbout("ucdetectorVersion", "UCDetector"       , UCDInfo.getUCDVersion()          , true );
      appendAbout("logfile"          , "Logfile"          , UCDInfo.getLogfile()             , false);
      appendAbout("workspace"        , "Workspace"        , UCDInfo.getWorkspace()           , false);
      appendAbout("mode"             , "Mode"             , Prefs.getModeName()              , true );
      appendAbout("host"             , "Host"             , UCDInfo.getHostName()            , false);
      appendAbout("createdBy"        , "Created by class" , getClass().getName()             , false);
      appendAbout("headless"         , "Headless"         , UCDetectorPlugin.isHeadlessMode(), false);
      // @formatter:on
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
    long durationMillis = (now - startTime);
    String durationString = StopWatch.timeAsString(durationMillis);
    // @formatter:off
    appendAbout("reportCreated"    , "Created report"          , UCDInfo.getNow(false), true );
    appendAbout("reportCreatedTS"  , "Created report timestamp", now                  , false);
    appendAbout("searchDuration"   , "Search duration"         , durationString       , true );
    appendAbout("searchDurationTS" , "Search duration millis"  , durationMillis       , false);
    appendAbout("detectionFinished", "Detection Finished"      , isEndReport          , false);
    appendAbout("warnings"         , "Warnings"                , markerCount          , true );
    // @formatter:on
  }

  /**
   * <pre>
   *  &lt;about name="operatingSystem" show="true">
   *     &lt;key>Operating system&lt;/key>
   *     &lt;value>Linux-2.6.27.39-0.2-default&lt;/value>
   *  &lt;/about>
   * </pre>
   */
  private void appendAbout(String nodeName, String nodeNiceName, Object value, boolean show) {
    Element alreadyCreated = aboutNodes.get(nodeName);
    if (alreadyCreated != null) {
      alreadyCreated.getParentNode().removeChild(alreadyCreated);
    }
    Element about = appendChild(abouts, "about", null);
    aboutNodes.put(nodeName, about);
    about.setAttribute("name", nodeName);
    about.setAttribute("show", Boolean.toString(show));
    appendChild(about, "key", nodeNiceName);
    appendChild(about, "value", String.valueOf(value));
  }

  private Element appendChild(Element parent, String tagName) {
    return appendChild(parent, tagName, null);
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

  @Override
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
      List<ReportExtension> xsltExtensions = ReportExtension.getXsltExtensions();
      for (ReportExtension xsltExtension : xsltExtensions) {
        if (Prefs.isCreateReport(xsltExtension)) {
          String reportName = ReportNameManager.getReportFileName(xsltExtension.getResultFile(), objectsToIterate);
          File resultFile = new File(reportDir, reportName);
          writeTextFile(doc, resultFile, xsltExtension.getXslt());
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

  private static void copyFilesToDotIconDir(File reportDir) {
    File iconsOutDir = new File(reportDir, ICONS_DIR);
    iconsOutDir.mkdirs();
    try {
      copyResource(iconsOutDir, HTML_XSLT);
      copyResource(iconsOutDir, DTD_FILE);
      copyIconFiles(iconsOutDir);
    }
    catch (IOException ex) {
      Log.error("Problems copying files to: " + ICONS_DIR, ex);
    }
  }

  @SuppressWarnings("resource")
  // copyStream closes streams
  private static void copyResource(File iconsOutDir, String resouce) throws IOException {
    InputStream inStream = XmlReport.class.getResourceAsStream(resouce);
    copyStream(inStream, new FileOutputStream(new File(iconsOutDir, resouce)));
  }

  /** Siehe: /org.ucdetector/plugin.xml */
  private static final String[] ICONS = new String[] { // @formatter:off
      //  "ElementClass.gif", "ElementField.gif", "ElementMethod.gif",//
      "FewReference.gif"     ,
      "Final.gif"            ,
      "Reference.gif"        ,
      "TestOnly.gif"         ,
      "ucd.gif"              ,
      "ucdetector32.png"     ,
      "Other.gif"            ,
      "VisibilityDefault.gif",
      "VisibilityPrivate.gif",
      "VisibilityProtected.gif"
  };    // @formatter:on

  private static void copyIconFiles(File iconsOutDir) throws IOException {
    Bundle bundle = UCDetectorPlugin.getDefault().getBundle();
    Path iconPath = new Path("icons");
    for (String iconName : ICONS) {
      copyToIconDir(bundle, iconPath, iconsOutDir, iconName);
    }
    for (MemberInfo memberInfo : MemberInfo.values()) {
      // Since eclipse 4.x icons are not anymore here
      // Bundle bundle = Platform.getBundle("org.eclipse.jdt.ui");
      // Path iconPath = new Path("icons/full/obj16/");
      copyToIconDir(bundle, iconPath, iconsOutDir, memberInfo.getIcon());
    }
  }

  @SuppressWarnings("resource")
  // copyStream closes streams
  private static void copyToIconDir(Bundle bundle, Path iconPath, File iconsOutDir, String iconName)
      throws IOException {
    File outFile = new File(iconsOutDir, iconName);
    if (!outFile.exists()) {
      InputStream inStream = FileLocator.openStream(bundle, iconPath.append(iconName), false);
      copyStream(inStream, new FileOutputStream(outFile));
    }
  }

  private static void writeTextFile(Document doc, File file, String xslt) throws IOException, TransformerException {
    String text = transformToText(doc, xslt);
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(file), UCDetectorPlugin.UTF_8);
      writer.write(text);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
    Log.info("Wrote file= " + UCDetectorPlugin.getCanonicalPath(file));
  }

  private static void writeDocumentToFile(Document docToWrite, File file) throws IOException, TransformerException {
    Source source = new DOMSource(docToWrite);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer xformer = tf.newTransformer();
    try {
      String dtdFile = String.format("%s/%s", ICONS_DIR, DTD_FILE);
      xformer.setOutputProperty(OutputKeys.METHOD, "xml");
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdFile);
      xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      xformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
      tf.setAttribute("indent-number", Integer.valueOf(2));// Broken for java 1.5
    }
    catch (IllegalArgumentException ignore) {
      Log.warn("Can't change output format: " + ignore);
    }
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(file), UCDetectorPlugin.UTF_8);
      Result result = new StreamResult(writer);
      xformer.transform(source, result);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
    Log.info("Wrote file= " + UCDetectorPlugin.getCanonicalPath(file));
  }

  /** Transform from xml to text using xslt transformation
   * @throws TransformerException */
  private static String transformToText(Document xmlDoc, String xslt) throws TransformerException {
    StringWriter stringWriter = new StringWriter();
    transform(xmlDoc, xslt, new StreamResult(stringWriter));
    return stringWriter.toString();
  }

  private static void transform(Document xmlDoc, String xslt, Result result) throws TransformerException {
    InputStream inStream = null;
    try {
      inStream = XmlReport.class.getClassLoader().getResourceAsStream(xslt);
      Templates template = TransformerFactory.newInstance().newTemplates(new StreamSource(inStream));
      Transformer transformer = template.newTransformer();
      Source source = new DOMSource(xmlDoc);
      transformer.transform(source, result);
    }
    finally {
      UCDetectorPlugin.closeSave(inStream);
    }
  }

  private static void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
    try {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = inStream.read(buffer)) != -1) {
        outStream.write(buffer, 0, read);
      }
    }
    finally {
      UCDetectorPlugin.closeSave(inStream);
      UCDetectorPlugin.closeSave(outStream);
    }
  }

  @Override
  public void setExtension(ReportExtension reportExtension) {
    //
  }
}
