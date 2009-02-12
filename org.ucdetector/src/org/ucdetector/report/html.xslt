<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
 Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  <xsl:strip-space elements="*"/>  -->
	<xsl:output encoding="ISO-8859-1" indent="yes" method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd" />
	<xsl:template match="/">
		<xsl:comment>
Copyright (c) 2008 Joerg Spieler
To create custom reports change: 
ECLIPSE_HOME/plugins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
		</xsl:comment>
		<html>
			<head>
				<title>UCDetector Report</title>
			</head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

  <!-- =============================== markers ========================= -->
	<xsl:template match="markers">
		<h1>UCDetector report</h1>
		<table border="1">
			<thead align="center">
				<tr bgcolor="#C0C0C0">
					<th>Nr</th>
					<th>Warn level</th>
					<th>Location</th>
					<th>Description</th>
				</tr>
			</thead>
			<xsl:apply-templates/>
		</table>
	</xsl:template>

  <!-- =============================== marker ========================= -->
	<xsl:template match="marker">
		<tr>
			<td>
				<xsl:value-of select="nr"/>
			</td>
			<td>
				<xsl:value-of select="level"/>
			</td>
			<td>
			  <!--  org.eclipse.swt.SWT.error(SWT.java:3634) -->
				<xsl:value-of select="concat(package, '.', class, '.')"/>
				<!-- class needs an additional string -->
				<xsl:if test="not(method) and not(field)">
					<xsl:text>declaration</xsl:text>
				</xsl:if>
				<!-- method -->
				<xsl:if test="method">
					<xsl:value-of select="method"/>
				</xsl:if>
				<!-- field TODO: 2008.11.25: Field is not a link in Eclipse StackTraceConsole -->
				<xsl:if test="field">
					<xsl:value-of select="field"/>
				</xsl:if>
				<!-- (SWT.java:3634) -->
				<xsl:value-of select="concat('(', resource, ':', line, ')')"/>
			</td>
			<td>
				<xsl:value-of select="description"/>
			</td>
		</tr>
	</xsl:template>
	
  <!-- =============================== statistics ========================= -->
	<xsl:template match="statistics">
		<h2>Search statistics</h2>
		Search finished: <xsl:value-of select="dateFinished"/>
		<br></br>
		Search duration: <xsl:value-of select="searchDuration"/><p></p>
		Searched in:
		<ul>
		<xsl:for-each select="searched/search">
	   	<li>
			<xsl:value-of select="."/>
				</li>
			</xsl:for-each>
		</ul>
To create custom reports change:
<pre>ECLIPSE_HOME/plugins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt</pre>	
	</xsl:template>
  <!-- :mode=xsl: -->
</xsl:stylesheet>
