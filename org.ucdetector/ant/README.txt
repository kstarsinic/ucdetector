            Run UCDetector in headless mode 

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).

Last version of this file: https://ucdetector.svn.sourceforge.net/svnroot/ucdetector/trunk/org.ucdetector/ant/README.txt?view=markup

Requirements:
- java    >= 5.0
- Eclipse >= 3.5
- org.ucdetector_x.y.z.jar
Optional:
- ant     >= 1.7.0  (http://ant.apache.org)


Directory setup:
- Create a new base directory          : ucdetector-headless
- Put java projects in                 : ucdetector-headless/workspace
- Unpack downloaded eclipse to         : ucdetector-headless/eclipse
- Put org.ucdetector_x.y.z.jar in      : ucdetector-headless/eclipse/plugins (dropins does not work)
- Unzip org.ucdetector_x.y.z.zip/ant to: ucdetector-headless/ant


To run UCDetector change working dir:
> cd ucdetector-headless/ant

And run one of (ant, linux, win):
> ant
> ./detect.sh 
> detect.bat

Check created reports


To Change settings, change following files:
 - build.xml
 - options.properties
 - ucdetector.target


Workarround to 'see' compile errors and warnings:
 - ucdetector-headless/workspace/.metadata/.plugins/org.eclipse.core.resources/.projects/myProject/.markers.snap