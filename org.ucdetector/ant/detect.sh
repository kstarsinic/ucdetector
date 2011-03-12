#!/bin/bash
#   ------------------------------------------------------------------------------
#   Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
#   accompanying materials are made available under the terms of the Eclipse
#   Public License v1.0 which accompanies this distribution, and is available at
#   http://www.eclipse.org/legal/epl-v10.html
#   ------------------------------------------------------------------------------

ECLIPSE_HOME=../eclipse
WORKSPACE=../workspace
LAUNCHER=$(find $ECLIPSE_HOME/plugins -name "org.eclipse.equinox.launcher_*.jar" | sort | tail -1);

echo "ECLIPSE_HOME: '$ECLIPSE_HOME'"
echo "WORKSPACE   : '$WORKSPACE'"
echo "LAUNCHER    : '$LAUNCHER'  - when launcher is empty, set it!"

java -Xmx1024m -jar $LAUNCHER -debug -clean -data $WORKSPACE -application org.eclipse.ant.core.antRunner -buildfile build.xml run-ucdetector $@

