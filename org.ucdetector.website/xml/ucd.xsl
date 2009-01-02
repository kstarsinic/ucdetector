<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  <xsl:strip-space elements="*"/>  -->
	<xsl:output encoding="ISO-8859-1" indent="yes" method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd" />
  <!--TODO: Copy node and attribute as found, to output.
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
	  -->
  <!-- =============================== ROOT ================================ -->
	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
				<meta http-equiv="Content-Language" content="em"/>
				<meta http-equiv="author" content="Joerg Spieler"/>
				<meta http-equiv="description" content="Eclipse PlugIn to detect unnecessary or dead java code and class cycles"/>
				
				<meta http-equiv="rating" content="eclipse PlugIn"/>
				<meta http-equiv="revisit-after" content="3 days"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta http-equiv="keywords" content="java, eclipse, tool, PlugIn, programmer, code quality, refactoring, unused code, dead code, class cycles"/>
				<meta name="robots" content="index,follow"/>
				
				<title>UCDetector</title>
				<link rel="shortcut icon" href="http://www.ucdetector.org/ucdetector.ico"/>
				<style type="text/css">
 thead.c8 {text-align: center}
 span.c7 {color: #009917}
 span.c6 {color: #000000}
 span.c5 {color: #0099FF}
 span.c4 {color: #6600CC}
 span.c3 {color: red}
 span.c2 {color: #006699}
 h1.c1 {text-align: center}
</style>
			</head>
			<body>
<!-- Navigation table -->
				<table width="100%" border="0" summary="left navigation, right text">
					<tr valign="top">
						<td width="8%" bgcolor="#FFFFE0">
							<table border="0">
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="index.html">Start</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="preferences.html">Preferences</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="more.html">More</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="releases.html">Releases</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="faq.html">FAQ</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="custom.html">Custom detection</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="license.html">License</a>
									</td>
								</tr>
							<!-- ==================================== -->
								<tr>
									<td>
										<a href="contact.html">Contact &amp; Links</a>
									</td>
								</tr>
							<!-- ==================================== -->
							</table>
						</td>
						<td>
							<h1 align="center">
								<img alt="UCDetector" src="http://www.ucdetector.org/ucdetector.ico"/>
UCDetector: Unnecessary Code Detector</h1>

<!-- !!!!!!!!!!!!!!!!!!!!!!!!! START !!!!!!!!!!!!!!!!!!!!!!!!!!!! -->
							<div align="center">
								<h2>
									<xsl:value-of select="ucd/@page"/>
								</h2>
							</div>
							<xsl:apply-templates/>
<!-- !!!!!!!!!!!!!!!!!!!!!!!!! END !!!!!!!!!!!!!!!!!!!!!!!!!!!! -->
						</td>
					</tr>
					<!--
					<tr>
						<td></td>
						<td>
							<hr/>
							<table width="100%" summary="footer">
								<tr>
									<td align="left" valign="middle">Last Update: 2008-10-12</td>
									<td align="right" valign="middle">Copyright &#169; 2008, by Joerg Spieler</td>
								</tr>
							</table>
						</td>
					</tr>
					-->
				</table>
				
<!-- FOOTER =============================================================== -->
				<hr/>
				<table width="100%" summary="footer">
					<tr>
						<td align="left" valign="middle">Last Update: @TODAY@</td>
						<td align="right" valign="middle">Copyright &#169; @YEAR@, by Joerg Spieler</td>
					</tr>
				</table>
				<hr/>
				<table width="100%" summary="footer 2">
					<tr>
						<td align="left" valign="middle">
	Page views since 2008-03-01:
			<img alt="Page views since 2008-03-01" src="http://www.jspieler.de/cgi-bin/counting.php.cgi?counterid=3" />
						</td>
						<td align="right" valign="middle">
	Hosted by:
	<a href="http://sourceforge.net">
		<img src="http://sflogo.sourceforge.net/sflogo.php?group_id=219599&amp;type=2" width="125" height="37" border="0" alt="SourceForge.net Logo" />
							</a>
						</td>
					</tr>
				</table>
<!-- FOOTER =============================================================== -->
			</body>
		</html>
	</xsl:template>
	<!-- ================ LISTS ===============================================-->
	<!-- 
	TODO:
	http://codexmonkey.blogspot.com/2007/02/stupid-xslt-trick-1-escape-madness.html
	 -->
	<xsl:template match="ul">
		<ul>
			<xsl:apply-templates/>
		</ul>
	</xsl:template>
	
	<xsl:template match="li">
		<li>
			<xsl:apply-templates/>
		</li>
	</xsl:template>
	
	<!-- ================ HEADER ==============================================-->
	<xsl:template match="h1">
		<h1>
			<xsl:apply-templates/>
		</h1>
	</xsl:template>
	
	<xsl:template match="h2">
		<h2>
			<xsl:apply-templates/>
		</h2>
	</xsl:template>
	
	<xsl:template match="h3">
		<h3>
			<xsl:apply-templates/>
		</h3>
	</xsl:template>
	
	<xsl:template match="h4">
		<h4>
			<xsl:apply-templates/>
		</h4>
	</xsl:template>
	
	<!-- ================ TABLE ===============================================-->
	<xsl:template match="table">
		<table border="{@border}" summary="{@summary}">
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	
	<xsl:template match="thead">
		<thead align="{@align}">
			<xsl:apply-templates/>
		</thead>
	</xsl:template>
	
	<xsl:template match="th">
		<th>
			<xsl:apply-templates/>
		</th>
	</xsl:template>
	
	<xsl:template match="tr">
		<tr valign="{@valign}" bgcolor="{@bgcolor}">
			<xsl:apply-templates/>
		</tr>
	</xsl:template>
	
	<xsl:template match="td">
		<td valign="{@valign}">
			<xsl:apply-templates/>
		</td>
	</xsl:template>

	<!-- ================ OTHER ===============================================-->
	<xsl:template match="img">
		<img alt="{@alt}" src="{@src}"/>
	</xsl:template>
	
	<xsl:template match="p">
		<p>
			<xsl:apply-templates/>
		</p>
	</xsl:template>
	
	<xsl:template match="a">
		<a href="{@href}">
			<xsl:apply-templates/>
		</a>
	</xsl:template>
	
	<xsl:template match="b">
		<b>
			<xsl:apply-templates/>
		</b>
	</xsl:template>
	
	<xsl:template match="br">
		<br>
			<xsl:apply-templates/>
		</br>
	</xsl:template>
	
	<xsl:template match="font">
		<font color="{@color}">
			<xsl:apply-templates/>
		</font>
	</xsl:template>
	
	<xsl:template match="strong">
		<strong>
			<xsl:apply-templates/>
		</strong>
	</xsl:template>
	
	<xsl:template match="pre">
		<pre>
			<xsl:apply-templates/>
		</pre>
	</xsl:template>
	
	<xsl:template match="span">
		<span class="{@class}">
			<xsl:apply-templates/>
		</span>
	</xsl:template>
</xsl:stylesheet>
