            Run UCDetector with ANT (http://ant.apache.org)

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).
Send feedback to feedback@ucdetector.org

Requirements:
- java    (>=5.0)
- Eclipse (>=3.3)
- ECLIPSE_HOME/dropins/org.ucdetector_x.y.z.jar

Workspace:
- An eclipse workspace with java projects is needed (no compile errors)
  (When you develop with eclipse, you already have one)
- Set your favorite UCDetector preferences in Eclipse IDE

Run scripts:
- unzip org.ucdetector_x.y.z.jar/ant to a new directory
- >cd ant
- >ant
- Edit build.properties: Change WORKSPACE, ECLIPSE_HOME, LAUNCHER [maybe more]
- >ant
- [it is also possible to run detect.sh or detect.bat]

Check logging:
 - There may appear some UI Exceptions, which could be ignored
 - There may appear some 'org.eclipse.jdt.core.search' Exceptions, which could be ignored
 - At the end, a html and xml report is created