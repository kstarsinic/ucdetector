#!/bin/bash
#   ------------------------------------------------------------------------------
#   Copyright (c) 2017 Joerg Spieler All rights reserved. This program and the
#   accompanying materials are made available under the terms of the Eclipse
#   Public License v1.0 which accompanies this distribution, and is available at
#   http://www.eclipse.org/legal/epl-v10.html
#   ------------------------------------------------------------------------------

# == Installer script for UCDetector headless ==

# == Usage ==
# * Put this file in a new directory
# * Run $ ./setup.sh

# == This script ==
# * Needs a java installation
# * Downloads eclipse
# * Unzips eclipse
# * Downloads UCDetector
# * Installs UCDetector in downloaded eclipse
# * Downloads UCDetectors source code
# * Unzips UCDetectors source code
# * Runs UCDetectors headless checking UCDetectors source code

DOWNLOADS_DIR=downloads
UCDETECTOR_VERSION=2.0.0
UCDETECTOR=org.ucdetector_$UCDETECTOR_VERSION.jar
UCDETECTOR_SOURCE=org.ucdetector.source_$UCDETECTOR_VERSION.zip

### Install java, if missing

downloadFile(){
    downloadRemoteDir=$1
    downloadFile=$2
    if [ ! -e $DOWNLOADS_DIR/$downloadFile ] ; then
        echo "* Download: $downloadFile"
        wget --directory-prefix=$DOWNLOADS_DIR $downloadRemoteDir/$downloadFile
    else
        echo "* Already downloaded: $downloadFile"
    fi
}

# eclipse
arch=`uname -m`
if [ "$arch" == 'x86_64' ] ; then
    ECLIPSE=eclipse-committers-neon-3-linux-gtk-x86_64.tar.gz
else
    ECLIPSE=eclipse-committers-neon-3-linux-gtk.tar.gz
fi

echo "== Download files =="
mkdir -p $DOWNLOADS_DIR
# Use Eclipse IDE for: 'Java EE Developers' or 'Eclipse Committers' ('Plug-in Development Environment' needed to load target platform)
downloadFile http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/technology/epp/downloads/release/neon/3/ $ECLIPSE

downloadFile http://netcologne.dl.sourceforge.net/project/ucdetector/ucdetector/$UCDETECTOR_VERSION/ $UCDETECTOR
downloadFile http://netcologne.dl.sourceforge.net/project/ucdetector/ucdetector/$UCDETECTOR_VERSION/ $UCDETECTOR_SOURCE

echo "== Install files =="
echo "* Unzip sources in workspace"
mkdir -p workspace/org.ucdetector
# ---------------------------------------------------------------------
# Adapt next lines: Put your eclipse projects in workspace folders
# ---------------------------------------------------------------------
# override, quiet
unzip -o -q downloads/$UCDETECTOR_SOURCE -d workspace/org.ucdetector
# ---------------------------------------------------------------------

if [ ! -e eclipse ] ; then
    echo "* Unpack eclipse"
    tar -xf downloads/$ECLIPSE
fi

echo "* Install UCDetector in eclipse"
cp -v downloads/$UCDETECTOR eclipse/plugins/

echo "* Install UCDetector headless scripts"
cp -r workspace/org.ucdetector/ant .

echo "== Run UCDetector for projects in workspace folder =="
cd ant
chmod 755 detect.sh 
./detect.sh
