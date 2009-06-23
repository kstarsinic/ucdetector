package org.ucdetector.example.classes;

import java.util.ArrayList;

public class AnonymousClass {
  public static void main(String[] args) {
    //
    ArrayList<String> list = new ArrayList<String>() {
      private static final long serialVersionUID = 1L;

      /** ignore, because it is overridden */
      @Override
      public int size() {
        changeToPrivate();
        return 2;
      }

      // 2009-06-23 Removed detection of anonymous classes
      public void changeToPrivate() { 

      }
    };
    System.out.println("size=" + list);
    //    
    ArrayList<String> list2 = new ArrayList<String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public int size() {
        return 2;
      }

      @SuppressWarnings("unused")
      // 2009-06-23 Removed detection of anonymous classes
      public void unused() {

      }
    };
    System.out.println("size=" + list2);
  }
}
