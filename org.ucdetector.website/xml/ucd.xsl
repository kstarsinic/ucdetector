<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  <xsl:strip-space elements="*"/>  -->
	<xsl:output encoding="ISO-8859-1" indent="yes" method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/transitional.dtd" />

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
			
<!-- Piwik -->
<script type="text/javascript">
var pkBaseURL = (("https:" == document.location.protocol) ? "https://apps.sourceforge.net/piwik/ucdetector/" : "http://apps.sourceforge.net/piwik/ucdetector/");
document.write(unescape("%3Cscript src='" + pkBaseURL + "piwik.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
piwik_action_name = '';
piwik_idsite = 1;
piwik_url = pkBaseURL + "piwik.php";
piwik_log(piwik_action_name, piwik_idsite, piwik_url);
</script>
<object><noscript><p><img src="http://apps.sourceforge.net/piwik/ucdetector/piwik.php?idsite=1" alt="piwik"/></p></noscript></object>
<!-- End Piwik Tag -->

			<body>
<!-- Navigation table -->
				<table width="100%" border="0" summary="left navigation, right text">
					<tr valign="top">
						<td width="10%" bgcolor="#FFFFE0">
							<table border="0">
								<tr height="100">
									<td></td>
								</tr>
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
				</table>
<!-- FOOTER =============================================================== -->
				<hr/>
				<table width="100%" summary="footer">
					<tr>
						<td align="left" valign="middle">Last Update: @TODAY@</td>
						<td align="right" valign="middle">Copyright &#169; @YEAR@, by <a href="mailto:feedback@ucdetector.org">Jörg Spieler</a></td>
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
	<!-- ============= COPY ALL NODES ======================================== -->
	<xsl:template match="*">
		<xsl:copy-of select="."/>
	</xsl:template>
	<!-- 
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
	 -->
</xsl:stylesheet>
