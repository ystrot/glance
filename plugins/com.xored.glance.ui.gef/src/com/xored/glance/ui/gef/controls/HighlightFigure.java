/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Igor Zapletnev)
 ******************************************************************************/
package com.xored.glance.ui.gef.controls;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;

import com.xored.glance.ui.sources.Match;

public abstract class HighlightFigure<T extends Figure> extends Figure implements IHighlightFigure {

    protected final T delegate;
    protected final Match match;

    private boolean selected;

    public HighlightFigure(final T delegate, final Match match) {
        this.delegate = delegate;
        this.match = match;
        this.selected = false;
    }

    @Override
    public Font getFont() {
        return delegate.getFont();
    }

    @Override
    public IFigure getHighlightedFigure() {
        return delegate;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void select(final boolean sel) {
        selected = sel;
    }

    protected TextUtilities getTextUtilities() {
        return TextUtilities.INSTANCE;
    }

    protected Dimension getTextExtents(final String s) {
        return getTextUtilities().getTextExtents(s, getFont());
    }
}
