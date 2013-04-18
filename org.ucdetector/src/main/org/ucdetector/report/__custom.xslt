<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 accompanying materials are made available under the terms of the Eclipse
 Public License v1.0 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output encoding="UTF-8" indent="no" method="text" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd"/>
	<xsl:template match="/">
    <!--  
          &#x9; = tab
          &#xA; = new line
     -->
    <!-- First line: about -->
		<xsl:value-of select="concat('UCDetector Report', '&#x9;', /ucdetector/statistics/abouts/about[@name='reportCreated']/value, '&#xA;', 'Created by resource org/ucdetector/report/custom.xslt', '&#xA;')"/>
    <!-- Second line: header -->
		<xsl:value-of select="concat('Location', '&#x9;', 'Description', '&#x9;', 'Java', '&#x9;', 'Marker', '&#x9;', 'Author', '&#xA;')"/>
		<xsl:for-each select="/ucdetector/markers/marker">
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
      <!-- field -->
			<xsl:if test="field">
				<xsl:value-of select="field"/>
			</xsl:if>
      <!-- Link in Eclipse Stack Trace Console View: (SWT.java:3634) -->
			<xsl:value-of select="concat('(', class, '.java:', @line, ')', '&#x9;', description, '&#x9;', javaType/@long, '&#x9;', @markerType, '&#x9;', author, '&#xA;')"/>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
<!-- :mode=xsl: -->