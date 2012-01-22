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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;

import com.xored.glance.ui.sources.ColorManager;
import com.xored.glance.ui.sources.Match;

public class HighlightFlow extends HighlightFigure<TextFlow> {

    public HighlightFlow(final TextFlow flow, final Match match) {
        super(flow, match);
    }

    @Override
    public void paint(final Graphics g) {
        if (!isVisible()) {
            return;
        }
        final String originalText = delegate.getText();
        final String matchString = originalText.substring(match.getOffset(),
            match.getOffset() + match.getLength());

        g.setBackgroundColor(isSelected() ? ColorManager.getInstance().getSelectedBackgroundColor()
            : ColorManager.getInstance().getBackgroundColor());

        final Dimension extent = getTextExtents(matchString);
        final Point topLeft = bounds.getTopLeft();
        g.fillRectangle(topLeft.x, topLeft.y, extent.width, extent.height);

        g.setForegroundColor(ColorConstants.black);
        g.setFont(getFont());
        g.drawText(matchString, topLeft.getCopy());
    }

    @Override
    public void updateBounds() {
        if (!isVisible()) {
            return;
        }
        final String originalText = delegate.getText();

        final FlowPage flowPage = (FlowPage) delegate.getParent();
        final Rectangle bound = delegate.getBounds().getCopy();

        IFigure flowParent = flowPage;
        while (flowParent != null) {
            flowParent.translateToParent(bound);
            flowParent = flowParent.getParent();
        }
        final Viewport viewPort = getViewPort(flowPage);
        if (viewPort.useGraphicsTranslate()) {
            bound.performTranslate(viewPort.getHorizontalRangeModel().getValue(), viewPort
                .getVerticalRangeModel().getValue());
        }
        final Point origin = bound.getLocation();
        final Point caret = origin.getCopy();

        final String beforHighligt = originalText.substring(0, match.getOffset());
        caret.x += getTextExtents(beforHighligt).width;
        caret.y -= getTextExtents(beforHighligt).height;

        final Dimension highlightExtent = getTextExtents(originalText.substring(match.getOffset(),
            match.getOffset() + match.getLength()));
        setBounds(new Rectangle(caret.x, origin.y, highlightExtent.width, highlightExtent.height));
    }

    private Viewport getViewPort(final IFigure figure) {
        IFigure figureParent = figure;
        Viewport viewPort = null;
        while (figureParent != null) {
            if (figureParent instanceof Viewport) {
                viewPort = (Viewport) figureParent;
            }
            figureParent = figureParent.getParent();
        }
        return viewPort;

    }
}
