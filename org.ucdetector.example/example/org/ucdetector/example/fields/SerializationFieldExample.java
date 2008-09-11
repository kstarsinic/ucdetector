package org.ucdetector.example.fields;

import java.io.ObjectStreamField;

/**
 * Test some special fields for Serialization
 */
public class SerializationFieldExample {
  public static final long UNUSED = 0; // Marker YES: unused code

  static final long serialVersionUID = 0;

  static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];
}
