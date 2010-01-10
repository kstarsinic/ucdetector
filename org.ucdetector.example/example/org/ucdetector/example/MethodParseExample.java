package org.ucdetector.example;

public class MethodParseExample {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("aaa");
    a();
    MethodParseExample ex = new MethodParseExample();
    ex.b();
    System.out.println("bbb");
    MethodParseExample.c(1);
    System.out.println("cccc");
  }

  private static void a() {

  }

  private void b() {

  }

  public static String c(int i) {
    return "";
  }
}
