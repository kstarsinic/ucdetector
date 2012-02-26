#!/bin/bash
#   ------------------------------------------------------------------------------
#   Copyright (c) 2012 Joerg Spieler All rights reserved. This program and the
#   accompanying materials are made available under the terms of the Eclipse
#   Public License v1.0 which accompanies this distribution, and is available at
#   http://www.eclipse.org/legal/epl-v10.html
#   ------------------------------------------------------------------------------

# == Installer script for UCDetector headless ==
# * Download eclipse and unzip it
# * Download UCDetector and install it in eclipse
# * Download UCDetectors source code
# * Run UCDetectors headless checking UCDetectors source code

DOWNLOADS_DIR=downloads
UCDETECTOR_VERSION=1.9.0
UCDETECTOR=org.ucdetector_$UCDETECTOR_VERSION.jar
UCDETECTOR_SOURCE=org.ucdetector.source_$UCDETECTOR_VERSION.zip

### Install java, if missing
# java --version || sudo apt-get install -y openjdk-6-jre

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
    ECLIPSE=eclipse-SDK-3.7.2-linux-gtk-x86_64.tar.gz
else
    ECLIPSE=eclipse-SDK-3.7.2-linux-gtk.tar.gz
fi

echo "== Download files =="
mkdir -p $DOWNLOADS_DIR
downloadFile http://ftp.halifax.rwth-aachen.de/eclipse/eclipse/downloads/drops/R-3.7.2-201202080800/ $ECLIPSE
downloadFile http://netcologne.dl.sourceforge.net/project/ucdetector/ucdetector/$UCDETECTOR_VERSION/ $UCDETECTOR
downloadFile http://netcologne.dl.sourceforge.net/project/ucdetector/ucdetector/$UCDETECTOR_VERSION/ $UCDETECTOR_SOURCE

echo "== Install files =="
echo "* Unzip sources in workspace"
mkdir -p workspace/org.ucdetector
# ---------------------------------------------------------------------
# Adapt next lines: Put your eclipse projects in workspace folders
# ---------------------------------------------------------------------
unzip -q downloads/$UCDETECTOR_SOURCE -d workspace/org.ucdetector
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
