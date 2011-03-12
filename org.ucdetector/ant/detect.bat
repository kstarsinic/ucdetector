@echo off
REM ----------------------------------------------------------------------------
REM Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
REM accompanying materials are made available under the terms of the Eclipse
REM Public License v1.0 which accompanies this distribution, and is available at
REM http://www.eclipse.org/legal/epl-v10.html
REM ----------------------------------------------------------------------------

set ECLIPSE_HOME=..\eclipse
set WORKSPACE=..\workspace
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSE_HOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set LAUNCHER=%%c

echo ECLIPSE_HOME: %ECLIPSE_HOME%
echo WORKSPACE   : %WORKSPACE%
echo LAUNCHER    : %LAUNCHER%

java -Xmx1024m -jar %LAUNCHER% -debug -clean -data %WORKSPACE% -application org.eclipse.ant.core.antRunner -buildfile build.xml run-ucdetector %*

