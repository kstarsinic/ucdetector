
REM ----------------------------------------------------------------------------
REM  Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
REM  accompanying materials are made available under the terms of the Eclipse
REM  Public License v1.0 which accompanies this distribution, and is available at
REM  http://www.eclipse.org/legal/epl-v10.html
REM ----------------------------------------------------------------------------

REM 'LAUNCHER' must point to launcher.jar which is located in ECLIPSE_HOME/plugins/
set LAUNCHER=C:\eclipse\eclipse-3.4\plugins\org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
REM set LAUNCHER=C:\eclipse\eclipse-3.3\plugins\org.eclipse.equinox.launcher_1.0.0.v20070606.jar
REM set LAUNCHER=C:\eclipse\eclipse-3.4.1\plugins\org.eclipse.equinox.launcher_1.0.101.R34x_v20080819.jar
REM set LAUNCHER=C:\eclipse\eclipse-3.5M5\plugins\org.eclipse.equinox.launcher_1.0.200.v20090128-1500.jar

REM WORKSPACE must point to your workspace directory (usuall parent directory of java projects)
set WORKSPACE=F:\ws\ucd\runtime-ucd_configuration

set PROJECTS=org.ucdetector.example,org.ucdetector

REM To run UCDetector for all projects: remove parameter: '-projects'
REM To run UCDetector for listet projects use: '-projects org.example.project1,org.example.project2'
java -jar %LAUNCHER% -application org.ucdetector.ucd -consolelog  -data %WORKSPACE% -debug -projects %PROJECTS%


REM OR USE eclipse executable:
C:\eclipse\eclipse-3.4\eclipse.exe -application org.ucdetector.ucd -nosplash -data %WORKSPACE% -debug -projects %PROJECTS%

