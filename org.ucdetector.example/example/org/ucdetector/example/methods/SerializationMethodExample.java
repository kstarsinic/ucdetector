package org.ucdetector.example.methods;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

/**
 * Test some special methods for Serialization
 */
public class SerializationMethodExample {

  /* private */void writeObject(ObjectOutputStream stream) throws IOException {
    if (true) {
      throw new IOException();
    }
  }

  /* private */void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    new File("a").getCanonicalFile();
    throw new ClassNotFoundException();
  }

  public Object writeReplace() throws ObjectStreamException {
    throw new InvalidClassException("Test");
  }

  public Object readResolve() throws ObjectStreamException {
    throw new InvalidClassException("Test");
  }

  /* private */void readObjectNoData() throws ObjectStreamException {
    if (true) {
      throw new InvalidClassException("Test");
    }
  }

  public void unused() { // Marker YES: unused code

  }
}
