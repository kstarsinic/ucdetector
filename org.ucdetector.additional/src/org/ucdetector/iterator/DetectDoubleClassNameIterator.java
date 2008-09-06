/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.ucdetector.util.MarkerFactory;

/**
 * Detect double class names
 */
public class DetectDoubleClassNameIterator extends AbstractUCDetectorIterator {
  private final AdditionalUtil additionalUtil = new AdditionalUtil();

  private final Map<String, Set<IType>> typeMap //
  = new HashMap<String, Set<IType>>();

  @Override
  protected void handleType(IType type) throws CoreException {
    String className = type.getElementName();
    Set<IType> typeList = typeMap.get(className);
    if (typeList == null) {
      typeList = new HashSet<IType>();
      typeMap.put(className, typeList);
    }
    typeList.add(type);
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    Set<Entry<String, Set<IType>>> entrySet = typeMap.entrySet();
    for (Entry<String, Set<IType>> entry : entrySet) {
      Set<IType> types = entry.getValue();
      if (types.size() > 1) {
        for (IType type : types) {
          additionalUtil.createMarker(type, "Type name found " + types.size()
              + " times");
        }
      }
    }
  }

  @Override
  public String getJobName() {
    return "Detect Double Class Name Job";
  }
}
