package org.ucdetector.example;

import java.util.ArrayList;

/**
 * 
 */
public class InnerClassExample {
  public static void main(String[] args) {
    new MyArrayList2<String>();
  }

  static class MyArrayList<E> extends ArrayList<E> { // Marker YES

  }

  private static class MyArrayList2<E> extends ArrayList<E> {
    public void unused() {// Marker YES

    }

    @Override
    public int size() {
      return super.size();
    }
  }
}
