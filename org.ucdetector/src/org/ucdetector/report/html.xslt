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
ECLIPSE_HOME/dropins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
		</xsl:comment>

		<html>
			<head>
				<title>UCDetector Report</title>
				<link rel="icon" href="http://www.ucdetector.org/ucdetector.ico" type="image/x-icon"/>
			</head>
			<body bgcolor="#FFFFE0">
				<h1 align="center">UCDetector Report</h1>

				<!-- ===================================================================
				     ABOUT SEARCH
				     =============================================================== -->

				<xsl:value-of select="concat('Searched started: ', /ucdetector/statistics/dateStarted, '. Duration: ', /ucdetector/statistics/searchDuration)"/>
				<table border="1">
					<thead align="center">
						<tr bgcolor="#C0C0C0">
							<th>Location*</th>
							<th>Nr</th>
							<!-- <th>Warn level</th> -->
							<th>Description</th>
							<th>References**</th>
							<th>Type</th>
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
							<!-- LEVEL
							<td>
								<xsl:value-of select="level"/>
							</td>
							 -->
							<!-- DESCRIPTION -->
							<td>
								<xsl:value-of select="description"/>
							</td>
							<!-- Reference Count -->
							<td align="right">
								<xsl:value-of select="referenceCount"/>
							</td>
							<!-- JAVA TYPE -->
							<td>
								<xsl:value-of select="javaType"/>
							</td>
						</tr>
					</xsl:for-each>
				</table>

				<!-- ===================================================================
				     FOOTNODE
				     =============================================================== -->
				<p>
* To get links to the source locations, copy and paste first column (or table) to Eclipse 'Java Stack Trace Console'<br></br>
** Set 'Detect code with max number of references' &gt; 0<br></br>
To create custom reports change ECLIPSE_HOME/dropins/org.ucdetector_*.jar/org/ucdetector/report/html.xslt
       </p>

				<!-- ===================================================================
				     SEARCH IN
				     =============================================================== -->
				<xsl:text>Searched in:</xsl:text>
				<ul>
					<xsl:for-each select="/ucdetector/statistics/searched/search">
						<li>
							<xsl:value-of select="."/>
						</li>
					</xsl:for-each>
				</ul>

				<!-- ===================================================================
				     PROBLEMS
				     =============================================================== -->
				<xsl:if test="count(/ucdetector/problems/problem) &gt; 0">
					<h2>
						<font color="red">Problems during Detection</font>
					</h2>
					<ul>
						<xsl:for-each select="/ucdetector/problems/problem">
							<li>
								<xsl:value-of select="status"/>
								<pre>
									<xsl:value-of select="exception"/>
								</pre>
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
<!-- :mode=xsl: -->
