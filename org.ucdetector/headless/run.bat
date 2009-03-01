
set launcher=C:\eclipse\eclipse-3.3\plugins\org.eclipse.equinox.launcher_1.0.0.v20070606.jar

rem set launcher=C:\eclipse\eclipse-3.4\plugins/plugins/org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
rem export launcher=C:\eclipse\eclipse-3.4M5\plugins\org.eclipse.equinox.launcher_1.0.100.v20071211.jar

set workspace=F:\ws\ucd\runtime-ucd_configuration

java -jar %launcher% -application org.eclipse.ant.core.antRunner -data %workspace% -file headless.xml -consolelog -debug

