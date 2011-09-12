== Run UCDetector in headless mode ==
Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).


== Requirements ==
* java            >= 5.0
* Eclipse Classic >= 3.5
* org.ucdetector_x.y.z.jar
* Optional: ant (http://ant.apache.org)


== Directory setup ==
* Please create NEW directories for eclipse and your workspace:
* Create a new base directory          : ucdetector-headless
* Put java projects in directory       : ucdetector-headless/workspace
* Put eclipse in                       : ucdetector-headless/eclipse
* Put org.ucdetector_x.y.z.jar in      : ucdetector-headless/eclipse/plugins (dropins may not work)
* Unzip org.ucdetector_x.y.z.zip/ant to: ucdetector-headless/ant


== Run UCDetector ==
* $ cd ucdetector-headless/ant
* Run one of:
** $ ant
** $ detect.bat
** $ ./detect.sh
* Check reports: ucdetector-headless/workspace/ucdetector_reports
* Next steps: Change ucdetector.options, run ucdetector again


== Troubleshooting ==
* Last version of this file: http://ucdetector.svn.sourceforge.net/svnroot/ucdetector/trunk/org.ucdetector/ant/README.txt
* Check: ECLIPSE_HOME/configuration/*.log
* Check: ucdetector-headless/workspace/.metadata/.log
* UCDetector must be installed in: ${ECLIPSE_HOME}/plugins/org.ucdetector_x.y.z.jar
* Workarround to 'see' compile errors, warnings, markers:
  ucdetector-headless/workspace/.metadata/.plugins/org.eclipse.core.resources/.projects/myProject/.markers.snap

