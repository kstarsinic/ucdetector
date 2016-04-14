== Run UCDetector in headless mode ==
Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).


== Requirements ==
* java            >= 5.0
* Eclipse package: CLASSIC or 'Eclipse for Committers'
* Eclipse Version: [3.5, 3.7]  for technical reason an old version is required. 
                               change TargetPlatformLoader.java and rebuild to use a newer version
* org.ucdetector_x.y.z.jar
* Optional: ant (http://ant.apache.org)


== Directory setup ==
* Please create NEW directories for eclipse and your workspace:
** Create a new base directory          : ucdetector-headless

* Install files automatically (linux) and run UCDetector on UCDetectors source code:
** Copy setup.sh to ucdetector-headless
** $ cd ucdetector-headless
** $ ./setup.sh

* Install files manually:
** Put java projects in directory       : ucdetector-headless/workspace
** Put eclipse in                       : ucdetector-headless/eclipse
** Put org.ucdetector_x.y.z.jar in      : ucdetector-headless/eclipse/plugins (dropins may not work)
** Unzip org.ucdetector_x.y.z.zip/ant to: ucdetector-headless/ant


== Run UCDetector ==
* $ cd ucdetector-headless/ant
* Run ONE of:
** $ ant
** $ detect.bat
** $ ./detect.sh
* Check reports: ucdetector-headless/workspace/ucdetector_reports
* Next steps: Change ucdetector.options, run ucdetector again


== Troubleshooting ==
* Check
** Last version of this file: http://svn.code.sf.net/p/ucdetector/code/trunk/org.ucdetector/ant/README.txt
** ECLIPSE_HOME/configuration/*.log
** ucdetector-headless/workspace/.metadata/.log
* UCDetector must be installed in: ${ECLIPSE_HOME}/plugins/org.ucdetector_x.y.z.jar
* Workarround to 'see' compile errors, warnings, markers (binary file!):
** Check file: ucdetector-headless/workspace/.metadata/.plugins/org.eclipse.core.resources/.projects/myProject/.markers.snap
