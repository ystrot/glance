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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
        return color;
    }

    public Color getSelectedBackgroundColor() {
        return selectedColor;
    }
    
    public Color getTreeSelectionBg() {
        return treeBg;
    }

    public Color getTreeSelectionFg() {
        return treeFg;
    }

    public void propertyChange(final PropertyChangeEvent event) {
        if (COLOR_BACKGROUND.equals(event.getProperty()) || 
            COLOR_SELECTED_BACKGROUND.equals(event.getProperty()) || 
            SELECTION_COLOR.equals(event.getProperty())) {
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
        final RGB rgb = PreferenceConverter.getColor(store, COLOR_BACKGROUND);
        color = new Color(display, rgb);
        
        final RGB selectedRgb = PreferenceConverter.getColor(store, COLOR_SELECTED_BACKGROUND);
        selectedColor = new Color(display, selectedRgb);
        
        
        RGB selectionRgb = PreferenceConverter.getColor(GlancePlugin.getDefault().getPreferenceStore(), SELECTION_COLOR);
        treeBg = new Color(display, selectionRgb);
        treeFg = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);

        toDispose.add(color);
        toDispose.add(selectedColor);
        toDispose.add(treeBg);
    }
    
    private static ColorManager INSTANCE;
    
    private Color color;
    private Color selectedColor;
    private Color treeBg;
    private Color treeFg;

    private List<Color> toDispose = new ArrayList<Color>();
}
