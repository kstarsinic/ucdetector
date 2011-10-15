/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * Provide access to the org.ucdetector.headless extension point
 * <p>
 * @author Joerg Spieler
 * @since 2011-05-17
 */
public final class HeadlessExtension {
  /** Simple identifier constant (value <code>"headless"</code>) for the UCDetector headless extension point. */
  private static final String EXTENSION_POINT_ID = UCDetectorPlugin.ID + ".headless"; //$NON-NLS-1$
  //
  private static final String ATTRIBUTE_CLASS = "class";//$NON-NLS-1$
  private static final String ATTRIBUTE_ID = "id";//$NON-NLS-1$
  private static final String ATTRIBUTE_ORDINAL = "ordinal";//$NON-NLS-1$
  private static boolean isInitialized = false;
  private static List<HeadlessExtension> headlessExtensionList;

  private final AbstractUCDetectorIterator iterator;

  private final String id;
  private final Integer ordinal;

  private HeadlessExtension(AbstractUCDetectorIterator iterator, String id, Integer ordinal) {
    this.iterator = iterator;
    this.id = id;
    this.ordinal = ordinal;
  }

  public String getId() {
    return id;
  }

  // STATIC -------------------------------------------------------------------
  private static void loadExtensions() {
    if (!isInitialized) {
      Log.info("Load HeadlessExtensions"); //$NON-NLS-1$
      isInitialized = true;
      headlessExtensionList = new ArrayList<HeadlessExtension>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IConfigurationElement[] elements = reg.getConfigurationElementsFor(EXTENSION_POINT_ID);
      for (IConfigurationElement element : elements) {
        try {
          String id = element.getAttribute(ATTRIBUTE_ID);
          Integer ordinal = Integer.valueOf(element.getAttribute(ATTRIBUTE_ORDINAL));
          AbstractUCDetectorIterator iterator = (AbstractUCDetectorIterator) WorkbenchPlugin.createExtension(//
              element, ATTRIBUTE_CLASS);
          Log.info("Found HeadlessExtension: %s, %s", id, element.getAttribute(ATTRIBUTE_CLASS)); //$NON-NLS-1$
          headlessExtensionList.add(new HeadlessExtension(iterator, id, ordinal));
        }
        catch (Exception ex) {
          UCDetectorPlugin.logToEclipseLog("Can't load ReportExtension", ex); //$NON-NLS-1$
        }
      }
      Log.info("Found HeadlessExtensions : " + headlessExtensionList.size()); //$NON-NLS-1$
    }
  }

  public static List<AbstractUCDetectorIterator> getPostIterators() {
    loadExtensions();
    ArrayList<AbstractUCDetectorIterator> result = new ArrayList<AbstractUCDetectorIterator>();
    Collections.sort(headlessExtensionList, new IteratorExtensionSorter());
    for (HeadlessExtension headlessExtension : headlessExtensionList) {
      result.add(headlessExtension.getIterator());
    }
    return result;
  }

  public AbstractUCDetectorIterator getIterator() {
    return iterator;
  }

  @Override
  public String toString() {
    return String.format("IteratorExtension [id=%s, iterator=%s,]", id, iterator); //$NON-NLS-1$
  }

  private static final class IteratorExtensionSorter implements Comparator<HeadlessExtension> {

    public int compare(HeadlessExtension o1, HeadlessExtension o2) {
      return o1.ordinal.compareTo(o2.ordinal);
    }
  }
}
