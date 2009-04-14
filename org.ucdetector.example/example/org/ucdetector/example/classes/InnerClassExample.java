package org.ucdetector.example.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class InnerClassExample {
  public static void main(String[] args) {
    new MyArrayList2<String>();
    System.out.println(new ArrayList<String>() {
      @Override
      public String toString() {
        return "anonymous class";
      }
    });
  }

  static class MyArrayList<E> extends ArrayList<E> { // Marker YES: unused code

  }

  static class MyArrayList2<E> extends ArrayList<E> {  // Marker YES: use private
    public void unused() {// Marker YES: unused code
      Map<String, String> map = new HashMap<String, String>() {
        @Override
        public int size() {
          return super.size();
        }
      };
      System.out.println(map);
    }

    @Override
    public int size() {
      return super.size();
    }
  }
}
