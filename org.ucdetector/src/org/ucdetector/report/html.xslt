<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
 Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  <xsl:strip-space elements="*"/>  -->
	<xsl:output encoding="UTF-8" indent="yes" method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd" />
	<xsl:template match="/">
		
		<xsl:comment>
 Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
		</xsl:comment>
		
		<xsl:comment>
To create custom reports change: 
ECLIPSE_HOME/plugins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
		</xsl:comment>
		
		<html>
			<head>
				<title>UCDetector Report</title>
				<link rel="icon" href="http://www.ucdetector.org/ucdetector.ico" type="image/x-icon"/>
			</head>
			<body bgcolor="#FFFFE0">
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

  <!-- =============================== markers ========================= -->
	<xsl:template match="markers">
		<h1 align="center">UCDetector Report</h1>

Searched started <xsl:value-of select="/ucdetector/statistics/dateStarted"/>. 
<!-- finished <xsl:value-of select="/ucdetector/statistics/dateFinished"/> -->
Duration <xsl:value-of select="/ucdetector/statistics/searchDuration"/>.
Searched in:
		<xsl:for-each select="/ucdetector/statistics/searched/search">
		  <xsl:value-of select="."/>
			<xsl:text>, </xsl:text>
		</xsl:for-each>
		
		<table border="1">
			<thead align="center">
				<tr bgcolor="#C0C0C0">
					<th>Location*</th>
					<th>Nr</th>
					<th>Warn level</th>
					<th>Description</th>
					<th>Reference Count**</th>
				</tr>
			</thead>
			<xsl:for-each select="/ucdetector/markers/marker">
				<xsl:variable name="color">
					<xsl:choose>
						<xsl:when test="position() mod 2 = 0">#E6E6FA</xsl:when>
						<xsl:otherwise>#FFFACD</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<tr bgcolor="{$color}">
					<td>
  			<!--  org.eclipse.swt.SWT.error(SWT.java:3634) -->
			  <!-- if not default package -->
						<xsl:if test="string-length(package) &gt; 0">
							<xsl:value-of select="concat(package, '.')"/>
						</xsl:if>
				
			  <!-- class name -->
						<xsl:value-of select="concat(class, '.')"/>

				<!-- class needs an additional string -->
						<xsl:if test="not(method) and not(field)">
							<xsl:text>declaration</xsl:text>
						</xsl:if>
				
				<!-- method -->
						<xsl:if test="method">
							<xsl:value-of select="method"/>
						</xsl:if>

				<!-- filed -->
						<xsl:if test="field">
							<xsl:value-of select="field"/>
						</xsl:if>
				
				<!-- Link in Eclipse Stack Trace Console View: (SWT.java:3634) -->
						<xsl:value-of select="concat('(', resourceName, ':', line, ')')"/>
					</td>
			<!-- NR -->
					<td align="right">
						<xsl:value-of select="nr"/>
					</td>
			<!-- LEVEL -->
					<td>
						<xsl:value-of select="level"/>
					</td>
			<!-- DESCRIPTION -->
					<td>
						<xsl:value-of select="description"/>
					</td>
			<!-- Reference Count -->
					<td align="right">
						<xsl:value-of select="referenceCount"/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<p></p>
* To get links to the source locations, copy and paste table to Eclipse 'Java Stack Trace Console'<br></br>
** Set 'Detect code with max number of references' &gt; 0<br></br>
To create custom reports change ECLIPSE_HOME/plugins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
	</xsl:template>
</xsl:stylesheet>
<!-- :mode=xsl: -->
