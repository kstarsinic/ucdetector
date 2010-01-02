REM @echo off
REM ----------------------------------------------------------------------------
REM  Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
REM  accompanying materials are made available under the terms of the Eclipse
REM  Public License v1.0 which accompanies this distribution, and is available at
REM  http://www.eclipse.org/legal/epl-v10.html
REM ----------------------------------------------------------------------------
REM FOR OPTIONS, CHECK build.properties_template!
REM Directory of eclipse executable
set ECLIPSE_HOME=C:\eclipse\eclipse-3.3

REM Your workspace directory (usually parent directory of java projects)
REM set WORKSPACE=F:\ws\ucd\runtime-ucd_configuration
set WORKSPACE=%USERPROFILE%\workspace

REM set LAUNCHER=%ECLIPSE_HOME%\plugins\org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSE_HOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set LAUNCHER=%%c

REM java -jar %LAUNCHER% -application org.ucdetector.ucd -consolelog -data %WORKSPACE% -debug -projects org.ucdetector -options warnLimit=1,finalMethod=IGNORE
java -jar %LAUNCHER% -application org.ucdetector.ucd -consolelog  -data %WORKSPACE% -debug

