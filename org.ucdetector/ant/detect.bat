
REM ----------------------------------------------------------------------------
REM  Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
REM  accompanying materials are made available under the terms of the Eclipse
REM  Public License v1.0 which accompanies this distribution, and is available at
REM  http://www.eclipse.org/legal/epl-v10.html
REM ----------------------------------------------------------------------------
REM FOR OPTIONS, CHECK build.xml!
REM Directory of eclipse executable
set ECLIPSE_HOME=C:\eclipse\eclipse-3.5

REM Your workspace directory (usually parent directory of java projects)
set WORKSPACE=%USERPROFILE%\workspace

REM set LAUNCHER=%ECLIPSE_HOME%\plugins\org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSE_HOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set LAUNCHER=%%c
java -jar %LAUNCHER% -application org.ucdetector.ucd -consolelog  -data $WORKSPACE -debug

