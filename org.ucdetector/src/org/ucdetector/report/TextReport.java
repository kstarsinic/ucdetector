/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
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
* @see /org.ucdetector/src/org/ucdetector/report/html.xslt
 */
public class TextReport implements IUCDetectorReport {
  private static final String EXTENSION_XML = ".xml"; //$NON-NLS-1$

  private static final String EXTENSION_HTML = ".html"; //$NON-NLS-1$

  private static final String XSL_FILE = "org/ucdetector/report/html.xslt";//$NON-NLS-1$

  private final DateFormat dateFormatter = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

  private Document doc;
  private Element markers;
  private Element statistcs;
  private int markerCount;
  private Throwable initXMLException;

  public TextReport() {
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
      doc.appendChild(root);
      markers = doc.createElement("markers");//$NON-NLS-1$
      root.appendChild(markers);
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
  public void reportMarker(ReportParam reportParam) throws CoreException {
    if (initXMLException != null || !Prefs.isWriteReportFile()) {
      return;
    }
    markerCount++;
    markers.appendChild(doc.createComment(" === Marker number " + markerCount));//$NON-NLS-1$
    Element marker = doc.createElement("marker"); //$NON-NLS-1$
    markers.appendChild(marker);
    IResource resource = reportParam.javaElement.getResource();

    // NODE: "Error", "Warning"
    appendChild(marker, "level", reportParam.level.toString());//$NON-NLS-1$

    // NODE: org.ucdetector
    IPackageFragment pack = JavaElementUtil
        .getPackageFor(reportParam.javaElement);
    appendChild(marker, "package", pack.getElementName());//$NON-NLS-1$

    // NODE: UCDetectorPlugin
    IType type = JavaElementUtil.getTypeFor(reportParam.javaElement);
    appendChild(marker, "class", JavaElementUtil.getElementName(type));//$NON-NLS-1$

    if (reportParam.javaElement instanceof IMethod) {
      // NODE: method
      IMethod method = (IMethod) reportParam.javaElement;
      appendChild(marker, "method", JavaElementUtil.getSimpleMethodName(method));//$NON-NLS-1$
    }
    if (reportParam.javaElement instanceof IField) {
      // NODE: field
      appendChild(marker,
          "field", JavaElementUtil.getElementName(reportParam.javaElement));//$NON-NLS-1$
    }

    if (resource != null) {
      // NODE: NoReferenceExample.java
      appendChild(marker, "resource", resource.getName());//$NON-NLS-1$
    }

    // NODE: 123
    appendChild(marker, "line", String.valueOf(reportParam.line));//$NON-NLS-1$

    // NODE: Change visibility of MixedExample to default
    appendChild(marker, "description", reportParam.message);//$NON-NLS-1$

    if (resource != null && resource.getLocation() != null) {
      // NODE:
      // C:/org.ucdetector_test/example/org/ucdetector/example/NoReferenceExample.java
      appendChild(marker, "location", resource.getLocation().toString());//$NON-NLS-1$
    }
    appendChild(marker, "nr", String.valueOf(markerCount));//$NON-NLS-1$
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
    String htmlFileName = Prefs.getReportFile();
    if (initXMLException != null) {
      logEndReportMessage(Messages.XMLReportWriteError, htmlFileName,
          IStatus.ERROR, initXMLException);
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
      logEndReportMessage(Messages.XMLReportWriteOk, htmlFile.getAbsoluteFile()
          .toString(), IStatus.INFO, null);

    }
    catch (Exception e) {
      logEndReportMessage(Messages.XMLReportWriteError, htmlFileName,
          IStatus.ERROR, e);
    }
  }

  /**
   * Create a <code>Status</code> and log it to the Eclipse log
   */
  private static void logEndReportMessage(String message, String parm,
      int iStatus, Throwable ex) {
    String mes = NLS.bind(message, new Object[] { parm });
    Status status = new Status(iStatus, UCDetectorPlugin.ID, iStatus, mes, ex);
    UCDetectorPlugin.logStatus(status);
  }

  /**
   * Append statistics like: date, duration, searched elements
   */
  private void appendStatistics(Object[] selected, long start) {
    long end = System.currentTimeMillis();
    appendChild(statistcs, "date", dateFormatter.format(new Date(end)));//$NON-NLS-1$
    appendChild(statistcs, "duration", String.valueOf((end - start) / 1000d));//$NON-NLS-1$
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
