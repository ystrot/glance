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
package com.xored.glance.ui.sources;

import static org.eclipse.jface.preference.PreferenceConverter.getColor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.xored.glance.internal.ui.GlancePlugin;
import com.xored.glance.internal.ui.preferences.IPreferenceConstants;

/**
 * @author Yuri Strot
 * 
 */
public class ColorManager implements IPropertyChangeListener, IPreferenceConstants {

	public static final String ANNOTATION_ID = "com.xored.glance.ui.highlight";
	public static final String ANNOTATION_SELECTED_ID = "com.xored.glance.ui.select";

	private ColorManager() {
		getStore().addPropertyChangeListener(this);
		GlancePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		updateColors();
	}

	@SuppressWarnings("deprecation")
	public static IPreferenceStore getStore() {
		return ((AbstractUIPlugin) Platform.getPlugin("org.eclipse.ui.editors")).getPreferenceStore();
	}

	public static ColorManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ColorManager();
		return INSTANCE;
	}

	public Color getBackgroundColor() {
		return selection;
	}

	public Color getSelectedBackgroundColor() {
		return highlight;
	}

	public Color getTreeSelectionBg() {
		return treeBg;
	}

	public Color getTreeSelectionFg() {
		return treeFg;
	}

	public void propertyChange(final PropertyChangeEvent event) {
		if (COLOR_HIGHLIGHT.equals(event.getProperty()) || COLOR_SELECTION.equals(event.getProperty())
				|| COLOR_TREE_BG.equals(event.getProperty()) || COLOR_TREE_FG.equals(event.getProperty())) {
			updateColors();
		}
	}

	public static Color lighten(Color color, int delta) {
		int r = ensureColor(color.getRed() + delta);
		int g = ensureColor(color.getGreen() + delta);
		int b = ensureColor(color.getBlue() + delta);
		return new Color(color.getDevice(), r, g, b);
	}

	private static int ensureColor(int value) {
		return value > 255 ? 255 : value;
	}

	private void updateColors() {
		for (Color color : toDispose) {
			color.dispose();
		}
		toDispose = new ArrayList<Color>();

		final Display display = PlatformUI.getWorkbench().getDisplay();
		final IPreferenceStore store = getStore();

		selection = new Color(display, getColor(store, COLOR_HIGHLIGHT));
		highlight = new Color(display, getColor(store, COLOR_SELECTION));
		treeBg = new Color(display, getColor(GlancePlugin.getDefault().getPreferenceStore(), COLOR_TREE_BG));
		treeFg = new Color(display, getColor(GlancePlugin.getDefault().getPreferenceStore(), COLOR_TREE_FG));

		toDispose.add(selection);
		toDispose.add(highlight);
		toDispose.add(treeBg);
		toDispose.add(treeFg);
	}

	private static ColorManager INSTANCE;

	private Color selection;
	private Color highlight;
	private Color treeBg;
	private Color treeFg;

	private List<Color> toDispose = new ArrayList<Color>();
}
