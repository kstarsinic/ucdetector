package org.ucdetector.example.methods;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;

/**
 * Test some special methods for Serialization
 */
public class SerializationMethodExample {

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

  public void unused() { // Marker YES: unused code

  }
}
