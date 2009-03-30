set launcher=C:\eclipse\eclipse-3.4\plugins\org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
REM set launcher=C:\eclipse\eclipse-3.3\plugins\org.eclipse.equinox.launcher_1.0.0.v20070606.jar
REM set launcher=C:\eclipse\eclipse-3.4.1\plugins\org.eclipse.equinox.launcher_1.0.101.R34x_v20080819.jar
REM set launcher=C:\eclipse\eclipse-3.5M5\plugins\org.eclipse.equinox.launcher_1.0.200.v20090128-1500.jar

set workspace=F:\ws\ucd\runtime-ucd_configuration

REM To detect listet projects, use: '-projects org.example.project1,org.example.project2'
REM To detect all projects, remove parameter: '-projects'
java -jar %launcher% -application org.ucdetector.ucd -consolelog  -data %workspace% -debug -projects org.ucdetector.example,org.ucdetector
                                                          
