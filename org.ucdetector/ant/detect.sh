#!/bin/bash
# ------------------------------------------------------------------------------
#  Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
#  accompanying materials are made available under the terms of the Eclipse
#  Public License v1.0 which accompanies this distribution, and is available at
#  http://www.eclipse.org/legal/epl-v10.html
# ------------------------------------------------------------------------------
# FOR OPTIONS, CHECK build.properties_template!
# Directory of eclipse executable
export ECLIPSE_HOME=/opt/eclipse-3.5-clean

# Your workspace directory (usually parent directory of java projects)
# export WORKSPACE=/home/js/workspace
export WORKSPACE=~/workspace

# export LAUNCHER=$ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
LAUNCHER=$(find $ECLIPSE_HOME -name "org.eclipse.equinox.launcher_*.jar" | sort | tail -1);

# java -jar $LAUNCHER -application org.ucdetector.ucd -consolelog  -data $WORKSPACE -debug -ucd.projects org.ucdetector -ucd.options warnLimit=1,finalMethod=IGNORE
java -jar $LAUNCHER -application org.ucdetector.ucd -consolelog  -data $WORKSPACE -debug

