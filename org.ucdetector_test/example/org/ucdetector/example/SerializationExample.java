package org.ucdetector.example;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;

/**
 * Test some special Methods and field for Serialization
 */
public class SerializationExample {
  public static final long UNUSED = 0; // Marker YES

  static final long serialVersionUID = 0;

  static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

  /* private */void writeObject(ObjectOutputStream stream) throws IOException {
    if (false) {
      throw new IOException();
    }
  }

  /* private */void readObject(ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    if (false) {
      throw new IOException();
    }
    else if (false) {
      throw new ClassNotFoundException();
    }
  }

  public Object writeReplace() throws ObjectStreamException {
    if (false) {
      throw new InvalidClassException("Test");
    }
    return null;
  }

  public Object readResolve() throws ObjectStreamException {
    if (false) {
      throw new InvalidClassException("Test");
    }
    return null;

  };

  /* private */void readObjectNoData() throws ObjectStreamException {
    if (false) {
      throw new InvalidClassException("Test");
    }
  };

  public void unused() { // Marker YES

  }

}
