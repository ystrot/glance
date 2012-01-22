/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Ivan Lobachev)
 ******************************************************************************/
package com.xored.glance.ui.ccvs;

import java.util.regex.Pattern;

public class VersionUtils {
	public static int compare(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		return s1.compareTo(s2);
	}

	public static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	public static String normalisedVersion(String version, String sep,
			int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}
}
