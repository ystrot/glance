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
package com.xored.glance.ui.gef.sources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;

import com.xored.glance.ui.gef.blocks.FigureTextBlock;
import com.xored.glance.ui.gef.controls.FigureDecorator;
import com.xored.glance.ui.gef.controls.IFigureDecorator;
import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;

public class GraphicalViewerTextSource extends AbstractGraphicalViewerTextSource {

    private final GraphicalViewer viewer;

    public GraphicalViewerTextSource(final GraphicalViewer viewer) {
        this.viewer = viewer;
        collectTextBlocks();
    }

    private final List<ITextBlock> textBlocks = new ArrayList<ITextBlock>();

    private IFigureDecorator decorator;

    public void collectTextBlocks() {
        if (viewer.getContents() instanceof GraphicalEditPart) {
            decorator = new FigureDecorator(((GraphicalEditPart) viewer.getContents()).getFigure());
            textBlocks.addAll(decorator.getTextBlocks());
        }
    }

    public ITextBlock[] getBlocks() {
        return textBlocks.toArray(new ITextBlock[0]);
    }

    public void addTextSourceListener(final ITextSourceListener listener) {
        decorator.addTextSourceListener(listener);
    }

    public void removeTextSourceListener(final ITextSourceListener listener) {
        decorator.removeTextSourceListener(listener);
    }

    public void select(final Match match) {
        decorator.selectMatch(match);
        if (match == null || !(match.getBlock() instanceof FigureTextBlock<?>)
            || !(viewer.getControl() instanceof FigureCanvas)) {
            return;
        }
        final FigureTextBlock<?> figureBlock = (FigureTextBlock<?>) match.getBlock();
        final FigureCanvas viewerCanvas = ((FigureCanvas) viewer.getControl());
        final Viewport port = viewerCanvas.getViewport();
        IFigure target = figureBlock.getFigure();
        final Rectangle exposeRegion = target.getBounds().getCopy();
        target = target.getParent();
        while (target != null && target != port) {
            target.translateToParent(exposeRegion);
            target = target.getParent();
        }
        exposeRegion.expand(5, 5);

        final Dimension viewportSize = port.getClientArea().getSize();

        final Point topLeft = exposeRegion.getTopLeft();
        final Point bottomRight = exposeRegion.getBottomRight().translate(viewportSize.getNegated());
        final Point finalLocation = new Point();
        if (viewportSize.width < exposeRegion.width) {
            finalLocation.x = Math.min(bottomRight.x, Math.max(topLeft.x, port.getViewLocation().x));
        } else {
            finalLocation.x = Math.min(topLeft.x, Math.max(bottomRight.x, port.getViewLocation().x));
        }

        if (viewportSize.height < exposeRegion.height) {
            finalLocation.y = Math.min(bottomRight.y, Math.max(topLeft.y, port.getViewLocation().y));
        } else {
            finalLocation.y = Math.min(topLeft.y, Math.max(bottomRight.y, port.getViewLocation().y));
        }

        viewerCanvas.scrollSmoothTo(finalLocation.x, finalLocation.y);
    }

    public void show(final Match[] matches) {
        if (decorator != null) {
            decorator.showMatches(matches);
        }
    }

    public GraphicalViewer getViewer() {
        return viewer;
    }

    @Override
    public void dispose() {
        if (decorator != null) {
            decorator.disconnect();
        }
        textBlocks.clear();
        super.dispose();
    }
}
