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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.ui.PlatformUI;

public class HighlightLayer extends Layer {

    @Override
    public IFigure findFigureAt(final int x, final int y, final TreeSearch search) {
        for (final IHighlightFigure hFigure : getHighlighFigures()) {
            if (hFigure.getHighlightedFigure().containsPoint(x, y)) {
                return hFigure.getHighlightedFigure();
            }
        }
        return null;
    }

    private boolean updating = false;

    public void updateHighlightFiguresBounds() {
        if (!updating) {
            updating = true;
            setBounds(getParent().getBounds());
            for (final IHighlightFigure figure : getHighlighFigures()) {
                figure.updateBounds();
            }
            updating = false;
            refresh();
        }
    }

    public void setSelected(final IFigure label) {
        for (final IHighlightFigure figure : getHighlighFigures()) {
            figure.select(figure.getHighlightedFigure() == label);
        }
        refresh();
    }

    private List<IHighlightFigure> getHighlighFigures() {
        final List<IHighlightFigure> hFigures = new ArrayList<IHighlightFigure>();
        for (final Object child : getChildren()) {
            if (child instanceof IHighlightFigure) {
                hFigures.add((IHighlightFigure) child);
            }
        }
        return hFigures;
    }

    public void refresh() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                getParent().repaint();
            }
        });
    }

}
