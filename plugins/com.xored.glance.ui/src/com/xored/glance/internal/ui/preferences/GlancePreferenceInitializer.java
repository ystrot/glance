/******************************************************************************* 
 * Copyright (c) 2008 xored software, Inc.  
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     xored software, Inc. - initial API and Implementation (Yuri Strot) 
 *******************************************************************************/
package com.xored.glance.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.xored.glance.internal.ui.GlancePlugin;

/**
 * @author Yuri Strot
 * 
 */
public class GlancePreferenceInitializer extends AbstractPreferenceInitializer
		implements IPreferenceConstants {

	private static final String OS_PROPERY = "os.name";
	private static final String WINDOWS = "windows";
	private static final String MAC = "mac";
	private static final String NIX = "nix";

	private static final String WIN_COLOR = "51,153,255";
	private static final String MAC_COLOR = "56,117,215";
	private static final String NIX_COLOR = "56,117,215";
	private static final String DEF_COLOR = WIN_COLOR;

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = GlancePlugin.getDefault()
				.getPreferenceStore();
		preferences.setDefault(SEARCH_CASE_SENSITIVE, false);
		preferences.setDefault(SEARCH_CAMEL_CASE, false);
		preferences.setDefault(SEARCH_REGEXP, false);
		preferences.setDefault(SEARCH_WORD_PREFIX, false);

		preferences.setDefault(PANEL_DIRECTIONS, true);
		preferences.setDefault(PANEL_STATUS_LINE, true);
		preferences.setDefault(PANEL_CLOSE, true);
		preferences.setDefault(PANEL_TEXT_SIZE, 20);
		preferences.setDefault(PANEL_LINK, true);
		preferences.setDefault(PANEL_STARTUP, false);
		preferences.setDefault(PANEL_AUTO_INDEXING, false);
		preferences.setDefault(SEARCH_INCREMENTAL, true);
		preferences.setDefault(PANEL_MAX_INDEXING_DEPTH, 4);

		preferences.setDefault(COLOR_TREE_BG, calculateDefSelectionColor());
		preferences.setDefault(COLOR_TREE_FG, "255,255,255");
	}

	private String calculateDefSelectionColor() {
		String osName = System.getProperty(OS_PROPERY).toLowerCase();

		if (osName.indexOf(WINDOWS) != -1) {
			return WIN_COLOR;
		} else if (osName.indexOf(MAC) != -1) {
			return MAC_COLOR;
		} else if (osName.indexOf(NIX) != -1) {
			return NIX_COLOR;
		} else {
			return DEF_COLOR;
		}
	}
}
