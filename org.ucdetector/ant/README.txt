Run UCDetector with ANT (experimental!)

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).
Send feeback to feedback@ucdetector.org

Requirements:
- java (>=5.0)
- Eclipse (>=3.3)
- UCDetector.jar

Workspace:
- An eclipse workspace with java projects is needed (no compile errors)
  (When you develop with eclipse, you already have one)
- Set your favorite UCDetector preferences

Run scripts:
- Choose one of: build.xml (recommended) or detect.sh or detect.bat
- Change LAUNCHER (or eclipse executable)
- Change WORKSPACE
- run selected file

Check logging:
 - There may appear some UI Exceptions, which could be ignored
 - There may appear some 'org.eclipse.jdt.core.search' Exceptions, which could be ignored
 - At the end, a html and xml report is created