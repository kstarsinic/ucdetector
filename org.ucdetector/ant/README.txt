            Run UCDetector with ANT (http://ant.apache.org)

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).
Send feedback to feedback@ucdetector.org


Requirements:
- java    (>=5.0)
- Eclipse (>=3.3)
- ant     (>= 1.7.1)
- org.ucdetector_x.y.z.jar


Create Workspace:
- Put java projects in a NEW directory (WORKSPACE)


Install eclipse:
- Unpack downloaded eclipse in a NEW directory (ECLIPSE_HOME)
- Put org.ucdetector_x.y.z.jar in ECLIPSE_HOME/dropins/


Run scripts:
- unzip org.ucdetector_x.y.z.zip/ant to a new directory
- $ cd ant
- $ ant
- Edit build.properties: Change at least WORKSPACE, ECLIPSE_HOME
- $ ant
- [it is also possible to run ./detect.sh or detect.bat (no ant needed)]


Check logging:
 - Check created reports
 - There may appear some UI Exceptions, which could be ignored
 - There may appear some 'org.eclipse.jdt.core.search' Exceptions, which could be ignored
