package org.ucdetector.example.inheritance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 
 */
public class ImplementsExample implements Observer {
  public static void main(String[] args) {

    new MyComparator().toString();
    new MyObserver().toString();

    List<String> list = new ArrayList<String>();
    Collections.sort(list, new Comparator<String>() {
      /**
       * Implemented method
       */
      // TODO: 2009-04-08: Broken no marker should be here!
      // problem fixed: use SDK instead of JRE???
      public int compare(String s1, String s2) {
        return s1.length() - s2.length();
      }

      @SuppressWarnings("unused")
      public int compareUnused(String s1, String s2) { // Marker YES: unused code
        return s1.length() - s2.length();
      }
    });
  }

  private static class MyComparator implements Comparator<String> {
    // @Override
    public int compare(String o1, String o2) {
      return 0;
    }
  }

  private static class MyObserver implements Observer {
    // @Override
    public void update(Observable o, Object arg) {
    }
  }

  /**
   * Implemented method
   */
  public void update(Observable o, Object arg) {

  }

  public void notImplements() { // Marker YES: unused code

  }
}
