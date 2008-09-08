package org.ucdetector.example.methods;
/**
 *
 */
public class BeanExample {
  private String hello = "Hello";
  private boolean isValid = true;

  /** no marker, because this is a bean method */
  public String getAge() {
    return hello;
  }

  /** no marker, because this is a bean method */
  public String getHello() {
    return hello;
  }

  /** no marker, because this is a bean method */
  public void setHello(String hello) {
    this.hello = hello;
  }

  /** no marker, because this is a bean method */
  public boolean isValid() {
    return isValid;
  }

  /** no marker, because this is a bean method */
  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }

  /** no marker, because this is a bean method */
  public int getTest() {
    return 1;
  }

  // ---------------------------------------------------------------------------
  // NO BEAN METHODS: getHello()
  // ---------------------------------------------------------------------------
  public String getHello(int i) { // Marker YES: unused code
    return hello;
  }

  public String gethello() { // Marker YES: unused code
    return hello;
  }

  public static String getHello2() { // Marker YES: unused code
    return "";
  }

  String getHello3() { // Marker YES: unused code
    return hello;
  }

  public void getHello4() { // Marker YES: unused code
  }

  public String GetHello() { // Marker YES: unused code
    return hello;
  }

  public String get() { // Marker YES: unused code
    return hello;
  }

  // ---------------------------------------------------------------------------
  // NO BEAN METHODS: setHello(hello)
  // ---------------------------------------------------------------------------
  public void setHello() { // Marker YES: unused code
  }

  public void sethello(String hello) { // Marker YES: unused code
    this.hello = hello;
  }

  public static void setHello2(String hello) { // Marker YES: unused code
  }

  void setHello3(String hello) { // Marker YES: unused code
    this.hello = hello;
  }

  public boolean setHello4(String hello) { // Marker YES: unused code
    this.hello = hello;
    return true;
  }

  public void SetHello(String hello1) { // Marker YES: unused code
    this.hello = hello1;
  }

  public void set(String hello) { // Marker YES: unused code
    this.hello = hello;
  }

  // ---------------------------------------------------------------------------
  // NO BEAN METHODS: isValid()
  // ---------------------------------------------------------------------------

  public boolean is() { // Marker YES: unused code
    return isValid;
  }

  public boolean isvalid() { // Marker YES: unused code
    return isValid;
  }

  public String isValid2() { // Marker YES: unused code
    return "";
  }

  public void isValid3() { // Marker YES: unused code
  }

  public boolean isValid4(boolean valid) { // Marker YES: unused code
    return valid;
  }

  boolean isValid5() { // Marker YES: unused code
    return isValid;
  }

  public static boolean isValid6() { // Marker YES: unused code
    return false;
  }
}
