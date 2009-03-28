#export launcher=/opt/eclipse-3.3/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar
#export launcher=/opt/eclipse-3.4M5/plugins/org.eclipse.equinox.launcher_1.0.100.v20071211.jar
export launcher=/opt/eclipse-3.4/plugins/org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar

export workspace=~/workspace/runtime-EclipseApplication

java -jar $launcher -application org.ucdetector.ucd -consolelog  -data $workspace -debug -projects org.ucdetector.example,org.ucdetector
