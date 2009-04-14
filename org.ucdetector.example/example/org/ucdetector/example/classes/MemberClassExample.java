package org.ucdetector.example.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class MemberClassExample {

  public static void main(String[] args) {
    new NoMemberUsed();
    new MemberClassUsed<String>();
    System.out.println(new ArrayList<String>() {
      private static final long serialVersionUID = 866740784057907531L;

      @Override
      public String toString() {
        return "anonymous class";
      }
    });
  }

  protected static class MemberClassUnused<E> extends ArrayList<E> { // Marker YES: unused code
    private static final long serialVersionUID = 1L;
  }

  static class MemberClassUsed<E> extends ArrayList<E> { // Marker YES: use private
    private static final long serialVersionUID = 1L;

    public void unused() {// Marker YES: unused code
      Map<String, String> map = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

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

/**
 * Only abstract or final are permitted
 */
class NoMemberUnused { // Marker YES: unused code

}

/**
 * Only abstract or final are permitted
 */
class NoMemberUsed {

}
