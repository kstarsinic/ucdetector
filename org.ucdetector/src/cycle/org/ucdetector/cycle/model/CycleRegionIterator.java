/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.LinkedList;
import java.util.List;

public class CycleRegionIterator {
  private CycleRegion first = null;
  private boolean startFound = false;
  private CycleBaseElement start;

  public CycleRegion getNext(CycleBaseElement startElement, boolean isNext) {
    this.start = startElement;
    List<CycleRegion> results = new LinkedList<>();
    getNext(SearchResultRoot.getInstance().getChildren(), results);
    int index = results.indexOf(first);
    if (!isNext) {
      index--;
    }
    else if (isNext && startElement instanceof CycleRegion) {
      index++;
    }
    return results.get(index < 0 ? results.size() - 1 : index >= results.size() ? 0 : index);
  }

  private void getNext(List<? extends CycleBaseElement> elements, List<CycleRegion> results) {
    for (CycleBaseElement baseElement : elements) {
      if (baseElement == start) {
        startFound = true;
      }
      if (baseElement instanceof CycleRegion) {
        CycleRegion region = (CycleRegion) baseElement;
        results.add(region);
        if (startFound && first == null) {
          first = region;
        }
      }
      getNext(baseElement.getChildren(), results);
    }
  }
}
