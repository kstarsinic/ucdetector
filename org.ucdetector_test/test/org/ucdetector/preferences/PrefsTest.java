package org.ucdetector.preferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;

public class PrefsTest extends TestCase {
  private static final int WARN_LIMIT_VALUE = 1;

  private static class StoreFactory {
    public static IPreferenceStore createStore(
        Class<IPreferenceStore> iPreferenceStore) {
      InvocationHandler handler = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
          String methodName = method.getName();
          String prefName = (String) args[0];
          if (methodName.equals("getString")) {
            // -----------------------------------------------------
            // WARN LEVEL
            // -----------------------------------------------------
            if (Prefs.ANALYZE_CLASSES.equals(prefName)) {
              return WarnLevel.ERROR.toString();
            }
            if (Prefs.ANALYZE_MEHTODS.equals(prefName)) {
              return WarnLevel.WARNING.toString();
            }
            if (Prefs.ANALYZE_FIELDS.equals(prefName)) {
              return WarnLevel.IGNORE.toString();
            }
            if (Prefs.ANALYZE_VISIBILITY_PROTECTED.equals(prefName)) {
              return WarnLevel.IGNORE.toString();
            }
            if (Prefs.ANALYZE_VISIBILITY_PRIVATE.equals(prefName)) {
              return WarnLevel.IGNORE.toString();
            }
            // -----------------------------------------------------
            // FILTER
            // -----------------------------------------------------
            if (Prefs.FILTER_CLASS.equals(prefName)) {
              return "*Test*,";
            }
            if (Prefs.FILTER_BEAN_METHOD.equals(prefName)) {
              return Boolean.TRUE;
            }
            if (Prefs.FILTER_METHOD.equals(prefName)) {
              return "*test*,";
            }
            if (Prefs.FILTER_FIELD.equals(prefName)) {
              return "*test*,";
            }
            if (Prefs.FILTER_PACKAGE.equals(prefName)) {
              return "";
            }
            if (Prefs.FILTER_SOURCE_FOLDER.equals(prefName)) {
              return "generated";
            }
          }
          if (methodName.equals("getInt")) {
            if (Prefs.WARN_LIMIT.equals(prefName)) {
              return Integer.valueOf(WARN_LIMIT_VALUE);
            }
          }
          throw new RuntimeException("Unhandeld method: " + methodName);
        }
      };
      return (IPreferenceStore) Proxy.newProxyInstance(iPreferenceStore
          .getClassLoader(), new Class[] { iPreferenceStore }, handler);
    }
  }

  protected void setUp() throws Exception {
    super.setUp();
    IPreferenceStore store = StoreFactory.createStore(IPreferenceStore.class);
    Prefs.setStore_FOR_TEST(store);
  }

  public final void testGetStore() {
    // Prefs prefs = new Prefs();
    IPreferenceStore store = Prefs.getStore();
    assertNotNull(store);
  }

  public final void testMatchFilterStars() {
    assertTrue(Prefs.matchFilter(Prefs.FILTER_CLASS, "Test"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_CLASS, "MyTest"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_CLASS, "TestMy"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_CLASS, "MyTestMy"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_CLASS, "Hallo"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_CLASS, ""));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_CLASS, "Class.Subclass"));
    // -------------------------------------------------------------------------
    assertTrue(Prefs.matchFilter(Prefs.FILTER_METHOD, "test"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_METHOD, "Mytest"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_METHOD, "testMy"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_METHOD, "MytestMy"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_METHOD, "Hallo"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_METHOD, ""));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_METHOD, "Class.Subclass"));
    // -------------------------------------------------------------------------
    assertTrue(Prefs.matchFilter(Prefs.FILTER_FIELD, "test"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_FIELD, "Mytest"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_FIELD, "testMy"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_FIELD, "MytestMy"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_FIELD, "Hallo"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_FIELD, ""));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_FIELD, "Class.Subclass"));
  }

  public void testMatchFilterIgnoreCase() {
    assertTrue(Prefs.matchFilter(Prefs.FILTER_CLASS, "Testbbb"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_METHOD, "testMy"));
    assertTrue(Prefs.matchFilter(Prefs.FILTER_FIELD, "testMy"));
  }

  public final void testMatchFilterEmpty() {
    assertFalse(Prefs.matchFilter(Prefs.FILTER_PACKAGE, "Test"));
    assertFalse(Prefs.matchFilter(Prefs.FILTER_PACKAGE, "Class"));
  }

  public final void testGetWarnLimit() {
    assertEquals(WARN_LIMIT_VALUE, Prefs.getWarnLimit());
  }

  // -------------------------------------------------------------------------
  // IS
  // -------------------------------------------------------------------------

  public final void testIsAnalyseClasses() {
    assertTrue(Prefs.isUCDetectionInClasses());
  }

  public final void testIsAnalyseMethods() {
    assertTrue(Prefs.isUCDetectionInMethods());
  }

  public final void testIsAnalyseFields() {
    assertFalse(Prefs.isUCDetectionInFields());
  }

  public final void testIsAnalyseVisibility() {
    assertFalse(Prefs.isCheckIncreaseVisibilityProtected());
  }

  // -------------------------------------------------------------------------
  // GET
  // -------------------------------------------------------------------------

  public final void testGetAnalyseClasses() {
    assertEquals(WarnLevel.ERROR, Prefs.getUCDetectionInClasses());
  }

  public final void testGetAnalyseMethods() {
    assertEquals(WarnLevel.WARNING, Prefs.getUCDetectionInMethods());
  }

  public final void testGetAnalyseFields() {
    assertEquals(WarnLevel.IGNORE, Prefs.getUCDetectionInFields());
  }

  public final void testGetAnalyseVisibility() {
    assertEquals(WarnLevel.IGNORE, Prefs.getCheckIncreaseVisibilityProtected());
  }
}
