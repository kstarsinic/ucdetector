Run UCDetector with ANT (experimental!)

Build manager may need to run UCDetector without starting the Eclipse IDE
(=Eclipse headless mode).
Send feeback to feedback@ucdetector.org

Requirements:
- Eclipse + UCDetector
- Eclipse workspace containing java projects, without compile errors

Run scripts:
- Choose one of: detect.sh or detect.bat or build.xml
- Change LAUNCHER 
- Change WORKSPACE
- run selected file

Check logging:
 - There may appear some UI Exceptions, which could be ignored
 - At the end, a html and xml report is created