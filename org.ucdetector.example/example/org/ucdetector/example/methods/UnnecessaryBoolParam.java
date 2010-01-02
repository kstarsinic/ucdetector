package org.ucdetector.example.methods;
/**
 * Check for unnecessary (boolean) parameters - ID: 2893808
 * 
 * http://sourceforge.net/tracker/?func=detail&aid=2893808&group_id=219599&atid=1046868
 */
public class UnnecessaryBoolParam {

  public void unnecessaryBool(String s, boolean bool) {
  }

  public void necessaryBool(String s, boolean bool) {
  }
}
