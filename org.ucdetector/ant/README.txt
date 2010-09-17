            Run UCDetector with ANT (http://ant.apache.org)

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).

Requirements:
- java    >= 5.0
- Eclipse >= 3.5
- ant     >= 1.7.1
- org.ucdetector_x.y.z.jar


Directory setup:
- Create a new base directory          : ucdetector-headless
- Put java projects in                 : ucdetector-headless/workspace
- Unpack downloaded eclipse to         : ucdetector-headless/eclipse
- Put org.ucdetector_x.y.z.jar in      : ucdetector-headless/eclipse/plugins    (dropins folder does not work!)
- unzip org.ucdetector_x.y.z.zip/ant to: ucdetector-headless/ant

Run UCDetector:
- $ cd ucdetector-headless/ant
- $ ant
- check created reports


Workarround to get compile errors:
 - ucdetector-headless/workspace/.metadata/.plugins/org.eclipse.core.resources/.projects/myProject/.markers.snap