== Run UCDetector in headless mode ==
Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).


== Requirements ==
* java    >= 5.0
* Eclipse >= 3.5
* org.ucdetector_x.y.z.jar
* Optional: ant (http://ant.apache.org)


== Directory setup ==
Please create NEW directories for eclipse and your workspace! Otherwise you may get broken eclipse or broken workspace.
* Create a new base directory          : ucdetector-headless
* Put java projects in new directory   : ucdetector-headless/workspace
* Unpack downloaded eclipse to         : ucdetector-headless/eclipse
* Put org.ucdetector_x.y.z.jar in      : ucdetector-headless/eclipse/plugins (dropins may not work)
* Unzip org.ucdetector_x.y.z.zip/ant to: ucdetector-headless/ant


== Run UCDetector ==
 > cd ucdetector-headless/ant
 > ant
* Or call shell scripts (windows/linux):
 > detect.bat
 > ./detect.sh 
* Check reports eg: ucdetector-headless/workspace/ucdetector_reports


== Change settings ==
* Change following files:
** build.xml
** ucdetector.options
** ucdetector.target



== Troubleshooting ==
* Last version of this file: http://ucdetector.svn.sourceforge.net/svnroot/ucdetector/trunk/org.ucdetector/ant/README.txt
* Eclipse log file: ucdetector-headless\workspace\.metadata\.log
* Workarround to 'see' compile errors and warnings:
 - ucdetector-headless/workspace/.metadata/.plugins/org.eclipse.core.resources/.projects/myProject/.markers.snap
* NOTE: The <ucdetector /> ant task need to be run inside an osgi environment and won't work in a normal ant run.
  This setup is done by target "detect".
** http://stackoverflow.com/questions/2327393/running-p2-ant-tasks-outside-eclipse
** http://www-01.ibm.com/support/docview.wss?uid=swg21273017


