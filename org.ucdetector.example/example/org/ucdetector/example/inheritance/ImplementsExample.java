package org.ucdetector.example.inheritance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

/**
 * 
 */
public class ImplementsExample implements java.util.Observer {
  public static void main(String[] args) {
    List<String> list = new ArrayList<String>();
    Collections.sort(list, new Comparator<String>() {

      /**
       * Implemented method
       */
      public int compare(String s1, String s2) {
        return s1.length() - s2.length();
      }
    });
  }

  /**
   * Implemented method
   */
  public void update(Observable o, Object arg) {

  }
}
