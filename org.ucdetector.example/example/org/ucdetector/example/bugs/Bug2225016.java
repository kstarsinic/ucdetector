package org.ucdetector.example.bugs;

/**
 * [ 2225016 ] Prevent bad advise on methods that could be made private
 * 
 * UCDetector advised me to make a protected method private, although it was
 * overridden in a subclass in another package.
 * 
 * @see "https://sourceforge.net/tracker/index.php?func=detail&aid=2225016&group_id=219599&atid=1046865"
 */
public class Bug2225016 {
  public static void main(String[] args) {
    Bug2225016 bug = new Bug2225016();
    bug.methodIsOverriden();
    bug.methodIsNOTOverriden();
  }

  // no markers here, because method is overridden
  protected void methodIsOverriden() {

  }

  protected void methodIsNOTOverriden() { // Marker YES: use private, use final

  }
}
