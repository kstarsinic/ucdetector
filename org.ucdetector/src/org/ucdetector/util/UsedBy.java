/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// http://findbugs.sourceforge.net/manual/annotations.html
// http://pmd.sourceforge.net/suppressing.html
// http://sourceforge.net/tracker/?func=detail&aid=2658675&group_id=219599&atid=1046868
// Similar to SuppressWarnings

@Target({ TYPE, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RetentionPolicy.SOURCE)
/**
 * Examples: @UsedBy("reflection", "external", "framework")
 */
public @interface UsedBy {
  String[] value();
}
