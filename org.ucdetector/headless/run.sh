#

#export launcher=/opt/eclipse-3.3/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar
export launcher=/opt/eclipse-3.4/plugins/org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
#export launcher=/opt/eclipse-3.4M5/plugins/org.eclipse.equinox.launcher_1.0.100.v20071211.jar

export workspace=F:/ws/ucd\/runtime-ucd_configuration

java -jar $launcher -application org.eclipse.ant.core.antRunner -data $workspace -file headless.xml -consolelog -debug

