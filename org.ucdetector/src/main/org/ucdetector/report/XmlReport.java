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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IProject;
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
import org.ucdetector.preferences.PreferenceInitializer;
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
  private static final String COPY_RIGHT = "\n" //
      + "    Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the\n"
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
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private final DateFormat timeFormat = new SimpleDateFormat("HHmmss");
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

  private String getProjectName() {
    SortedSet<String> projects = new TreeSet<String>();
    for (IJavaElement element : objectsToIterate) {
      if (element.getJavaProject() != null) {
        projects.add(element.getJavaProject().getElementName());
      }
    }
    return projects.size() == 0 ? "unknown_project" : projects.size() == 1 ? projects.first() : "several_projects";
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
      // Add <?xml-stylesheet type="text/xsl" href="html.xslt" ?>
      // ProcessingInstruction instruction = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" HREF=\"html.xslt\"");
      // doc.insertBefore(instruction, doc.getDocumentElement());
      Element root = doc.createElement("ucdetector");
      root.appendChild(doc.createComment(COPY_RIGHT));
      doc.appendChild(root);
      //
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

  public void startReport(IJavaElement[] objectsToIterateArray, long startTime) throws CoreException {
    this.objectsToIterate = objectsToIterateArray;
    this.startTime = startTime;
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

      // ===== Attributes =====
      marker.setAttribute("nr", String.valueOf(markerCount));
      marker.setAttribute("level", String.valueOf(reportParam.getLevel()));// "Error", "Warning"
      marker.setAttribute("line", String.valueOf(reportParam.getLine()));
      marker.setAttribute("markerType", markerType);
      int iRefCount = reportParam.getReferenceCount();
      String sReferenceCount = (iRefCount == -1) ? "-" : "" + iRefCount;
      marker.setAttribute("referenceCount", sReferenceCount);

      // ===== Nodes =====
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
      String author = reportParam.getAuthor();
      if (author != null) {
        String trimmedAuthor = author.length() > 70 ? author.substring(0, 70) + "..." : author;
        appendChild(marker, "author", trimmedAuthor);
      }
      appendChild(marker, "description", reportParam.getMessage());// NODE: Change visibility of MixedExample to default

      IResource resource = javaElement.getResource();
      if (resource != null) {
        // F:/ws/ucd/org.ucdetector.example/src/main/org/ucdetector/example/Bbb.java
        // if (resource.getRawLocation() != null) {
        //   appendChild(marker, "resourceRawLocation", resource.getRawLocation().toString());
        // }
        IProject project = resource.getProject();
        if (project != null && project.getLocation() != null) {
          IPath location = project.getLocation();
          Element projectElement = appendChild(marker, "project", null);
          // [ 2762967 ] XmlReport: Problems running UCDetector
          // NODE:  org.ucdetector.example - maybe different projectDir!
          String projectName = project.getName();
          projectElement.setAttribute("name", projectName);
          // NODE:  org.ucdetector.example - maybe different projectName!
          String projectDir = location.lastSegment();
          if (!projectName.equals(projectDir)) {
            projectElement.setAttribute("dir", projectDir);
          }
          String workspaceDir = UCDetectorPlugin.getAboutWorkspace();
          String parentDir = location.removeLastSegments(1).toString();
          if (!workspaceDir.equals(parentDir)) {
            projectElement.setAttribute("parentDir", parentDir);// NODE:  F:/ws/ucd
          }
        }
        IPackageFragmentRoot sourceFolder = JavaElementUtil.getPackageFragmentRootFor(javaElement);
        if (sourceFolder != null && sourceFolder.getResource() != null) {
          IPath path = sourceFolder.getResource().getProjectRelativePath();
          if (path != null) {
            appendChild(marker, "sourceFolder", path.toString()); // NODE:  src/main
          }
        }
        // IContainer parent = resource.getParent();
        //if (parent != null && parent.getProjectRelativePath() != null) {
        //  appendChild(marker, "resourceLocation", packageName.replace('.', '/')); / NODE:  org/ucdetector/example
        //}
        // appendChild(marker, "resourceName", resource.getName()); // NODE: NoReferenceExample.java
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
    Throwable ex = status.getException();
    String exString = "";
    if (ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));
      exString = writer.toString().replace("\r\n", "\n");
    }
    appendChild(problem, "exception", exString);
  }

  /**
   * @return File name, with does not exist, containing a number.
   * eg: UCDetectorReport_001
   */
  // Fix [2811049]  Html report is overridden each run
  private String getReportName() {
    String reportFile = Prefs.getReportFile();
    reportFile = reportFile.replace("${project}", getProjectName());
    reportFile = reportFile.replace("${date}", dateFormat.format(new Date()));
    reportFile = reportFile.replace("${time}", timeFormat.format(new Date()));
    //
    File reportDir = new File(PreferenceInitializer.getReportDir(true));
    if (reportFile.contains(PreferenceInitializer.FILE_NAME_REPLACE_NUMBER)) {
      String[] files = reportDir.list();
      files = (files == null) ? new String[0] : files;
      for (int i = 1; i < 1000; i++) {
        String number = FORMAT_REPORT_NUMBER.format(i);
        boolean numberExists = false;
        for (String file : files) {
          if (file.contains(number)) {
            numberExists = true;
            break;
          }
        }
        if (!numberExists) {
          return reportFile.replace(PreferenceInitializer.FILE_NAME_REPLACE_NUMBER, number);
        }
      }
    }
    return reportFile;
  }

  /** Create a <code>Status</code> and log it to the Eclipse log */
  private static void logEndReportMessage(String message, int iStatus, Throwable ex, String... parms) {
    String mes = NLS.bind(message, parms);
    Status status = new Status(iStatus, UCDetectorPlugin.ID, iStatus, mes, ex);
    if (iStatus == IStatus.ERROR) {
      UCDetectorPlugin.logToEclipseLog(status); // Create status in Error Log View
      return;
    }
    UCDetectorPlugin.logToEclipseLog(status);
  }

  /**
   * Append statistics like: date, searchDuration, searched elements
   */
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

  public void endReport() throws CoreException {
    endReportCalled = true;
    writeReports(true);
  }

  /**
   * Write report to xml file, do xslt transformation to an html file or text file
   */
  private void writeReports(boolean isEndReport) {
    if (!Prefs.isWriteReportFile()) {
      return;
    }
    long start = System.currentTimeMillis();
    File reportDir = new File(PreferenceInitializer.getReportDir(true));
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
    copyIconFiles(reportDir);
    try {
      String reportNumberName = getReportName();
      ArrayList<ReportExtension> xsltExtensions = ReportExtension.getXsltExtensions();
      for (ReportExtension xsltExtension : xsltExtensions) {
        String fileNameFromExtension = xsltExtension.getResultFile().replace("${name}", reportNumberName);
        File resultFile = new File(reportDir, fileNameFromExtension);
        writeTextFile(resultFile, xsltExtension.getXslt());
      }

      if (Prefs.isCreateReportHTML()) {
        Document htmlDocument = transformToHTML(doc);
        File htmlFile = new File(reportDir, reportNumberName + ".html");
        writeDocumentToFile(htmlDocument, htmlFile);
      }
      if (Prefs.isCreateReportXML()) {
        File xmlFile = new File(reportDir, reportNumberName + ".xml");
        writeDocumentToFile(doc, xmlFile);
      }
      if (Prefs.isCreateReportTXT()) {
        File txtFile = new File(reportDir, reportNumberName + ".txt");
        writeTextFile(txtFile, TEXT_XSL_FILE);
      }
      long duration = System.currentTimeMillis() - start;
      Log.info("Created reports in: %s", StopWatch.timeAsString(duration));
      logEndReportMessage(Messages.XMLReport_WriteOk, IStatus.INFO, null, String.valueOf(markerCount), reportPath);
    }
    catch (Exception e) {
      logEndReportMessage(Messages.XMLReport_WriteError, IStatus.ERROR, e, reportPath);
    }
  }

  private static final String[] ICONS = new String[] { //
  //  "ElementClass.gif", "ElementField.gif", "ElementMethod.gif",//
      "FewReference.gif", "Final.gif", "Reference.gif", "TestOnly.gif",//
      "ucd.gif", "ucdetector32.png", //
      "VisibilityDefault.gif", "VisibilityPrivate.gif", "VisibilityProtected.gif", //
  };

  private void copyIconFiles(File reportDir) {
    try {
      File iconsOutDir = new File(reportDir, ".icons");
      iconsOutDir.mkdirs();
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
    catch (IOException ex) {
      Log.error("Problems copying icon files", ex);
    }
  }

  private void copyToIconDir(Path iconPath, String iconName, File outFile, Bundle bundle) throws IOException {
    if (!outFile.exists()) {
      InputStream inStream = FileLocator.openStream(bundle, iconPath.append(iconName), false);
      copyStream(inStream, new FileOutputStream(outFile));
    }
  }

  private void writeTextFile(File file, String xslt) throws Exception, IOException {
    String text = transformToText(doc, xslt);
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(file);
      fileWriter.write(text);
    }
    finally {
      UCDetectorPlugin.closeSave(fileWriter);
    }
    Log.info("Wrote file= " + file.getCanonicalPath());
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
      xformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
      tf.setAttribute("indent-number", Integer.valueOf(2));
    }
    catch (IllegalArgumentException ignore) {
      Log.warn("Can't change output format: " + ignore);
    }
    xformer.transform(source, result);
    Log.info("Wrote file= " + file.getCanonicalPath());
  }

  /** Transform from xml to html using xslt transformation */
  private Document transformToHTML(Document xmlDoc) throws Exception {
    Document transformedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    transform(xmlDoc, HTML_XSL_FILE, new DOMResult(transformedDoc));
    return transformedDoc;
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
}
